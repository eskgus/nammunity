package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.reports.ContentReportSummary;
import com.eskgus.nammunity.domain.reports.ContentReportSummaryRepository;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummarySaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ReportSummaryService {
    private final ContentReportSummaryRepository contentReportSummaryRepository;
    private final TypesService typesService;

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
    public Long saveContentReportSummary(ContentReportSummarySaveDto requestDto) {
        return contentReportSummaryRepository.save(requestDto.toEntity()).getId();
    }

    @Transactional
    public <T> Long updateContentReportSummary(ContentReportSummarySaveDto requestDto, T contents) {
        ContentReportSummary reportSummary = contentReportSummaryRepository.findByContents(contents);
        reportSummary.update(requestDto.getReportedDate(), requestDto.getReporter(),
                requestDto.getReasons(), requestDto.getOtherReasons());
        return reportSummary.getId();
    }

    @Transactional(readOnly = true)
    public List<ContentReportSummaryDto> findAllDesc() {
        return contentReportSummaryRepository.findAllDesc();
    }

    @Transactional(readOnly = true)
    public List<ContentReportSummaryDto> findByTypes(ContentType contentType) {
        Types type = typesService.findByContentType(contentType);
        return contentReportSummaryRepository.findByTypes(type);
    }
}
