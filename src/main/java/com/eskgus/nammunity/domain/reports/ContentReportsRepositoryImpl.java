package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.enums.ContentType;
import com.eskgus.nammunity.domain.posts.Posts;
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

public class ContentReportsRepositoryImpl extends QuerydslRepositorySupport implements CustomContentReportsRepository {
    @Autowired
    private EntityManager entityManager;

    private final QContentReports qContentReports = QContentReports.contentReports;

    public ContentReportsRepositoryImpl() {
        super(ContentReports.class);
    }

    @Override
    public <Contents> User findReporterByContents(Contents contents) {
        JPAQuery<User> query = createBaseQueryForFindingReporterOrReason(contents, qContentReports.reporter);
        return query.fetchOne();
    }

    @Override
    public <Contents> LocalDateTime findReportedDateByContents(Contents contents) {
        BooleanBuilder whereCondition = createWhereConditionByContents(contents);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(qContentReports.createdDate.max()).from(qContentReports)
                .where(whereCondition)
                .fetchOne();
    }

    private <Contents> BooleanBuilder createWhereConditionByContents(Contents contents) {
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
    public <Contents> Reasons findReasonByContents(Contents contents) {
        JPAQuery<Reasons> query = createBaseQueryForFindingReporterOrReason(contents, qContentReports.reasons);
        return query.fetchOne();
    }

    @Override
    public <Contents> String findOtherReasonByContents(Contents contents, Reasons reason) {
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
    public <Contents> Page<ContentReportDetailListDto> findByContents(Contents contents, Pageable pageable) {
        return findReportsByFields(contents, pageable);
    }

    private <Contents> Page<ContentReportDetailListDto> findReportsByFields(Contents contents, Pageable pageable) {
        EssentialQuery<ContentReportDetailListDto, ContentReports> essentialQuery
                = createEssentialQueryForReports();
        JPAQuery<ContentReportDetailListDto> query = createQueryForFindReports(essentialQuery, contents, pageable);
        return createReportsPage(query, essentialQuery, pageable);
    }

    private EssentialQuery<ContentReportDetailListDto, ContentReports> createEssentialQueryForReports() {
        Expression[] constructorParams = { qContentReports };

        return EssentialQuery.<ContentReportDetailListDto, ContentReports>builder()
                .entityManager(entityManager).queryType(qContentReports)
                .dtoType(ContentReportDetailListDto.class).constructorParams(constructorParams).build();
    }

    private <Contents> JPAQuery<ContentReportDetailListDto>
        createQueryForFindReports(EssentialQuery<ContentReportDetailListDto, ContentReports> essentialQuery,
                                  Contents contents, Pageable pageable) {
        BooleanBuilder whereCondition = createWhereConditionByContents(contents);

        FindQueries<ContentReportDetailListDto, ContentReports> findQueries = FindQueries.<ContentReportDetailListDto, ContentReports>builder()
                .essentialQuery(essentialQuery)
                .whereCondition(whereCondition).build();
        JPAQuery<ContentReportDetailListDto> query = findQueries.createQueryForFindContents();
        return PaginationRepoUtil.addPageToQuery(query, pageable);
    }

    private Page<ContentReportDetailListDto>
        createReportsPage(JPAQuery<ContentReportDetailListDto> query,
                          EssentialQuery<ContentReportDetailListDto, ContentReports> essentialQuery,
                          Pageable pageable) {
        List<ContentReportDetailListDto> reports = query.fetch();
        JPAQuery<Long> totalQuery = essentialQuery.createBaseQueryForPagination(query);
        return PaginationRepoUtil.createPage(reports, pageable, totalQuery);
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
    public <Contents> long countByContents(Contents contents) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        BooleanBuilder whereCondition = createWhereConditionByContents(contents);
        return query.select(qContentReports.count()).from(qContentReports).where(whereCondition).fetchOne();
    }

    private <Contents, Field> JPAQuery<Field> createBaseQueryForFindingReporterOrReason(Contents contents,
                                                                                        EntityPathBase<Field> qField) {
        BooleanBuilder whereCondition = createWhereConditionByContents(contents);

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(qField).from(qContentReports)
                .where(whereCondition)
                .groupBy(qField)
                .orderBy(qContentReports.count().desc(), qContentReports.createdDate.max().desc())
                .limit(1);
    }
}
