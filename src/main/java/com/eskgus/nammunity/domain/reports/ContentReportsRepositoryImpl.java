package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.time.LocalDateTime;
import java.util.List;

public class ContentReportsRepositoryImpl extends QuerydslRepositorySupport implements CustomContentReportsRepository {
    @Autowired
    private EntityManager entityManager;

    public ContentReportsRepositoryImpl() {
        super(ContentReports.class);
    }

    @Override
    public <T> User findReporterByContents(T contents) {
        QContentReports report = QContentReports.contentReports;

        JPAQuery<User> query = createBaseQueryForFindingReporterOrReason(report, contents, report.reporter);
        return query.fetchOne();
    }

    @Override
    public <T> LocalDateTime findReportedDateByContents(T contents) {
        QContentReports report = QContentReports.contentReports;

        Predicate whereCondition = createWhereConditionByContents(report, contents);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(report.createdDate.max()).from(report)
                .where(whereCondition)
                .fetchOne();
    }

    @Override
    public <T> Reasons findReasonByContents(T contents) {
        QContentReports report = QContentReports.contentReports;

        JPAQuery<Reasons> query = createBaseQueryForFindingReporterOrReason(report, contents, report.reasons);
        return query.fetchOne();
    }

    @Override
    public <T> String findOtherReasonByContents(T contents, Reasons reason) {
        QContentReports report = QContentReports.contentReports;

        Predicate whereCondition1 = createWhereConditionByContents(report, contents);
        Predicate whereCondition2 = createWhereConditionByContents(report, reason);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(report.otherReasons).from(report)
                .where(whereCondition1, whereCondition2)
                .orderBy(report.createdDate.desc())
                .limit(1)
                .fetchOne();
    }

    @Override
    public <T> List<ContentReportDetailListDto> findByContents(T contents) {
        QContentReports report = QContentReports.contentReports;

        Predicate whereCondition = createWhereConditionByContents(report, contents);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(
                    Projections.constructor(ContentReportDetailListDto.class, report))
                .from(report)
                .where(whereCondition).fetch();
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

    @Override
    public <T> long countByContents(T contents) {
        QContentReports report = QContentReports.contentReports;
        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        Predicate whereCondition = createWhereConditionByContents(report, contents);
        return query.select(report.count()).from(report).where(whereCondition).fetchOne();
    }

    private long countByUserInTypes(EntityPathBase<User> qUser, User user) {
        QContentReports report = QContentReports.contentReports;

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(report.count()).from(report).where(qUser.eq(user)).fetchOne();
    }

    private <T, U> JPAQuery<U> createBaseQueryForFindingReporterOrReason(QContentReports report, T contents,
                                                                         EntityPathBase<U> qField) {
        Predicate whereCondition = createWhereConditionByContents(report, contents);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(qField).from(report)
                .where(whereCondition)
                .groupBy(qField)
                .orderBy(report.count().desc(), report.createdDate.max().desc())
                .limit(1);
    }

    private <T> Predicate createWhereConditionByContents(QContentReports report, T contents) {
        Predicate whereCondition;
        if (contents instanceof Posts) {
            whereCondition = report.posts.eq((Posts) contents);
        } else if (contents instanceof Comments) {
            whereCondition = report.comments.eq((Comments) contents);
        } else if (contents instanceof User) {
            whereCondition = report.user.eq((User) contents);
        } else {
            whereCondition = report.reasons.eq((Reasons) contents);
        }

        return whereCondition;
    }
}
