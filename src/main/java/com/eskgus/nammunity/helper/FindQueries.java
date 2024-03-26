package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.domain.user.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.Builder;

public class FindQueries<T, U> {    // T: listDto, U: entity
    private EssentialQuery<T, U> essentialQuery;
    private NumberPath<Long> userId;
    private User user;
    private EntityPathBase contentTypeOfLikes;
    private BooleanBuilder whereCondition;

    @Builder
    public FindQueries(EssentialQuery<T, U> essentialQuery,
                       NumberPath<Long> userId, User user,
                       EntityPathBase contentTypeOfLikes,
                       BooleanBuilder whereCondition) {
        this.essentialQuery = essentialQuery;
        this.userId = userId;
        this.user = user;
        this.contentTypeOfLikes = contentTypeOfLikes;
        this.whereCondition = whereCondition;
    }

    public JPAQuery<T> createQueryForFindContents() {
        if (whereCondition == null) {
            createWhereCondition();
        }
        return createQueryWithConditions();
    }

    private void createWhereCondition() {
        BooleanBuilder whereCondition = new BooleanBuilder();
        if (user != null) {
            whereCondition.and(userId.eq(user.getId()));

            if (contentTypeOfLikes != null) {
                whereCondition.and(contentTypeOfLikes.isNotNull());
            }
        }
        this.whereCondition = whereCondition;
    }

    private JPAQuery<T> createQueryWithConditions() {
        JPAQuery<T> query = essentialQuery.createBaseQuery();
        return query.where(whereCondition);
    }
}
