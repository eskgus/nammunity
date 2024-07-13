package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.EssentialQuery;
import com.eskgus.nammunity.helper.FindQueries;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.web.dto.reports.ContentReportDetailListDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.time.LocalDateTime;
import java.util.List;

import static com.eskgus.nammunity.domain.enums.ContentType.COMMENTS;
import static com.eskgus.nammunity.domain.enums.ContentType.POSTS;

public class ContentReportsRepositoryImpl extends QuerydslRepositorySupport implements CustomContentReportsRepository {
    @Autowired
    private EntityManager entityManager;

    private static final QContentReports Q_CONTENT_REPORTS = QContentReports.contentReports;

    public ContentReportsRepositoryImpl() {
        super(ContentReports.class);
    }

    @Override
    public User findReporterByElement(Element element) {
        JPAQuery<User> query = findReporterOrReason(element, Q_CONTENT_REPORTS.reporter);

        return query.fetchOne();
    }

    @Override
    public LocalDateTime findReportedDateByElement(Element element) {
        BooleanBuilder whereCondition = createWhereCondition(element);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        return query.select(Q_CONTENT_REPORTS.createdDate.max()).from(Q_CONTENT_REPORTS)
                .where(whereCondition)
                .fetchOne();
    }

    @Override
    public Reasons findReasonByElement(Element element) {
        JPAQuery<Reasons> query = findReporterOrReason(element, Q_CONTENT_REPORTS.reasons);

        return query.fetchOne();
    }

    @Override
    public String findOtherReasonByElement(Element element, Reasons reason) {
        BooleanBuilder elementCondition = createWhereCondition(element);
        BooleanBuilder reasonCondition = createWhereCondition(reason);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        return query.select(Q_CONTENT_REPORTS.otherReasons).from(Q_CONTENT_REPORTS)
                .where(elementCondition, reasonCondition)
                .orderBy(Q_CONTENT_REPORTS.createdDate.desc())
                .limit(1)
                .fetchOne();
    }

    @Override
    public Page<ContentReportDetailListDto> findByElement(Element element, Pageable pageable) {
        return findReportsByElement(element, pageable);
    }

    @Override
    public long countReportsByContentTypeAndUser(ContentType contentType, User user) {
        Predicate whereCondition = createWhereCondition(contentType, user);

        return countReports(whereCondition);
    }

    @Override
    public long countByElement(Element element) {
        BooleanBuilder whereCondition = createWhereCondition(element);

        return countReports(whereCondition);
    }

    private <Field> JPAQuery<Field> findReporterOrReason(Element element,
                                                         EntityPathBase<Field> qField) {
        BooleanBuilder whereCondition = createWhereCondition(element);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        return query.select(qField).from(Q_CONTENT_REPORTS)
                .where(whereCondition)
                .groupBy(qField)
                .orderBy(Q_CONTENT_REPORTS.count().desc(), Q_CONTENT_REPORTS.createdDate.max().desc())
                .limit(1);
    }

    private Page<ContentReportDetailListDto> findReportsByElement(Element element, Pageable pageable) {
        EssentialQuery<ContentReportDetailListDto, ContentReports> essentialQuery
                = createEssentialQuery();

        JPAQuery<ContentReportDetailListDto> query = createFindQuery(essentialQuery, element, pageable);

        return createReportsPage(query, essentialQuery, pageable);
    }

    private long countReports(Predicate whereCondition) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        return query.select(Q_CONTENT_REPORTS.count()).from(Q_CONTENT_REPORTS).where(whereCondition).fetchOne();
    }

    private EssentialQuery<ContentReportDetailListDto, ContentReports> createEssentialQuery() {
        Expression[] constructorParams = { Q_CONTENT_REPORTS };

        return EssentialQuery.<ContentReportDetailListDto, ContentReports>builder()
                .entityManager(entityManager).queryType(Q_CONTENT_REPORTS)
                .dtoType(ContentReportDetailListDto.class).constructorParams(constructorParams).build();
    }

    private JPAQuery<ContentReportDetailListDto> createFindQuery(
            EssentialQuery<ContentReportDetailListDto, ContentReports> essentialQuery,
            Element element, Pageable pageable) {
        BooleanBuilder whereCondition = createWhereCondition(element);

        FindQueries<ContentReportDetailListDto, ContentReports> findQueries = FindQueries.<ContentReportDetailListDto, ContentReports>builder()
                .essentialQuery(essentialQuery).whereCondition(whereCondition).build();

        JPAQuery<ContentReportDetailListDto> query = findQueries.createQueryForFindContents();

        return PaginationRepoUtil.addPageToQuery(query, pageable);
    }

    private Predicate createWhereCondition(ContentType contentType, User user) {
        if (POSTS.equals(contentType)) {
            return Q_CONTENT_REPORTS.posts.user.eq(user);
        } else if (COMMENTS.equals(contentType)) {
            return Q_CONTENT_REPORTS.comments.user.eq(user);
        } else {
            return Q_CONTENT_REPORTS.user.eq(user);
        }
    }

    private BooleanBuilder createWhereCondition(Element element) {
        ReportsVisitor visitor = new ReportsVisitor(Q_CONTENT_REPORTS);
        element.accept(visitor);

        Predicate whereCondition = visitor.getWhereCondition();
        return new BooleanBuilder().and(whereCondition);
    }

    private Page<ContentReportDetailListDto> createReportsPage(
            JPAQuery<ContentReportDetailListDto> query,
            EssentialQuery<ContentReportDetailListDto, ContentReports> essentialQuery, Pageable pageable) {
        List<ContentReportDetailListDto> reports = query.fetch();
        JPAQuery<Long> totalQuery = essentialQuery.createBaseQueryForPagination(query);

        return PaginationRepoUtil.createPage(reports, pageable, totalQuery);
    }
}
