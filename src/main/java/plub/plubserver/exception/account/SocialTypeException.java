package plub.plubserver.exception.account;

import lombok.Getter;
import plub.plubserver.exception.ErrorCode;

@Getter
public class SocialTypeException extends RuntimeException {
    private final ErrorCode errorCode;

    public SocialTypeException(){
        super("소셜 정보를 찾을 수 없습니다.");
        this.errorCode = ErrorCode.NOT_FOUND_ACCOUNT;
    }
}
