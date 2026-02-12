package com.caro.bizkit.common.monitoring;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class QueryCountFilter extends OncePerRequestFilter {

    private static final int QUERY_COUNT_WARNING_THRESHOLD = 10;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        QueryCountInspector.reset();
        try {
            filterChain.doFilter(request, response);
        } finally {
            int count = QueryCountInspector.getCount();
            String method = request.getMethod();
            String uri = request.getRequestURI();

            if (count >= QUERY_COUNT_WARNING_THRESHOLD) {
                log.warn("[Query Count] {} {} - {} queries (임계값 {} 초과)", method, uri, count,
                        QUERY_COUNT_WARNING_THRESHOLD);
            } else {
                log.info("[Query Count] {} {} - {} queries", method, uri, count);
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator") || uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs");
    }
}
