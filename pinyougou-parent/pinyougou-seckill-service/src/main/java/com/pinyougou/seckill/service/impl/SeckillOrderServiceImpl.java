package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	@Autowired
	private IdWorker idWorker;
	

	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
		
		@Autowired
		private TbSeckillGoodsMapper seckillGoodsMapper;
		
		//提交订单
		/**
		 * //提交订单<将订单先存入缓存>
		 * */
		@Override
		public void submitOrder(Long seckillId, String userId) {
			
			//从缓存中提出秒杀商品信息
			TbSeckillGoods seckillGoods= (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
			
			if (seckillGoods==null) {
				throw new RuntimeException("商品不存在");
			}
			if (seckillGoods.getStockCount()<=0) {
				throw new RuntimeException("商品已抢购一空");
			}
			//扣除缓存商品库存数量
			seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
			//再次存入缓存(此处的seckillId和seckillGoods.getId（）是一样的)
			redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
			
			//如果刚好这次商品是最后一件，说明库存已没有了
			if (seckillGoods.getStockCount()==0) {//商品已抢购一空
				seckillGoodsMapper.updateByPrimaryKey(seckillGoods);//同步到数据库
				//同时删除缓存中的该商品数据
				redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
				
			}
			
			/**
			 * //保存订单<将订单先存入缓存>
			 * */
			TbSeckillOrder seckillOrder = new TbSeckillOrder();
			
			long orderId = idWorker.nextId();
			
			seckillOrder.setId(orderId);
			
			seckillOrder.setCreateTime(new Date());
			
			seckillOrder.setMoney(seckillGoods.getCostPrice());//秒杀价格
			
			seckillOrder.setSeckillId(seckillId);
			
			seckillOrder.setUserId(userId);//设置用户ID
			
			seckillOrder.setSellerId(seckillGoods.getSellerId());
			
			seckillOrder.setStatus("0");//订单状态
			
			//将订单存入缓存（在没有完成支付前所有的订单都将先存入换）
			redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
			
			
			
		}

		
		//在秒杀缓存中查询订单
		@Override
		public TbSeckillOrder searchOrderFromRedisByUserId(String userId) {
			
			return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		}

		
		
		//保存订单到数据库
		@Override
		public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
			
			System.out.println("saveOrderFromRedisToDb:"+userId);
			
			//提取缓存中的订单
			TbSeckillOrder seckillOrder= (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
			
			//根据用户 ID 查询日志
			if (seckillOrder==null) {
				throw new RuntimeException("订单不存在");
			}
			//如果与传递过来的订单号不符
			if (seckillOrder.getId().longValue()!= orderId.longValue()) {
				throw new RuntimeException("订单不相符");
			}
			
			seckillOrder.setTransactionId(transactionId);
			seckillOrder.setPayTime(new Date());
			seckillOrder.setStatus("1");
			
			//保存订单到数据库
			seckillOrderMapper.insert(seckillOrder);//保存到数据库
			
			//从缓存中删除该订单
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
		}

		
		//从缓存中删除订单
		@Override
		public void deleteOrderFromRedis(String userId, Long orderId) {
			
			TbSeckillOrder seckillOrder= (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
			
			if (seckillOrder.getId().longValue()==orderId.longValue()&&seckillOrder!=null) {
				
				//从缓存中删除订单
				redisTemplate.boundHashOps("seckillOrder").delete(userId);
				
				//恢复库存
				//1.从缓存中提取秒杀商品
				TbSeckillGoods seckillGoods= (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
				
				if (seckillGoods!=null) {//商品存在
					seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
					
					//再次将秒杀商品存入缓存中
					redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
				}
				
				
				
				
			}
			
		}
		
		
		
	
}
