package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.comments.QComments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.posts.QPosts;
import com.eskgus.nammunity.domain.user.QUser;
import com.eskgus.nammunity.domain.user.User;
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

    private final QContentReportSummary qReportSummary = QContentReportSummary.contentReportSummary;
    private final QPosts qPosts = QPosts.posts;
    private final QComments qComments = QComments.comments;
    private final QUser qUser = QUser.user;

    public ContentReportSummaryRepositoryImpl() {
        super(ContentReportSummary.class);
    }

    @Override
    public <Contents> boolean existsByContents(Contents contents) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        Predicate whereCondition = createWhereConditionByContents(contents);

        return query.selectFrom(qReportSummary)
                .where(whereCondition)
                .fetchFirst() != null;
    }

    private <Contents> BooleanBuilder createWhereConditionByContents(Contents contents) {
        Predicate whereCondition;
        if (contents instanceof Posts) {
            whereCondition = qReportSummary.posts.eq((Posts) contents);
        } else if (contents instanceof Comments) {
            whereCondition = qReportSummary.comments.eq((Comments) contents);
        } else if (contents instanceof User){
            whereCondition = qReportSummary.user.eq((User) contents);
        } else {
            whereCondition = qReportSummary.types.eq((Types) contents);
        }

        return new BooleanBuilder().and(whereCondition);
    }

    @Override
    public <Contents> ContentReportSummary findByContents(Contents contents) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);

        Predicate whereCondition = createWhereConditionByContents(contents);

        return query.selectFrom(qReportSummary)
                .where(whereCondition).fetchOne();
    }

    @Override
    public Page<ContentReportSummaryDto> findAllDesc(Pageable pageable) {
        return findReportSummariesByFields(null, pageable);
    }

    private Page<ContentReportSummaryDto> findReportSummariesByFields(Types type, Pageable pageable) {
        EssentialQuery<ContentReportSummaryDto, ContentReportSummary> essentialQuery
                = createEssentialQueryForReportSummaries();
        JPAQuery<ContentReportSummaryDto> query = createQueryForFindReportSummaries(essentialQuery, type, pageable);
        return createReportSummariesPage(query, essentialQuery, pageable);
    }

    private EssentialQuery<ContentReportSummaryDto, ContentReportSummary> createEssentialQueryForReportSummaries() {
        Expression[] constructorParams = { qReportSummary, qPosts, qComments, qUser};

        return EssentialQuery.<ContentReportSummaryDto, ContentReportSummary>builder()
                .entityManager(entityManager).queryType(qReportSummary)
                .dtoType(ContentReportSummaryDto.class).constructorParams(constructorParams).build();
    }

    private JPAQuery<ContentReportSummaryDto>
        createQueryForFindReportSummaries(EssentialQuery<ContentReportSummaryDto, ContentReportSummary> essentialQuery,
                                          Types type, Pageable pageable) {
        BooleanBuilder whereCondition = type != null ? createWhereConditionByContents(type) : new BooleanBuilder();

        FindQueries<ContentReportSummaryDto, ContentReportSummary> findQueries = FindQueries.<ContentReportSummaryDto, ContentReportSummary>builder()
                .essentialQuery(essentialQuery)
                .whereCondition(whereCondition).build();
        JPAQuery<ContentReportSummaryDto> query = findQueries.createQueryForFindContents();
        return PaginationRepoUtil.addPageToQuery(query, pageable);
    }

    private Page<ContentReportSummaryDto>
        createReportSummariesPage(JPAQuery<ContentReportSummaryDto> query,
                                  EssentialQuery<ContentReportSummaryDto, ContentReportSummary> essentialQuery,
                                  Pageable pageable) {
        List<ContentReportSummaryDto> reportSummaries = createLeftJoinClauseForReportSummaries(query).fetch();
        JPAQuery<Long> totalQuery = essentialQuery.createBaseQueryForPagination(query);
        return PaginationRepoUtil.createPage(reportSummaries, pageable, totalQuery);
    }

    private JPAQuery<ContentReportSummaryDto> createLeftJoinClauseForReportSummaries(JPAQuery<ContentReportSummaryDto> query) {
        return query.leftJoin(qReportSummary.posts, qPosts)
                .leftJoin(qReportSummary.comments, qComments)
                .leftJoin(qReportSummary.user, qUser);
    }

    @Override
    public Page<ContentReportSummaryDto> findByTypes(Types type, Pageable pageable) {
        return findReportSummariesByFields(type, pageable);
    }

    @Override
    @Transactional
    public <Contents> void deleteByContents(Contents contents) {
        Predicate whereCondition = createWhereConditionByContents(contents);

        JPADeleteClause query = new JPADeleteClause(entityManager, qReportSummary);
        query.where(whereCondition).execute();
    }
}
