package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomCommentsRepository {
    Page<CommentsListDto> searchByContent(String keywords, Pageable pageable);
    Page<CommentsListDto> findByUser(User user, Pageable pageable);
}
