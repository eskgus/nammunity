package com.eskgus.nammunity.domain.user;

import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.time.LocalDateTime;

public class BannedUsersRepositoryImpl extends QuerydslRepositorySupport implements CustomBannedUsersRepository {
    @Autowired
    private EntityManager entityManager;

    public BannedUsersRepositoryImpl() {
        super(BannedUsers.class);
    }

    @Override
    public void updateExpiredDate(User user, LocalDateTime expiredDate) {
        QBannedUsers bannedUser = QBannedUsers.bannedUsers;

        JPAUpdateClause query = new JPAUpdateClause(entityManager, bannedUser);
        query.set(bannedUser.expiredDate, expiredDate).where(bannedUser.user.eq(user)).execute();
    }
}
