package com.pinyougou.page.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCatExample;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;

@Service
public class ItemPageServiceImpl implements ItemPageService {
	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private FreeMarkerConfig freeMarkerConfig;
	
	@Value("${pagedir}")
	private String pagedir;
	
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbItemMapper itemMapper;
	
	/**
	 * 生产商品详细页
	 * */
	@Override
	public boolean genItemHtml(Long goodsId) {
		// TODO Auto-generated method stub
		//创建配置类
		Configuration configuration=freeMarkerConfig.getConfiguration();
		try {
			//加载模板
			Template template = configuration.getTemplate("item.ftl");
			//加载模板
			Map dataModel=new HashMap<>();
			
			//1.加载商品表数据
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			
			dataModel.put("goods", goods);
			
			//2.加载商品扩展表数据
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			
			dataModel.put("goodsDesc", goodsDesc);
			
			//3.加载商品分类
			String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			
			dataModel.put("itemCat1", itemCat1);
			dataModel.put("itemCat2", itemCat2);
			dataModel.put("itemCat3", itemCat3);
			
			//4.加载sku列表
			TbItemExample example=new TbItemExample();
			Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goodsId);//指定spuId
			criteria.andStatusEqualTo("1");//商品状态为1
			example.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默认
			List<TbItem> itemList = itemMapper.selectByExample(example);
			
			dataModel.put("itemList", itemList);
			
			Writer out=new FileWriter(pagedir+goodsId+".html");
			
			//输出
			template.process(dataModel, out);
			//关闭writer对象
			out.close();
			return true;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return false;
	}

	@Override
	public boolean deleteItemHtml(Long[] goodsIds) {
		// TODO Auto-generated method stub
		
		try {
			for(Long goodsId:goodsIds){
				new File(pagedir+goodsId+".html").delete();
			}
			return true;
			} catch (Exception e) {
				e.printStackTrace();
			return false;
			}

	}

}
