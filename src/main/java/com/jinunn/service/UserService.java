package com.jinunn.service;

/**
 * @author jinunn.
 * @date 2020/11/25 21:17
 */
public interface UserService {
    /**
     * 向redis中写入用户访问次数
     * @param userId  用户id
     * @return
     */
    int saveUserCount(Integer userId);

    /**
     *  判断单位时间调用次数
     * @param userId  用户id
     * @return
     */
    boolean getUserCount(Integer userId);
}
