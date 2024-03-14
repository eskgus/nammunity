package com.eskgus.nammunity.domain.user;

import com.querydsl.jpa.impl.JPAUpdateClause;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public class BannedUsersRepositoryImpl extends QuerydslRepositorySupport implements CustomBannedUsersRepository {
    @Autowired
    private EntityManager entityManager;

    public BannedUsersRepositoryImpl() {
        super(BannedUsers.class);
    }

    @Override
    @Transactional
    public void updateExpiredDate(Long id, LocalDateTime expiredDate) {
        QBannedUsers qBannedUser = QBannedUsers.bannedUsers;

        JPAUpdateClause query = new JPAUpdateClause(entityManager, qBannedUser);
        query.set(qBannedUser.expiredDate, expiredDate).where(qBannedUser.id.eq(id)).execute();
    }
}
