package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.converter.CommentsConverterForTest;
import com.eskgus.nammunity.domain.common.BaseVisitor;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public class CommentsTestVisitor<Dto> extends BaseVisitor {
    private final CommentsConverterForTest<Dto> commentsConverter;

    @Getter
    private Predicate<Comments> filter;

    @Override
    public void visit(Posts post) {
        this.filter = comment -> commentsConverter.extractPostId(comment).equals(post.getId());
    }

    @Override
    public void visit(User user) {
        this.filter = comment -> commentsConverter.extractUserId(comment).equals(user.getId());
    }
}
