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
public class EssentialQuery<T, U> {    // T: listDto, U: entity
    private EntityManager entityManager;
    private EntityPathBase<U> queryType;

    private Class<T> classOfListDto;
    private Expression[] constructorParams;

    @Builder
    public EssentialQuery(EntityManager entityManager, EntityPathBase<U> queryType,
                          Class<T> classOfListDto, Expression[] constructorParams) {
        this.entityManager = entityManager;
        this.queryType = queryType;
        this.classOfListDto = classOfListDto;
        this.constructorParams = constructorParams;
    }

    public JPAQuery<T> createBaseQuery(StringPath... fields) {
        JPAQuery<T> query = createSelectClause(fields);

        NumberPath<Long> id = getNumberPathId();

        return query.from(queryType)
                .groupBy(id)
                .orderBy(id.desc());
    }

    private JPAQuery<T> createSelectClause(StringPath... fields) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        Expression projections = createProjections();

        if (fields.length > 1) {
            return query.selectDistinct(projections);
        }
        return query.select(projections);
    }

    private Expression createProjections() {
        return Projections.constructor(classOfListDto, constructorParams);
    }

    private NumberPath<Long> getNumberPathId() {
        return Expressions.numberPath(Long.class, queryType, "id");
    }

    public JPAQuery<Long> createBaseQueryForPagination(JPAQuery<T> query) {
        JPAQuery<Long> total = new JPAQuery<>(entityManager);
        return total.select(queryType.count())
                .from(queryType)
                .where(query.getMetadata().getWhere());
    }
}
