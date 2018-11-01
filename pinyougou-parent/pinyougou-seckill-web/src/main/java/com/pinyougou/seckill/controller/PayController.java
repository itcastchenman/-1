package com.pinyougou.seckill.controller;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;

import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;

import entity.Result;
import util.IdWorker;

/**
* 支付控制层
* @author Administrator
*
*/
@RestController
@RequestMapping("/pay")
public class PayController {
	
	@Reference(timeout=60000)
	private WeixinPayService weixinPayService;
	
	@Reference(timeout=60000)
	private SeckillOrderService seckillOrderService;
	/**
	* 生成二维码
	* @return
	*/
	@RequestMapping("/createNative")
	public Map createNative() {
		//获取当前用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		//到 redis 查询秒杀订单
		TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(username);
		//判断秒杀订单存在
		if (seckillOrder!=null) {
			long fen=(long)(seckillOrder.getMoney().doubleValue()*100);
			
			return weixinPayService.createNative(seckillOrder.getId()+"",fen +"");
		}else {
			return new HashMap<>();
		}
		
	}
	
	/**
	* 查询支付状态
	* @param out_trade_no
	* @return
	*/
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		
		Result result=null;
		//调用查询接口
		int x=0;
		while (true){//出错
			Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
			
			if (map==null) {
				result= new Result(false, "支付出错");
				break;
			}
			if (map.get("trade_state").equals("SUCCESS")) {//如果成功
				result=new Result(true, "支付成功");
				//修改订单状态
				seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), map.get("transaction_id"));
				
				break;
			}
			
			try {
				Thread.sleep(3000);//间隔三秒
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//针对秒杀二维码超时要特殊处理《超时后用户的订单要删除并回滚缓存库库存》
			x++;
			if (x>=100) {
				result=new Result(false, "二维码超时");
				
				//1.调用微信的关闭订单接口（学员实现）
				Map<String,String> payResult = weixinPayService.closePay(out_trade_no);
				
				if (!"SUCCESS".equals(payResult.get("result_code"))) {//如果返回结果是正常关闭<表示二维码超时异常时系统用户已经支付>
					if ("ORDERPAID".equals(payResult.get("result_code"))) {//表示已经支付
						result=new Result(true, "支付成功");
						//只能保存订单
						seckillOrderService.saveOrderFromRedisToDb(username, Long.valueOf(out_trade_no), map.get("transaction_id"));
					}
				}
				
				if (result.isSuccess()==false) {
					System.out.println("超时，取消订单");
					
					//调用删除订单
					seckillOrderService.deleteOrderFromRedis(username, Long.valueOf(out_trade_no));
				}
				break;
			}
			
		}
		return result;
	}

}

