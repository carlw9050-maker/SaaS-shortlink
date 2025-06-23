package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.biz.ShortLinkStatisticRecordDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCountQueryRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageResDTO;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.util.List;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     * @param requestParam 请求参数
     * @return 响应参数
     */
    ShortLinkCreateRespDTO creatShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 查询参数
     * @return 返回分页查询结果
     */
    IPage<ShortLinkPageResDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 短链接分组数量查询
     * @param requestParam 分组数量查询参数
     * @return 返回分组数量查询结果
     */
    List<ShortLinkCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);

    /**
     * 修改短链接
     * @param requestParam 请求参数
     * @return 返回修改结果
     */
    void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    /**
     * 短链接重定向（短链接跳转到原始链接）
     * @param shortUri 短链接
     * @param request   http请求
     * @param response  http响应
     */
    void restoreUrl(String shortUri, ServletRequest request, ServletResponse response);

    /**
     * 批量创建短链接
     *
     * @param requestParam 批量创建短链接请求参数
     * @return 批量创建短链接返回参数
     */
    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam);

    /**
     * 短链接统计
     *
     * @param fullShortUrl         完整短链接
     * @param gid                  分组标识
     * @param shortLinkStatisticRecord 短链接统计实体参数
     */
    void shortLinkStatistic(String fullShortUrl, String gid, ShortLinkStatisticRecordDTO shortLinkStatisticRecord);
}
