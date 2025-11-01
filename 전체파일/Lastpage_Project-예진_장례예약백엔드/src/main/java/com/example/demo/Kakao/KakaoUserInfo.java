package com.example.demo.Kakao;

import lombok.Getter;

import java.util.Map;

@Getter
public class KakaoUserInfo {
    private final Map<String, Object> attributes; // 전체 JSON

    public KakaoUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    // 카카오 JSON 구조 기준으로 데이터 꺼내기
    public String getId() {
        Object id = attributes.get("id");
        return id != null ? id.toString() : null;
    }

    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            Object email = kakaoAccount.get("email");
            return email != null ? email.toString() : null;
        }
        return null;
    }

    public String getName() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                Object nickname = profile.get("nickname");
                return nickname != null ? nickname.toString() : null;
            }
        }
        return null;
    }
}
