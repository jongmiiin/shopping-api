package com.skala.shopapi.common;

import com.skala.shopapi.exception.Error;
import com.skala.shopapi.exception.ResponseException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.stereotype.Component;

/**
 * 2순위 임시 구현: 쿠키에 customerId를 평문(Base64) 형태로 저장한다.
 * 4순위(JWT)에서 내부 구현만 서명된 JWT로 교체되며, 이 클래스의 메서드 시그니처는 유지된다.
 */
@Component
public class SessionHandler {

    public static final String COOKIE_NAME = "bff-access";

    public void storeAccessToken(HttpServletResponse response, String customerId) {
        String token = Base64.getEncoder().encodeToString(customerId.getBytes(StandardCharsets.UTF_8));
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public String getCurrentCustomerId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return new String(Base64.getDecoder().decode(cookie.getValue()), StandardCharsets.UTF_8);
                }
            }
        }
        throw new ResponseException(Error.NOT_AUTHENTICATED);
    }
}
