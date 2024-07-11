package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.common.BaseVisitor;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.EssentialQuery;
import com.eskgus.nammunity.helper.FindQueries;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommentsVisitor<Dto> extends BaseVisitor {
    private final QComments qComments;
    private final EssentialQuery<Dto, Comments> essentialQuery;
    private FindQueries<Dto, Comments> findQueries;

    public FindQueries<Dto, Comments> getFindQueries() {
        return findQueries;
    }

    @Override
    public void visit(Posts post) {
        BooleanBuilder whereCondition = new BooleanBuilder().and(qComments.posts.eq(post));
        this.findQueries = FindQueries.<Dto, Comments>builder()
                .essentialQuery(essentialQuery).whereCondition(whereCondition).build();
    }

    @Override
    public void visit(User user) {
        this.findQueries = FindQueries.<Dto, Comments>builder()
                .essentialQuery(essentialQuery).userId(qComments.user.id).user(user).build();
    }
}
