package com.eskgus.nammunity.domain.comments;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.eskgus.nammunity.util.KeywordUtil.searchByField;

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
}
