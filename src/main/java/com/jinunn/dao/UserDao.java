package com.jinunn.dao;

import com.jinunn.domian.User;
import org.springframework.stereotype.Repository;

/**
 * @author jinunn.
 * @date 2020/11/25 20:18
 */
@Repository
public interface UserDao {
    /**
     * 查询用户id
     * @param userid
     * @return
     */
    User findByid(Integer userid);
}
