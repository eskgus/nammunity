package com.eskgus.nammunity.domain.common;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;

public abstract class BaseVisitor implements Visitor {
    @Override
    public void visit(Posts post) {}

    @Override
    public void visit(Comments comment) {}

    @Override
    public void visit(User user) {}
}
