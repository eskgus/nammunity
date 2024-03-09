package com.eskgus.nammunity.converter;

import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.web.dto.posts.PostsListDto;

public class PostsConverterForTest implements EntityConverterForTest<Posts, PostsListDto> {
    @Override
    public Long extractEntityId(Posts entity) {
        return entity.getId();
    }

    @Override
    public Long extractUserId(Posts entity) {
        return entity.getUser().getId();
    }

    @Override
    public Long extractListDtoId(PostsListDto listDto) {
        return listDto.getId();
    }

    @Override
    public PostsListDto generateListDto(Posts entity) {
        return new PostsListDto(entity);
    }
}
