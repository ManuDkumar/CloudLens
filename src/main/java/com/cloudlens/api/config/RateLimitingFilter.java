package com.cloudlens.api.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private final Map<String, List<Long>> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100;
    private static final long TIME_WINDOW_MS = 60_000;

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

        long count = requestCounts.compute(ip, (key, timestamps) -> {
            if (timestamps == null) {
                timestamps = Collections.synchronizedList(new ArrayList<>());
            }
            synchronized (timestamps) {
                timestamps.removeIf(ts -> now - ts > TIME_WINDOW_MS);
                timestamps.add(now);
            }
            return timestamps;
        }).size();

        if (count > MAX_REQUESTS) {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return path.equals("/login") || path.equals("/signup")
                || path.startsWith("/css/") || path.startsWith("/js/")
                || path.equals("/actuator/health");
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
}
