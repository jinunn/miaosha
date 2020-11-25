package com.jinunn.service.impl;

import com.jinunn.dao.OrederDao;
import com.jinunn.dao.StockDao;
import com.jinunn.dao.UserDao;
import com.jinunn.domian.Oreder;
import com.jinunn.domian.Stock;
import com.jinunn.domian.User;
import com.jinunn.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author jinunn.
 * @date 2020/11/25 12:01
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private StockDao stockDao;

    @Autowired
    private OrederDao orderDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String getMd5(Integer id, Integer userid) {
        //验证userid 存在用户信息
        User user = userDao.findByid(userid);
        if (user==null){ throw  new RuntimeException("用户信息不存在"); }

        //验证id  存在商品id
        Stock stock = stockDao.cherkStock(id);
        if (stock==null){throw  new RuntimeException("商品信息不合法");}

        //生成Hashkey
        String hashkey= "KEY_"+userid+"_"+id;
        //生成md5,!Q*js#是一个随机盐 随机生成
        String key = DigestUtils.md5DigestAsHex((userid + id + "!Q*js#").getBytes());

        //key 为用户id+商品id  value 为md5随机盐 60为存储时间 后面是秒
        stringRedisTemplate.opsForValue().set(hashkey,key,60*2, TimeUnit.SECONDS);
        return key;
    }

    @Override
    public int kill(Integer id, Integer userid, String md5) {
        //校验redis中秒杀商品是否超时
        //if (!stringRedisTemplate.hasKey("kill"+id)) {
        //    throw new RuntimeException("抢购活动结束~~");
        //}

        //验证签名
        String hashkey= "KEY_"+userid+"_"+id;
        String s = stringRedisTemplate.opsForValue().get(hashkey);
        if (s==null){ throw  new RuntimeException("没有携带验证签名，请求不合法！");}
        if (!s.equals(md5)) { throw  new RuntimeException("当前请求不合法，请稍后再试！"); }

        //校验库存
        Stock stock = cherkStock(id);
        //扣除库存
        upsataSale(stock);
        //创建订单
        return createOrder(stock);
    }

    //效验库存
    private Stock cherkStock(Integer id) {
        //根据商品id校验库存
        Stock stock = stockDao.cherkStock(id);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    //扣除库存
    private void upsataSale(Stock stock) {
        //在sql层面完成销量的+1 和版本号的+1 ，并且根据商品id和版本号同时查询更新的商品。
        int upsataSale = stockDao.upsataSale(stock);
        if (upsataSale==0){
            throw new RuntimeException("抢购失败，请重试！");
        }
    }

    //创建订单
    private Integer createOrder(Stock stock) {
        Oreder oreder = new Oreder();
        oreder.setSid(stock.getId());
        oreder.setName(stock.getName());
        oreder.setCreateTime(new Date());
        orderDao.createOrder(oreder);
        return oreder.getId();
    }



    @Override
    public int kill(Integer id) {
        //校验redis中秒杀商品是否超时
        if (!stringRedisTemplate.hasKey("kill"+id)) {
            throw new RuntimeException("抢购活动结束~~");
        }

        //校验库存
        Stock stock = cherkStock(id);
        //扣除库存
        upsataSale(stock);
        //创建订单
        return createOrder(stock);
    }
}
