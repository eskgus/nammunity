package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.domain.comments.QComments;
import com.eskgus.nammunity.domain.likes.QLikes;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.EssentialQuery;
import com.eskgus.nammunity.helper.FindQueries;
import com.eskgus.nammunity.helper.SearchQueries;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class PostsRepositoryImpl extends QuerydslRepositorySupport implements CustomPostsRepository {
    @Autowired
    private EntityManager entityManager;

    private static final QPosts Q_POSTS = QPosts.posts;
    private static final QComments Q_COMMENTS = QComments.comments;
    private static final QLikes Q_LIKES = QLikes.likes;

    public PostsRepositoryImpl() {
        super(Posts.class);
    }

    @Override
    public Page<PostsListDto> searchByTitle(String keywords, Pageable pageable) {
        return searchPostsByFields(pageable, keywords, Q_POSTS.title);
    }

    @Override
    public Page<PostsListDto> searchByContent(String keywords, Pageable pageable) {
        return searchPostsByFields(pageable, keywords, Q_POSTS.content);
    }

    @Override
    public Page<PostsListDto> searchByTitleAndContent(String keywords, Pageable pageable) {
        return searchPostsByFields(pageable, keywords, Q_POSTS.title, Q_POSTS.content);
    }

    @Override
    public Page<PostsListDto> findAllDesc(Pageable pageable) {
        return findPostsByFields(null, pageable);
    }

    @Override
    public Page<PostsListDto> findByUser(User user, Pageable pageable) {
        return findPostsByFields(user, pageable);
    }

    private Page<PostsListDto> searchPostsByFields(Pageable pageable, String keywords, StringPath... fields) {
        EssentialQuery<PostsListDto, Posts> essentialQuery = createEssentialQueryForPosts();
        JPAQuery<PostsListDto> query = createQueryForSearchPosts(pageable, essentialQuery, keywords, fields);

        return createPostsPage(query, essentialQuery, pageable);
    }

    private Page<PostsListDto> findPostsByFields(User user, Pageable pageable) {
        EssentialQuery<PostsListDto, Posts> essentialQuery = createEssentialQueryForPosts();
        JPAQuery<PostsListDto> query = createQueryForFindPosts(essentialQuery, user, pageable);

        return createPostsPage(query, essentialQuery, pageable);
    }

    private EssentialQuery<PostsListDto, Posts> createEssentialQueryForPosts() {
        Expression[] constructorParams = {Q_POSTS, Q_COMMENTS.id.countDistinct(), Q_LIKES.id.countDistinct() };

        return EssentialQuery.<PostsListDto, Posts>builder()
                .entityManager(entityManager).queryType(Q_POSTS)
                .dtoType(PostsListDto.class).constructorParams(constructorParams).build();
    }

    private JPAQuery<PostsListDto> createQueryForSearchPosts(Pageable pageable,
                                                             EssentialQuery<PostsListDto, Posts> essentialQuery,
                                                             String keywords, StringPath... fields) {
        SearchQueries<PostsListDto, Posts> searchQueries = SearchQueries.<PostsListDto, Posts>builder()
                .essentialQuery(essentialQuery).keywords(keywords).fields(fields).build();
        JPAQuery<PostsListDto> query = searchQueries.createQueryForSearchContents();

        return addPageToQuery(query, pageable);
    }

    private JPAQuery<PostsListDto> createQueryForFindPosts(EssentialQuery<PostsListDto, Posts> essentialQuery,
                                                           User user, Pageable pageable) {
        FindQueries<PostsListDto, Posts> findQueries = FindQueries.<PostsListDto, Posts>builder()
                .essentialQuery(essentialQuery).userId(Q_POSTS.user.id).user(user).build();
        JPAQuery<PostsListDto> query = findQueries.createQueryForFindContents();

        return addPageToQuery(query, pageable);
    }

    private Page<PostsListDto> createPostsPage(JPAQuery<PostsListDto> query,
                                               EssentialQuery<PostsListDto, Posts> essentialQuery,
                                               Pageable pageable) {
        List<PostsListDto> posts = createLeftJoinClauseForPosts(query).fetch();
        JPAQuery<Long> totalQuery = essentialQuery.createBaseQueryForPagination(query);

        return PaginationRepoUtil.createPage(posts, pageable, totalQuery);
    }

    private JPAQuery<PostsListDto> addPageToQuery(JPAQuery<PostsListDto> query, Pageable pageable) {
        return PaginationRepoUtil.addPageToQuery(query, pageable);
    }

    private JPAQuery<PostsListDto> createLeftJoinClauseForPosts(JPAQuery<PostsListDto> query) {
        return query.leftJoin(Q_POSTS.comments, Q_COMMENTS)
                .leftJoin(Q_POSTS.likes, Q_LIKES);
    }
}
