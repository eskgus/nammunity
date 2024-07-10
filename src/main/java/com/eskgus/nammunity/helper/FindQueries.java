package com.eskgus.nammunity.helper;

import com.eskgus.nammunity.domain.user.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.Builder;

public class FindQueries<Dto, Entity> {
    private final EssentialQuery<Dto, Entity> essentialQuery;
    private NumberPath<Long> userId;
    private User user;
    private EntityPathBase contentTypeOfLikes;
    private BooleanBuilder whereCondition;

    @Builder
    public FindQueries(EssentialQuery<Dto, Entity> essentialQuery,
                       NumberPath<Long> userId, User user,
                       EntityPathBase contentTypeOfLikes,
                       BooleanBuilder whereCondition) {
        this.essentialQuery = essentialQuery;
        this.userId = userId;
        this.user = user;
        this.contentTypeOfLikes = contentTypeOfLikes;
        this.whereCondition = whereCondition;
    }

    public JPAQuery<Dto> createQueryForFindContents() {
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

    private JPAQuery<Dto> createQueryWithConditions() {
        JPAQuery<Dto> query = essentialQuery.createBaseQuery();
        return query.where(whereCondition);
    }
}
