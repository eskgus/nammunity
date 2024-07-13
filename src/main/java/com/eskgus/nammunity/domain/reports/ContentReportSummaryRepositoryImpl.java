package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.QComments;
import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.posts.QPosts;
import com.eskgus.nammunity.domain.user.QUser;
import com.eskgus.nammunity.helper.EssentialQuery;
import com.eskgus.nammunity.helper.FindQueries;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.web.dto.reports.ContentReportSummaryDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class ContentReportSummaryRepositoryImpl extends QuerydslRepositorySupport implements CustomContentReportSummaryRepository {
    @Autowired
    private EntityManager entityManager;

    private static final QContentReportSummary Q_REPORT_SUMMARY = QContentReportSummary.contentReportSummary;
    private static final QPosts Q_POSTS = QPosts.posts;
    private static final QComments Q_COMMENTS = QComments.comments;
    private static final QUser Q_USER = QUser.user;

    public ContentReportSummaryRepositoryImpl() {
        super(ContentReportSummary.class);
    }

    @Override
    public boolean existsByElement(Element element) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        Predicate whereCondition = createWhereCondition(element);

        return query.selectFrom(Q_REPORT_SUMMARY)
                .where(whereCondition)
                .fetchFirst() != null;
    }

    public ContentReportSummary findByElement(Element element) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        Predicate whereCondition = createWhereCondition(element);

        return query.selectFrom(Q_REPORT_SUMMARY)
                .where(whereCondition).fetchOne();
    }

    @Override
    public Page<ContentReportSummaryDto> findAllDesc(Pageable pageable) {
        return findReportSummaries(null, pageable);
    }

    @Override
    public Page<ContentReportSummaryDto> findByTypes(Types type, Pageable pageable) {
        return findReportSummaries(type, pageable);
    }

    @Override
    @Transactional
    public void deleteByElement(Element element) {
        Predicate whereCondition = createWhereCondition(element);

        JPADeleteClause query = new JPADeleteClause(entityManager, Q_REPORT_SUMMARY);
        query.where(whereCondition).execute();
    }

    private Page<ContentReportSummaryDto> findReportSummaries(Types type, Pageable pageable) {
        EssentialQuery<ContentReportSummaryDto, ContentReportSummary> essentialQuery
                = createEssentialQuery();
        JPAQuery<ContentReportSummaryDto> query = createFindQuery(essentialQuery, type, pageable);

        return createReportSummariesPage(query, essentialQuery, pageable);
    }

    private EssentialQuery<ContentReportSummaryDto, ContentReportSummary> createEssentialQuery() {
        Expression[] constructorParams = {Q_REPORT_SUMMARY, Q_POSTS, Q_COMMENTS, Q_USER};

        return EssentialQuery.<ContentReportSummaryDto, ContentReportSummary>builder()
                .entityManager(entityManager).queryType(Q_REPORT_SUMMARY)
                .dtoType(ContentReportSummaryDto.class).constructorParams(constructorParams).build();
    }

    private JPAQuery<ContentReportSummaryDto> createFindQuery(
            EssentialQuery<ContentReportSummaryDto, ContentReportSummary> essentialQuery, Types type, Pageable pageable) {
        BooleanBuilder whereCondition = type != null ? createWhereCondition(type) : new BooleanBuilder();

        FindQueries<ContentReportSummaryDto, ContentReportSummary> findQueries = FindQueries.<ContentReportSummaryDto, ContentReportSummary>builder()
                .essentialQuery(essentialQuery)
                .whereCondition(whereCondition).build();
        JPAQuery<ContentReportSummaryDto> query = findQueries.createQueryForFindContents();

        return PaginationRepoUtil.addPageToQuery(query, pageable);
    }

    private BooleanBuilder createWhereCondition(Element element) {
        ReportSummaryVisitor visitor = new ReportSummaryVisitor(Q_REPORT_SUMMARY);
        element.accept(visitor);

        Predicate whereCondition = visitor.getWhereCondition();
        return new BooleanBuilder().and(whereCondition);
    }

    private Page<ContentReportSummaryDto> createReportSummariesPage(
            JPAQuery<ContentReportSummaryDto> query,
            EssentialQuery<ContentReportSummaryDto, ContentReportSummary> essentialQuery, Pageable pageable) {
        List<ContentReportSummaryDto> reportSummaries = createLeftJoinClauseForReportSummaries(query).fetch();
        JPAQuery<Long> totalQuery = essentialQuery.createBaseQueryForPagination(query);

        return PaginationRepoUtil.createPage(reportSummaries, pageable, totalQuery);
    }

    private JPAQuery<ContentReportSummaryDto> createLeftJoinClauseForReportSummaries(JPAQuery<ContentReportSummaryDto> query) {
        return query.leftJoin(Q_REPORT_SUMMARY.posts, Q_POSTS)
                .leftJoin(Q_REPORT_SUMMARY.comments, Q_COMMENTS)
                .leftJoin(Q_REPORT_SUMMARY.user, Q_USER);
    }
}
