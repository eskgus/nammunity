package com.eskgus.nammunity.domain.user;

import com.eskgus.nammunity.helper.EssentialQuery;
import com.eskgus.nammunity.helper.SearchQueries;
import com.eskgus.nammunity.util.PaginationRepoUtil;
import com.eskgus.nammunity.web.dto.user.UsersListDto;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<UsersListDto> searchByNickname(String keywords, Pageable pageable) {
        return searchUsersByFields(pageable, keywords, qUser.nickname);
    }

    private Page<UsersListDto> searchUsersByFields(Pageable pageable, String keywords, StringPath... fields) {
        EssentialQuery<UsersListDto, User> essentialQuery = createEssentialQueryForUsers();
        JPAQuery<UsersListDto> query = createQueryForSearchUsers(pageable, essentialQuery, keywords, fields);
        return createUsersPage(query, essentialQuery, pageable);
    }

    private EssentialQuery<UsersListDto, User> createEssentialQueryForUsers() {
        Expression[] constructorParams = { qUser };

        return EssentialQuery.<UsersListDto, User>builder()
                .entityManager(entityManager).queryType(qUser)
                .dtoType(UsersListDto.class).constructorParams(constructorParams).build();
    }

    private JPAQuery<UsersListDto> createQueryForSearchUsers(Pageable pageable,
                                                             EssentialQuery<UsersListDto, User> essentialQuery,
                                                             String keywords, StringPath... fields) {
        SearchQueries<UsersListDto, User> searchQueries = SearchQueries.<UsersListDto, User>builder()
                .essentialQuery(essentialQuery).keywords(keywords).fields(fields).build();
        JPAQuery<UsersListDto> query = searchQueries.createQueryForSearchContents();
        return PaginationRepoUtil.addPageToQuery(query, pageable);
    }

    private Page<UsersListDto> createUsersPage(JPAQuery<UsersListDto> query,
                                               EssentialQuery<UsersListDto, User> essentialQuery,
                                               Pageable pageable) {
        List<UsersListDto> users = query.fetch();
        JPAQuery<Long> totalQuery = essentialQuery.createBaseQueryForPagination(query);
        return PaginationRepoUtil.createPage(users, pageable, totalQuery);
    }
}
