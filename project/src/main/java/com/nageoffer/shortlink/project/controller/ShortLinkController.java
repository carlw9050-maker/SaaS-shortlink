package com.nageoffer.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.project.common.convention.result.Result;
import com.nageoffer.shortlink.project.common.convention.result.Results;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageResDTO;
import com.nageoffer.shortlink.project.service.ShortLinkService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    @GetMapping("/{short-uri}")
    public void restoreUrl(@PathVariable ("short-uri") String shortUri, ServletRequest request, ServletResponse response) {
        //@PathVariable ("short-uri")表示从URL路径中提取名为"short-uri"的变量值并传递给方法参数，
        //ServletRequest request: HTTP请求对象,ServletResponse response: HTTP响应对象
        shortLinkService.restoreUrl(shortUri,request,response);
    }
    //"/{short-uri}"定义了一个 动态路径变量，{short-uri} 是一个占位符，可以匹配任意值；假设你的服务部署在 https://example.com，
    // 那么访问 https://example.com/abc123 → {short-uri} 的值就是 "abc123"
    //在 HTTP 请求到达你的代码之前，Servlet 容器（如 Tomcat、Jetty）已经初始化了 request 和 response 对象，并交给 Spring 处理。
    //即使你什么都没做，response 仍然是一个有效的响应对象，只是它的状态是默认的（例如，状态码 200，没有内容）

    /**
     * 新增短链接
     */
    @PostMapping("/api/shortlink/v1/create-shortlink")
    public Result<ShortLinkCreateRespDTO> creatShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return Results.success(shortLinkService.creatShortLink(requestParam));
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/shortlink/v1/get-page")
    public Result<IPage<ShortLinkPageResDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        //IPage<> 是 MyBatis-Plus 框架中定义的一个分页结果接口，用于封装分页查询的结果数据
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }

    /**
     * 查询每个分组中短链接数量
     * @param requestParam 数量查询参数，gid列表
     * @return 返回查询结果
     */
    @GetMapping("/api/shortlink/v1/get-count")
    public Result<List<ShortLinkCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam){
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }

    /**
     * 新增短链接
     * TODO 仅仅是考虑到了gid不变时的变更，后续还需做调整
     */
    @PostMapping("/api/shortlink/v1/update-shortlink")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkService.updateShortLink(requestParam);
        return Results.success();
    }


}
