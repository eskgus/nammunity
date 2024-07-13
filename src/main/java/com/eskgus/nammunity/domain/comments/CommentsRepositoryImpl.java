package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.common.Element;
import com.eskgus.nammunity.domain.likes.QLikes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.EssentialQuery;
import com.eskgus.nammunity.helper.FindQueries;
import com.eskgus.nammunity.helper.SearchQueries;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class CommentsRepositoryImpl extends QuerydslRepositorySupport implements CustomCommentsRepository {
    @Autowired
    private EntityManager entityManager;

    private static final QComments Q_COMMENTS = QComments.comments;
    private static final QLikes Q_LIKES = QLikes.likes;

    public CommentsRepositoryImpl() {
        super(Comments.class);
    }

    @Override
    public Page<CommentsListDto> searchByContent(String keywords, Pageable pageable) {
        return searchCommentsByFields(pageable, keywords, Q_COMMENTS.content);
    }

    @Override
    public Page<CommentsListDto> findByUser(User user, Pageable pageable) {
        return findCommentsByElement(user, pageable, CommentsListDto.class);
    }

    @Override
    public Page<CommentsReadDto> findByPosts(Posts post, Pageable pageable) {
        return findCommentsByElement(post, pageable, CommentsReadDto.class);
    }

    @Override
    public long countCommentIndex(Long postId, Long commentId) {
        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        BooleanBuilder whereCondition = new BooleanBuilder();
        whereCondition.and(Q_COMMENTS.posts.id.eq(postId));
        whereCondition.and(Q_COMMENTS.id.gt(commentId));

        return query.select(Q_COMMENTS.count()).from(Q_COMMENTS).where(whereCondition).fetchOne();
    }

    private Page<CommentsListDto> searchCommentsByFields(Pageable pageable, String keywords, StringPath... fields) {
        EssentialQuery<CommentsListDto, Comments> essentialQuery = createEssentialQuery(CommentsListDto.class);
        JPAQuery<CommentsListDto> query = createSearchQuery(pageable, essentialQuery, keywords, fields);

        return createCommentsPage(query, essentialQuery, pageable);
    }

    private <Dto> Page<Dto> findCommentsByElement(Element element, Pageable pageable, Class<Dto> dtoType) {
        EssentialQuery<Dto, Comments> essentialQuery = createEssentialQuery(dtoType);
        JPAQuery<Dto> query = createFindQuery(essentialQuery, element, pageable);

        return createCommentsPage(query, essentialQuery, pageable);
    }

    private <Dto> EssentialQuery<Dto, Comments> createEssentialQuery(Class<Dto> dtoType) {
        Expression[] constructorParams = { Q_COMMENTS, Q_LIKES.id.countDistinct() };

        return EssentialQuery.<Dto, Comments>builder()
                .entityManager(entityManager).queryType(Q_COMMENTS)
                .dtoType(dtoType).constructorParams(constructorParams).build();
    }

    private JPAQuery<CommentsListDto> createSearchQuery(Pageable pageable,
                                                        EssentialQuery<CommentsListDto, Comments> essentialQuery,
                                                        String keywords, StringPath... fields) {
        SearchQueries<CommentsListDto, Comments> searchQueries = SearchQueries.<CommentsListDto, Comments>builder()
                .essentialQuery(essentialQuery).keywords(keywords).fields(fields).build();
        JPAQuery<CommentsListDto> query = searchQueries.createQueryForSearchContents();

        return addPageToQuery(query, pageable);
    }

    private <Dto> JPAQuery<Dto> createFindQuery(EssentialQuery<Dto, Comments> essentialQuery,
                                                Element element, Pageable pageable) {
        FindQueries<Dto, Comments> findQueries = createFindQueries(essentialQuery, element);
        JPAQuery<Dto> query = findQueries.createQueryForFindContents();

        return addPageToQuery(query, pageable);
    }

    private <Dto> FindQueries<Dto, Comments> createFindQueries(EssentialQuery<Dto, Comments> essentialQuery,
                                                               Element element) {
        CommentsVisitor<Dto> visitor = new CommentsVisitor<>(Q_COMMENTS, essentialQuery);
        element.accept(visitor);

        return visitor.getFindQueries();
    }

    private <Dto> Page<Dto> createCommentsPage(JPAQuery<Dto> query, EssentialQuery<Dto, Comments> essentialQuery,
                                               Pageable pageable) {
        List<Dto> comments = createLeftJoinClause(query).fetch();
        JPAQuery<Long> totalQuery = essentialQuery.createBaseQueryForPagination(query);

        return PaginationRepoUtil.createPage(comments, pageable, totalQuery);
    }

    private <Dto> JPAQuery<Dto> createLeftJoinClause(JPAQuery<Dto> query) {
        return query.leftJoin(Q_COMMENTS.likes, Q_LIKES);
    }

    private <Dto> JPAQuery<Dto> addPageToQuery(JPAQuery<Dto> query, Pageable pageable) {
        return PaginationRepoUtil.addPageToQuery(query, pageable);
    }
}
