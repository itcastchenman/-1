package com.pinyougou.user.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
* 认证类
*/

public class UserDetailServiceImpl implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		System.out.println("进过认证类"+username);
		//构建角色集合
	
		Collection<GrantedAuthority> authorties=new ArrayList<>();
		
		authorties.add(new SimpleGrantedAuthority("ROLE_USER"));
		
		return new User(username, "", authorties);
	}

}
