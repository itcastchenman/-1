package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;
import util.CookieUtil;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins="http://localhost:9105",allowCredentials="true")
public class CartController {
	@Reference(timeout=60000)
	private CartService cartService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private HttpServletResponse response;
	
	/**
	* 购物车列表
	* @param request
	* @return
	*/
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		
		//得到登陆人账号,判断当前是否有人登陆
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		
		//通过工具类从cookie中获取订单项
		String cartListString = util.CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		
		if (cartListString==null||cartListString.equals("")) {
			cartListString="[]";
		}
		
		List<Cart> cartList_cookie = JSON.parseArray(cartListString,Cart.class);
		
		if (username.equals("anonymousUser")) {//判断是否登录
			//没有登录，读取本地购物车//
			return cartList_cookie;
			
		}else {//如果已登录
			//从redis 中提取

			List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
			
			if (cartList_cookie.size()>0) {//如果本地存在购物车

				//合并购物车
				cartList_redis=cartService.mergeCartList(cartList_redis, cartList_cookie);
				
				//清除本地 cookie 的数据
				util.CookieUtil.deleteCookie(request, response, "cartList");
				
				//将合并后的数据存入 redis
				cartService.saveCartListToRedis(username, cartList_redis);
				
			}
			return cartList_redis;
		}
		
		
		
	}
	
	/**
	* 添加商品到购物车
	* @param request
	* @param response
	* @param itemId
	* @param num
	* @return
	*/
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId,Integer num) {
		//跨域请求支持
		//response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
		//response.setHeader("Access-Control-Allow-Credentials", "true");
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		
		System.out.println("当前登录用户："+username);

		try {
			List<Cart> cartList = findCartList();//获取购物车列表
			
			cartList= cartService.addGoodsToCartList(cartList, itemId, num);
			if (username.equals("anonymousUser")) {//没有登录保存到 cookie
				
				//限制cookie保存时间
				util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600*24, "utf-8");
				
				System.out.println("向 cookie 存入数据");
			}else {//如果是已登录，保存到 redis
				
				cartService.saveCartListToRedis(username, cartList);
				
			}
			
			return new Result(true, "添加成功");
			
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			return new Result(false, "添加失败");
		}
		
		
	}
	
	
}
