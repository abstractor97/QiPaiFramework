package com.yaowan.httpserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.csv.ConfigLoader;

/**
 * 用于在线重新加载配置文件
 * 使用前先上传Class文件到csv/clazzes目录下
 * http://ip:port?action=CSVLoader.load&csv=csv1,csv2,csv3 
 * @author YW0941
 *
 */
@Service("HHCSVLoader")
public class HHCSVLoader {
	private static final String Key = "csv";
	
	@Autowired
	private ConfigLoader configLoader;
	/**
	 *  Key =  clazzes
	 * @param params
	 */
	public Object load(Map<String, String> params){
		HashMap<String, Integer> hm=new HashMap<String, Integer>();
		if(params!=null && params.containsKey(Key)){
			String[] fileNames = params.get(Key).split(",");
			configLoader.load(fileNames);
		}
		hm.put("result", 1);
		return hm;
	}
}
