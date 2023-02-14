package plub.plubserver.domain.notice.model;

import lombok.*;
import plub.plubserver.common.dto.CommentDto.*;
import plub.plubserver.common.model.BaseEntity;
import plub.plubserver.domain.account.model.Account;

import javax.persistence.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_comment_id")
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    public void updateNoticeComment(UpdateCommentRequest request) {
        this.content = request.content();
    }
}