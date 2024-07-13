package com.eskgus.nammunity.domain.likes;

import com.eskgus.nammunity.converter.LikesConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.common.BaseVisitor;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public class LikesTestVisitor extends BaseVisitor {
    private final LikesConverterForTest likesConverter;

    @Getter
    private Predicate<Likes> filter;

    @Override
    public void visit(Posts post) {
        this.filter = like -> likesConverter.getPosts(like) != null;
    }

    @Override
    public void visit(Comments comment) {
        this.filter = like -> likesConverter.getComments(like) != null;
    }

    @Override
    public void visit(User user) {
        this.filter = like -> likesConverter.extractUserId(like).equals(user.getId());
    }
}
