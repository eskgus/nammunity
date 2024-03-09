package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.likes.QLikes;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.EssentialQuery;
import com.eskgus.nammunity.helper.FindQueries;
import com.eskgus.nammunity.helper.SearchQueries;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
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
    public List<CommentsListDto> searchByContent(String keywords) {
        return searchCommentsByFields(keywords, qComments.content);
    }

    private List<CommentsListDto> searchCommentsByFields(String keywords, StringPath... fields) {
        EssentialQuery<CommentsListDto, Comments> essentialQuery = createEssentialQueryForComments();
        SearchQueries<CommentsListDto, Comments> searchQueries = SearchQueries.<CommentsListDto, Comments>builder()
                .essentialQuery(essentialQuery)
                .keywords(keywords).fields(fields).build();
        JPAQuery<CommentsListDto> query = searchQueries.createQueryForSearchContents();
        return createLeftJoinClauseForComments(query).fetch();
    }

    private EssentialQuery<CommentsListDto, Comments> createEssentialQueryForComments() {
        Expression[] constructorParams = { qComments, qLikes.id.countDistinct() };

        return EssentialQuery.<CommentsListDto, Comments>builder()
                .entityManager(entityManager).queryType(qComments)
                .classOfListDto(CommentsListDto.class).constructorParams(constructorParams).build();
    }

    private JPAQuery<CommentsListDto> createLeftJoinClauseForComments(JPAQuery<CommentsListDto> query) {
        return query.leftJoin(qComments.likes, qLikes);
    }

    @Override
    public Page<CommentsListDto> findByUser(User user, Pageable pageable) {
        return findCommentsByFields(user, pageable);
    }

    private Page<CommentsListDto> findCommentsByFields(User user, Pageable pageable) {
        EssentialQuery<CommentsListDto, Comments> essentialQuery = createEssentialQueryForComments();
        JPAQuery<CommentsListDto> query = createQueryForFindComments(essentialQuery, user, pageable);
        return createCommentsPage(query, essentialQuery, pageable);
    }

    private JPAQuery<CommentsListDto> createQueryForFindComments(EssentialQuery<CommentsListDto, Comments> essentialQuery,
                                                                 User user, Pageable pageable) {
        FindQueries<CommentsListDto, Comments> findQueries = FindQueries.<CommentsListDto, Comments>builder()
                .essentialQuery(essentialQuery).userId(qComments.user.id).user(user).build();
        JPAQuery<CommentsListDto> query = findQueries.createQueryForFindContents();

        return addPageToQuery(query, pageable);
    }

    private Page<CommentsListDto> createCommentsPage(JPAQuery<CommentsListDto> query,
                                                     EssentialQuery<CommentsListDto, Comments> essentialQuery,
                                                     Pageable pageable) {
        List<CommentsListDto> comments = createLeftJoinClauseForComments(query).fetch();
        JPAQuery<Long> totalQuery = essentialQuery.createBaseQueryForPagination(query);
        return createPage(comments, pageable, totalQuery);
    }
}
