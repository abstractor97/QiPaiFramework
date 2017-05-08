package com.yaowan.httpserver.handler;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.yaowan.constant.GameType;

/**
 * 用于在线修复bug，来替换class文件
 * 使用前先上传Class文件
 * http://ip:port?action=HHTest.test&clazzes=clazzName1,clazzName2,clazzName3 
 * @author YW0941
 *
 */
@Service("HHTest")
public class HHTest {
	
	
	/**
	 *  Key =  clazzes
	 * @param params
	 */
	public void test(Map<String, String> params){
		System.out.println(GameType.TEST);
	}
	
	
}
