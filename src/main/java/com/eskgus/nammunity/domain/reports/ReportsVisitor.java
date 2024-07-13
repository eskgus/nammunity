package com.eskgus.nammunity.domain.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.common.BaseVisitor;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.user.User;
import com.querydsl.core.types.Predicate;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.function.Supplier;

@NoArgsConstructor
public class ReportsVisitor extends BaseVisitor {
    private QContentReports qReports;

    @Getter
    private Predicate whereCondition;

    @Getter
    private boolean isUser;

    public ReportsVisitor(QContentReports qReports) {
        this.qReports = qReports;
    }

    @Override
    public void visit(Posts post) {
        setWhereConditionOrIsUser(() -> qReports.posts.eq(post), false);
    }

    @Override
    public void visit(Comments comment) {
        setWhereConditionOrIsUser(() -> qReports.comments.eq(comment), false);
    }

    @Override
    public void visit(User user) {
        setWhereConditionOrIsUser(() -> qReports.user.eq(user), true);
    }

    @Override
    public void visit(Reasons reason) {
        setWhereConditionOrIsUser(() -> qReports.reasons.eq(reason), false);
    }

    private void setWhereConditionOrIsUser(Supplier<Predicate> predicateSupplier, boolean isUser) {
        if (qReports != null) {
            this.whereCondition = predicateSupplier.get();
        } else {
            this.isUser = isUser;
        }
    }
}
