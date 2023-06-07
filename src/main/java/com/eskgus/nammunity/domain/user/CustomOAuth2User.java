package com.eskgus.nammunity.domain.user;

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CustomOAuth2User{
    private Map<String, Object> attributes = new HashMap<>();
    private String name;
    private String email;

    @Builder
    public CustomOAuth2User(Map<String, Object> attributes, String name, String email) {
        this.attributes.putAll(attributes);
        this.name = name;
        this.email = email;
    }

    public static CustomOAuth2User of(String registrationId, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver(attributes);
        } else if ("kakao".equals(registrationId)) {
            return ofKakao(attributes);
        }
        return ofGoogle(attributes);
    }

    public static CustomOAuth2User ofGoogle(Map<String, Object> attributes) {
        return CustomOAuth2User.builder()
                .attributes(attributes)
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .build();
    }

    public static CustomOAuth2User ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return CustomOAuth2User.builder()
                .attributes(response)
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .build();
    }

    public static CustomOAuth2User ofKakao(Map<String, Object> attributes) {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        return CustomOAuth2User.builder()
                .attributes(attributes)
                .name((String) properties.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .build();
    }
}
