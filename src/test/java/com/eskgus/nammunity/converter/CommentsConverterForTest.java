package com.eskgus.nammunity.converter;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import com.eskgus.nammunity.web.dto.comments.CommentsReadDto;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class CommentsConverterForTest<Dto> implements EntityConverterForTest<Dto, Comments> {
    private Class<Dto> dtoType;

    @Override
    public Long extractEntityId(Comments entity) {
        return entity.getId();
    }

    @Override
    public Long extractUserId(Comments entity) {
        return entity.getUser().getId();
    }

    @Override
    public Long extractDtoId(Dto dto) {
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
    public Dto generateDto(Comments entity) {
        if (dtoType.equals(CommentsListDto.class)) {
            return (Dto) new CommentsListDto(entity);
        } else {
            return (Dto) new CommentsReadDto(entity);
        }
    }
}
