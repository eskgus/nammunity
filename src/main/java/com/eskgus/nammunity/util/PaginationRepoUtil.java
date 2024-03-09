package com.eskgus.nammunity.util;

import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

public class PaginationRepoUtil {
    public static <T> JPAQuery<T> addPageToQuery(JPAQuery<T> query, Pageable pageable) {
        return query.offset(pageable.getOffset())
                .limit(pageable.getPageSize());
    }

    public static <T> Page<T> createPage(List<T> dtos, Pageable pageable, JPAQuery<Long> totalQuery) {
        return PageableExecutionUtils.getPage(dtos, pageable, totalQuery::fetchOne);
    }
}
