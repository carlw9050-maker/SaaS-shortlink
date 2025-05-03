package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.biz.user.UserContext;
import com.nageoffer.shortlink.admin.common.convention.result.Result;
import com.nageoffer.shortlink.admin.dao.entity.GroupDO;
import com.nageoffer.shortlink.admin.dao.mapper.GroupMapper;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupSortedReqDTO;
import com.nageoffer.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.nageoffer.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;
import com.nageoffer.shortlink.admin.remote.dto.ShortLinkRemoteService;
import com.nageoffer.shortlink.admin.service.GroupService;
import com.nageoffer.shortlink.admin.toolkit.RandomGenerator;
import com.nageoffer.shortlink.admin.remote.dto.resp.ShortLinkCountQueryRespDTO;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 短链接分组接口实现层
 */
@Slf4j
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    /**
     * 后续重构为SpringCloud Feign调用
     */
    ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {};

    @Override
    public void saveGroup(String groupName){
        saveGroup(UserContext.getUsername(),groupName);
    }

    @Override
    public void saveGroup(String username,String groupName) {
        String gid;
        do{
            gid = RandomGenerator.generateRandom();
        } while (!hasGid(username,gid));//gid的重复性检查
        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .sortOrder(0)   //sortOrder的功能是实现自定义拖拽，但是这里还体现不出来
                .name(groupName)
                .username(UserContext.getUsername())
                .build();//链式调用的方式创建对象
        baseMapper.insert(groupDO);
    }
    private boolean hasGid(String username,String gid){
        LambdaQueryWrapper<GroupDO> query = Wrappers.lambdaQuery(GroupDO.class) //MyBatis-Plus 的 Lambda 表达式
                .eq(GroupDO::getGid,gid)
                .eq(GroupDO::getUsername,Optional.ofNullable(username).orElse(UserContext.getUsername()));
        GroupDO hasGroupFlag = baseMapper.selectOne(query);
        return hasGroupFlag == null;
    }

    @Override
    public List<ShortLinkGroupRespDTO> listGroup(){
        LambdaQueryWrapper<GroupDO> query = Wrappers.lambdaQuery(GroupDO.class) //定义查询条件
                .eq(GroupDO::getDelFlag,0)
                .orderByDesc(GroupDO::getSortOrder)  //指定查询结果的降序排序规则
                .orderByDesc(GroupDO::getUpdateTime); //指定排序规则，首先是序号，其次是更新时间
        List<GroupDO> groupDOList = baseMapper.selectList(query);  //使用MyBatis-Plus的baseMapper执行查询获取分组，将结果存储在List<GroupDO>中
        Result<List<ShortLinkCountQueryRespDTO>> result = shortLinkRemoteService
                .listGroupShortLinkCount(groupDOList.stream().map(GroupDO::getGid).toList());
//        总的作用是提取所有分组gid；调用远程服务获取每个分组的短链接数量
    //        groupDOList.stream()，将groupDOList（List<GroupDO>）转换为Java 8 Stream流，以便进行链式操作
    //        使用map操作将每个GroupDO对象转换为其gid（分组ID），GroupDO::getGid是方法引用，等同于group -> group.getGid()，其中group是对象
    //        在Java Stream API中，map操作是一种元素转换操作，经过map转换后，流中的元素从GroupDO对象变成了字符串类型的gid：
    //        将Stream中的gid收集到一个List<String>中，例如：输入是[GroupDO{gid=1}, GroupDO{gid=2}]，输出是["1", "2"]
        List<ShortLinkGroupRespDTO> shortLinkGroupRespDTOList = BeanUtil.copyToList(groupDOList,ShortLinkGroupRespDTO.class);
//        将DO列表转换为DTO列表
        shortLinkGroupRespDTOList.forEach(each -> {
            Optional<ShortLinkCountQueryRespDTO> first = result.getData().stream()
                    .filter(item -> Objects.equals(item.getGid(),each.getGid()))
                    .findFirst();
            first.ifPresent(item -> each.setShortLinkCount(first.get().getShortLinkCount()));
        });
//        shortLinkGroupRespDTOList.forEach(each -> { ... });
    //        对shortLinkGroupRespDTOList（分组响应DTO列表）中的每个元素（each）执行操作，each代表当前正在处理的单个ShortLinkGroupRespDTO对象
    //    Optional的使用：安全处理可能不存在计数项的情况。
//        .stream()：转换为流以便进行链式操作
//        .filter(...)：筛选出与当前分组gid相同的计数项，Objects.equals安全比较两个gid（避免NPE）
//        .findFirst()：返回第一个匹配项的Optional（可能为空）
//        ifPresent：当Optional不为空时执行操作
//        item：匹配到的ShortLinkCountQueryRespDTO对象；each.setShortLinkCount(...)：将计数设置到当前分组DTO中
        return shortLinkGroupRespDTOList;
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

    @Override
    public void sortGroup(List<ShortLinkGroupSortedReqDTO> requestParam){
        requestParam.forEach(each -> {
            //使用 Java 8 的 forEach 方法遍历传入的 requestParam 列表，对列表中的每个元素（命名为 each）执行后续操作。
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(each.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername,UserContext.getUsername())
                    .eq(GroupDO::getGid,each.getGid())
                    .eq(GroupDO::getDelFlag,0);
            baseMapper.update(groupDO,updateWrapper);
            //接收一个包含gid和sortOrder的请求参数列表
            //为列表里的每个参数创建更新对象和更新条件
            //执行数据库更新，只更新排序字段。是典型的批量更新操作。
        });
    }
}
