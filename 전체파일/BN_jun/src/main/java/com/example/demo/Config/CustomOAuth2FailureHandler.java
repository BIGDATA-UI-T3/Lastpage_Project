package com.example.demo.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

@Component
public class CustomOAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String errorMessage = "소셜 로그인 중 오류가 발생했습니다.";

        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2Error error = ((OAuth2AuthenticationException) exception).getError();
            if ("duplicate_email".equals(error.getErrorCode())) {
                errorMessage = error.getDescription();
            }
        }

        // /signin 페이지로 리다이렉트할 때, message 파라미터에 오류 메시지를 담아 보냅니다.
        String encodedMessage = URLEncoder.encode(errorMessage, "UTF-8");
        setDefaultFailureUrl("/signin?error=true&message=" + encodedMessage);

        super.onAuthenticationFailure(request, response, exception);
    }
}