package com.example.demo.Domain.Common.Service;

import com.example.demo.Domain.Common.Dto.SignupDto;
import com.example.demo.Domain.Common.Entity.Signup;
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

    private final String kakao_client_id = "e5923ec597bc30b05ac13d8e57e0d18a";
    private final String kakao_client_secret = "AiGd2ZsH9J5mMhv4eLqc3yydHPLPtwoA";
    private final String naver_client_id = "JpAxufwm7yy8tFcT2Rmz";
    private final String naver_client_secret = "kyqlYdOKZd";
   







    // ============================================================
    //  카카오 로그인
    // ============================================================
    public SignupDto loginWithKakao(String code) {

        // 1) Access Token
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakao_client_id);
        params.add("client_secret", kakao_client_secret);
        params.add("redirect_uri", "http://localhost:8090/login/oauth2/code/kakao");
        params.add("code", code);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, params, Map.class);
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2) 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> userResponse =
                restTemplate.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.GET, entity, Map.class);

        Map<String, Object> kakaoAccount = (Map<String, Object>) userResponse.getBody().get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        // 3) DTO 생성
        SignupDto dto = SignupDto.builder()
                .provider("kakao")
                .providerId(String.valueOf(userResponse.getBody().get("id")))
                .name((String) profile.get("nickname"))
                .oauthEmail((String) kakaoAccount.get("email"))
                .profileImage((String) profile.get("profile_image_url"))
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .build();

        // 4) 기존 사용자 조회
        Signup existing = signupService.findByProviderAndProviderId("kakao", dto.getProviderId());

        // 5) 신규 저장 또는 기존 DTO 변환
        SignupDto result = (existing == null)
                ? signupService.saveUserInfo(dto)
                : SignupDto.fromEntity(existing);

        log.info("1. 카카오 로그인 완료 userSeq={}", result.getUserSeq());
        return result;
    }



    // ============================================================
    // 네이버 로그인
    // ============================================================
    public SignupDto loginWithNaver(String code, String state) {

        String tokenUrl = "https://nid.naver.com/oauth2.0/token";

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

        ResponseEntity<Map> userResponse =
                restTemplate.exchange("https://openapi.naver.com/v1/nid/me",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class);

        Map<String, Object> body = (Map<String, Object>) userResponse.getBody().get("response");

        SignupDto dto = SignupDto.builder()
                .provider("naver")
                .providerId((String) body.get("id"))
                .name((String) body.get("name"))
                .oauthEmail((String) body.get("email"))
                .profileImage((String) body.get("profile_image"))
                .created_at(LocalDateTime.now())
                .updated_at(LocalDateTime.now())
                .build();

        Signup existing = signupService.findByProviderAndProviderId("naver", dto.getProviderId());

        SignupDto result = (existing == null)
                ? signupService.saveUserInfo(dto)
                : SignupDto.fromEntity(existing);

        log.info("2. 네이버 로그인 완료 userSeq={}", result.getUserSeq());
        return result;
    }



    // ============================================================
    //  구글 로그인
    // ============================================================
    public SignupDto loginWithGoogle(String code) {

        String tokenUrl = "https://oauth2.googleapis.com/token";

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

        ResponseEntity<Map> userResponse =
                restTemplate.exchange("https://www.googleapis.com/oauth2/v2/userinfo",
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        Map.class);

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

        SignupDto result = (existing == null)
                ? signupService.saveUserInfo(dto)
                : SignupDto.fromEntity(existing);

        log.info("3. 구글 로그인 완료 userSeq={}", result.getUserSeq());
        return result;
    }
}
