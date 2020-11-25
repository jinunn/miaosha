package com.jinunn.dao;

import com.jinunn.domian.Oreder;
import org.springframework.stereotype.Repository;

/**
 * @author jinunn.
 * @date 2020/11/25 12:31
 */
@Repository
public interface OrederDao {

    /**
     * 创建订单
     * @param oreder
     */
    void createOrder(Oreder oreder);
}
