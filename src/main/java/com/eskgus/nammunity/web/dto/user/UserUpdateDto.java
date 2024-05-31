package com.eskgus.nammunity.web.dto.user;

import com.eskgus.nammunity.domain.user.User;
import lombok.Getter;

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

    private void generateSocial(String social) {
        if (!social.equals("none")) {
            switch (social) {
                case "google" -> this.google = true;
                case "naver" -> this.naver = true;
                default -> this.kakao = true;
            }
            this.none = false;
        }
    }
}
