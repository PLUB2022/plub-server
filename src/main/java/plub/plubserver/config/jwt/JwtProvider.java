package plub.plubserver.config.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import plub.plubserver.config.security.PrincipalDetailService;
import plub.plubserver.config.security.PrincipalDetails;
import plub.plubserver.domain.account.dto.AuthDto;
import plub.plubserver.domain.account.config.AccountCode;
import plub.plubserver.domain.account.exception.AccountException;
import plub.plubserver.domain.account.model.Account;
import plub.plubserver.domain.account.model.Role;
import plub.plubserver.util.CustomEncryptUtil;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.security.Key;
import java.util.Date;

@Component
@Slf4j
@Transactional
public class JwtProvider {

    private final PrincipalDetailService principalDetailService;
    private final RedisService redisService;
    private final CustomEncryptUtil customEncryptUtil;
    private final Key privateKey;

    public JwtProvider(@Value("${jwt.secret-key}") String secretKey,
                       PrincipalDetailService principalDetailService,
                       RedisService redisService, CustomEncryptUtil customEncryptUtil) {
        this.privateKey = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.principalDetailService = principalDetailService;
        this.redisService = redisService;
        this.customEncryptUtil = customEncryptUtil;
    }

    // milliseconds
    @Value("${jwt.access-duration}")
    public long accessDuration;

    // milliseconds
    @Value("${jwt.refresh-duration}")
    public long refreshDuration;

    // Request 헤더에서 토큰을 파싱한다
    public String resolveToken(HttpServletRequest request) {
        String rawToken = request.getHeader("Authorization");
        if (rawToken != null && rawToken.startsWith("Bearer "))
            return rawToken.replace("Bearer ", "");
        else return null;
    }

    public String resolveSignToken(String rawToken) {
        if (rawToken != null && rawToken.startsWith("Bearer "))
            return rawToken.replace("Bearer ", "");
        else throw new AccountException(AccountCode.SIGNUP_TOKEN_ERROR);
    }

    // Sign Token 생성
    public String createSignToken(String email) {
        Date now = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .setSubject(customEncryptUtil.encrypt(email))
                .claim("sign", Role.ROLE_USER)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessDuration))
                .signWith(privateKey)
                .compact();
    }

    // Access Token 생성
    private String createAccessToken(Account account) {
        Date now = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .setSubject(customEncryptUtil.encrypt(account.getEmail()))
                .claim("role", account.getRole())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessDuration))
                .signWith(privateKey)
                .compact();
    }

    // Refresh Token 생성
    private String createRefreshToken(Account account) {
        Date now = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .setSubject(customEncryptUtil.encrypt(account.getEmail()))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshDuration))
                .signWith(privateKey)
                .compact();
    }

    // Access, Refresh Token 검증 (만료 여부 검사)
    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(privateKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 입니다.");
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 잘못 되었습니다.");
        }
        return false;
    }

    /**
     * Authentication 객체 가져오기
     */
    public Authentication getAuthentication(String accessToken) {
        Claims body = Jwts.parserBuilder()
                .setSigningKey(privateKey)
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
        String email = customEncryptUtil.decrypt(body.getSubject());
        UserDetails userDetails = principalDetailService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public AuthDto.SigningAccount getSignKey(String signToken) {
        Claims body = Jwts.parserBuilder()
                .setSigningKey(privateKey)
                .build()
                .parseClaimsJws(signToken)
                .getBody();
        String email = customEncryptUtil.decrypt(body.getSubject());
        String[] split = email.split("@");
        return new AuthDto.SigningAccount(email, split[1]);
    }

    /**
     * Access, Refresh 최초 발행
     */
    public JwtDto issue(Account account) {
        String access = createAccessToken(account);
        String refresh = createRefreshToken(account);
        redisService.setRefreshToken(account.getId(), refresh);
        return new JwtDto(access, refresh);
    }

    /**
     * Refresh Token 으로 Access Token 재발급 (Access, Refresh 둘 다 재발급)
     */
    public JwtDto reissue(String refreshToken) {
        Authentication authentication = getAuthentication(refreshToken);
        PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();
        Account account = principal.getAccount();

        redisService.getRefreshToken(account.getId());

        String newAccessToken = createAccessToken(account);
        String newRefreshToken = createRefreshToken(account);

        // 기존 refresh 토큰 값 변경
        redisService.setRefreshToken(account.getId(), newRefreshToken);

        return new JwtDto(newAccessToken, newRefreshToken);
    }

}
