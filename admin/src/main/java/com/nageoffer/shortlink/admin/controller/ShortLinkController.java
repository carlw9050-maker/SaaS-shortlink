package com.nageoffer.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkPageResDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShortLinkController {

    /**
     * TODO后续重构为SpringCloud Feign调用
     */

    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};
//    语法 new InterfaceName() {} 或 new ClassName() {}表示创建一个接口的匿名实现类的实例，shortLinkRemoteService 是一个对象
//    default方法，属于接口的实例方法，不同于接口的 静态方法，后者属于接口本身，前者的调用方式是对象.静态方法
//    default方法有默认实现，即如果实现类未覆写该方法，那么也可以直接调用该方法。

    /**
     * 创建短链接
     */
    @PostMapping("/api/shortlink/admin/v1/create-shortlink")
    public Result<ShortLinkCreateRespDTO> creatShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return shortLinkRemoteService.creatShortLink(requestParam);
    }
//    前端发送的数据是JSON字符串，@RequestBody注解会让spring自动将字符串转换为java对象

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/shortlink/admin/v1/get-page")
    public Result<IPage<ShortLinkPageResDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){
        //IPage<> 是 MyBatis-Plus 框架中定义的一个分页结果接口，用于封装分页查询的结果数据
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

    /**
     * 新增短链接
     * TODO 仅仅是考虑到了gid不变时的变更，后续还需做调整
     */
    @PostMapping("/api/shortlink/admin/v1/update-shortlink")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam){
        shortLinkRemoteService.updateShortLink(requestParam);
        return Results.success();
    }
}
