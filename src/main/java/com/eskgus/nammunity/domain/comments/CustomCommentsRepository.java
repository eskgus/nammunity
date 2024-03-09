package com.eskgus.nammunity.domain.comments;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.web.dto.comments.CommentsListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomCommentsRepository {
    List<CommentsListDto> searchByContent(String keywords);
    Page<CommentsListDto> findByUser(User user, Pageable pageable);
}
