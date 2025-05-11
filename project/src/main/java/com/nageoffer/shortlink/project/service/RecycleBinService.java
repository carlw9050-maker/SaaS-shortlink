package com.nageoffer.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinAddReqDTO;
import com.nageoffer.shortlink.project.dto.req.RecycleBinRemoveReqDTO;
import com.nageoffer.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.nageoffer.shortlink.project.dto.resp.ShortLinkPageResDTO;

/**
 * 回收站管理接口层
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 将短链接添加至回收站
     * @param requestParam 请求参数
     */
    void addRecycleBin(RecycleBinAddReqDTO requestParam);

    /**
     * 分页查询回收站里的短链接
     * @param requestParam 查询参数
     * @return 返回分页查询结果
     */
    IPage<ShortLinkPageResDTO> pageRecycleBinShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 将短链接从回收站移出
     * @param requestParam 请求参数
     */
    void removeRecycleBin(RecycleBinRemoveReqDTO requestParam);
}
