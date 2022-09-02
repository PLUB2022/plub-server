package plub.plubserver.domain.recruit.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import plub.plubserver.common.model.BaseTimeEntity;
import plub.plubserver.domain.account.model.Account;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountBoard extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_board_id")
    private Long id;
    private boolean isBookmarked;

    // 회원_모집페이지(다) - 회원(1) # 다대다 용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    // 회원_모집페이지(다) - 모집(1) # 다대다 용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;


}