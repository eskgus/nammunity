package com.eskgus.nammunity.util;

import com.eskgus.nammunity.domain.user.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

public class PaginationRepoUtil {
    @Getter
    public static class QueryParams<U> {
        private EntityManager entityManager;
        private EntityPathBase<U> queryType;
        private Pageable pageable;
        private BooleanBuilder whereCondition;

        @Builder
        public QueryParams(EntityManager entityManager, EntityPathBase<U> queryType, Pageable pageable,
                           BooleanBuilder whereCondition) {
            this.entityManager = entityManager;
            this.queryType = queryType;
            this.pageable = pageable;
            this.whereCondition = whereCondition;
        }
    }

    public static <T> JPAQuery<T> createBaseQueryForPagination(QueryParams queryParams, Class<T> type) {
        NumberPath<Long> id = Expressions.numberPath(Long.class, queryParams.getQueryType(), "id");

        JPAQuery<T> query = new JPAQuery<>(queryParams.getEntityManager());
        return query.select(
                    Projections.constructor(type, queryParams.getQueryType())).from(queryParams.getQueryType())
                .where(queryParams.getWhereCondition())
                .orderBy(id.desc())
                .offset(queryParams.getPageable().getOffset())
                .limit(queryParams.getPageable().getPageSize());
    }

    public static <T> BooleanBuilder createWhereConditionForPagination(NumberPath<Long> userId, User user,
                                                                       EntityPathBase<T> content) {
        BooleanBuilder whereCondition = new BooleanBuilder();
        whereCondition.and(userId.eq(user.getId()));

        if (content != null) {
            whereCondition.and(content.isNotNull());
        }

        return whereCondition;
    }

    public static <T> Page<T> createPage(QueryParams queryParams, List<T> dtos) {
        JPAQuery<Long> total = new JPAQuery<>(queryParams.getEntityManager());
        total.select(queryParams.getQueryType().count()).from(queryParams.getQueryType())
                .where(queryParams.getWhereCondition());
        return PageableExecutionUtils.getPage(dtos, queryParams.getPageable(), total::fetchOne);
    }
}
