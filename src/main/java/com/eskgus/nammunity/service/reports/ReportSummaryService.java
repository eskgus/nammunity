package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.domain.reports.ContentReportSummaryRepository;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsSearchService;
import com.eskgus.nammunity.service.posts.PostsSearchService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDeleteDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummarySaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Function;

import static com.eskgus.nammunity.util.PaginationRepoUtil.createPageable;

@RequiredArgsConstructor
@Service
public class ReportSummaryService {
    private final ContentReportSummaryRepository contentReportSummaryRepository;
    private final TypesService typesService;
    private final PostsSearchService postsSearchService;
    private final CommentsSearchService commentsSearchService;
    private final UserService userService;

    @Transactional
    public <T> Long saveOrUpdateContentReportSummary(ContentReportSummarySaveDto requestDto) {
        // contentReportSummary 테이블에 컨텐츠 (posts, comments, user) 없으면 저장, 있으면 수정
        T contents = getContents(requestDto);
        boolean doContentsExist = contentReportSummaryRepository.existsByContents(contents);

        if (doContentsExist) {
            return updateContentReportSummary(requestDto, contents);
        }
        return saveContentReportSummary(requestDto);
    }

    private <T> T getContents(ContentReportSummarySaveDto requestDto) {
        if (requestDto.getTypes().getDetail().equals("게시글")) {
            return (T) requestDto.getPosts();
        } else if (requestDto.getTypes().getDetail().equals("댓글")) {
            return (T) requestDto.getComments();
        } else {
            return (T) requestDto.getUser();
        }
    }

    @Transactional
    public <T> Long updateContentReportSummary(ContentReportSummarySaveDto requestDto, T contents) {
        ContentReportSummary reportSummary = contentReportSummaryRepository.findByContents(contents);
        reportSummary.update(requestDto.getReportedDate(), requestDto.getReporter(),
                requestDto.getReasons(), requestDto.getOtherReasons());
        return reportSummary.getId();
    }

    @Transactional
    public Long saveContentReportSummary(ContentReportSummarySaveDto requestDto) {
        return contentReportSummaryRepository.save(requestDto.toEntity()).getId();
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<ContentReportSummaryDto> findAllDesc(int page) {
        Pageable pageable = createPageable(page, 20);
        Page<ContentReportSummaryDto> contents = contentReportSummaryRepository.findAllDesc(pageable);
        return new ContentsPageDto<>(contents);
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<ContentReportSummaryDto> findByTypes(ContentType contentType, int page) {
        Pageable pageable = createPageable(page, 20);
        Types type = typesService.findByContentType(contentType);
        Page<ContentReportSummaryDto> contents = contentReportSummaryRepository.findByTypes(type, pageable);
        return new ContentsPageDto<>(contents);
    }

    @Transactional(readOnly = true)
    public ContentReportSummary findByUser(User user) {
        return contentReportSummaryRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("신고 요약 내역이 존재하지 않는 회원입니다."));
    }

    @Transactional
    public void deleteSelectedReportSummary(ContentReportSummaryDeleteDto deleteDto) {
        validateDeleteDto(deleteDto);

        deleteByContents(deleteDto.getPostsId(), postsSearchService::findById);
        deleteByContents(deleteDto.getCommentsId(), commentsSearchService::findById);
        deleteByContents(deleteDto.getUserId(), userService::findById);
    }

    private void validateDeleteDto(ContentReportSummaryDeleteDto deleteDto) {
        if (deleteDto.getPostsId().isEmpty()
                && deleteDto.getCommentsId().isEmpty()
                && deleteDto.getUserId().isEmpty()) {
            throw new IllegalArgumentException("삭제할 항목을 선택하세요.");
        }
    }

    @Transactional
    private <T> void deleteByContents(List<Long> contentIds, Function<Long, T> finder) {
        contentIds.forEach(id -> {
            T content = finder.apply(id);
            contentReportSummaryRepository.deleteByContents(content);
        });
    }
}
