package com.eskgus.nammunity.domain.user;

import com.eskgus.nammunity.helper.EssentialQuery;
import com.eskgus.nammunity.helper.SearchQueries;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

public class UserRepositoryImpl extends QuerydslRepositorySupport implements CustomUserRepository {
    @Autowired
    private EntityManager entityManager;

    private final QUser qUser = QUser.user;

    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public List<UsersListDto> searchByNickname(String keywords) {
        return searchUsersByFields(keywords, qUser.nickname);
    }

    private List<UsersListDto> searchUsersByFields(String keywords, StringPath... fields) {
        EssentialQuery<UsersListDto, User> essentialQuery = createEssentialQueryForUsers();
        SearchQueries<UsersListDto, User> searchQueries = SearchQueries.<UsersListDto, User>builder()
                .essentialQuery(essentialQuery)
                .keywords(keywords).fields(fields).build();
        JPAQuery<UsersListDto> query = searchQueries.createQueryForSearchContents();
        return query.fetch();
    }

    private EssentialQuery<UsersListDto, User> createEssentialQueryForUsers() {
        Expression[] constructorParams = { qUser };

        return EssentialQuery.<UsersListDto, User>builder()
                .entityManager(entityManager).queryType(qUser)
                .classOfListDto(UsersListDto.class).constructorParams(constructorParams).build();
    }
}
