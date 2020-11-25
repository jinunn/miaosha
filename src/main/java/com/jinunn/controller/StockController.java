package com.jinunn.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.jinunn.service.OrderService;

import com.jinunn.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * @author jinunn.
 * @date 2020/11/25 11:55
 */
@RestController
@RequestMapping("stock")
@Slf4j
public class StockController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    //创建令牌桶实例
    private RateLimiter rateLimiter= RateLimiter.create(20);


    //开发秒杀方法 使用乐观锁防止超卖
    @GetMapping("kill")
    public String kill(Integer id){
        try {
            //根据秒杀商品id去调用秒杀业务
            int orderid = orderService.kill(id);
            return "秒杀成功,订单id为"+String.valueOf(orderid);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    //开发一个秒杀方法，乐观锁防止超卖+令牌桶算法限流
    @GetMapping("killtoken")
    public String killtoken(Integer id){
        //加入令牌桶的限流措施
        if (!rateLimiter.tryAcquire(5,TimeUnit.SECONDS)){
            log.info("抛弃请求：抢购失败，当前秒杀活动过于火爆，请重试！");
            return "抢购失败，当前秒杀活动过于火爆，请重试！";
        }
        try {
            //根据秒杀商品id去调用秒杀业务
            int orderid = orderService.kill(id);
            return "秒杀成功,订单id为"+String.valueOf(orderid);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    //生成MD5值的方法
    @RequestMapping("md5")
    public String getMd5(Integer id,Integer userid){
        String md5;
        try {
            md5=orderService.getMd5(id,userid);
        } catch (Exception e) {
            e.printStackTrace();
            return "获取md5失败："+e.getMessage();
        }
        return "获取md5信息为"+md5;
    }

    //开发一个秒杀方法，乐观锁防止超卖+令牌桶算法限流+md5签名
    @GetMapping("killtokenmd5/{id}/{userid}/{md5}")
    public String killtoken(@PathVariable Integer id,
                            @PathVariable Integer userid,
                            @PathVariable String md5){
        //加入令牌桶的限流措施
        if (!rateLimiter.tryAcquire(5,TimeUnit.SECONDS)){
            log.info("抛弃请求：抢购失败，当前秒杀活动过于火爆，请重试！");
            return "抢购失败，当前秒杀活动过于火爆，请重试！";
        }
        try {
            //根据秒杀商品id去调用秒杀业务
            int orderid = orderService.kill(id,userid,md5);
            return "秒杀成功,订单id为"+String.valueOf(orderid);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    //开发一个秒杀方法，乐观锁防止超卖+令牌桶算法限流+md5签名+单用户访问频率限制
    @GetMapping("killtokenmd5limit/{id}/{userid}/{md5}")
    public String killtokenlimit(@PathVariable Integer id,
                            @PathVariable Integer userid,
                            @PathVariable String md5){
        //加入令牌桶的限流措施
        if (!rateLimiter.tryAcquire(5,TimeUnit.SECONDS)){
            log.info("抛弃请求：抢购失败，当前秒杀活动过于火爆，请重试！");
            return "抢购失败，当前秒杀活动过于火爆，请重试！";
        }
        try {
            //单个用户调用接口的频率限制
            int count = userService.saveUserCount(userid);
            log.info("用户截至该次访问次数为：[{}]",count);
            //经行调用次数的判断
            boolean userCount = userService.getUserCount(userid);
            if (userCount){
                log.info("购买失败！超过访问频率限制");
                return "购买失败！超过访问频率限制";
            }

            //根据秒杀商品id去调用秒杀业务
            int orderid = orderService.kill(id,userid,md5);
            return "秒杀成功,订单id为"+String.valueOf(orderid);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
