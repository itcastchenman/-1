package cn.itcast.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.itcast.pojo.TbItem;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext-solr.xml")
public class testTemplate {
	@Autowired
	private SolrTemplate solrTemplate;

	
	//增加修改
	@Test
	public void testAdd() {
		TbItem item = new TbItem();
		item.setId(1L);
		item.setTitle("华为METE10");
		item.setPrice(new BigDecimal(3000.01));
		item.setGoodsId(10L);
		item.setCategory("手机");
		item.setBrand("华为");
		item.setSeller("华为旗舰店");
		solrTemplate.saveBean(item);
		solrTemplate.commit();
		
	}
	
	//根据id查询
	@Test
	public void findById() {
		TbItem item = solrTemplate.getById(1L, TbItem.class);
		System.out.println(item.getTitle());
	}
	
	//根据id删除
	@Test
	public void deleById() {
		solrTemplate.deleteById("1");
		solrTemplate.commit();
	}
	
	//批量插入数据
	@Test
	public void listAdd() {
		List list=new ArrayList();
		for (int i = 0; i < 100; i++) {
			TbItem item = new TbItem();
			item.setId(1L+i);
			item.setTitle("华为METE10"+i);
			item.setPrice(new BigDecimal(3000.01+i));
			item.setGoodsId(10L);
			item.setCategory("手机");
			item.setBrand("华为"+i);
			item.setSeller("华为旗舰店");
			list.add(item);
		}
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}
	
	
	//分页查询
	@Test
	public void testPageQuery() {
		
		Query query = new SimpleQuery("*:*");
		
		query.setOffset(20);//从索引20开始显示
		query.setRows(20);//每页记录数
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query , TbItem.class);
		List<TbItem> list = page.getContent();
		for (TbItem tbItem : list) {
			System.out.println(tbItem.getTitle()+" "+tbItem.getPrice()+" "+tbItem.getBrand());
		}
		System.out.println("总记录数"+page.getTotalElements());
		System.out.println("总页数"+page.getTotalPages());
	}
	
	//条件查询
	@Test
	public void testPageQueryMutl() {
		Query query = new SimpleQuery("*:*");
		Criteria criteria=new Criteria("item_category").contains("手机");
		criteria=criteria.and("item_brand").contains("2");
		query.addCriteria(criteria);
		
		query.setOffset(0);//从索引0开始显示
		query.setRows(20);//每页记录数
		ScoredPage<TbItem> page = solrTemplate.queryForPage(query , TbItem.class);
		List<TbItem> list = page.getContent();
		for (TbItem tbItem : list) {
			System.out.println(tbItem.getTitle()+" "+tbItem.getPrice()+" "+tbItem.getBrand());
		}
		System.out.println("总记录数"+page.getTotalElements());
		System.out.println("总页数"+page.getTotalPages());
	}
	
	//删除全部
	@Test
	public void deleAll() {
		Query query=new SimpleQuery("*:*");
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}
