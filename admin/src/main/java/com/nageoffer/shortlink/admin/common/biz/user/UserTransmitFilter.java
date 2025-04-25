package com.nageoffer.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.util.Objects;

/**
 * 用户信息传输过滤器,用于在Web请求中处理用户认证信息并将其存入线程上下文。
 * 线程上下文指与当前执行线程相关联的一组数据或状态信息。
 */
@RequiredArgsConstructor
public class UserTransmitFilter implements Filter {
    //实现了Filter接口，表示这是一个Servlet过滤器

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //实现Filter接口的核心方法，处理每个HTTP请求
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        //将ServletRequest转换为HttpServletRequest以获取更多HTTP相关信息
        String requestURI = httpServletRequest.getRequestURI();
        if(!Objects.equals(requestURI, "/api/shortlink/admin/v1/user-login")){    //将登陆接口放行
            //后续可能还得对验证登陆接口放行，后面再说吧，可以参考”拦截器封装用户上下文功能“视频的最后
            String username = httpServletRequest.getHeader("username");
            String token = httpServletRequest.getHeader("token");//从请求头中获取username和token字段
            Object UserInfoJsonStr = stringRedisTemplate.opsForHash().get("login:"+username, token);
            //使用这两个凭证从Redis的Hash结构中获取用户信息，key格式为"login_username"，field是token
            if(UserInfoJsonStr != null) {
                UserInfoDTO userInfoDTO = JSON.parseObject(UserInfoJsonStr.toString(), UserInfoDTO.class);
                UserContext.setUser(userInfoDTO);
                //如果Redis中存在该用户信息，将JSON字符串反序列化为UserInfoDTO对象，将用户信息存入UserContext（通常是一个ThreadLocal实现的上下文）
            }
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
        //继续执行过滤器链。
        //在finally块中确保无论是否发生异常都清除UserContext中的用户信息，防止内存泄漏
    }
}