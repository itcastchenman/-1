package cn.itcast.demo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class test {
	public static void main(String[] args) throws IOException, TemplateException {
		//创建配置类
		Configuration configuration=new Configuration(Configuration.getVersion());
		//设置模板所在目录
		configuration.setDirectoryForTemplateLoading(new File("E:\\eiclipse\\gouwu\\freemarkerDemo\\src\\main\\resources"));
		//设置字符集
		configuration.setDefaultEncoding("utf-8");
		//加载模板
		Template template = configuration.getTemplate("test.ftl");
		//创建数据模型
		Map map=new HashMap();
		map.put("name", "张三");
		map.put("message", "欢迎来到神奇的品优购世界！");
		map.put("success", false);
		map.put("today", new Date());
		map.put("point", 12345687);
		
		List goodsList=new ArrayList();
		Map goods1=new HashMap();
		
		goods1.put("name", "苹果");
		goods1.put("price", 5.8);
		Map goods2=new HashMap();
		goods2.put("name", "香蕉");
		goods2.put("price", 2.5);
		Map goods3=new HashMap();
		goods3.put("name", "橘子");
		goods3.put("price", 3.2);
		goodsList.add(goods1);
		goodsList.add(goods2);
		goodsList.add(goods3);
		map.put("goodsList", goodsList);
		
		//创建writer对象
		Writer out=new FileWriter(new File("E:\\test.html"));
		//输出
		template.process(map, out);
		//关闭writer对象
		out.close();
	}
}
