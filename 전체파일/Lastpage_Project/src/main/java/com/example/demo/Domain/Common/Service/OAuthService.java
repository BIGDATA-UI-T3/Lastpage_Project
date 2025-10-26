package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.SignupDto;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    /** ✅ 카카오 로그인 */
    public SignupDto loginWithKakao(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        String userUrl = "https://kapi.kakao.com/v2/user/me";

        // 1️⃣ Access Token 요청
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakao_client_id);
        params.add("client_secret", kakao_client_secret);
        params.add("redirect_uri", "http://localhost:8090/login/oauth2/code/kakao");
        params.add("code", code);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, params, Map.class);
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2️⃣ 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userResponse =
                restTemplate.exchange(userUrl, HttpMethod.GET, entity, Map.class);
        Map<String, Object> kakaoAccount = (Map<String, Object>) userResponse.getBody().get("kakao_account");

        return SignupDto.builder()
                .provider("kakao")
                .id(String.valueOf(userResponse.getBody().get("id")))
                .name((String) ((Map<String, Object>) kakaoAccount.get("profile")).get("nickname"))
                .oauthEmail((String) kakaoAccount.get("oauthEmail"))
                .profileImage((String) ((Map<String, Object>) kakaoAccount.get("profile")).get("profile_image_url"))
                .build();
    }

    /** ✅ 네이버 로그인 */
    public SignupDto loginWithNaver(String code, String state) {
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";
        String userUrl = "https://openapi.naver.com/v1/nid/me";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naver_client_id);
        params.add("client_secret", naver_client_secret);
        params.add("code", code);
        params.add("state", state);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, params, Map.class);
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userResponse = restTemplate.exchange(userUrl, HttpMethod.GET, entity, Map.class);
        Map<String, Object> response = (Map<String, Object>) userResponse.getBody().get("response");

        return SignupDto.builder()
                .provider("naver")
                .id((String) response.get("id"))
                .name((String) response.get("name"))
                .oauthEmail((String) response.get("oauthEmail"))
                .profileImage((String) response.get("profile_image"))
                .build();
    }

    /** ✅ 구글 로그인 */
    public SignupDto loginWithGoogle(String code) {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        String userUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", google_client_id);
        params.add("client_secret", google_client_secret);
        params.add("redirect_uri", "http://localhost:8090/login/oauth2/code/google");
        params.add("code", code);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, params, Map.class);
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userResponse = restTemplate.exchange(userUrl, HttpMethod.GET, entity, Map.class);
        Map<String, Object> body = userResponse.getBody();

        return SignupDto.builder()
                .provider("google")
                .id((String) body.get("id"))
                .name((String) body.get("name"))
                .oauthEmail((String) body.get("oauthEmail"))
                .profileImage((String) body.get("picture"))
                .build();
    }
}
