package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.converter.ContentReportsConverterForTest;
import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.common.BaseVisitor;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

@RequiredArgsConstructor
public class ReportsTestVisitor extends BaseVisitor {
    private final ContentReportsConverterForTest reportsConverter;
    private Predicate<ContentReports> filter;

    public Predicate<ContentReports> getFilter() {
        return filter;
    }

    @Override
    public void visit(Posts post) {
        this.filter = report -> reportsConverter.extractPostId(report).equals(post.getId());
    }

    @Override
    public void visit(Comments comment) {
        this.filter = report -> reportsConverter.extractCommentId(report).equals(comment.getId());
    }

    @Override
    public void visit(User user) {
        this.filter = report -> reportsConverter.extractUserId(report).equals(user.getId());
    }
}
