package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class ContentReportSummaryRepositoryImpl extends QuerydslRepositorySupport implements CustomContentReportSummaryRepository {
    @Autowired
    private EntityManager entityManager;

    private final QContentReportSummary qReportSummary = QContentReportSummary.contentReportSummary;

    public ContentReportSummaryRepositoryImpl() {
        super(ContentReportSummary.class);
    }

    @Override
    public <T> boolean existsByContents(T contents) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        Predicate whereCondition = createWhereConditionByContents(contents);

        return query.selectFrom(qReportSummary)
                .where(whereCondition)
                .fetchFirst() != null;
    }

    private <T> Predicate createWhereConditionByContents(T contents) {
        Predicate whereCondition;
        if (contents instanceof Posts) {
            whereCondition = qReportSummary.posts.eq((Posts) contents);
        } else if (contents instanceof Comments) {
            whereCondition = qReportSummary.comments.eq((Comments) contents);
        } else {
            whereCondition = qReportSummary.user.eq((User) contents);
        }

        return whereCondition;
    }

    @Override
    public <T> ContentReportSummary findByContents(T contents) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        Predicate whereCondition = createWhereConditionByContents(contents);

        return query.selectFrom(qReportSummary)
                .where(whereCondition).fetchOne();
    }

    @Override
    public List<ContentReportSummaryDto> findAllDesc() {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        List<ContentReportSummaryDto> reportSummaries = query.select(
                    Projections.constructor(ContentReportSummaryDto.class, qReportSummary))
                .from(qReportSummary)
                .orderBy(qReportSummary.id.desc()).fetch();
        return reportSummaries;
    }

    @Override
    public List<ContentReportSummaryDto> findByTypes(Types type) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        List<ContentReportSummaryDto> reportSummaries = query.select(
                    Projections.constructor(ContentReportSummaryDto.class, qReportSummary))
                .from(qReportSummary)
                .where(qReportSummary.types.eq(type))
                .orderBy(qReportSummary.id.desc()).fetch();
        return reportSummaries;
    }

    @Override
    @Transactional
    public <T> void deleteByContents(T contents) {
        Predicate whereCondition = createWhereConditionByContents(contents);

        JPADeleteClause query = new JPADeleteClause(entityManager, qReportSummary);
        query.where(whereCondition).execute();
    }
}
