package cn.itcast.demo;


import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
* 消息生产者
* @author Administrator
*/
@RestController
public class QueueController {

	@Autowired
	private JmsMessagingTemplate jmsMessagingTemplate;
	
	@RequestMapping("/send")
	public void send(String text) {
		//在springBoot中生产者的参数可以直接转换发送
		jmsMessagingTemplate.convertAndSend("itcast", text);
	}
	
	@RequestMapping("/sendmap")
	public void sendMap() {
		Map<String, String> map=new HashMap<>();
		map.put("mobile", "13527574169");
		map.put("parm", "打的");
		jmsMessagingTemplate.convertAndSend("itcast_map", map);
	}
	
	@RequestMapping("/sendsms")
	public void sendSms(){
	Map map=new HashMap<>();
	map.put("mobile", "15123371567");
	map.put("template_code", "SMS_147201957");
	map.put("sign_name", "小蜜蜂");
	map.put("param", "{\"code\":\"102931\"}");
	jmsMessagingTemplate.convertAndSend("sms",map);
	}
}
