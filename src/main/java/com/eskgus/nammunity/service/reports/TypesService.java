package com.eskgus.nammunity.service.reports;

import com.eskgus.nammunity.domain.comments.Comments;
import com.eskgus.nammunity.domain.posts.Posts;
import com.eskgus.nammunity.domain.reports.Types;
import com.eskgus.nammunity.domain.reports.TypesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class TypesService {
    private final TypesRepository typesRepository;

    @Transactional(readOnly = true)
    public <T> Types findByClass(Class<T> classOfType) {
        if (classOfType.equals(Posts.class)) {
            return findByDetail("게시글");
        } else if (classOfType.equals(Comments.class)) {
            return findByDetail("댓글");
        }
        return findByDetail("사용자");
    }

    @Transactional(readOnly = true)
    private Types findByDetail(String detail) {
        return typesRepository.findByDetail(detail).orElseThrow(() -> new
                IllegalArgumentException("해당 분류가 없습니다."));
    }
}
