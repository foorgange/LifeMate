package com.lifemate.config;

import com.lifemate.interceptor.LoginInterceptor;
import com.lifemate.interceptor.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

/**
 * MVC 配置：注册登录校验与 token 刷新两个拦截器。
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // token 刷新拦截器：先执行（order 0），把用户恢复到 ThreadLocal
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
                .addPathPatterns("/**").order(0);
        // 登录校验拦截器：后执行（order 1），对需要登录的接口校验用户
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/user/login",
                        "/user/code",
                        "/upload/**",
                        "/voucher/**",
                        "/shop/**",
                        "/shop-type/**",
                        "/blog/hot"
                ).order(1);
    }
}
