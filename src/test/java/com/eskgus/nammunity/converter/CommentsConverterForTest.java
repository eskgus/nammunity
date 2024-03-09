package com.eskgus.nammunity.converter;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;

public class CommentsConverterForTest implements EntityConverterForTest<Comments, CommentsListDto> {
    @Override
    public Long extractEntityId(Comments entity) {
        return entity.getId();
    }

    @Override
    public Long extractUserId(Comments entity) {
        return entity.getUser().getId();
    }

    @Override
    public Long extractListDtoId(CommentsListDto listDto) {
        return listDto.getCommentsId();
    }

    @Override
    public CommentsListDto generateListDto(Comments entity) {
        return new CommentsListDto(entity);
    }
}
