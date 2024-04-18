package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.converter.EntityConverterForTest;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import com.eskgus.nammunity.web.dto.pagination.PaginationDto;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.function.Function;

@Log4j2
public class FindHelperForTest2<T, U> { // T: dto, U: entity
    private final Function<Integer, ContentsPageDto<T>> finder;
    private final Function<Pageable, Page<T>> pageGetter;
    private final int page = 1;
    private final int size;
    private final EntityConverterForTest<U, T> entityConverter;

    @Builder
    public FindHelperForTest2(Function<Integer, ContentsPageDto<T>> finder, Function<Pageable, Page<T>> pageGetter,
                              int size, EntityConverterForTest<U, T> entityConverter) {
        log.info("FindHelperForTest2().....");
        this.finder = finder;
        this.pageGetter = pageGetter;
        this.size = size;
        this.entityConverter = entityConverter;
    }

    public void callAndAssertFind() {
        log.info("callAndAssertFind().....");
        ContentsPageDto<T> actualResult = applyFinder();
        log.info("created actualFindResult...");

        Page<T> expectedContents = createExpectedContents();
        PaginationDto<T> expectedPages = createExpectedPages(expectedContents);

        PaginationHelperForTest2<T, U> paginationHelper = PaginationHelperForTest2.<T, U>builder()
                .actualResult(actualResult).expectedContents(expectedContents).expectedPages(expectedPages)
                .entityConverter(entityConverter).build();
        paginationHelper.assertResults();
    }

    private ContentsPageDto<T> applyFinder() {
        log.info("applyFinder().....");
        return finder.apply(page);
    }

    private Page<T> createExpectedContents() {
        log.info("createExpectedContents().....");
        Pageable pageable = createPageable(size);
        return applyPageGetter(pageable);
    }

    private Pageable createPageable(int size) {
        log.info("createPageable().....");
        return PageRequest.of(page - 1, size);
    }

    private Page<T> applyPageGetter(Pageable pageable) {
        log.info("applyPageGetter().....");
        return pageGetter.apply(pageable);
    }

    private PaginationDto<T> createExpectedPages(Page<T> expectedContents) {
        log.info("createExpectedPages().....");
        return PaginationDto.<T>builder().page(expectedContents).display(10).build();
    }
}
