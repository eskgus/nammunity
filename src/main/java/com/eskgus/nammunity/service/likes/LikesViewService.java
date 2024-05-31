package com.eskgus.nammunity.service.likes;

import com.eskgus.nammunity.domain.user.User;
import com.eskgus.nammunity.helper.PrincipalHelper;
import com.eskgus.nammunity.web.dto.likes.LikesListDto;
import com.eskgus.nammunity.web.dto.pagination.ContentsPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.function.BiFunction;

@RequiredArgsConstructor
@Service
public class LikesViewService {
    private final LikesService likesService;

    @Autowired
    private PrincipalHelper principalHelper;

    @Transactional(readOnly = true)
    public ContentsPageDto<LikesListDto> listLikes(BiFunction<User, Pageable, Page<LikesListDto>> finder,
                                                   Principal principal, int page) {
        User user = principalHelper.getUserFromPrincipal(principal, true);
        Page<LikesListDto> contents = likesService.findLikesByUser(user, finder, page, 20);
        return new ContentsPageDto<>(contents);
    }
}
