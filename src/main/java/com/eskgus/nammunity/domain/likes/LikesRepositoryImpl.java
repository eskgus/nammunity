package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.eskgus.nammunity.util.PaginationRepoUtil.*;

public class LikesRepositoryImpl extends QuerydslRepositorySupport implements CustomLikesRepository {
    @Autowired
    private EntityManager entityManager;

    public LikesRepositoryImpl() {
        super(Likes.class);
    }

    @Override
    public Page<LikesListDto> findByUser(User user, Pageable pageable) {
        QLikes like = QLikes.likes;

        BooleanBuilder whereCondition = createWhereConditionForPagination(like.user.id, user, null);
        QueryParams<Likes> queryParams = QueryParams.<Likes>builder()
                .entityManager(entityManager).queryType(like).pageable(pageable).whereCondition(whereCondition).build();
        List<LikesListDto> likes = createBaseQueryForPagination(queryParams, LikesListDto.class).fetch();
        return createPage(queryParams, likes);
    }

    @Override
    public Page<LikesListDto> findPostLikesByUser(User user, Pageable pageable) {
        QLikes like = QLikes.likes;

        BooleanBuilder whereCondition = createWhereConditionForPagination(like.user.id, user, like.posts);
        QueryParams<Likes> queryParams = QueryParams.<Likes>builder()
                .entityManager(entityManager).queryType(like).pageable(pageable).whereCondition(whereCondition).build();
        List<LikesListDto> likes = createBaseQueryForPagination(queryParams, LikesListDto.class).fetch();
        return createPage(queryParams, likes);
    }


    @Override
    public Page<LikesListDto> findCommentLikesByUser(User user, Pageable pageable) {
        QLikes like = QLikes.likes;

        BooleanBuilder whereCondition = createWhereConditionForPagination(like.user.id, user, like.comments);
        QueryParams<Likes> queryParams = QueryParams.<Likes>builder()
                .entityManager(entityManager).queryType(like).pageable(pageable).whereCondition(whereCondition).build();
        List<LikesListDto> likes = createBaseQueryForPagination(queryParams, LikesListDto.class).fetch();
        return createPage(queryParams, likes);
    }

    @Override
    @Transactional
    public void deleteByPosts(Posts post, User user) {
        deleteByField(QLikes.likes.posts, post, user);
    }

    @Override
    @Transactional
    public void deleteByComments(Comments comment, User user) {
        deleteByField(QLikes.likes.comments, comment, user);
    }

    @Override
    public long countPostLikesByUser(User user) {
        return countContentsByUser(QLikes.likes.posts, user);
    }

    @Override
    public long countCommentLikesByUser(User user) {
        return countContentsByUser(QLikes.likes.comments, user);
    }

    private <T> long countContentsByUser(EntityPathBase<T> qContent, User user) {
        QLikes like = QLikes.likes;

        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.select(like.count()).from(like).where(like.user.eq(user).and(qContent.isNotNull())).fetchOne();
    }

    private <T> void deleteByField(EntityPathBase<T> qField, T field, User user) {
        QLikes like = QLikes.likes;

        JPADeleteClause query = new JPADeleteClause(entityManager, like);
        query.where(qField.eq(field).and(like.user.eq(user))).execute();
    }
}
