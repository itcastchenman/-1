package cn.itcast.mongoDBDemo;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class TestDemo {
	
	public static void main(String[] args) {
		
		MongoClient client = new MongoClient();//创建连接对象
		
		MongoDatabase database = client.getDatabase("itcastdb");//获取数据库
		
		MongoCollection<Document> collection = database.getCollection("student");//获取集合
		
		FindIterable<Document> list = collection.find();//获取文档集合
		
		for (Document doc : list) {//遍历集合中的文档输出数据
			System.out.println("name:"+ doc.getString("name") );
			
			System.out.println("sex:"+ doc.getString("sex") );
			
			System.out.println("age:"+ doc.getString("age") );//默认为浮点型
			
			System.out.println("address:"+ doc.getString("address") );
			
			System.out.println("--------------------------");	
		}
		
		
	}
	

}
