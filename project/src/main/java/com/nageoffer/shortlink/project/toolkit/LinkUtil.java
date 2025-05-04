package com.nageoffer.shortlink.project.toolkit;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.Optional;

import static com.nageoffer.shortlink.project.common.constant.ShortLinkConstant.DEFAULT_CACHE_VALID_TIME;

/**
 * 短链接工具类
 */
public class LinkUtil {

    /**
     * 获取短链接在缓存里的有效时间
     * @param validDate 如果传入参数validDate有值，则为短期；若无值，则为永久短链接
     * @return
     */
    public static long getLinkCacheValidDate(Date validDate){
        return Optional.ofNullable(validDate)
                .map(each -> DateUtil.between(new Date(),each,DateUnit.MS))
                .orElse(DEFAULT_CACHE_VALID_TIME);
    }
    //方法接收一个 Date 类型的参数 validDate，表示用户指定的有效期截止日期
    // 使用 Optional.ofNullable 包装这个参数，避免空指针异常
    // 如果 validDate 有值（不为 null）：
        // 使用 DateUtil.between 方法计算当前时间(new Date())到 validDate 之间的毫秒数，这意味着返回的是短期链接的剩余有效时间
    // 如果 validDate 为 null：
        // 返回 DEFAULT_CACHE_VALID_TIME 常量值，这表示永久短链接的默认缓存有效期
}
