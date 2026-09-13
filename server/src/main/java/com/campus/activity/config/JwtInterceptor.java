package com.campus.activity.config;

import com.campus.activity.common.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

/** JWT 登录拦截：校验通过后把 userId / role 放入请求属性 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 公开浏览：活动列表与详情的 GET 请求无需登录
        String path = request.getRequestURI();
        if ("GET".equals(request.getMethod())
                && (path.equals("/api/activities") || path.matches("/api/activities/\\d+"))) {
            return true;
        }
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtil.parse(auth.substring(7));
                request.setAttribute("userId", Long.valueOf(claims.getSubject()));
                request.setAttribute("role", claims.get("role", String.class));
                return true;
            } catch (JwtException | IllegalArgumentException ignored) {
                // token 无效或过期，返回未登录
            }
        }
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new HashMap<>();
        body.put("code", 401);
        body.put("message", "未登录或登录已过期");
        body.put("data", null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        return false;
    }
}
