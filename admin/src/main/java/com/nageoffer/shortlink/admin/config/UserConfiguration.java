package com.nageoffer.shortlink.admin.config;

import com.nageoffer.shortlink.admin.common.biz.user.UserFlowRiskControlFilter;
import com.nageoffer.shortlink.admin.common.biz.user.UserTransmitFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 用户配置自动装配
 */
@Configuration
public class UserConfiguration {

    //这里的过滤器与网关过滤器的职责分工不同：
        //网关过滤器过针对全局，滤掉未登陆或者 token 无效的请求，是各服务的第一道防线；
        //下面的两个过滤器是仅针对单个服务的过滤器，（基于 Spring MVC 的 filter）
    /**
     * 用户信息传递过滤器（提取请求头中的信息）
     * Spring配置类，这样配置后，所有进入应用的请求都会先经过这个过滤器，确保用户信息能够从请求头正确提取出来并设置到线程上下文中，供后续处理流程使用。
     */
    @Bean
    //@Bean：将该方法的返回对象注册为Spring容器中的Bean
    public FilterRegistrationBean<UserTransmitFilter> globalUserTransmitFilter() {
        //参数StringRedisTemplate stringRedisTemplate：自动注入Redis操作模板
        FilterRegistrationBean<UserTransmitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserTransmitFilter());
        registration.addUrlPatterns("/*");//设置过滤器的URL匹配模式,"/*"表示匹配所有请求路径
        registration.setOrder(0);//设置过滤器的执行顺序（优先级）,值为0表示高优先级（数值越小优先级越高）,确保这个用户信息过滤器在其他过滤器之前执行
        return registration;//返回配置好的过滤器注册Bean
    }

    /**
     * 用户操作流量风控过滤器
     * UserFlowRiskControlFilter 之所以能拦截所有请求，是因为下面的代码
     * 它被注册为一个 Spring Boot 的 Servlet 过滤器，
     * 并设置了 addUrlPatterns("/*")，从而拦截进入应用的所有 HTTP 请求路径。
     */
    @Bean
    @ConditionalOnProperty(name = "short-link.flow-limit.enable", havingValue = "true")
    public FilterRegistrationBean<UserFlowRiskControlFilter> globalUserFlowRiskControlFilter(
            StringRedisTemplate stringRedisTemplate,
            UserFlowRiskControlConfiguration userFlowRiskControlConfiguration) {
        FilterRegistrationBean<UserFlowRiskControlFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new UserFlowRiskControlFilter(stringRedisTemplate, userFlowRiskControlConfiguration));
        //注册自定义的过滤器
        //这里的构造函数为什么有两个参数，因为：该类有@RequiredArgsConstructor注解,它自动将所有 final修饰 的成员变量作为构造方法的参数列表。
        //构造方法的目标是初始化成员变量
        registration.addUrlPatterns("/*");
        //设置拦截路径为 /* ，表示拦截所有请求
        registration.setOrder(10);
        //设置优先级，数值越高 优先级越低
        return registration;
        //registration是一个 FilterRegistrationBean<UserFlowRiskControlFilter> 实例，这个对象会被 Spring Boot 自动识别并注册为一个 Servlet 过滤器，
        //FilterRegistrationBean 是 Spring Boot 提供的一个包装类，用于手动注册 Servlet 过滤器（Filter）。
        //Spring Boot 启动时，会扫描所有 @Bean 方法。如果你返回了一个 FilterRegistrationBean，它就会：
            //从里面拿到你定义的 Filter 实例；
            //拿到你配置的 URL 匹配规则；
            //注册到内嵌的 Tomcat（或 Jetty、Undertow）Servlet 容器中；
            //生效于 Web 请求生命周期中。
        //举个例子：这段代码在告诉 spring “我要加一个拦截器，这里是它的类（UserFlowRiskControlFilter），它要拦哪些 URL（/*），它的优先级是这个（order=10）。”
    }
}

