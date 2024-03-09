package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class ContentReportSummaryRepositoryImpl extends QuerydslRepositorySupport implements CustomContentReportSummaryRepository {
    @Autowired
    private EntityManager entityManager;

    public ContentReportSummaryRepositoryImpl() {
        super(ContentReportSummary.class);
    }

    @Override
    public <T> boolean existsByContents(T contents) {
        QContentReportSummary reportSummary = QContentReportSummary.contentReportSummary;

        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        Predicate whereCondition = createWhereConditionByContents(reportSummary, contents);

        return query.selectFrom(reportSummary)
                .where(whereCondition)
                .fetchFirst() != null;
    }

    @Override
    public <T> ContentReportSummary findByContents(T contents) {
        QContentReportSummary reportSummary = QContentReportSummary.contentReportSummary;

        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        Predicate whereCondition = createWhereConditionByContents(reportSummary, contents);

        return query.selectFrom(reportSummary)
                .where(whereCondition).fetchOne();
    }

    private <T> Predicate createWhereConditionByContents(QContentReportSummary reportSummary, T contents) {
        Predicate whereCondition;
        if (contents instanceof Posts) {
            whereCondition = reportSummary.posts.eq((Posts) contents);
        } else if (contents instanceof Comments) {
            whereCondition = reportSummary.comments.eq((Comments) contents);
        } else {
            whereCondition = reportSummary.user.eq((User) contents);
        }

        return whereCondition;
    }

    @Override
    public List<ContentReportSummaryDto> findAllDesc() {
        QContentReportSummary reportSummary = QContentReportSummary.contentReportSummary;

        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        List<ContentReportSummaryDto> reportSummaries = query.select(
                    Projections.constructor(ContentReportSummaryDto.class, reportSummary))
                .from(reportSummary)
                .orderBy(reportSummary.id.desc()).fetch();
        return reportSummaries;
    }

    @Override
    public List<ContentReportSummaryDto> findByTypes(Types type) {
        QContentReportSummary reportSummary = QContentReportSummary.contentReportSummary;

        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        List<ContentReportSummaryDto> reportSummaries = query.select(
                    Projections.constructor(ContentReportSummaryDto.class, reportSummary))
                .from(reportSummary)
                .where(reportSummary.types.eq(type))
                .orderBy(reportSummary.id.desc()).fetch();
        return reportSummaries;
    }
}
