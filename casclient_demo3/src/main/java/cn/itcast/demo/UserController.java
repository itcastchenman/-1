package cn.itcast.demo;

import java.util.HashMap;
import java.util.Map;

import org.opensaml.xml.encryption.Public;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

	
	@RequestMapping("/findLoginUser")
	public void findLoginUser(){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println(name);
		
	}
}
