package com.eskgus.nammunity.domain.comments;

import java.util.List;

public interface CustomCommentsRepository {
    List<Comments> searchByContent(String keywords);
}
