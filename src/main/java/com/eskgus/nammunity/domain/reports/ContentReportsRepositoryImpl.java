package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.QComments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.QPosts;
import com.eskgus.nammunity.domain.user.QUser;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportDistinctDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public class ContentReportsRepositoryImpl extends QuerydslRepositorySupport implements CustomContentReportsRepository {
    @Autowired
    private EntityManager entityManager;

    public ContentReportsRepositoryImpl() {
        super(ContentReports.class);
    }

    @Override
    public List<ContentReportDistinctDto> findDistinct() {
        EntityPathBase[] qFields = { QContentReports.contentReports.posts, QContentReports.contentReports.comments, QContentReports.contentReports.user };
        Path[] aliases = { QPosts.posts, QComments.comments, QUser.user };
        return findDistinctByTypes(qFields, aliases);
    }

    @Override
    public List<ContentReportDistinctDto> findDistinctByPosts() {
        EntityPathBase[] qFields = { QContentReports.contentReports.posts };
        Path[] aliases = { QPosts.posts };
        return findDistinctByTypes(qFields, aliases);
    }

    @Override
    public List<ContentReportDistinctDto> findDistinctByComments() {
        EntityPathBase[] qFields = { QContentReports.contentReports.comments };
        Path[] aliases = { QComments.comments };
        return findDistinctByTypes(qFields, aliases);
    }

    @Override
    public List<ContentReportDistinctDto> findDistinctByUsers() {
        EntityPathBase[] qFields = { QContentReports.contentReports.user };
        Path[] aliases = { QUser.user };
        return findDistinctByTypes(qFields, aliases);
    }

    @Override
    public User findReporterByPosts(Posts post) {
        return findReporterByTypes(post);
    }

    @Override
    public User findReporterByComments(Comments comment) {
        return findReporterByTypes(comment);
    }

    @Override
    public User findReporterByUsers(User user) {
        return findReporterByTypes(user);
    }

    @Override
    public LocalDateTime findReportedDateByPosts(Posts post) {
        return findReportedDateByTypes(post);
    }

    @Override
    public LocalDateTime findReportedDateByComments(Comments comment) {
        return findReportedDateByTypes(comment);
    }

    @Override
    public LocalDateTime findReportedDateByUsers(User user) {
        return findReportedDateByTypes(user);
    }

    @Override
    public Reasons findReasonByPosts(Posts post) {
        return findReasonByTypes(post);
    }

    @Override
    public Reasons findReasonByComments(Comments comment) {
        return findReasonByTypes(comment);
    }

    @Override
    public Reasons findReasonByUsers(User user) {
        return findReasonByTypes(user);
    }

    @Override
    public String findOtherReasonByPosts(Posts post, Reasons reason) {
        return findOtherReasonByTypes(post, reason);
    }

    @Override
    public String findOtherReasonByComments(Comments comment, Reasons reason) {
        return findOtherReasonByTypes(comment, reason);
    }

    @Override
    public String findOtherReasonByUsers(User user, Reasons reason) {
        return findOtherReasonByTypes(user, reason);
    }

    @Override
    public String findOtherReasonById(Long id) {
        QContentReports report = QContentReports.contentReports;

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(report.otherReasons).from(report)
                .where(report.id.eq(id))
                .fetchOne();
    }

    @Override
    public List<ContentReports> findByPosts(Posts post) {
        return findByTypes(post);
    }

    @Override
    public List<ContentReports> findByComments(Comments comment) {
        return findByTypes(comment);
    }

    @Override
    public List<ContentReports> findByUsers(User user) {
        return findByTypes(user);
    }

    @Override
    @Transactional
    public void deleteByPosts(Posts post) {
        deleteByTypes(post);
    }

    @Override
    @Transactional
    public void deleteByComments(Comments comment) {
        deleteByTypes(comment);
    }

    @Override
    @Transactional
    public void deleteByUsers(User user) {
        deleteByTypes(user);
    }

    @Override
    public long countPostReportsByUser(User user) {
        return countByUserInTypes(QContentReports.contentReports.posts.user, user);
    }

    @Override
    public long countCommentReportsByUser(User user) {
        return countByUserInTypes(QContentReports.contentReports.comments.user, user);
    }

    @Override
    public long countUserReportsByUser(User user) {
        return countByUserInTypes(QContentReports.contentReports.user, user);
    }

    private List<ContentReportDistinctDto> findDistinctByTypes(EntityPathBase[] qFields, Path[] aliases) {
        // main 쿼리 준비물
        QContentReports report = QContentReports.contentReports;
        QTypes type = QTypes.types;

        // sub 쿼리 준비물
        QContentReports reportSub = QContentReports.contentReports;

        // main 쿼리 생성
        JPAQuery<ContentReportDistinctDto> query = new JPAQuery<>(entityManager);
        query.from(report).leftJoin(report.types, type);

        // main-select
        Expression[] constructorArgs = new Expression[aliases.length + 1];
        constructorArgs[0] = type;

        // sub-groupBy
        Expression[] groupByCondition = new Expression[qFields.length + 1];
        groupByCondition[0] = reportSub.types;

        // sub-having
        BooleanBuilder havingCondition = new BooleanBuilder();

        for (int i = 0; i < aliases.length; i++) {
            constructorArgs[i + 1] = aliases[i];    // main-select
            query.leftJoin(qFields[i], aliases[i]); // main-leftJoin
            groupByCondition[i + 1] = qFields[i];   // sub-groupBy

            // sub-having
            int right = User.class.isAssignableFrom(qFields[i].getType()) ? 3 : 10;
            havingCondition.or(qFields[i].count().goe(right));
        }

        // sub-where
        Predicate whereCondition = (qFields.length > 1) ? null : qFields[0].isNotNull();

        return query.select(
                    Projections.constructor(ContentReportDistinctDto.class, constructorArgs))
                .where(report.id.in(
                        JPAExpressions.select(reportSub.id.min()).from(reportSub)
                                .where(whereCondition)
                                .groupBy(groupByCondition)
                                .having(havingCondition)))
                .orderBy(report.id.asc()).fetch();
    }

    private <T> User findReporterByTypes(T type) {
        QContentReports report = QContentReports.contentReports;

        JPAQuery<User> query = createBaseQueryForFindingReporterOrReason(report, type, report.reporter);
        return query.fetchOne();
    }

    private <T> LocalDateTime findReportedDateByTypes(T type) {
        QContentReports report = QContentReports.contentReports;

        Predicate whereCondition = createWhereConditionByTypes(report, type);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(report.createdDate.max()).from(report)
                .where(whereCondition)
                .fetchOne();
    }

    private <T> Reasons findReasonByTypes(T type) {
        QContentReports report = QContentReports.contentReports;

        JPAQuery<Reasons> query = createBaseQueryForFindingReporterOrReason(report, type, report.reasons);
        return query.fetchOne();
    }

    private <T> String findOtherReasonByTypes(T type, Reasons reason) {
        QContentReports report = QContentReports.contentReports;

        Predicate whereCondition1 = createWhereConditionByTypes(report, type);
        Predicate whereCondition2 = createWhereConditionByTypes(report, reason);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(report.otherReasons).from(report)
                .where(whereCondition1, whereCondition2)
                .orderBy(report.createdDate.desc())
                .limit(1)
                .fetchOne();
    }

    private <T> List<ContentReports> findByTypes(T type) {
        QContentReports report = QContentReports.contentReports;

        Predicate whereCondition = createWhereConditionByTypes(report, type);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.selectFrom(report).where(whereCondition).fetch();
    }

    private <T> void deleteByTypes(T type) {
        QContentReports report = QContentReports.contentReports;

        Predicate whereCondition = createWhereConditionByTypes(report, type);

        JPADeleteClause query = new JPADeleteClause(entityManager, report);
        query.where(whereCondition).execute();
    }

    private long countByUserInTypes(EntityPathBase<User> qUser, User user) {
        QContentReports report = QContentReports.contentReports;

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(report.count()).from(report).where(qUser.eq(user)).fetchOne();
    }

    private <T, U> JPAQuery<U> createBaseQueryForFindingReporterOrReason(QContentReports report, T type,
                                                                         EntityPathBase<U> qField) {
        Predicate whereCondition = createWhereConditionByTypes(report, type);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(qField).from(report)
                .where(whereCondition)
                .groupBy(qField)
                .orderBy(report.count().desc(), report.createdDate.max().desc())
                .limit(1);
    }

    private <T> Predicate createWhereConditionByTypes(QContentReports report, T type) {
        Predicate whereCondition;
        if (type instanceof Posts) {
            whereCondition = report.posts.eq((Posts) type);
        } else if (type instanceof Comments) {
            whereCondition = report.comments.eq((Comments) type);
        } else if (type instanceof User) {
            whereCondition = report.user.eq((User) type);
        } else {
            whereCondition = report.reasons.eq((Reasons) type);
        }

        return whereCondition;
    }
}
