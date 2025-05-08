package com.nageoffer.shortlink.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 短链接不存在跳转控制器
 */
@Controller
//注意与@RestController不同
public class ShortLinkNotFoundController {

    /**
     * 短链接不存在跳转页面
     */
    @RequestMapping("/page/notfound")
    public String notfound() {
        return "notfound";
    }
}
//((HttpServletResponse) response).sendRedirect("/page/notfound");于project.service.Impl里的restoreUrl()
//通过这行代码就可直接调用该控制器方法了