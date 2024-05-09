package com.eskgus.nammunity.converter;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;

public class CommentsConverterForTest<T> implements EntityConverterForTest<T, Comments> {
    private final Class<T> classOfDto;

    public CommentsConverterForTest(Class<T> classOfDto) {
        this.classOfDto = classOfDto;
    }

    @Override
    public Long extractEntityId(Comments entity) {
        return entity.getId();
    }

    @Override
    public Long extractUserId(Comments entity) {
        return entity.getUser().getId();
    }

    @Override
    public Long extractDtoId(T dto) {
        if (dto instanceof CommentsListDto) {
            return ((CommentsListDto) dto).getCommentsId();
        } else {
            return ((CommentsReadDto) dto).getId();
        }
    }

    public Long extractPostId(Comments entity) {
        return entity.getPosts().getId();
    }

    @Override
    public T generateDto(Comments entity) {
        if (classOfDto.equals(CommentsListDto.class)) {
            return (T) new CommentsListDto(entity);
        } else {
            return (T) new CommentsReadDto(entity);
        }
    }
}
