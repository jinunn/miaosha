package com.jinunn.service;

/**
 * @author jinunn.
 * @date 2020/11/25 11:58
 */
public interface OrderService {
    /**
     * 处理秒杀下单方法
     * @param id
     * @return
     */
    int kill(Integer id);

    /**
     * 生成md5签名的方法
     * @param id
     * @param userid
     * @return
     */
    String getMd5(Integer id, Integer userid);

    /**
     * 处理秒杀的下单方法
     * @param id  商品id
     * @param userid 用户id
     * @param md5  加盐
     * @return
     */
    int kill(Integer id, Integer userid, String md5);
}
