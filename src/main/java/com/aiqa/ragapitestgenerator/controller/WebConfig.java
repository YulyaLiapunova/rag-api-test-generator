package com.aiqa.ragapitestgenerator.controller;

import com.aiqa.ragapitestgenerator.interceptor.GitHubSignatureInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private GitHubSignatureInterceptor gitHubSignatureInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(gitHubSignatureInterceptor)
                .addPathPatterns("/webhook/pr-created");
    }
}
