/**
 * 
 */
package com.yaowan.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

import com.yaowan.core.base.GlobalConfig;

/**
 * @author huangyuyuan
 *
 */
@Component
public class ConfigLoader {
	
	private Map<String, IConfig<?>> registerMap = new HashMap<>();
	
	public void register(String csvName, IConfig<?> config) {
		registerMap.put(csvName, config);
	}
	
	public void loadConfigToCache() {
		loadConfigDir(new File(GlobalConfig.csvConfigDir));
	}
	
	private void loadConfigDir(File dirFile) {
		File[] files = dirFile.listFiles();
		if (null == files) {
			return;
		}
		for(File file : files) {
			if(file.isDirectory()) {
				loadConfigDir(file);
				continue;
			}
			if(!file.getName().endsWith(".csv")) {
				continue;
			}
			loadConfigFile(file);
		}
	}
	
	public void loadAfterAllConfigReady() {
		for(IConfig<?> iConfig : registerMap.values()) {
			iConfig.loadAfterAllConfigReady();
		}
	}
	
	/**
	 * 加载配置文件
	 * 
	 * @param file
	 */
	private void loadConfigFile(File file) {
		try {
			CSVReader csvReader = new CSVReader(new FileReader(file));
			List<String[]> rowDatas = csvReader.readAll();
			csvReader.close(); 
			
			//可以为增加表头做处理
			
			String[] rowHead = rowDatas.remove(0);
			
			IConfig<?> config = registerMap.get(file.getName().replace(".csv", ""));
			if(config != null) {
				config.loadCache(rowHead, rowDatas);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void load() {
		loadConfigToCache();
		loadAfterAllConfigReady();
	}
	
	/**
	 * 加载指定的配置文件
	 * @param fileNames
	 */
	public void load(String ... fileNames){
		for (String fileName : fileNames) {
			String path = GlobalConfig.csvConfigDir.endsWith("/") ? GlobalConfig.csvConfigDir+fileName : GlobalConfig.csvConfigDir+"/"+fileName;
			File file = new File(path);
			loadConfigFile(file);
		}
		
		for (String fileName : fileNames) {
			IConfig<?> config = registerMap.get(fileName.replace(".csv", ""));
			if(config != null) {
				config.loadAfterAllConfigReady();
			}
		}
	}
}
