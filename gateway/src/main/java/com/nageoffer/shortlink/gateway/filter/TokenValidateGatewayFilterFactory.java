package com.nageoffer.shortlink.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nageoffer.shortlink.gateway.config.Config;
import com.nageoffer.shortlink.gateway.dto.GatewayErrorResult;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * SpringCloud Gateway Token 拦截器
 */
@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {

    private final StringRedisTemplate stringRedisTemplate;
    //用于从 Redis 中查询 token 对应的用户信息，验证 Token 的有效性

    public TokenValidateGatewayFilterFactory(StringRedisTemplate stringRedisTemplate) {
        super(Config.class);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            //返回一个网关过滤器实例
            ServerHttpRequest request = exchange.getRequest();
            String requestPath = request.getPath().toString();
            String requestMethod = request.getMethod().name();
            // 获取当前请求对象
            if (!isPathInWhiteList(requestPath, requestMethod, config.getWhitePathList())) {
                //判断请求路径是否在白名单中，不在，则验证 Token
                String username = request.getHeaders().getFirst("username");
                String token = request.getHeaders().getFirst("token");
                Object userInfo;
                if (StringUtils.hasText(username) && StringUtils.hasText(token) && (userInfo = stringRedisTemplate.opsForHash().get("login:" + username, token)) != null) {
                    // 检查 username、token 不为空，且 Redis 存在该 token 对应的用户信息，通过则执行下面逻辑（token 在 Redis 中是 值中的 field，对应的 value 是用户对象的 jason 字符串
                    JSONObject userInfoJsonObject = JSON.parseObject(userInfo.toString());
                    ServerHttpRequest.Builder builder = exchange.getRequest().mutate().headers(httpHeaders -> {
                        httpHeaders.set("userId", userInfoJsonObject.getString("id"));
                        httpHeaders.set("realName", URLEncoder.encode(userInfoJsonObject.getString("realName"), StandardCharsets.UTF_8));
                    });
                    //解析用户信息，将 userId、realName 添加到请求头，供后续的过滤器逻辑提取请求头里的信息；
                    // 后续的过滤器会将这些信息提取出来放在 UserInfoDTO 里，并将该对象存与 TTL（线程专属变量副本）
                    return chain.filter(exchange.mutate().request(builder.build()).build());
                    // 继续执行执行后续过滤器或者路由到目标服务
                }
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory bufferFactory = response.bufferFactory();
                    GatewayErrorResult resultMessage = GatewayErrorResult.builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("Token validation error")
                            .build();
                    return bufferFactory.wrap(JSON.toJSONString(resultMessage).getBytes());
                    //请求不在不在白名单中，且验证 token 未通过，返回 401 错误；
                }));
            }
            return chain.filter(exchange);
            // 请求在白名单中，直接放行
        };
    }

    private boolean isPathInWhiteList(String requestPath, String requestMethod, List<String> whitePathList) {
        return (!CollectionUtils.isEmpty(whitePathList) && whitePathList.stream().anyMatch(requestPath::startsWith)) || (Objects.equals(requestPath, "/api/shortlink/admin/v1") && Objects.equals(requestMethod, "POST"));
        // 路径是 api/short-link/admin/v1/user 且 http 请求方法是 post，则也放行，因为是用户注册，没有 token 信息是正常的，因此也放行；
    }
}
