package com.eskgus.nammunity.domain.user;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static com.eskgus.nammunity.util.KeywordUtil.searchByField;

public class UserRepositoryImpl extends QuerydslRepositorySupport implements CustomUserRepository {
    @Autowired
    private EntityManager entityManager;

    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public List<User> searchByNickname(String keywords) {
        QUser user = QUser.user;
        return searchByField(entityManager, user, user.nickname, keywords);
    }
}
