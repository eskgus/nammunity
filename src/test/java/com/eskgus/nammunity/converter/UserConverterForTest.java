package com.eskgus.nammunity.converter;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.user.UsersListDto;

public class UserConverterForTest implements EntityConverterForTest<UsersListDto, User> {
    @Override
    public Long extractEntityId(User entity) {
        return entity.getId();
    }

    @Override
    public Long extractUserId(User entity) {
        return entity.getId();
    }

    @Override
    public Long extractDtoId(UsersListDto dto) {
        return dto.getId();
    }

    @Override
    public UsersListDto generateDto(User entity) {
        return new UsersListDto(entity);
    }
}
