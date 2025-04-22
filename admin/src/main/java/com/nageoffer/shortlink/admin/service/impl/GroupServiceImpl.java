package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.toolkit.RandomGenerator;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Override
    public void saveGroup(String groupName) {
        String gid;
        do{
            gid = RandomGenerator.generateRandom();
        } while (!hasGid(gid));//gid的重复性检查
        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .sortOrder(0)   //sortOrder的功能是实现自定义拖拽，但是这里还体现不出来
                .name(groupName)
                .username(UserContext.getUsername())
                .build();//链式调用的方式创建对象
        baseMapper.insert(groupDO);
    }
    private boolean hasGid(String gid){
        LambdaQueryWrapper<GroupDO> query = Wrappers.lambdaQuery(GroupDO.class) //MyBatis-Plus 的 Lambda 表达式
                .eq(GroupDO::getGid,gid)
                .eq(GroupDO::getUsername,UserContext.getUsername());
        GroupDO hasGroupFlag = baseMapper.selectOne(query);
        return hasGroupFlag == null;
    }

    @Override
    public List<ShortLinkGroupRespDTO> listGroup(){
        LambdaQueryWrapper<GroupDO> query = Wrappers.lambdaQuery(GroupDO.class) //定义查询条件
                .eq(GroupDO::getDelFlag,0)
                .eq(GroupDO::getUsername,UserContext.getUsername())
                .orderByDesc(GroupDO::getSortOrder)
                .orderByDesc(GroupDO::getUpdateTime); //指定排序规则，首先是序号，其次是更新时间
        List<GroupDO> list = baseMapper.selectList(query);  //使用MyBatis-Plus的baseMapper执行查询，将结果存储在List<GroupDO>中
        return BeanUtil.copyToList(list,ShortLinkGroupRespDTO.class);
        //使用BeanUtil工具类将GroupDO实体列表转换为ShortLinkGroupRespDTO响应对象列表
    }

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam){
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername,UserContext.getUsername())
                .eq(GroupDO::getGid,requestParam.getGid())
                .eq(GroupDO::getDelFlag,0);
        GroupDO groupDO = new GroupDO();
        groupDO.setName(requestParam.getName());
        baseMapper.update(groupDO,updateWrapper);
        //调用 MyBatis-Plus 的 update 方法执行更新操作
        //groupDO 包含要更新的字段值,updateWrapper 包含更新条件（WHERE 子句）
        //最终生成的SQL类似于：UPDATE group SET name = ? WHERE username = ? AND gid = ? AND del_flag = 0
    }

    @Override
    public void deleteGroup(String gid){
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername,UserContext.getUsername())
                .eq(GroupDO::getGid,gid)
                .eq(GroupDO::getDelFlag,0);
        GroupDO groupDO = new GroupDO();
        groupDO.setDelFlag(1);
        baseMapper.update(groupDO,updateWrapper);
    }
}
