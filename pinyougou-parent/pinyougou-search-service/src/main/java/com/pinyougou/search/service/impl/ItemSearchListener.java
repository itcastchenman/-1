package com.pinyougou.search.service.impl;

import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Commit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Component
public class ItemSearchListener implements MessageListener {

	@Autowired
	private ItemSearchService itemSearchService;
	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		System.out.println("监听接收到消息...");
		try {
			TextMessage textMessage=(TextMessage)message;
			String text = textMessage.getText();
			List<TbItem> itemList = JSON.parseArray(text, TbItem.class);
			for (TbItem tbItem : itemList) {
				System.out.println(tbItem.getId()+" "+tbItem.getTitle());
				Map specMap = JSON.parseObject(tbItem.getSpec());//将 spec 字段中的 json字符串转换为 map
				tbItem.setSpecMap(specMap);//给带注解的字段赋值
			}
			itemSearchService.importList(itemList);//导入solr库
			System.out.println("成功导入到索引库");
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
