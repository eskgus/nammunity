package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.eskgus.nammunity.util.FindUtil.findContentsByUser;

public class LikesRepositoryImpl extends QuerydslRepositorySupport implements CustomLikesRepository {
    @Autowired
    private EntityManager entityManager;

    public LikesRepositoryImpl() {
        super(Likes.class);
    }

    @Override
    public List<Likes> findByUser(User user) {
        QLikes like = QLikes.likes;
        return findContentsByUser(entityManager, like, null, user);
    }

    @Override
    public List<Likes> findPostLikesByUser(User user) {
        QLikes like = QLikes.likes;
        return findContentsByUser(entityManager, like, like.posts, user);
    }

    @Override
    public List<Likes> findCommentLikesByUser(User user) {
        QLikes like = QLikes.likes;
        return findContentsByUser(entityManager, like, like.comments, user);
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
