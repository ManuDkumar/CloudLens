package com.cloudlens.api.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
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
        String ip = httpRequest.getRemoteAddr();
        long now = System.currentTimeMillis();

        requestCounts.compute(ip, (key, timestamps) -> {
            if (timestamps == null) {
                List<Long> newList = new ArrayList<>();
                newList.add(now);
                return newList;
            }
            timestamps.removeIf(ts -> now - ts > TIME_WINDOW_MS);
            timestamps.add(now);
            return timestamps;
        });

        List<Long> timestamps = requestCounts.get(ip);
        if (timestamps != null && timestamps.size() > MAX_REQUESTS) {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}
