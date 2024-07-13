package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.domain.reports.ContentReportSummaryRepository;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.service.comments.CommentsService;
import com.eskgus.nammunity.service.posts.PostsService;
import com.eskgus.nammunity.service.user.UserService;
import com.eskgus.nammunity.util.PaginationRepoUtil;
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

import static com.eskgus.nammunity.domain.enums.ContentType.COMMENTS;
import static com.eskgus.nammunity.domain.enums.ContentType.POSTS;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.EMPTY_CONTENT_IDS;
import static com.eskgus.nammunity.domain.enums.ExceptionMessages.REPORT_SUMMARY_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class ReportSummaryService {
    private final ContentReportSummaryRepository contentReportSummaryRepository;
    private final TypesService typesService;
    private final PostsService postsService;
    private final CommentsService commentsService;
    private final UserService userService;

    @Transactional
    public Long saveOrUpdateContentReportSummary(ContentReportSummarySaveDto requestDto) {
        // contentReportSummary 테이블에 컨텐츠 (posts, comments, user) 없으면 저장, 있으면 수정
        Element element = getElement(requestDto);
        boolean doesSummaryExist = contentReportSummaryRepository.existsByElement(element);

        if (doesSummaryExist) {
            return updateContentReportSummary(requestDto, element);
        } else {
            return saveContentReportSummary(requestDto);
        }
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<ContentReportSummaryDto> findAllDesc(int page) {
        Pageable pageable = createPageable(page);
        Page<ContentReportSummaryDto> summariesPage = contentReportSummaryRepository.findAllDesc(pageable);

        return createContentsPageDto(summariesPage);
    }

    @Transactional(readOnly = true)
    public ContentsPageDto<ContentReportSummaryDto> findByTypes(ContentType contentType, int page) {
        Pageable pageable = createPageable(page);
        Types type = typesService.findByContentType(contentType);
        Page<ContentReportSummaryDto> summariesPage = contentReportSummaryRepository.findByTypes(type, pageable);

        return createContentsPageDto(summariesPage);
    }

    @Transactional(readOnly = true)
    public ContentReportSummary findByUser(User user) {
        return contentReportSummaryRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException(REPORT_SUMMARY_NOT_FOUND.getMessage()));
    }

    @Transactional
    public void deleteSelectedReportSummaries(ContentReportSummaryDeleteDto deleteDto) {
        validateDeleteDto(deleteDto);

        deleteByContents(deleteDto.getPostsId(), postsService::findById);
        deleteByContents(deleteDto.getCommentsId(), commentsService::findById);
        deleteByContents(deleteDto.getUserId(), userService::findById);
    }

    private Element getElement(ContentReportSummarySaveDto requestDto) {
        String type = requestDto.getTypes().getDetail();

        if (POSTS.getDetail().equals(type)) {
            return requestDto.getPosts();
        } else if (COMMENTS.getDetail().equals(type)) {
            return requestDto.getComments();
        } else {
            return requestDto.getUser();
        }
    }

    private Long updateContentReportSummary(ContentReportSummarySaveDto requestDto, Element element) {
        ContentReportSummary reportSummary = contentReportSummaryRepository.findByElement(element);
        reportSummary.update(requestDto.getReportedDate(), requestDto.getReporter(),
                requestDto.getReasons(), requestDto.getOtherReasons());

        return reportSummary.getId();
    }

    private Long saveContentReportSummary(ContentReportSummarySaveDto requestDto) {
        return contentReportSummaryRepository.save(requestDto.toEntity()).getId();
    }

    private Pageable createPageable(int page) {
        return PaginationRepoUtil.createPageable(page, 20);
    }

    private ContentsPageDto<ContentReportSummaryDto> createContentsPageDto(Page<ContentReportSummaryDto> summariesPage) {
        return new ContentsPageDto<>(summariesPage);
    }

    private void validateDeleteDto(ContentReportSummaryDeleteDto deleteDto) {
        if (deleteDto.getPostsId().isEmpty()
                && deleteDto.getCommentsId().isEmpty()
                && deleteDto.getUserId().isEmpty()) {
            throw new IllegalArgumentException(EMPTY_CONTENT_IDS.getMessage());
        }
    }

    private void deleteByContents(List<Long> contentIds, Function<Long, Element> finder) {
        contentIds.forEach(id -> {
            Element element = finder.apply(id);
            contentReportSummaryRepository.deleteByElement(element);
        });
    }
}
