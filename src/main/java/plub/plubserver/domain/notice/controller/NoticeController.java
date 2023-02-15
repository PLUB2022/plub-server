package plub.plubserver.domain.notice.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import plub.plubserver.common.dto.ApiResponse;
import plub.plubserver.common.dto.PageResponse;
import plub.plubserver.domain.account.model.Account;
import plub.plubserver.domain.account.service.AccountService;
import plub.plubserver.common.dto.CommentDto.*;
import plub.plubserver.domain.notice.dto.NoticeDto.*;
import plub.plubserver.domain.notice.service.NoticeService;

import javax.validation.Valid;

import static plub.plubserver.common.dto.ApiResponse.success;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plubbings")
@Slf4j
@Api(tags = "공지 API")
public class NoticeController {
    private final NoticeService noticeService;
    private final AccountService accountService;


    @ApiOperation(value = "공지 작성")
    @PostMapping("/{plubbingId}/notices")
    public ApiResponse<NoticeIdResponse> createNotice(@PathVariable Long plubbingId,
                                                      @Valid @RequestBody CreateNoticeRequest createNoticeRequest) {
        Account loginAccount = accountService.getCurrentAccount();
        return success(noticeService.createNotice(plubbingId, loginAccount, createNoticeRequest));
    }

    @ApiOperation(value = "공지 목록 조회")
    @GetMapping("/{plubbingId}/notices")
    public ApiResponse<PageResponse<NoticeCardResponse>> getNoticeList(@PathVariable Long plubbingId,
                                                          @PageableDefault(size = 20) Pageable pageable) {
        Account loginAccount = accountService.getCurrentAccount();
        return success(noticeService.getNoticeList(loginAccount, plubbingId, pageable));
    }

    @ApiOperation(value = "공지 상세 조회")
    @GetMapping("/notices/{noticeId}")
    public ApiResponse<NoticeResponse> getNotice(@PathVariable Long noticeId) {
        Account loginAccount = accountService.getCurrentAccount();
        return success(noticeService.getNotice(loginAccount, noticeId));
    }

    @ApiOperation(value = "공지 수정")
    @PutMapping("/{plubbingId}/notices/{noticeId}")
    public ApiResponse<NoticeIdResponse> updateNotice(
            @PathVariable Long plubbingId,
            @PathVariable Long noticeId,
            @Valid @RequestBody UpdateNoticeRequest updateNoticeRequest) {
        Account loginAccount = accountService.getCurrentAccount();
        return success(noticeService.updateNotice(loginAccount, plubbingId, noticeId, updateNoticeRequest));
    }

    @ApiOperation(value = "공지 삭제")
    @DeleteMapping("/notices/{noticeId}")
    public ApiResponse<NoticeMessage> deleteNotice(@PathVariable Long noticeId) {
        Account loginAccount = accountService.getCurrentAccount();
        return success(noticeService.softDeleteNotice(loginAccount, noticeId));
    }

    @ApiOperation(value = "공지 좋아요")
    @PutMapping("/notices/{noticeId}/like")
    public ApiResponse<NoticeMessage> likeNotice(@PathVariable Long noticeId) {
        Account loginAccount = accountService.getCurrentAccount();
        return success(noticeService.likeNotice(loginAccount, noticeId));
    }

    @ApiOperation(value = "공지별 댓글 조회")
    @GetMapping("/notices/{noticeId}/comments")
    public ApiResponse<PageResponse<NoticeCommentResponse>> getNoticeCommentList(@PathVariable Long noticeId,
                                                                       @PageableDefault(size = 20) Pageable pageable) {
        Account loginAccount = accountService.getCurrentAccount();
        return success(noticeService.getNoticeCommentList(loginAccount, noticeId, pageable));
    }

    @ApiOperation(value = "공지 댓글 생성")
    @PostMapping("/notices/{noticeId}/comments")
    public ApiResponse<CommentIdResponse> createNoticeComment(@PathVariable Long noticeId,
                                                              @Valid @RequestBody CreateCommentRequest createCommentRequest) {
        Account loginAccount = accountService.getCurrentAccount();
        return success(noticeService.createNoticeComment(loginAccount, noticeId, createCommentRequest));
    }

    @ApiOperation(value = "공지 댓글 수정")
    @PutMapping("/notices/comments/{commentId}")
    public ApiResponse<CommentIdResponse> updateNoticeComment(@PathVariable Long commentId,
                                                              @Valid @RequestBody UpdateCommentRequest updateCommentRequest) {
        Account loginAccount = accountService.getCurrentAccount();
        return success(noticeService.updateNoticeComment(loginAccount, commentId, updateCommentRequest));
    }

    @ApiOperation(value = "공지 댓글 삭제")
    @DeleteMapping("/notices/comments/{commentId}")
    public ApiResponse<CommentMessage> deleteNoticeComment(@PathVariable Long commentId) {
        Account loginAccount = accountService.getCurrentAccount();
        return success(noticeService.deleteNoticeComment(loginAccount, commentId));
    }

    @ApiOperation(value = "공지 댓글 신고")
    @PostMapping("/notices/comments/{commentId}/report")
    public ApiResponse<CommentIdResponse> reportNoticeComment(@PathVariable Long commentId) {
        Account loginAccount = accountService.getCurrentAccount();
        return success(noticeService.reportNoticeComment(loginAccount, commentId));
    }
}
