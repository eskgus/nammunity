package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.EssentialQuery;
import com.eskgus.nammunity.helper.FindQueries;
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

import static com.eskgus.nammunity.util.PaginationRepoUtil.*;

public class LikesRepositoryImpl extends QuerydslRepositorySupport implements CustomLikesRepository {
    @Autowired
    private EntityManager entityManager;

    private final QLikes qLikes = QLikes.likes;

    public LikesRepositoryImpl() {
        super(Likes.class);
    }

    @Override
    public Page<LikesListDto> findByUser(User user, Pageable pageable) {
        return findLikesByFields(user, pageable, null);
    }

    private <T> Page<LikesListDto> findLikesByFields(User user, Pageable pageable, EntityPathBase<T> contentTypeOfLikes) {
        EssentialQuery<LikesListDto, Likes> essentialQuery = createEssentialQueryForLikes();
        JPAQuery<LikesListDto> query = createQueryForFindLikes(essentialQuery, user, pageable, contentTypeOfLikes);
        return createLikesPage(query, essentialQuery, pageable);
    }

    private EssentialQuery<LikesListDto, Likes> createEssentialQueryForLikes() {
        Expression[] constructorParams = { qLikes };

        return EssentialQuery.<LikesListDto, Likes>builder()
                .entityManager(entityManager).queryType(qLikes)
                .classOfListDto(LikesListDto.class).constructorParams(constructorParams).build();
    }

    private <T> JPAQuery<LikesListDto> createQueryForFindLikes(EssentialQuery<LikesListDto, Likes> essentialQuery,
                                                           User user, Pageable pageable,
                                                           EntityPathBase<T> contentTypeOfLikes) {
        FindQueries<LikesListDto, Likes> findQueries = FindQueries.<LikesListDto, Likes>builder()
                .essentialQuery(essentialQuery).userId(qLikes.user.id).user(user)
                .contentTypeOfLikes(contentTypeOfLikes).build();
        JPAQuery<LikesListDto> query = findQueries.createQueryForFindContents();

        return addPageToQuery(query, pageable);
    }

    private Page<LikesListDto> createLikesPage(JPAQuery<LikesListDto> query,
                                               EssentialQuery<LikesListDto, Likes> essentialQuery,
                                               Pageable pageable) {
        List<LikesListDto> likes = query.fetch();
        JPAQuery<Long> totalQuery = essentialQuery.createBaseQueryForPagination(query);
        return createPage(likes, pageable, totalQuery);
    }


    @Override
    public Page<LikesListDto> findPostLikesByUser(User user, Pageable pageable) {
        return findLikesByFields(user, pageable, qLikes.posts);
    }


    @Override
    public Page<LikesListDto> findCommentLikesByUser(User user, Pageable pageable) {
        return findLikesByFields(user, pageable, qLikes.comments);
    }

    @Override
    @Transactional
    public void deleteByPosts(Posts post, User user) {
        deleteByField(qLikes.posts, post, user);
    }

    private <T> void deleteByField(EntityPathBase<T> qField, T field, User user) {
        JPADeleteClause query = new JPADeleteClause(entityManager, qLikes);
        query.where(qField.eq(field).and(qLikes.user.eq(user))).execute();
    }

    @Override
    @Transactional
    public void deleteByComments(Comments comment, User user) {
        deleteByField(qLikes.comments, comment, user);
    }
}
