package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageMoreDto;
import com.eskgus.nammunity.web.dto.pagination.PageItem;
import com.eskgus.nammunity.web.dto.pagination.PaginationDto;
import org.springframework.data.domain.Page;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PaginationTestHelper<Dto, Entity> {
    private ContentsPageDto<Dto> actualContentsPage;
    private ContentsPageDto<Dto> expectedContentsPage;

    private ContentsPageMoreDto<Dto> actualContentsPageMore;
    private ContentsPageMoreDto<Dto> expectedContentsPageMore;

    private Page<Dto> actualContents;
    private Page<Dto> expectedContents;
    private EntityConverterForTest<Dto, Entity> entityConverter;

    public PaginationTestHelper(ContentsPageDto<Dto> actualContentsPage,
                                ContentsPageDto<Dto> expectedContentsPage,
                                EntityConverterForTest<Dto, Entity> entityConverter) {
        this.actualContentsPage = actualContentsPage;
        this.expectedContentsPage = expectedContentsPage;
        generateContentsAndConverter(actualContentsPage.getContents(), expectedContentsPage.getContents(),
                entityConverter);
    }

    public PaginationTestHelper(Page<Dto> actualContents, Page<Dto> expectedContents,
                                EntityConverterForTest<Dto, Entity> entityConverter) {
        generateContentsAndConverter(actualContents, expectedContents, entityConverter);
    }

    public PaginationTestHelper(ContentsPageMoreDto<Dto> actualContentsPageMore,
                                ContentsPageMoreDto<Dto> expectedContentsPageMore,
                                EntityConverterForTest<Dto, Entity> entityConverter) {
        this.actualContentsPageMore = actualContentsPageMore;
        this.expectedContentsPageMore = expectedContentsPageMore;
        generateContentsAndConverter(actualContentsPageMore.getContents(), expectedContentsPageMore.getContents(),
                entityConverter);
    }

    private void generateContentsAndConverter(Page<Dto> actualContents, Page<Dto> expectedContents,
                                              EntityConverterForTest<Dto, Entity> entityConverter) {
        this.actualContents = actualContents;
        this.expectedContents = expectedContents;
        this.entityConverter = entityConverter;
    }

    public void assertContentsPage() {
        assertContents();
        assertPages();
    }

    public void assertContents() {
        assertPage();
        assertContent(actualContents.getContent(), expectedContents.getContent());
    }

    private void assertPage() {
        assertThat(actualContents.getTotalElements()).isEqualTo(expectedContents.getTotalElements());
        assertThat(actualContents.getTotalPages()).isEqualTo(expectedContents.getTotalPages());
        assertThat(actualContents.getNumberOfElements()).isEqualTo(expectedContents.getNumberOfElements());
    }

    private void assertContent(List<Dto> actualContent, List<Dto> expectedContent) {
        assertThat(actualContent.size()).isEqualTo(expectedContent.size());

        for (int i = 0; i < actualContent.size(); i++) {
            Dto actualDto = actualContent.get(i);
            Dto expectedDto = expectedContent.get(i);
            assertThat(entityConverter.extractDtoId(actualDto)).isEqualTo(entityConverter.extractDtoId(expectedDto));
        }
    }

    private void assertPages() {
        PaginationDto<Dto> actualPages = actualContentsPage.getPages();
        PaginationDto<Dto> expectedPages = expectedContentsPage.getPages();
        assertPaginationDto(actualPages, expectedPages);
        assertDisplayPages(actualPages.getDisplayPages(), expectedPages.getDisplayPages());
    }

    private void assertPaginationDto(PaginationDto<Dto> actualPages, PaginationDto<Dto> expectedPages) {
        assertThat(actualPages.getLastPage()).isEqualTo(expectedPages.getLastPage());
        assertThat(actualPages.getNextPage()).isEqualTo(expectedPages.getNextPage());
        assertThat(actualPages.getPreviousPage()).isEqualTo(expectedPages.getPreviousPage());
    }

    private void assertDisplayPages(List<PageItem> actualDisplayPages, List<PageItem> expectedDisplayPages) {
        assertThat(actualDisplayPages.size()).isEqualTo(expectedDisplayPages.size());

        for (int i = 0; i < actualDisplayPages.size(); i++) {
            PageItem actualPageItem = actualDisplayPages.get(i);
            PageItem expectedPageItem = expectedDisplayPages.get(i);

            assertThat(actualPageItem.getPage()).isEqualTo(expectedPageItem.getPage());
            assertThat(actualPageItem.isCurrentPage()).isEqualTo(expectedPageItem.isCurrentPage());
        }
    }

    public void assertContentsPageMore() {
        assertContents();
        assertMore();
    }

    private void assertMore() {
        assertThat(actualContentsPageMore.isMore()).isEqualTo(expectedContentsPageMore.isMore());
    }
}
