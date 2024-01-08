package com.eskgus.nammunity.domain.posts;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.util.KeywordUtil;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.eskgus.nammunity.util.KeywordUtil.searchByField;
import static com.eskgus.nammunity.util.PaginationRepoUtil.*;

public class PostsRepositoryImpl extends QuerydslRepositorySupport implements CustomPostsRepository {
    @Autowired
    private EntityManager entityManager;

    public PostsRepositoryImpl() {
        super(Posts.class);
    }

    @Override
    public List<Posts> searchByTitle(String keywords) {
        QPosts post = QPosts.posts;
        return searchByField(entityManager, post, post.title, keywords);
    }

    @Override
    public List<Posts> searchByContent(String keywords) {
        QPosts post = QPosts.posts;
        return searchByField(entityManager, post, post.content, keywords);
    }

    @Override
    public List<Posts> searchByTitleAndContent(String keywords) {
        // keywords를 includeKeywordList(검색어)와 excludeKeywordList(검색 제외 단어)로 분리
        KeywordUtil.KeywordLists keywordLists = KeywordUtil.extractKeywords(keywords);
        List<String> includeKeywordList = keywordLists.getIncludeKeywordList();
        List<String> excludeKeywordList = keywordLists.getExcludeKeywordList();

        // main query에 사용할 거
        QPosts post = QPosts.posts;
        BooleanBuilder builder = new BooleanBuilder();
        // subquery에 사용할 거
        BooleanBuilder builderSub = new BooleanBuilder();

        StringExpression title = post.title.toLowerCase();
        StringExpression content = post.content.toLowerCase();

        // main query 생성: title이나 content에 includeKeywordList 중 하나라도 포함돼있으면 추출
        for (String includeKeyword : includeKeywordList) {
            builder.or(title.contains(includeKeyword).or(content.contains(includeKeyword)));
        }

        // subquery 생성: title이나 content에 excludeKeywordList 중 하나라도 포함돼있으면 추출 x
        for (String excludeKeyword : excludeKeywordList) {
            builderSub.and(title.contains(excludeKeyword).not().and(content.contains(excludeKeyword).not()));
        }

        // 쿼리 생성 + 실행
        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.selectDistinct(post).from(post).where(builder.and(post.in(
                JPAExpressions.selectFrom(post).where(builderSub)))).orderBy(post.id.desc()).fetch();
    }

    @Override
    public Page<PostsListDto> findAllDesc(Pageable pageable) {
        QPosts post = QPosts.posts;

        QueryParams<Posts> queryParams = QueryParams.<Posts>builder()
                .entityManager(entityManager).queryType(post).pageable(pageable).build();
        List<PostsListDto> posts = createBaseQueryForPagination(queryParams, PostsListDto.class).fetch();
        return createPage(queryParams, posts);
    }

    @Override
    public Page<PostsListDto> findByUser(User user, Pageable pageable) {
        QPosts post = QPosts.posts;

        BooleanBuilder whereCondition = createWhereConditionForPagination(post.user.id, user, null);
        QueryParams<Posts> queryParams = QueryParams.<Posts>builder()
                .entityManager(entityManager).queryType(post).pageable(pageable).whereCondition(whereCondition).build();
        List<PostsListDto> posts = createBaseQueryForPagination(queryParams, PostsListDto.class).fetch();
        return createPage(queryParams, posts);
    }
}
