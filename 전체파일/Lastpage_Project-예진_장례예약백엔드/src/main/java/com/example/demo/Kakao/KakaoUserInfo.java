package com.example.demo.Kakao;

import lombok.Getter;

import java.util.Map;

@Getter
public class KakaoUserInfo {
    private Map<String, Object> attributes;
    private Map<String, Object> kakaoAccount;
    private Map<String, Object> profile;

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        this.profile = (Map<String, Object>) kakaoAccount.get("profile");
    }

    public String getId() {
        return attributes.get("id").toString();
    }

    public String getEmail() {
        return kakaoAccount.get("email").toString();
    }

    public String getName() {
        return profile.get("nickname").toString();
    }
}
