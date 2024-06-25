package com.eskgus.nammunity.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SocialType {
    GOOGLE("google"),
    NAVER("naver"),
    KAKAO("kakao"),
    NONE("none");

    private final String key;

    private static final Map<String, SocialType> SOCIAL_MAP = new HashMap<>();

    static {
        for (SocialType socialType : SocialType.values()) {
            SOCIAL_MAP.put(socialType.getKey(), socialType);
        }
    }

    public static SocialType convertSocialType(String social) {
        return SOCIAL_MAP.getOrDefault(social, NONE);
    }
}
