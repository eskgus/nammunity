package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.likes.QLikes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.EssentialQuery;
import com.eskgus.nammunity.helper.FindQueries;
import com.eskgus.nammunity.helper.SearchQueries;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.eskgus.nammunity.util.PaginationRepoUtil.*;

public class CommentsRepositoryImpl extends QuerydslRepositorySupport implements CustomCommentsRepository {
    @Autowired
    private EntityManager entityManager;

    private final QComments qComments = QComments.comments;
    private final QLikes qLikes = QLikes.likes;

    public CommentsRepositoryImpl() {
        super(Comments.class);
    }

    @Override
    public Page<CommentsListDto> searchByContent(String keywords, Pageable pageable) {
        return searchCommentsByFields(pageable, keywords, qComments.content);
    }

    private Page<CommentsListDto> searchCommentsByFields(Pageable pageable, String keywords, StringPath... fields) {
        EssentialQuery<CommentsListDto, Comments> essentialQuery = createEssentialQueryForComments(CommentsListDto.class);
        JPAQuery<CommentsListDto> query = createQueryForSearchComments(pageable, essentialQuery, keywords, fields);
        return createCommentsPage(query, essentialQuery, pageable);
    }

    private <T> EssentialQuery<T, Comments> createEssentialQueryForComments(Class<T> classOfDto) {
        Expression[] constructorParams = { qComments, qLikes.id.countDistinct() };

        return EssentialQuery.<T, Comments>builder()
                .entityManager(entityManager).queryType(qComments)
                .classOfListDto(classOfDto).constructorParams(constructorParams).build();
    }

    private JPAQuery<CommentsListDto> createQueryForSearchComments(Pageable pageable,
                                                                   EssentialQuery<CommentsListDto, Comments> essentialQuery,
                                                                   String keywords, StringPath... fields) {
        SearchQueries<CommentsListDto, Comments> searchQueries = SearchQueries.<CommentsListDto, Comments>builder()
                .essentialQuery(essentialQuery).keywords(keywords).fields(fields).build();
        JPAQuery<CommentsListDto> query = searchQueries.createQueryForSearchContents();
        return addPageToQuery(query, pageable);
    }

    private <T> JPAQuery<T> createLeftJoinClauseForComments(JPAQuery<T> query) {
        return query.leftJoin(qComments.likes, qLikes);
    }

    @Override
    public Page<CommentsListDto> findByUser(User user, Pageable pageable) {
        return findCommentsByFields(user, pageable, CommentsListDto.class);
    }

    private <T, U> Page<T> findCommentsByFields(U field, Pageable pageable, Class<T> classOfDto) {
        EssentialQuery<T, Comments> essentialQuery = createEssentialQueryForComments(classOfDto);
        JPAQuery<T> query = createQueryForFindComments(essentialQuery, field, pageable);
        return createCommentsPage(query, essentialQuery, pageable);
    }

    private <T, U> JPAQuery<T> createQueryForFindComments(EssentialQuery<T, Comments> essentialQuery,
                                                          U field, Pageable pageable) {
        FindQueries<T, Comments> findQueries = createFindQueries(essentialQuery, field);
        JPAQuery<T> query = findQueries.createQueryForFindContents();

        return addPageToQuery(query, pageable);
    }

    private <T, U> FindQueries<T, Comments> createFindQueries(EssentialQuery<T, Comments> essentialQuery, U field) {
        if (field instanceof User) {
            return FindQueries.<T, Comments>builder()
                    .essentialQuery(essentialQuery).userId(qComments.user.id).user((User) field).build();
        } else {
            BooleanBuilder whereCondition = createWhereConditionByField(field);
            return FindQueries.<T, Comments>builder()
                    .essentialQuery(essentialQuery).whereCondition(whereCondition).build();
        }
    }

    private <U> BooleanBuilder createWhereConditionByField(U field) {
        BooleanBuilder whereCondition = new BooleanBuilder();
        return whereCondition.and(qComments.posts.eq((Posts) field));
    }

    private <T> Page<T> createCommentsPage(JPAQuery<T> query,
                                                     EssentialQuery<T, Comments> essentialQuery,
                                                     Pageable pageable) {
        List<T> comments = createLeftJoinClauseForComments(query).fetch();
        JPAQuery<Long> totalQuery = essentialQuery.createBaseQueryForPagination(query);
        return createPage(comments, pageable, totalQuery);
    }

    @Override
    public Page<CommentsReadDto> findByPosts(Posts post, Pageable pageable) {
        return findCommentsByFields(post, pageable, CommentsReadDto.class);
    }
}
