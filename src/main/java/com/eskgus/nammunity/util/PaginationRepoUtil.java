package com.eskgus.nammunity.util;

import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

public class PaginationRepoUtil {
    public static <Dto> JPAQuery<Dto> addPageToQuery(JPAQuery<Dto> query, Pageable pageable) {
        return query.offset(pageable.getOffset())
                .limit(pageable.getPageSize());
    }

    public static <Dto> Page<Dto> createPage(List<Dto> dtos, Pageable pageable, JPAQuery<Long> totalQuery) {
        return PageableExecutionUtils.getPage(dtos, pageable, totalQuery::fetchOne);
    }

    public static Pageable createPageable(int page, int size) {
        return PageRequest.of(page - 1, size);
    }
}
