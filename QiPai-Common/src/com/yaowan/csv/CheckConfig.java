package com.yaowan.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

import com.yaowan.core.base.GlobalConfig;
import com.yaowan.core.base.Spring;
import com.yaowan.framework.util.FileUtil;

/**
 * 检测csv文件是否有utf bom头,或者列不存在
 * @author zane 2016年10月13日 下午4:03:47
 *
 */
@Component
public class CheckConfig {
	

	public void check(){
		try {
			Set<Class<?>> clazzs = FileUtil.getClasses("com.yaowan.csv.entity");
			
			File csvFolder = new File(GlobalConfig.csvConfigDir);
			Map<String, File> csvFiles = new HashMap<>();
			System.out.println(csvFolder +" csvFiles "+ csvFiles +"csvConfigDir"+GlobalConfig.csvConfigDir) ;
			scanCsvFile(csvFolder, csvFiles);
			
			for (Class<?> cla : clazzs) {
				String className = cla.getSimpleName();
				String fileName = className;
				try {
					Class<?> cache = Class.forName("com.yaowan.csv.cache" + '.'
							+ cla.getSimpleName().replace("Csv", "") + "Cache");
					Object obj = Spring.getBean(cache);
					Method method = obj.getClass().getDeclaredMethod(
							"getFileName");
					fileName = (String) method.invoke(obj);
				} catch (Exception e) {
					e.printStackTrace();
				}
				File configFile = csvFiles.get(fileName);
				if (configFile != null) {
					
					CSVReader cr = new CSVReader(new FileReader(configFile));
					List<String[]> configArr = cr.readAll();
					cr.close();
	
					// 配置文件的字段定义
					String[] column = configArr.remove(0);
					
					// 类文件的字段定义
					Field[] fields = cla.getDeclaredFields();
					// 按配置文件去检查配置类
					for (String columnStr : column) {
						String[] nameType = columnStr.split(":");
						if(nameType.length < 2) {
							System.out.println(className + " config on " + columnStr + " not match Name:Type.");
							continue;
						}
						String columnName = nameType[0];
						String columnType = nameType[1];
						
						Field columnField = null;
						
						for(Field field : fields) {
							if(field.getName().toLowerCase().equals(columnName.toLowerCase())) {
								columnField = field;
								break;
							}
						}
						//类中是否存在该字段
						if(columnField == null) {
							StringBuilder ssb = new StringBuilder();
							for(byte data:columnName.toLowerCase().getBytes()){
								ssb.append(data).append("|");;
							}
							System.out.println(cla.getSimpleName() + " class missing field named " + columnName+":"+ssb);
							continue;
						}
						String fieldType = columnField.getType().getSimpleName();
						if(!columnType.equalsIgnoreCase(fieldType)) {
							System.out.println(configFile.getName() + "||" + columnName + ":" + fieldType + "||csv type:" + columnType);
						}
					} 
				} else {
					System.out.println("missing config file " + fileName);
				}
			}
			System.out.println("server config check ok.");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void scanCsvFile(File csvFolder, Map<String, File> fileList) {
		for(File file : csvFolder.listFiles()) {
			if(file.isDirectory()) {
				scanCsvFile(file, fileList);
				continue;
			}
			if(file.getName().endsWith(".csv")) {
				fileList.put(file.getName().replace(".csv", ""), file);
			}
		}
	}
}
