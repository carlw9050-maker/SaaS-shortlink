package com.nageoffer.shortlink.admin.remote.dto;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkCountQueryRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkPageResDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {

    /**
     * 创建短链接
     * @param requestParam 请求参数
     * @return 返回响应
     */
    default Result<ShortLinkCreateRespDTO> creatShortLink(ShortLinkCreateReqDTO requestParam){
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/create-shortlink", JSON.toJSONString(requestParam));
//        第二个参数：请求体，将 requestParam 对象转换成 JSON 字符串（com.alibaba.fastjson2.JSON.toJSONString）。
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {});
    }

    /**
     * 短链接分页查询
     * @param requestParam 请求查询参数
     * @return 返回查询信息
     */
    default Result<IPage<ShortLinkPageResDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){

        Map<String ,Object> map = new HashMap<String ,Object>();
//        创建一个 HashMap 用于存储请求参数，键是字符串类型，值是对象类型
        map.put("gid", requestParam.getGid());
        map.put("current", requestParam.getCurrent());
        map.put("size", requestParam.getSize());
//        将请求参数中的字段放入 map 中
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/get-page", map);
//        使用 HttpUtil.get 方法发送 HTTP GET 请求：将 map 作为查询参数传递
        return JSON.parseObject(resultPageStr, new TypeReference<>(){});
//        反序列化操作需要知道目标对象的类型; Java 的泛型在编译后会进行类型擦除，TypeReference 通过匿名子类的方式保留了完整的泛型类型信息
//        将 JSON 字符串 resultPageStr 反序列化为 Result<IPage<ShortLinkPageResDTO>> 类型的对象
    }

    /**
     * 分组内的短链接数量查询
     * @param requestParam 请求查询参数
     * @return 返回查询信息
     */
    default Result<List<ShortLinkCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam){

        Map<String ,Object> map = new HashMap<>();
//        创建一个 HashMap 用于存储请求参数，键是字符串类型，值是对象类型
        map.put("requestParam", requestParam);
//        将请求参数中的字段放入 map 中
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/get-count", map);
//        使用 HttpUtil.get 方法发送 HTTP GET 请求：将 map 作为查询参数传递
        return JSON.parseObject(resultPageStr, new TypeReference<>(){});
//        反序列化操作需要知道目标对象的类型; Java 的泛型在编译后会进行类型擦除，TypeReference 通过匿名子类的方式保留了完整的泛型类型信息
//        将 JSON 字符串 resultPageStr 反序列化为 Result<IPage<ShortLinkPageResDTO>> 类型的对象
    }

    default void updateShortLink(ShortLinkUpdateReqDTO requestParam) {

        HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/update-shortlink", JSON.toJSONString(requestParam));
    }
}
