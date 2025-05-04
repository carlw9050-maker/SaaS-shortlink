package com.nageoffer.shortlink.project.test;

import com.nageoffer.shortlink.project.dao.entity.ShortLinkDO;
import com.nageoffer.shortlink.project.dao.mapper.ShortLinkMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional  // 测试后回滚，避免污染数据库
public class CreatTimeMappingTest {

    @Autowired
    private ShortLinkMapper shortLinkMapper;

    @Test
    public void testCreatTimeAutoFillAndMapping() {
        // 1. 插入数据（不设置 creatTime，依赖自动填充）
        ShortLinkDO shortLinkDO = new ShortLinkDO();
        shortLinkDO.setFullShortUrl("meituan12.com/3Ydkla12");
        shortLinkDO.setGid("ePGsDF");
        shortLinkMapper.insert(shortLinkDO);  // 触发 insertFill

        // 2. 查询数据库，验证 creatTime 是否自动填充并映射成功
        ShortLinkDO dblink = shortLinkMapper.selectById(shortLinkDO.getId());
        assertNotNull(dblink.getCreatTime());  // 断言 creatTime 不为空

        // 3. 打印日志，查看实际插入的 creatTime 和数据库列名
        System.out.println("Java 字段 creatTime: " + dblink.getCreatTime());
    }
}