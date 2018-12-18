package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;

import java.util.List;

public interface GoodsService extends BaseService<TbGoods> {

    PageResult search(Integer page, Integer rows, TbGoods goods);

    /**
     * 商品的基本.描述.sku列表信息之后要保存到数据库
     * @param goods 商品信息
     * @return 操作结果
     */
    void addGoods(Goods goods);

    Goods findGoodsById(Long id);

    void updateGoods(Goods goods);

    void updateStatus(Long[] ids, String status);

    void deleteGoodsByIds(Long[] ids);

    /**
     * 根据商品SPU id集合和状态查询这些商品对应的sku商品列表
     * @param ids 商品SPU id集合
     * @param status sku商品状态
     * @return sku商品列表
     */
    List<TbItem> findItemListByGoodsIdsAndStatus(Long[] ids, String status);

    /**
     * 根据商品spu id查询商品基本、描述、sku列表（根据是否默认排序，降序排序），并加载商品1、2、3级商品分类中文名称。
     * @param goodsId 商品spu id
     * @param itemStatus 商品spu 状态
     * @return 商品信息
     */
    Goods findGoodsByIdAndStatus(Long goodsId, String itemStatus);
}