package plub.plubserver.notice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plub.plubserver.common.dto.CommentDto.*;
import plub.plubserver.domain.account.model.Account;
import plub.plubserver.domain.feed.repository.PlubbingFeedRepository;
import plub.plubserver.notice.dto.PlubbingNoticeDto.*;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlubbingNoticeService {
    private final PlubbingFeedRepository plubbingFeedRepository;

    public NoticeIdResponse createNotice(Long plubbingId, Account owner, CreateNoticeRequest createNoticeRequest) {
        return new NoticeIdResponse(1L);
    }

    public NoticeListResponse getNoticeList(Account loginAccount, Long plubbingId, Pageable pageable) {
        List<NoticeCardResponse> feedCardResponses = List.of(
                new NoticeCardResponse(1L, "noticeTitle1", "중요한 공지 내용 ~~~", "2020-02-02 12:12:00"),
                new NoticeCardResponse(2L, "noticeTitle3", "중요한 공지 내용 ~~~", "2020-02-02 12:12:00"),
                new NoticeCardResponse(3L, "noticeTitle4", "중요한 공지 내용 ~~~", "2020-02-02 12:12:00")
        );

        return NoticeListResponse.of(feedCardResponses);
    }

    public NoticeIdResponse updateNotice(Account loginAccount, Long feedId, UpdateNoticeRequest updateNoticeRequest) {
        return new NoticeIdResponse(1L);
    }

    public NoticeMessage deleteNotice(Account loginAccount, Long feedId) {
        return new NoticeMessage("success");
    }

    public NoticeResponse getNotice(Account loginAccount, Long feedId) {
        return new NoticeResponse(1L, "noticeTitle1", "중요한 공지 내용 ~~~", "2020-02-02 12:12:00", 5L, 3L, List.of(
                new CommentResponse(1L, "commentContent1", "2020-02-02 12:12:00", "profileUrl1", "nickname1"),
                new CommentResponse(2L, "commentContent2", "2020-02-02 12:12:00", "profileUrl2", "nickname2"),
                new CommentResponse(3L, "commentContent3", "2020-02-02 12:12:00", "profileUrl3", "nickname3")
        ));
    }

    public NoticeIdResponse pinNotice(Account loginAccount, Long feedId) {
        return new NoticeIdResponse(1L);
    }

    public NoticeIdResponse likeNotice(Account loginAccount, Long feedId) {
        return new NoticeIdResponse(1L);
    }

    public CommentIdResponse createNoticeComment(Account loginAccount, Long feedId, CreateCommentRequest createCommentRequest) {
        return new CommentIdResponse(1L);
    }

    public CommentIdResponse updateNoticeComment(Account loginAccount, Long feedId, UpdateCommentRequest updateCommentRequest) {
        return new CommentIdResponse(1L);
    }

    public CommentMessage deleteNoticeComment(Account loginAccount, Long feedId) {
        return new CommentMessage("success");
    }

    public CommentIdResponse reportNoticeComment(Account loginAccount, Long feedId) {
        return new CommentIdResponse(1L);
    }
}
