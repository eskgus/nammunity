package com.eskgus.nammunity.converter;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;

public class CommentsConverterForTest<V> implements EntityConverterForTest<Comments, V> {
    private final Class<V> classOfDto;

    public CommentsConverterForTest(Class<V> classOfDto) {
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
    public Long extractDtoId(V dto) {
        if (dto instanceof CommentsListDto) {
            return ((CommentsListDto) dto).getCommentsId();
        } else {
            return ((CommentsReadDto) dto).getId();
        }
    }

    @Override
    public V generateDto(Comments entity) {
        if (classOfDto.equals(CommentsListDto.class)) {
            return (V) new CommentsListDto(entity);
        } else {
            return (V) new CommentsReadDto(entity);
        }
    }
}
