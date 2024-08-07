package com.eskgus.nammunity.domain.common;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.Reasons;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.user.User;

public interface Visitor {
    void visit(Posts post);
    void visit(Comments comment);
    void visit(User user);
    void visit(Reasons reason);
    void visit(Types type);
}
