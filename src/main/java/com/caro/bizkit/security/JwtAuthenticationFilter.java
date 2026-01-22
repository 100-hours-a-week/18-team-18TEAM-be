package com.caro.bizkit.security;

import com.caro.bizkit.common.ApiResponse.ApiResponse;
import com.caro.bizkit.domain.user.dto.UserResponse;
import com.caro.bizkit.domain.user.entity.User;
import com.caro.bizkit.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException
    {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length());
        if (!jwtTokenProvider.isValid(token)) {
            log.warn("Invalid token: method={}, path={}", request.getMethod(), request.getRequestURI());
            writeUnauthorizedResponse(response, "유효하지 않은 토큰입니다. 서명 또는 만료 시간을 확인하세요.");
            return;
        }

        Claims claims = jwtTokenProvider.parseClaims(token);
        Integer userId = parseUserId(claims.getSubject());
        if (userId == null) {
            log.warn("Invalid token subject: method={}, path={}", request.getMethod(), request.getRequestURI());
            writeUnauthorizedResponse(response, "유효하지 않은 토큰입니다. 사용자 식별자가 올바르지 않습니다.");
            return;
        }
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            log.warn("User not found for token: method={}, path={}, userId={}",
                    request.getMethod(), request.getRequestURI(), userId);
            writeUnauthorizedResponse(response, "유효하지 않은 토큰입니다. 사용자 정보를 찾을 수 없습니다.");
            return;
        }

        UserResponse principal = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getLinedNumber(),
                user.getCompany(),
                user.getDepartment(),
                user.getPosition(),
                user.getProfileImageKey(),
                user.getDescription()
        );
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private Integer parseUserId(String subject) {
        try {
            return Integer.valueOf(subject);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        ApiResponse<Void> body = ApiResponse.failed(HttpStatus.UNAUTHORIZED, message);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
