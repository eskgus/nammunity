package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.PageItem;
import com.eskgus.nammunity.web.dto.pagination.PaginationDto;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Log4j2
public class PaginationHelperForTest2<T, U> {   // T: dto, U: entity
    private final Page<T> actualContents;
    private final PaginationDto<T> actualPages;
    private final Page<T> expectedContents;
    private final PaginationDto<T> expectedPages;
    private final EntityConverterForTest<U, T> entityConverter;

    @Builder
    public PaginationHelperForTest2(ContentsPageDto<T> actualResult,
                                    Page<T> expectedContents,
                                    PaginationDto<T> expectedPages,
                                    EntityConverterForTest<U, T> entityConverter) {
        log.info("initializePaginationUtil().....");
        this.actualContents = actualResult.getContents();
        this.actualPages = actualResult.getPages();
        this.expectedContents = expectedContents;
        this.expectedPages = expectedPages;
        this.entityConverter = entityConverter;
    }

    public void assertResults() {
        log.info("assertResults().....");
        assertContents();
        assertPages();
    }

    private void assertContents() {
        log.info("assertContents().....");
        assertPage();
        assertContent(actualContents.getContent(), expectedContents.getContent());
    }

    private void assertPage() {
        log.info("assertPage().....");
        assertThat(actualContents.getTotalElements()).isEqualTo(expectedContents.getTotalElements());
        assertThat(actualContents.getTotalPages()).isEqualTo(expectedContents.getTotalPages());
        assertThat(actualContents.getNumberOfElements()).isEqualTo(expectedContents.getNumberOfElements());
    }

    private void assertContent(List<T> actualContent, List<T> expectedContent) {
        log.info("assertContent().....");
        assertThat(actualContent.size()).isEqualTo(expectedContent.size());

        for (int i = 0; i < actualContent.size(); i++) {
            T actualDto = actualContent.get(i);
            T expectedDto = expectedContent.get(i);
            assertThat(entityConverter.extractDtoId(actualDto)).isEqualTo(entityConverter.extractDtoId(expectedDto));
        }
    }

    private void assertPages() {
        assertPaginationDto();
        assertDisplayPages(actualPages.getDisplayPages(), expectedPages.getDisplayPages());
    }

    private void assertPaginationDto() {
        log.info("assertPaginationDto().....");
        assertThat(actualPages.getLastPage()).isEqualTo(expectedPages.getLastPage());
        assertThat(actualPages.getNextPage()).isEqualTo(expectedPages.getNextPage());
        assertThat(actualPages.getPreviousPage()).isEqualTo(expectedPages.getPreviousPage());
    }

    private void assertDisplayPages(List<PageItem> actualDisplayPages, List<PageItem> expectedDisplayPages) {
        log.info("assertDisplayPages().....");
        assertThat(actualDisplayPages.size()).isEqualTo(expectedDisplayPages.size());

        for (int i = 0; i < actualDisplayPages.size(); i++) {
            PageItem actualPageItem = actualDisplayPages.get(i);
            PageItem expectedPageItem = expectedDisplayPages.get(i);

            assertThat(actualPageItem.getPage()).isEqualTo(expectedPageItem.getPage());
            assertThat(actualPageItem.isCurrentPage()).isEqualTo(expectedPageItem.isCurrentPage());
        }
    }
}
