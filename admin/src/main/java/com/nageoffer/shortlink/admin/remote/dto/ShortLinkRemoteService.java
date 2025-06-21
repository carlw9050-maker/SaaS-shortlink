package com.nageoffer.shortlink.admin.remote.dto;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.remote.dto.req.*;
import com.nageoffer.shortlink.admin.remote.dto.resp.*;
import org.springframework.web.bind.annotation.RequestParam;

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
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return 短链接批量创建响应
     */
    default Result<ShortLinkBatchCreateRespDTO> batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        String resultBodyStr = HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/create-shortlink/batch", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 短链接分页查询
     * @param requestParam 请求查询参数
     * @return 返回查询信息
     */
    default Result<IPage<ShortLinkPageResDTO>> pageShortLink(ShortLinkPageReqDTO requestParam){

        Map<String ,Object> map = new HashMap<>();
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

    /**
     * 根据 URL 获取标题
     * @param url 目标网址
     * @return 网站标题
     */
    default Result<String> getTitleByUrl(@RequestParam("url") String url){
        String resultStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/get-title?url=" + url);
        return JSON.parseObject(resultStr, new TypeReference<>() {
        });
    }

    /**
     * 将短链接添加至回收站
     * @param requestParam 请求参数
     */
    default void addRecycleBin(RecycleBinAddReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/recycle-bin/add", JSON.toJSONString(requestParam));
    }

    /**
     * 回收站里的短链接分页查询
     * @param requestParam 请求查询参数
     * @return 返回查询信息
     */
    default Result<IPage<ShortLinkPageResDTO>> pageRecycleBinShortLink(ShortLinkPageReqDTO requestParam){

        Map<String ,Object> map = new HashMap<>();
//        创建一个 HashMap 用于存储请求参数，键是字符串类型，值是对象类型
        map.put("gid", requestParam.getGid());
        map.put("current", requestParam.getCurrent());
        map.put("size", requestParam.getSize());
//        将请求参数中的字段放入 map 中
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/recycle-bin/get-page", map);
//        使用 HttpUtil.get 方法发送 HTTP GET 请求：将 map 作为查询参数传递
        return JSON.parseObject(resultPageStr, new TypeReference<>(){});
//        反序列化操作需要知道目标对象的类型; Java 的泛型在编译后会进行类型擦除，TypeReference 通过匿名子类的方式保留了完整的泛型类型信息
//        将 JSON 字符串 resultPageStr 反序列化为 Result<IPage<ShortLinkPageResDTO>> 类型的对象
    }

    /**
     * 将短链接从回收站移出
     * @param requestParam 请求参数
     */
    default void removeRecycleBin(RecycleBinRemoveReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/recycle-bin/remove", JSON.toJSONString(requestParam));
    }

    /**
     *将回收站的短链接彻底删除
     * @param requestParam 请求参数
     */
    default void deleteRecycleBin(RecycleBinDeleteReqDTO requestParam){
        HttpUtil.post("http://127.0.0.1:8001/api/shortlink/v1/recycle-bin/delete", JSON.toJSONString(requestParam));
    }

    /**
     * 访问单个短链接指定时间内监控数据
     *
     * @param requestParam 访问短链接监控请求参数
     * @return 短链接监控信息
     */
    default Result<ShortLinkStatisticRespDTO> oneShortLinkStatistic(ShortLinkStatisticReqDTO requestParam) {
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/statistic", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 访问分组短链接指定时间内监控数据
     *
     * @param requestParam 访问短链接监控请求参数
     * @return 短链接监控信息
     */
    default Result<ShortLinkStatisticRespDTO> groupShortLinkStatistic(ShortLinkGroupStatisticReqDTO requestParam) {
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/statistic/group", BeanUtil.beanToMap(requestParam));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 分页访问单个短链接指定时间内监控访问记录数据
     *
     * @param requestParam 访问短链接监控访问记录请求参数
     * @return 短链接监控访问记录信息
     */
    default Result<IPage<ShortLinkStatisticAccessRecordRespDTO>> shortLinkStatisticAccessRecord(ShortLinkStatisticAccessRecordReqDTO requestParam) {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam, false, true);
        stringObjectMap.remove("orders");
        stringObjectMap.remove("records");
        //The Page class (especially from libraries like Mybatis-Plus) 带有的属性，而reqDTO又继承了Page类，所以也就有了这俩属性
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/statistic/get-page", stringObjectMap);
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    /**
     * 分页访问分组短链接指定时间内监控访问记录数据
     *
     * @param requestParam 访问短链接监控访问记录请求参数
     * @return 短链接监控访问记录信息
     */
    default Result<IPage<ShortLinkStatisticAccessRecordRespDTO>> groupShortLinkStatisticAccessRecord(ShortLinkGroupStatisticAccessRecordReqDTO requestParam) {
        Map<String, Object> stringObjectMap = BeanUtil.beanToMap(requestParam, false, true);
        stringObjectMap.remove("orders");
        stringObjectMap.remove("records");
        //The Page class (especially from libraries like Mybatis-Plus) 带有的属性，而reqDTO又继承了Page类，所以也就有了这俩属性
        String resultBodyStr = HttpUtil.get("http://127.0.0.1:8001/api/shortlink/v1/statistic/group/get-page", stringObjectMap);
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }
}
