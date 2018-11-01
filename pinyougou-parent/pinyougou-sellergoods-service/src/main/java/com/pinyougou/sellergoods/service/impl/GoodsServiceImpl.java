package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.plaf.TabbedPaneUI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.annotation.Transactional;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemMapper tbItemMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");//未审核
		goodsMapper.insert(goods.getGoods());//插入商品表
		//goods表的id和goodsDesc表的goodsId一样
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展数据
		
		addItemList(goods);
	}
	
	@Autowired
	private TbItemCatMapper itemCatMapper ;
	
	@Autowired
	private TbBrandMapper brandMapper;
	
	@Autowired
	private TbSellerMapper sellerMapper;
	public void addItemList(Goods goods) {
		List<TbItem> itemList = goods.getItemList();
		if("1".equals(goods.getGoods().getIsEnableSpec())) {
			for (TbItem tbItem : itemList) {
				//构建标题 SPU名称+规格选项值
				String title = goods.getGoods().getGoodsName();//SPU名称
				Map<String, Object> map= JSON.parseObject(tbItem.getSpec()) ;
				for (String key : map.keySet()) {
					title+=" "+map.get(key);
				}
				tbItem.setTitle(title);
				
				//商品分类
				tbItem.setCategoryid(goods.getGoods().getCategory3Id());//三级分类ID
				tbItem.setCreateTime(new Date());//创建日期
				tbItem.setUpdateTime(new Date());//更新日期
				
				tbItem.setGoodsId(goods.getGoods().getId());//商品Id
				tbItem.setSellerId(goods.getGoods().getSellerId());//商家Id
				
				TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
				tbItem.setCategory(itemCat.getName());//商品分类名称
				
				TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
				tbItem.setBrand(tbBrand.getName());//商品的品牌
				
				TbSeller tbSeller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
				tbItem.setSeller(tbSeller.getNickName());//商家的店铺名称
				
				//图片
				List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(),Map.class);
				if (imageList.size()>0) {
					tbItem.setImage((String)imageList.get(0).get("url"));//存第一张图片
				}
				tbItemMapper.insert(tbItem);
			}
		}else {
				TbItem tbItem = new TbItem();
				tbItem.setTitle(goods.getGoods().getGoodsName());//标题
				tbItem.setPrice(goods.getGoods().getPrice());
				tbItem.setNum(99999);
				tbItem.setStatus("1");
				tbItem.setIsDefault("1");
				tbItem.setSpec("{}");
				//商品分类
				tbItem.setCategoryid(goods.getGoods().getCategory3Id());//三级分类ID
				tbItem.setCreateTime(new Date());//创建日期
				tbItem.setUpdateTime(new Date());//更新日期
				
				tbItem.setGoodsId(goods.getGoods().getId());//商品Id
				tbItem.setSellerId(goods.getGoods().getSellerId());//商家Id
				
				TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
				tbItem.setCategory(itemCat.getName());//商品分类名称
				
				TbBrand tbBrand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
				tbItem.setBrand(tbBrand.getName());//商品的品牌
				
				TbSeller tbSeller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
				tbItem.setSeller(tbSeller.getNickName());//商家的店铺名称
				
				//图片
				List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(),Map.class);
				if (imageList.size()>0) {
					tbItem.setImage((String)imageList.get(0).get("url"));//存第一张图片
				}
				
				tbItemMapper.insert(tbItem);
				
		}
	}
	

	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		goods.getGoods().setAuditStatus("0");//设置未申请状态:如果是经过修改的商品，需要重新设置状态
		goodsMapper.updateByPrimaryKey(goods.getGoods());//保存商品表
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());//保存商品扩展表
		
		TbItemExample example=new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		tbItemMapper.deleteByExample(example);
		addItemList(goods);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(goodsDesc);
		//查询 SKU 商品列表
		TbItemExample example=new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);;//查询条件：商品 ID

		List<TbItem> list = tbItemMapper.selectByExample(example);
		goods.setItemList(list);
		
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
			
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();//非删除状态
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());//商家必须不可以模糊查询
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Override
		public void updateStatus(Long[] ids, String status) {
			// TODO Auto-generated method stub
			for (Long id : ids) {
				TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
				tbGoods.setAuditStatus(status);
				goodsMapper.updateByPrimaryKey(tbGoods);
			}
		}

		@Override
		public void updateIsMarketable(Long[] ids, String status) {
			// TODO Auto-generated method stub
			for (Long id : ids) {
				TbGoods goods = goodsMapper.selectByPrimaryKey(id);
				goods.setIsMarketable(status);
				goodsMapper.updateByPrimaryKey(goods);
			}
		}

		@Override
		public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
			// TODO Auto-generated method stub
			
			TbItemExample example =new TbItemExample();
			com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdIn(Arrays.asList(goodsIds));
			criteria.andStatusEqualTo(status);
			List<TbItem> list = tbItemMapper.selectByExample(example );
			return list;
		}
		
		
	
}
