package com.nageoffer.shortlink.project.handler;


import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;

/**
 * 自定义流量控制策略
 */
public class CustomBlockHandler {

    public static Result<ShortLinkCreateRespDTO> createShortLinkBlockHandlerMethod(ShortLinkCreateReqDTO requestParam, BlockException exception) {
        return new Result<ShortLinkCreateRespDTO>().setCode("B100000").setMessage("当前访问网站人数过多，请稍后再试...");
    }
    //createShortLinkBlockHandlerMethod 是通过 @SentinelResource 注解的 blockHandler 属性反射调用的，而不是直接通过Java代码调用。
    //静态分析工具无法识别这种运行时动态绑定的调用关系，因此误判为"未使用"。
}
