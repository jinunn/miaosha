package com.jinunn.service.impl;

import com.jinunn.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * @author jinunn.
 * @date 2020/11/25 21:20
 */
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public int saveUserCount(Integer userId) {
        //1、根据不同用户id生成调用次数
        String limitKey = "LIMIT"+"_"+userId;
        //2、获取Redis中执行key的调用次数
        String limitNum = redisTemplate.opsForValue().get(limitKey);
        //-1 不管是第一次还是第二次访问，都会变为0，添加到redis中
        int limit = -1;
        if (limitNum ==null){
            //第一次调用放入redis中设置为0
            redisTemplate.opsForValue().set(limitKey,"0",60*2, TimeUnit.SECONDS);
        }else {
            //不是第一次调用每次+1
            limit = Integer.parseInt(limitNum)+1;
            redisTemplate.opsForValue().set(limitKey,String.valueOf(limit),60*2,TimeUnit.SECONDS);
        }
        //返回调用次数
        return limit;
    }

    @Override
    public boolean getUserCount(Integer userId) {
        String limitKey = "LIMIT"+"_"+userId;
        //1、根据用户调用次数的key获取redis中调用次数
        String limitNum = redisTemplate.opsForValue().get(limitKey);
        if (limitKey==null){
            //为空直接抛弃说明key出现异常  throw new RuntimeException("该用户没有访问申请验证记录，出现异常");
            log.info("该用户没有访问申请验证记录，出现异常");
            return true;
        }
        //false代表没有超过，ture代码超过
        return Integer.parseInt(limitNum)>10;
    }
}
