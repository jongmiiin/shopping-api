package com.skala.shopapi.config;

import org.h2.server.web.JakartaWebServlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot 4.x부터 H2ConsoleAutoConfiguration이 제거되어(spring-boot-autoconfigure 4.1.0에
 * 해당 클래스가 존재하지 않음) spring.h2.console.enabled 프로퍼티만으로는 콘솔이 뜨지 않는다.
 * H2 콘솔 서블릿을 직접 등록해 이전과 동일하게 /h2-console에서 접속 가능하게 한다.
 * H2의 org.h2.server.web.WebServlet은 javax.servlet 기반이라 Jakarta 서블릿 컨테이너(Tomcat 10+)에
 * 등록할 수 없으므로, jakarta.servlet 기반의 JakartaWebServlet을 사용한다.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.h2.console", name = "enabled", havingValue = "true")
public class H2ConsoleConfig {

    @Bean
    public ServletRegistrationBean<JakartaWebServlet> h2ConsoleServlet() {
        ServletRegistrationBean<JakartaWebServlet> registrationBean =
                new ServletRegistrationBean<>(new JakartaWebServlet(), "/h2-console/*");
        registrationBean.addInitParameter("webAllowOthers", "true");
        return registrationBean;
    }
}
