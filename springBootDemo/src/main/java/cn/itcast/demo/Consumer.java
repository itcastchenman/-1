package cn.itcast.demo;

import java.util.Map;

import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class Consumer {
	
	@JmsListener(destination="itcast")//指明activeMQ接收名称
	
	public void readMessage(String text) {
		
		System.out.println("接受到的消息"+text);
	}
	
	@JmsListener(destination="itcast_map")
	
	public void readMap(Map map) {
		System.out.println(map);
	}

}
