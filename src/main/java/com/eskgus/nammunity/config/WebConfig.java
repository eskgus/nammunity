package com.eskgus.nammunity.config;

import com.eskgus.nammunity.config.interceptor.CommentsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.PostsAuthInterceptor;
import com.eskgus.nammunity.config.interceptor.UserAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private PostsAuthInterceptor postsAuthInterceptor;

    @Autowired
    private UserAuthInterceptor userAuthInterceptor;

    @Autowired
    private CommentsAuthInterceptor commentsAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(postsAuthInterceptor)
                .addPathPatterns("/api/posts/**", "/posts/update/**")
                .excludePathPatterns("/api/posts/selected-delete");

        registry.addInterceptor(userAuthInterceptor)
                .addPathPatterns("/users/sign-up", "/users/sign-in", "/users/my-page/update/user-info");

        registry.addInterceptor(commentsAuthInterceptor)
                .addPathPatterns("/api/comments/**")
                .excludePathPatterns("/api/comments/selected-delete");
    }
}
