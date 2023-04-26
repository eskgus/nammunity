package com.eskgus.nammunity.config;

import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    PostsAuthInterceptor postsAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(postsAuthInterceptor)
                .addPathPatterns("/api/posts/**", "/posts/update/**");
    }
}