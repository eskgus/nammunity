package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.common.BaseVisitor;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ReportSummaryVisitor extends BaseVisitor {
    private final QContentReportSummary qReportSummary;

    @Getter
    private Predicate whereCondition;

    @Override
    public void visit(Posts post) {
        this.whereCondition = qReportSummary.posts.eq(post);
    }

    @Override
    public void visit(Comments comment) {
        this.whereCondition = qReportSummary.comments.eq(comment);
    }

    @Override
    public void visit(User user) {
        this.whereCondition = qReportSummary.user.eq(user);
    }

    @Override
    public void visit(Types type) {
        this.whereCondition = qReportSummary.types.eq(type);
    }
}
