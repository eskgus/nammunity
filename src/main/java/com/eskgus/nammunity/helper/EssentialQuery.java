package com.eskgus.nammunity.helper;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EssentialQuery<Dto, Entity> {
    private final EntityManager entityManager;
    private final EntityPathBase<Entity> queryType;

    private final Class<Dto> dtoType;
    private final Expression[] constructorParams;

    @Builder
    public EssentialQuery(EntityManager entityManager, EntityPathBase<Entity> queryType,
                          Class<Dto> dtoType, Expression[] constructorParams) {
        this.entityManager = entityManager;
        this.queryType = queryType;
        this.dtoType = dtoType;
        this.constructorParams = constructorParams;
    }

    public JPAQuery<Dto> createBaseQuery(StringPath... fields) {
        JPAQuery<Dto> query = createSelectClause(fields);

        NumberPath<Long> id = getNumberPathId();

        return query.from(queryType)
                .groupBy(id)
                .orderBy(id.desc());
    }

    public JPAQuery<Long> createBaseQueryForPagination(JPAQuery<Dto> query) {
        JPAQuery<Long> total = new JPAQuery<>(entityManager);
        return total.select(queryType.count())
                .from(queryType)
                .where(query.getMetadata().getWhere());
    }

    private JPAQuery<Dto> createSelectClause(StringPath... fields) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        Expression projections = createProjections();

        if (fields.length > 1) {
            return query.selectDistinct(projections);
        }
        return query.select(projections);
    }

    private Expression createProjections() {
        return Projections.constructor(dtoType, constructorParams);
    }

    private NumberPath<Long> getNumberPathId() {
        return Expressions.numberPath(Long.class, queryType, "id");
    }
}
