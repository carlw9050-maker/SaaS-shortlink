package com.nageoffer.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nageoffer.shortlink.admin.common.convention.exception.ClientException;
import com.nageoffer.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.nageoffer.shortlink.admin.dao.entity.UserDO;
import com.nageoffer.shortlink.admin.dao.mapper.UserMapper;
import com.nageoffer.shortlink.admin.dto.req.UserLoginReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.nageoffer.shortlink.admin.dto.req.UserUpdateDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.nageoffer.shortlink.admin.dto.resp.UserRespDTO;
import com.nageoffer.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.nageoffer.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername,username);
        //Wrappers.lambdaQuery(UserDO.class),创建一个针对UserDO的查询包装器（来自MyBatis-plus）
        //.eq(UserDO::getUsername,username)，添加查询条件，
        UserDO userDO=baseMapper.selectOne(queryWrapper);//selectOne()查询单条记录，返回一个对象
        UserRespDTO result=new UserRespDTO();
        if(userDO!=null){
            BeanUtils.copyProperties(userDO,result);//来自spring的BeanUtils.copyProperties()，将数据库实体的属性值赋给响应DTO
            return result;
        } else {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
    }
    @Override
    public Boolean availableUsername(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }
    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if(!availableUsername(requestParam.getUsername())){
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }
        RLock lock=redissonClient.getLock(LOCK_USER_REGISTER_KEY+requestParam.getUsername());
        //RLock 是 Redisson 提供的分布式锁接口，扩展了 Java 的 Lock 接口，支持分布式场景下的加锁、解锁、超时等操作。
        //redissonClient 是 Redisson (一个基于 Redis 的 Java 客户端)的客户端实例，用于与 Redis 服务器交互
        //getLock(String name) 是 Redisson 提供的方法，用于获取一个分布式锁（RLock 对象）
        //LOCK_USER_REGISTER_KEY + requestParam.getUsername(),这是锁的名称（Key），在 Redis 中唯一标识这把锁
        try{
            if (lock.tryLock()){
                //tryLock() 方法尝试获取锁，如果锁可用则立即返回true，否则返回false,该方法是非阻塞行为：当调用 tryLock() 时，
                // 它会立即返回一个布尔值：如果锁可用（没有被其他线程/服务持有），则获取锁并返回 true；如果锁不可用，则立即返回 false，不会等待
                //lock() 方法是阻塞行为：当调用 lock() 时：如果锁可用，则获取锁并继续执行；如果锁不可用，则当前线程会阻塞，直到锁被释放，没有返回值，
                // 因为它会一直等待直到获取锁，对于同一个用户名的并发请求，第一个请求获取锁，其他请求会排队等待，直到锁释放，这会阻塞其他请求，直到它们能获取锁
                try{
                    int inserted=baseMapper.insert(BeanUtil.toBean(requestParam,UserDO.class));
                    //将 UserRegisterReqDTO 对象转换为 UserDO 对象
                    // 调用 baseMapper.insert 方法将用户数据插入数据库
                    if(inserted<1){
                        throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);}
                        //检查插入操作的影响行数,如果影响行数小于1（表示插入失败），抛出 ClientException 异常
                }catch (DuplicateKeyException ex){
                    throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
                }
                userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());//将数据库的用户名加载到布隆过滤器中
                return;
                //return表明成功时直接退出.运行到这一步,则提前终止register方法的执行,不再运行后面你throw代码行
            }
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }finally {
            lock.unlock();
            //在finally块中释放锁确保锁一定会被释放，避免死锁,即使业务逻辑抛出异常，锁也会被正确释放
        }
    }

    @Override
    public void update(UserUpdateDTO requestParam) {
        //TODO 验证当前用户是否为登陆用户
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername,requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam,UserDO.class),updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam){
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername,requestParam.getUsername())
                .eq(UserDO::getPassword,requestParam.getPassword())
                .eq(UserDO::getDelFlag,0);
        UserDO userDO=baseMapper.selectOne(queryWrapper);
        if(userDO==null){
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        Boolean isLogin=stringRedisTemplate.hasKey("login:"+requestParam.getUsername());
        if(isLogin != null && isLogin){
            //防御性编程：避免因 Redis 异常（返回 null）导致误判。
            throw new ClientException("用户已登录");
        }
        /**
         * Hash
         * Key:login_用户名
         * Value:
         *  key:token标识
         *  Value:JSON 字符串 (用户信息)
         */
        String uuid= UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put("login:"+requestParam.getUsername(),uuid, JSON.toJSONString(userDO));
        //使用redis的Hash结构存储用户信息到Redis(Key为"login:用户名"，field(内部key)为uuid,Hash的value为用户对象的JSON字符串)
        stringRedisTemplate.expire("login:"+requestParam.getUsername(),30L, TimeUnit.DAYS);
        //使用Spring的RedisTemplate操作Redis
        //验证成功则生成UUID作为token,token为key,将用户对象(userDO)转换为JSON字符串后作为值,将用户信息存入Redis
        //key-30分钟有效期,到期后redis将删除该key
        return new UserLoginRespDTO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token){
//        return stringRedisTemplate.opsForHash().get("login:"+username,"token") != null;
        Object remoteToke=stringRedisTemplate.opsForHash().get("login:"+username,token);
        //remoteToke是UserDO 对象的 JSON 字符串
        return remoteToke != null;
    }

    @Override
    public void logout(String username, String token){
        if(checkLogin(username,token)){
            stringRedisTemplate.delete("login:"+username);
            return;
        }
        throw new ClientException("用户token不存在或用户未登陆");
    }
}
