package com.nageoffer.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.config.UserFlowRiskControlConfiguration;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import static com.nageoffer.shortlink.admin.common.convention.errorcode.BaseErrorCode.FLOW_LIMIT_ERROR;

/**
 * 用户操作流量风控过滤器
 * 用户流量风险控制功能的逻辑核心
 */
@Slf4j
@RequiredArgsConstructor
public class UserFlowRiskControlFilter implements Filter {

    private final StringRedisTemplate stringRedisTemplate;
    //用于操作 Redis（执行限流 Lua 脚本）

    private final UserFlowRiskControlConfiguration userFlowRiskControlConfiguration;
    //读取限流配置参数，比如 timeWindow 和 maxAccessCount

    private static final String USER_FLOW_RISK_CONTROL_LUA_SCRIPT_PATH = "lua/user_flow_risk_control.lua";

    /**
     *控制访问流量，在请求到达业务逻辑前将其拦截
     * @param request
     * @param response
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @SneakyThrows
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        //doFilter()是Fillter接口的方法，实现了该接口也得对应覆写这个方法。filterChain：过滤器链，用于继续执行下一个过滤器或目标资源。
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        //创建一个Redis Lua脚本执行对象，用于封装脚本文件和返回值类型
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(USER_FLOW_RISK_CONTROL_LUA_SCRIPT_PATH)));
        //ClassPathResource(...)：从类路径下加载Lua脚本；ResourceScriptSource(...)将该脚本封装为spring脚本资源对象；再把该资源赋给redisScript
        redisScript.setResultType(Long.class);
        //指定脚本的返回值类型
        String username = Optional.ofNullable(UserContext.getUsername()).orElse("other");
        //从 UserContext 的 ThreadLocal 变量中获取用户名
        Long result = null;
        try {
            result = stringRedisTemplate.execute(redisScript, Lists.newArrayList(username), userFlowRiskControlConfiguration.getTimeWindow());
            //Spring对 Redis 脚本参数传递的一种规范形式：Java - Redis - Lua.
            //在Spring的Redis脚本执行方法中，<T> T execute(RedisScript<T> script, List<K> keys, Object... args);
                //传给 Lua 脚本中的 KEYS[1], KEYS[2]...；
                //传给 Lua 脚本中的 ARGV[1], ARGV[2]...；
        } catch (Throwable ex) {
            log.error("执行用户请求流量限制LUA脚本出错", ex);
            //如果Redis执行出错，记录异常日志
            returnJson((HttpServletResponse) response, JSON.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
        }
        if (result == null || result > userFlowRiskControlConfiguration.getMaxAccessCount()) {
            returnJson((HttpServletResponse) response, JSON.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 向 http 响应中写入 json 格式的数据
     * @param response
     * @param json
     * @throws Exception
     */
    private void returnJson(HttpServletResponse response, String json) throws Exception {
        response.setCharacterEncoding("UTF-8");
        //设置字符编码为UTF-8，避免中文乱码
        response.setContentType("text/html; charset=utf-8");
        //设置响应类型为 HTML + UTF-8
        try (PrintWriter writer = response.getWriter()) {
            //获取响应输出流（自动关闭），准备写入数据。
            writer.print(json);
            //writer.print(json)：把 JSON 内容写入到response的body，返回给客户端
        }   //Java 7 引入的语法：ry-with-resources（带资源的 try 语句）
        //try 块执行结束后（无论是正常结束还是抛出异常），资源会自动关闭，不需要你手动调用 .close() 方法；相当于你写了 finally { writer.close(); }
        //try()括号里是资源声明，资源必须是实现了 java.lang.AutoCloseable 接口的对象
    }
    //response 代表服务端给客户端的响应（也就是 HTTP 的响应）。
    //json 是一个字符串变量，通常是将对象序列化得到的JSON格式的数据，是具体要返回给前端的内容
    //response 是输出工具，json 是输出内容，后者需要前者发送给浏览器
}
