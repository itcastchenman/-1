package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=10000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	@Override
	public Map search(Map searchMap) {
		// TODO Auto-generated method stub
		Map map=new HashMap();
	
		//1.按关键字查询（高亮显示）
		Map searchList = searchList(searchMap);
		map.putAll(searchList);
		
		//2.根据关键字查询商品分类
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		
		//3.查询品牌和规格列表
		
		String category = (String) searchMap.get("category");
		if (!category.equals("")) {
			map.putAll(searchBrandAndSpecList(category));
		}else {
			if (categoryList.size()>0) {
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}
		
		return map;
	}
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	* 查询品牌和规格列表
	* @param category 分类名称
	* @return
	*/

	public Map searchBrandAndSpecList(String category) {
		Map map=new HashMap();
		//因缓存itemCat里存有map（category，typeId）集合
		Long typeId =(Long) redisTemplate.boundHashOps("itemCat").get(category);//获取模板ID
		if (typeId!=null) {
			//根据模板 ID 查询品牌列表
			List brandList=(List)redisTemplate.boundHashOps("brandList").get(typeId);
			
			map.put("brandList", brandList);//返回值添加到品牌列表
			//根据模板 ID 查询规格列表
			List specList=(List) redisTemplate.boundHashOps("specList").get(typeId);
			
			map.put("specList", specList);
		}
		
		return map;
	}
	
	//根据关键字查询商品分类
	private List searchCategoryList(Map searchMap) {
		List<String> list=new ArrayList<>();
		
		Query query=new SimpleQuery();
		//安关键字查询
		Criteria criteria=null;
		if (searchMap.get("keywords")!=null &&!"".equals(searchMap.get("keywords"))) {
			criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		}else {
			criteria=new Criteria().expression("*:*");//查询所有
		}
		query.addCriteria(criteria);
		
		//设置分组选项
		GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		//得到分组页		
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		
		//根据列得到分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		
		//根据结果集得到入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		
		//得到分组入口集合
		List<GroupEntry<TbItem>> content = groupEntries.getContent();
		for (GroupEntry<TbItem> entry : content) {
			//将分组结果的名称封装到返回值中
			list.add(entry.getGroupValue());
		}
		
		
		return list;
	}
	
	/**
	* 根据关键字搜索列表及高亮
	* @param keywords
	* @return
	*/
	private Map searchList(Map searchMap) {
		Map map=new HashMap();
		
		//空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		//普通条件查询
		/*Query query=new SimpleQuery("*:*");
		Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);*/
		
		//高亮显示设置
		HighlightQuery query=new SimpleHighlightQuery();
		HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮的域
		highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
		highlightOptions.setSimplePostfix("</em>");//高亮后缀
		query.setHighlightOptions(highlightOptions);//设置高亮选项
		
		//1.1按照关键字查询
		Criteria criteria=null;
		if (searchMap.get("keywords")!=null &&!"".equals(searchMap.get("keywords"))) {
			criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
		}else {
			criteria=new Criteria().expression("*:*");//查询所有
		}
		query.addCriteria(criteria);
		
		//1.2按商品分类过滤
		if (!"".equals(searchMap.get("category"))) {//如果用户选择了分类
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//1.3按商品品牌过滤
		if (!"".equals(searchMap.get("brand"))) {//如果用户选择了品牌
			FilterQuery filterQuery=new SimpleFilterQuery();
			Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//1.4按商品规格过滤
		if (searchMap.get("spec")!=null) {
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for (String key : specMap.keySet()) {
				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_spec_"+key).is(specMap.get(key));
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		
		
		//1.5按商品价格过滤
		String priceStr = (String) searchMap.get("price");
		if (!"".equals(priceStr)) {
			String[] price = priceStr.split("-");
			if (!price[0].equals("0")) {//如果区间起点不等于 0
				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(price[0]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
			if (!price[1].equals("*")) {//如果区间终点不等于 *
				FilterQuery filterQuery=new SimpleFilterQuery();
				Criteria filterCriteria=new Criteria("item_price").lessThanEqual(price[1]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		
		//1.6分页查询
		Integer pageNo = (Integer) searchMap.get("pageNo");//当前页面
		Integer pageSize = (Integer) searchMap.get("pageSize");//每页显示条数
		if (pageNo==null) {
			pageNo=1;//默认第一页
		}
		if (pageSize==null) {
			pageSize=20;//默认20
		}
		
		query.setOffset((pageNo-1)*pageSize);//设置从第几条记录开始查询
		query.setRows(pageSize);//设置每页显示条数
		
		//1.7排序
		String sortValue = (String) searchMap.get("sort");//ASC  DESC
		String sortField = (String) searchMap.get("sortField");//排序字段
		if (sortValue!=null&&!"".equals(sortValue)) {
			if (sortValue.equals("ASC")) {//升序
				Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			if (sortValue.equals("DESC")) {//降序
				Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
		}
		
		//***********  获取高亮结果集  ***************
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		
		List<HighlightEntry<TbItem>> highlightlist= page.getHighlighted();
		//循环高亮入口集合
		for (HighlightEntry<TbItem> highlightEntry : highlightlist) {
			TbItem item = highlightEntry.getEntity();//获得原实体
			if (highlightEntry.getHighlights().size()>0&&highlightEntry.getHighlights().get(0).getSnipplets().size()>0) {
				item.setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));//设置高亮的结果
			}
		}
		map.put("rows", page.getContent());
		map.put("totalPages", page.getTotalPages());//返回总页数
		map.put("total", page.getTotalElements());//返回总记录数
		return map;
		
	}
	
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
		
	}

	/**
	* 删除数据
	* @param ids
	*/
	@Override
	public void deleteByGoodsIds(List goodsIdList) {
		// TODO Auto-generated method stub
		
		System.out.println("删除商品 ID"+goodsIdList);
		
		Query query=new SimpleQuery();
		Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
	};

}
