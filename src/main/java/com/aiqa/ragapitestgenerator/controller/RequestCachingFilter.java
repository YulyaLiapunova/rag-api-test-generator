package com.aiqa.ragapitestgenerator.controller;

import jakarta.servlet.*;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class RequestCachingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
