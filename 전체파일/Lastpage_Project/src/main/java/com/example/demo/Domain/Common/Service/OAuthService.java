package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final SignupService signupService;
    private String kakao_client_id="e5923ec597bc30b05ac13d8e57e0d18a";
    private String kakao_client_secret="AiGd2ZsH9J5mMhv4eLqc3yydHPLPtwoA";
    private String naver_client_id="JpAxufwm7yy8tFcT2Rmz";
    private String naver_client_secret="kyqlYdOKZd";
   





    /**  카카오 로그인 */
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

        // 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userResponse =
                restTemplate.exchange(userUrl, HttpMethod.GET, entity, Map.class);
        Map<String, Object> kakaoAccount = (Map<String, Object>) userResponse.getBody().get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        SignupDto dto = SignupDto.builder()
                .provider("kakao")
                .providerId(String.valueOf(userResponse.getBody().get("id")))
                .name((String) profile.get("nickname"))
                .oauthEmail((String) kakaoAccount.get("email"))
                .profileImage((String) profile.get("profile_image_url"))
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .build();

        // DB 저장
        Signup existing = signupService.findByProviderAndProviderId("kakao", dto.getProviderId());
        Signup saved = (existing == null)
                ? signupService.saveUserInfo(dto)
                : existing;

        log.info("1.카카오 로그인 DB 저장 완료: {}", saved.getUser_seq());
        return dto;

    }

    /** 네이버 로그인 */
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

        SignupDto dto = SignupDto.builder()
                .provider("naver")
                .providerId((String) response.get("id"))
                .name((String) response.get("name"))
                .oauthEmail((String) response.get("email"))
                .profileImage((String) response.get("profile_image"))
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .build();

        Signup existing = signupService.findByProviderAndProviderId("naver", dto.getProviderId());
        Signup saved = (existing == null)
                ? signupService.saveUserInfo(dto)
                : existing;

        log.info(" 2.네이버 로그인 DB 저장 완료: {}", saved.getUser_seq());
        return dto;

    }

    /** 구글 로그인 */
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

        SignupDto dto = SignupDto.builder()
                .provider("google")
                .providerId((String) body.get("id"))
                .name((String) body.get("name"))
                .oauthEmail((String) body.get("email"))
                .profileImage((String) body.get("picture"))
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .build();

        Signup existing = signupService.findByProviderAndProviderId("google", dto.getProviderId());
        Signup saved = (existing == null)
                ? signupService.saveUserInfo(dto)
                : existing;

        log.info("3.구글 로그인 DB 저장 완료: {}", saved.getUser_seq());
        return dto;
    }
}

