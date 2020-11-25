package com.jinunn.dao;

import com.jinunn.domian.Stock;
import org.springframework.stereotype.Repository;

/**
 * @author jinunn.
 * @date 2020/11/25 12:03
 */
@Repository
public interface StockDao {

    /**
     * 根据商品id查询库存信息的方法
     * @param id
     * @return
     */
    Stock cherkStock(Integer id);

    /**
     * 根据商品id扣除库存
     * @param stock
     */
    int upsataSale(Stock stock);
}
