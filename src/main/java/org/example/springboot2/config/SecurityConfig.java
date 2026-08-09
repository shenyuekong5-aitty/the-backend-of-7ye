package org.example.springboot2.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");   // 允许所有来源
        config.addAllowedMethod("*");          // 允许所有 HTTP 方法
        config.addAllowedHeader("*");          // 允许所有请求头
        config.setAllowCredentials(false);     // 使用 token 认证，无需 cookie

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))   // ✅ 使用上面定义的 CORS 配置
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .anonymous(anonymous -> {})
                .authorizeHttpRequests(auth -> auth
                        // 使用 Ant 路径匹配器确保放行规则生效
                        .requestMatchers(
                                antMatcher("/api/user/login"),
                                antMatcher("/api/user/register"),
                                antMatcher("/api/user/list"),
                                antMatcher("/api/user/role"),
                                antMatcher("/api/user/**"),
                                antMatcher("/api/sms/**"),
                                antMatcher("/api/recommendation/**"),
                                antMatcher("/api/notice/**"),
                                antMatcher("/api/book/**"),
                                antMatcher("/api/music/**"),
                                antMatcher("/api/anime/**"),
                                antMatcher("/api/game/**"),
                                antMatcher("/api/emotion/**"),
                                antMatcher("/api/quote/**"),
                                antMatcher("/api/creed/**"),
                                antMatcher("/api/tag/**"),
                                antMatcher("/api/cognize/**"),
                                antMatcher("/api/comment/**"),
                                antMatcher("/api/study/**"),
                                antMatcher("/api/favorite/**"),
                                antMatcher("/api/like/**"),
                                antMatcher("/api/view/**"),
                                antMatcher("/api/role/**"),
                                antMatcher("/api/permission/**"),
                                antMatcher("/api/user/register-by-phone"),
                                antMatcher("/api/user/reset-password"),
                                antMatcher("/api/user/check-phone"),
                                antMatcher("/ws/**"),
                                antMatcher("/api/qrlogin/**"),
                                antMatcher("/api/user/deactivate"),
                                antMatcher("/api/friend/**"),
                                antMatcher("/api/memory/**"),
                                antMatcher("/api/notice/**"),
                                antMatcher("/api/ai/**")
                        ).permitAll()
                        .requestMatchers(antMatcher("/static/**"), antMatcher("/favicon.ico"), antMatcher("/error"), antMatcher("/assets/**")).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            System.out.println("认证失败访问路径: " + request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> body = new HashMap<>();
            body.put("code", 401);
            body.put("message", "未认证或 Token 无效，请重新登录");

            objectMapper.writeValue(response.getOutputStream(), body);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> body = new HashMap<>();
            body.put("code", 403);
            body.put("message", "无权限访问该资源");

            objectMapper.writeValue(response.getOutputStream(), body);
        };
    }
}