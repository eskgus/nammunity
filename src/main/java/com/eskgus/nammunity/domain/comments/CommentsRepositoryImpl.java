package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.querydsl.core.BooleanBuilder;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.eskgus.nammunity.util.KeywordUtil.searchByField;
import static com.eskgus.nammunity.util.PaginationRepoUtil.*;

public class CommentsRepositoryImpl extends QuerydslRepositorySupport implements CustomCommentsRepository {
    @Autowired
    private EntityManager entityManager;

    public CommentsRepositoryImpl() {
        super(Comments.class);
    }

    @Override
    public List<Comments> searchByContent(String keywords) {
        QComments comment = QComments.comments;
        return searchByField(entityManager, comment, comment.content, keywords);
    }

    @Override
    public Page<CommentsListDto> findByUser(User user, Pageable pageable) {
        QComments comment = QComments.comments;

        BooleanBuilder whereCondition = createWhereConditionForPagination(comment.user.id, user, null);
        QueryParams<Comments> queryParams = QueryParams.<Comments>builder()
                .entityManager(entityManager).queryType(comment).pageable(pageable).whereCondition(whereCondition).build();
        List<CommentsListDto> comments = createBaseQueryForPagination(queryParams, CommentsListDto.class).fetch();
        return createPage(queryParams, comments);
    }
}
