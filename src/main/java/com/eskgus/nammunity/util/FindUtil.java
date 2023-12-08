package com.eskgus.nammunity.util;

import com.eskgus.nammunity.domain.user.QUser;
import com.eskgus.nammunity.domain.user.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

public class FindUtil {
    public static <T, U> List<T> findContentsByUser(EntityManager entityManager,
                                         EntityPathBase<T> queryType, EntityPathBase<U> content, User user) {
        BooleanBuilder builder = new BooleanBuilder();

        // QUser의 id 찾아서 User의 id와 비교하는 조건 추가
        SimplePath<QUser> qUserPath = Expressions.path(QUser.class, queryType, "user");
        NumberPath<Long> qUserId = Expressions.numberPath(Long.class, qUserPath, "id");
        builder.and(qUserId.eq(user.getId()));

        // content(QPosts/QComments)가 들어왔으면 queryType의 해당 필드가 null이 아닌 조건 추가
        if (content != null) {
            builder.and(content.isNotNull());
        }

        // queryType의 id 찾기
        NumberPath<Long> queryTypeId = Expressions.numberPath(Long.class, queryType, "id");

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.selectFrom(queryType).where(builder).orderBy(queryTypeId.desc()).fetch();
    }
}
