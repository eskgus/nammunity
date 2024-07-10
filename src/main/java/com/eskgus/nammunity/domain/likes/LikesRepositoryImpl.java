package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.EssentialQuery;
import com.eskgus.nammunity.helper.FindQueries;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class LikesRepositoryImpl extends QuerydslRepositorySupport implements CustomLikesRepository {
    @Autowired
    private EntityManager entityManager;

    private static final QLikes Q_LIKES = QLikes.likes;

    public LikesRepositoryImpl() {
        super(Likes.class);
    }

    @Override
    public Page<LikesListDto> findByUser(User user, Pageable pageable) {
        return findLikesByFields(user, pageable, null);
    }

    @Override
    public Page<LikesListDto> findPostLikesByUser(User user, Pageable pageable) {
        return findLikesByFields(user, pageable, Q_LIKES.posts);
    }

    @Override
    public Page<LikesListDto> findCommentLikesByUser(User user, Pageable pageable) {
        return findLikesByFields(user, pageable, Q_LIKES.comments);
    }

    @Override
    @Transactional
    public void deleteByPosts(Posts post, User user) {
        deleteByField(Q_LIKES.posts, post, user);
    }

    @Override
    @Transactional
    public void deleteByComments(Comments comment, User user) {
        deleteByField(Q_LIKES.comments, comment, user);
    }

    private <Content> Page<LikesListDto> findLikesByFields(User user, Pageable pageable,
                                                           EntityPathBase<Content> contentTypeOfLikes) {
        EssentialQuery<LikesListDto, Likes> essentialQuery = createEssentialQueryForLikes();
        JPAQuery<LikesListDto> query = createQueryForFindLikes(essentialQuery, user, pageable, contentTypeOfLikes);
        return createLikesPage(query, essentialQuery, pageable);
    }

    private EssentialQuery<LikesListDto, Likes> createEssentialQueryForLikes() {
        Expression[] constructorParams = { Q_LIKES };

        return EssentialQuery.<LikesListDto, Likes>builder()
                .entityManager(entityManager).queryType(Q_LIKES)
                .dtoType(LikesListDto.class).constructorParams(constructorParams).build();
    }

    private <Content> JPAQuery<LikesListDto> createQueryForFindLikes(EssentialQuery<LikesListDto, Likes> essentialQuery,
                                                                     User user, Pageable pageable,
                                                                     EntityPathBase<Content> contentTypeOfLikes) {
        FindQueries<LikesListDto, Likes> findQueries = FindQueries.<LikesListDto, Likes>builder()
                .essentialQuery(essentialQuery).userId(Q_LIKES.user.id).user(user)
                .contentTypeOfLikes(contentTypeOfLikes).build();
        JPAQuery<LikesListDto> query = findQueries.createQueryForFindContents();

        return PaginationRepoUtil.addPageToQuery(query, pageable);
    }

    private Page<LikesListDto> createLikesPage(JPAQuery<LikesListDto> query,
                                               EssentialQuery<LikesListDto, Likes> essentialQuery,
                                               Pageable pageable) {
        List<LikesListDto> likes = query.fetch();
        JPAQuery<Long> totalQuery = essentialQuery.createBaseQueryForPagination(query);
        return PaginationRepoUtil.createPage(likes, pageable, totalQuery);
    }

    private <Field> void deleteByField(EntityPathBase<Field> qField, Field field, User user) {
        JPADeleteClause query = new JPADeleteClause(entityManager, Q_LIKES);
        query.where(qField.eq(field).and(Q_LIKES.user.eq(user))).execute();
    }
}
