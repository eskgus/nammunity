package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.EssentialQuery;
import com.eskgus.nammunity.helper.FindQueries;
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

import static com.eskgus.nammunity.util.PaginationRepoUtil.addPageToQuery;
import static com.eskgus.nammunity.util.PaginationRepoUtil.createPage;

public class ContentReportsRepositoryImpl extends QuerydslRepositorySupport implements CustomContentReportsRepository {
    @Autowired
    private EntityManager entityManager;

    private final QContentReports qContentReports = QContentReports.contentReports;

    public ContentReportsRepositoryImpl() {
        super(ContentReports.class);
    }

    @Override
    public <T> User findReporterByContents(T contents) {
        JPAQuery<User> query = createBaseQueryForFindingReporterOrReason(contents, qContentReports.reporter);
        return query.fetchOne();
    }

    @Override
    public <T> LocalDateTime findReportedDateByContents(T contents) {
        BooleanBuilder whereCondition = createWhereConditionByContents(contents);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(qContentReports.createdDate.max()).from(qContentReports)
                .where(whereCondition)
                .fetchOne();
    }

    private <T> BooleanBuilder createWhereConditionByContents(T contents) {
        Predicate whereCondition;
        if (contents instanceof Posts) {
            whereCondition = qContentReports.posts.eq((Posts) contents);
        } else if (contents instanceof Comments) {
            whereCondition = qContentReports.comments.eq((Comments) contents);
        } else if (contents instanceof User) {
            whereCondition = qContentReports.user.eq((User) contents);
        } else {
            whereCondition = qContentReports.reasons.eq((Reasons) contents);
        }

        return new BooleanBuilder().and(whereCondition);
    }

    @Override
    public <T> Reasons findReasonByContents(T contents) {
        JPAQuery<Reasons> query = createBaseQueryForFindingReporterOrReason(contents, qContentReports.reasons);
        return query.fetchOne();
    }

    @Override
    public <T> String findOtherReasonByContents(T contents, Reasons reason) {
        BooleanBuilder whereCondition1 = createWhereConditionByContents(contents);
        BooleanBuilder whereCondition2 = createWhereConditionByContents(reason);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(qContentReports.otherReasons).from(qContentReports)
                .where(whereCondition1, whereCondition2)
                .orderBy(qContentReports.createdDate.desc())
                .limit(1)
                .fetchOne();
    }

    @Override
    public <T> Page<ContentReportDetailListDto> findByContents(T contents, Pageable pageable) {
        return findReportsByFields(contents, pageable);
    }

    private <T> Page<ContentReportDetailListDto> findReportsByFields(T contents, Pageable pageable) {
        EssentialQuery<ContentReportDetailListDto, ContentReports> essentialQuery
                = createEssentialQueryForReports();
        JPAQuery<ContentReportDetailListDto> query = createQueryForFindReports(essentialQuery, contents, pageable);
        return createReportsPage(query, essentialQuery, pageable);
    }

    private EssentialQuery<ContentReportDetailListDto, ContentReports> createEssentialQueryForReports() {
        Expression[] constructorParams = { qContentReports };

        return EssentialQuery.<ContentReportDetailListDto, ContentReports>builder()
                .entityManager(entityManager).queryType(qContentReports)
                .classOfListDto(ContentReportDetailListDto.class).constructorParams(constructorParams).build();
    }

    private <T> JPAQuery<ContentReportDetailListDto>
        createQueryForFindReports(EssentialQuery<ContentReportDetailListDto, ContentReports> essentialQuery,
                                  T contents, Pageable pageable) {
        BooleanBuilder whereCondition = createWhereConditionByContents(contents);

        FindQueries<ContentReportDetailListDto, ContentReports> findQueries = FindQueries.<ContentReportDetailListDto, ContentReports>builder()
                .essentialQuery(essentialQuery)
                .whereCondition(whereCondition).build();
        JPAQuery<ContentReportDetailListDto> query = findQueries.createQueryForFindContents();
        return addPageToQuery(query, pageable);
    }

    private Page<ContentReportDetailListDto>
        createReportsPage(JPAQuery<ContentReportDetailListDto> query,
                          EssentialQuery<ContentReportDetailListDto, ContentReports> essentialQuery,
                          Pageable pageable) {
        List<ContentReportDetailListDto> reports = query.fetch();
        JPAQuery<Long> totalQuery = essentialQuery.createBaseQueryForPagination(query);
        return createPage(reports, pageable, totalQuery);
    }

    @Override
    public long countReportsByContentTypeAndUser(ContentType contentType, User user) {
        QContentReports qContentReports = QContentReports.contentReports;
        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        Predicate whereCondition = createWhereConditionForCount(qContentReports, contentType, user);
        return query.select(qContentReports.count()).from(qContentReports).where(whereCondition).fetchOne();
    }

    private Predicate createWhereConditionForCount(QContentReports qContentReports, ContentType contentType, User user) {
        if (contentType.equals(ContentType.POSTS)) {
            return qContentReports.posts.user.eq(user);
        } else if (contentType.equals(ContentType.COMMENTS)) {
            return qContentReports.comments.user.eq(user);
        } else {
            return qContentReports.user.eq(user);
        }
    }

    @Override
    public <T> long countByContents(T contents) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        BooleanBuilder whereCondition = createWhereConditionByContents(contents);
        return query.select(qContentReports.count()).from(qContentReports).where(whereCondition).fetchOne();
    }

    private <T, U> JPAQuery<U> createBaseQueryForFindingReporterOrReason(T contents,
                                                                         EntityPathBase<U> qField) {
        BooleanBuilder whereCondition = createWhereConditionByContents(contents);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(qField).from(qContentReports)
                .where(whereCondition)
                .groupBy(qField)
                .orderBy(qContentReports.count().desc(), qContentReports.createdDate.max().desc())
                .limit(1);
    }
}
