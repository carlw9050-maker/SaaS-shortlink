package com.nageoffer.shortlink.admin.config;

import com.nageoffer.shortlink.admin.common.biz.user.UserTransmitFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 用户配置自动装配
 */
@Configuration
public class UserConfiguration {

    /**
     * 用户信息传递过滤器
     * Spring配置类，这样配置后，所有进入应用的请求都会先经过这个过滤器，确保用户信息能够被正确提取并设置到线程上下文中，供后续处理流程使用。
     */
    @Bean
    //@Bean：将该方法的返回对象注册为Spring容器中的Bean
    public FilterRegistrationBean<UserTransmitFilter> globalUserTransmitFilter(StringRedisTemplate stringRedisTemplate) {
        //参数StringRedisTemplate stringRedisTemplate：自动注入Redis操作模板
        FilterRegistrationBean<UserTransmitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserTransmitFilter(stringRedisTemplate));
        registration.addUrlPatterns("/*");//设置过滤器的URL匹配模式,"/*"表示匹配所有请求路径
        registration.setOrder(0);//设置过滤器的执行顺序（优先级）,值为0表示高优先级（数值越小优先级越高）,确保这个用户信息过滤器在其他过滤器之前执行
        return registration;//返回配置好的过滤器注册Bean
    }
}

