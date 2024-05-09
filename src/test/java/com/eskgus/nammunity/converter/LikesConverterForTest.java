package com.eskgus.nammunity.converter;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.likes.Likes;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;

public class LikesConverterForTest implements EntityConverterForTest<LikesListDto, Likes> {
    @Override
    public Long extractEntityId(Likes entity) {
        return entity.getId();
    }

    @Override
    public Long extractUserId(Likes entity) {
        return entity.getUser().getId();
    }

    @Override
    public Long extractDtoId(LikesListDto dto) {
        return dto.getLikesId();
    }

    @Override
    public LikesListDto generateDto(Likes entity) {
        return new LikesListDto(entity);
    }

    public Posts getPosts(Likes entity) {
        return entity.getPosts();
    }

    public Comments getComments(Likes entity) {
        return entity.getComments();
    }
}
