package com.cloudlens.api.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private final Cache<String, List<Long>> requestCounts;

    private static final int MAX_REQUESTS = 100;
    private static final long TIME_WINDOW_MS = 60_000;

    public RateLimitingFilter() {
        this.requestCounts = Caffeine.newBuilder()
                .expireAfterAccess(TIME_WINDOW_MS * 2, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isPublicEndpoint(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String ip = resolveClientIp(httpRequest);
        long now = System.currentTimeMillis();

        List<Long> timestamps = requestCounts.get(ip, k -> Collections.synchronizedList(new ArrayList<>()));
        synchronized (timestamps) {
            timestamps.removeIf(ts -> now - ts > TIME_WINDOW_MS);
            timestamps.add(now);
        }

        if (timestamps.size() > MAX_REQUESTS) {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/login") || path.equals("/signup")
                || path.startsWith("/css/") || path.startsWith("/js/")
                || path.equals("/actuator/health");
    }
}
