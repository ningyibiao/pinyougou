package com.pinyougou.manage.controller;

        import com.alibaba.dubbo.config.annotation.Reference;
        import com.alibaba.fastjson.JSON;
        import com.pinyougou.pojo.TbGoods;
        import com.pinyougou.pojo.TbItem;
        import com.pinyougou.search.service.ItemSearchService;
        import com.pinyougou.sellergoods.service.GoodsService;
        import com.pinyougou.vo.Goods;
        import com.pinyougou.vo.PageResult;
        import com.pinyougou.vo.Result;
        import org.apache.activemq.command.ActiveMQQueue;
        import org.apache.activemq.command.ActiveMQTopic;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.jms.core.JmsTemplate;
        import org.springframework.jms.core.MessageCreator;
        import org.springframework.security.core.context.SecurityContextHolder;
        import org.springframework.web.bind.annotation.*;


        import javax.jms.*;
        import java.util.List;

@RequestMapping("/goods")
@RestController
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    @Reference
    private ItemSearchService itemSearchService;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ActiveMQQueue itemSolrQueue;

    @Autowired
    private ActiveMQQueue itemSolrDeleteQueue;

    @Autowired
    private ActiveMQTopic itemTopic;

    @Autowired
    private ActiveMQTopic itemDeleteTopic;

    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        return goodsService.findPage(page, rows);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Goods goods) {
        try {
            //获取当前登录的用户(商家)设置商家信息
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goods.getGoods().setSellerId(sellerId);
            //未审核
            goods.getGoods().setAuditStatus("0");
            goodsService.addGoods(goods);
            return Result.ok("增加成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("增加失败");
    }

    @GetMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findGoodsById(id);
    }

    @PostMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            //校验商家
            TbGoods oldGoods = goodsService.findOne(goods.getGoods().getId());
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!sellerId.equals(oldGoods.getSellerId()) || !sellerId.equals(goods.getGoods().getSellerId())) {
                return Result.fail("操作非法");
            }

            goodsService.updateGoods(goods);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.deleteGoodsByIds(ids);
            //删除solr中对应商品索引数据
            sendMQMsg(itemSolrDeleteQueue, ids);
            //发送商品删除的订阅消息
            sendMQMsg(itemDeleteTopic,ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    private void sendMQMsg(Destination destination, Long[] ids) {
        try {
            jmsTemplate.send(destination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 分页查询列表
     *
     * @param goods 查询条件
     * @param page  页号
     * @param rows  每页大小
     * @return
     */


    @PostMapping("/search")
    public PageResult search(@RequestBody TbGoods goods,
                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        //只能查询当前商家自己的商品
//        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
//        goods.setSellerId(sellerId);
        return goodsService.search(page, rows, goods);
    }

    //更新商品状态
    @GetMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);
            System.out.println("status : --- " + status);
            if ("2".equals(status)) {
                //如果审核通过则需要更新solr索引数据
                //查询到需要更新的商品列表
                List<TbItem> itemList = goodsService.findItemListByGoodsIdsAndStatus(ids, "1");
                itemSearchService.importItemList(itemList);

                jmsTemplate.send(itemSolrQueue, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage();
                        textMessage.setText(JSON.toJSONString(itemList));
                        return textMessage;
                    }
                });
                //发送商品审核通过的订阅消息
                sendMQMsg(itemTopic,ids);
            }
            return Result.ok("更新成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("更新失败");
    }

}

