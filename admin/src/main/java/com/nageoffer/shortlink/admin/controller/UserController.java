package com.nageoffer.shortlink.admin.controller;

import cn.hutool.core.bean.BeanUtil;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.common.convention.result.Results;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserActualRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 根据用户名查询用户信息
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable("username") String username) {
        UserRespDTO result = userService.getUserByUsername(username);
        return Results.success(result);
    }

    @GetMapping("/api/shortlink/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable("username") String username) {
        UserActualRespDTO result = BeanUtil.toBean(userService.getUserByUsername(username),UserActualRespDTO.class);
        //将UserRespDTO类型的result对象的属性值复制给UserActualRespDTO类型的result对象的同名属性
        return Results.success(result);
    }

    @GetMapping("/api/shortlink/v1/user/available-username")
    public Result<Boolean> availableUsername(String username) {
        return Results.success(userService.availableUsername(username));
    }

    @PostMapping("/api/shortlink/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO requestParam){
        userService.register(requestParam);
        return Results.success();
    }

    @PutMapping("/api/shortlink/v1/update-user")
    public Result<Void> update(@RequestBody UserUpdateDTO requestParam){
        userService.update(requestParam);
        return Results.success();
    }

    /**
     * 用户登陆
     */
    @PostMapping("/api/shortlink/v1/user-login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO requestParam){
        UserLoginRespDTO result=userService.login(requestParam);
        return Results.success(result);
    }

    /**
     * 检查用户是否登陆
     */
    @GetMapping("/api/shortlink/v1/user-checkLogin")
    public Result<Boolean> checkLogin(@RequestParam("username") String username,@RequestParam("token") String token){
        Boolean result=userService.checkLogin(username,token);
        return Results.success(result);
    }

    /**
     * 用户退出登陆
     */
    @DeleteMapping("/api/shortlink/v1/user-logout")
    public Result<Void> logout(@RequestParam("username") String username,@RequestParam("token") String token){
        userService.logout(username,token);
        return Results.success();
    }
}
