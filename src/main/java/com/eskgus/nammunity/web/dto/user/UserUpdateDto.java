package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.domain.enums.SocialType;
import com.eskgus.nammunity.domain.user.User;
import lombok.Getter;

import static com.eskgus.nammunity.domain.enums.SocialType.NONE;

@Getter
public class UserUpdateDto {
    private final Long id;
    private final String username;
    private final String nickname;
    private final String email;
    private boolean google = false;
    private boolean naver = false;
    private boolean kakao = false;
    private boolean none = true;

    public UserUpdateDto(User user){
        this.id = user.getId();
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        generateSocial(user.getSocial());
    }

    private void generateSocial(SocialType socialType) {
        if (!NONE.equals(socialType)) {
            switch (socialType) {
                case GOOGLE -> this.google = true;
                case NAVER -> this.naver = true;
                case KAKAO -> this.kakao = true;
            }
            this.none = false;
        }
    }
}
