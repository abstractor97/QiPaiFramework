package com.yaowan.framework.excel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * 通过传入的文件流，获取到通用的Workbook接口，就可以进行一系列不同的业务操作了，
 * 实际上到这一步EXCEL2003与EXCEL2007的兼容性问题已经基本解决了
 *
 */
public class ExcelVersion {
	/**
	 * 可拓展，判断office版本，对应进行处理
	 * 
	 * @param file
	 */
	public static int getExcelType(File file) {
		InputStream inputFile = null;
		try {
			inputFile = new BufferedInputStream(new FileInputStream(file));
			
			if (POIFSFileSystem.hasPOIFSHeader(inputFile)) {
				// EXCEL2003使用的是微软的文件系统
				return 2003;
			} else if (POIXMLDocument.hasOOXMLHeader(inputFile)) {
				// EXCEL2007使用的是OOM文件格式
				return 2007;
			} else {
				throw new RuntimeException("不能解析的excel版本");
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if(inputFile != null) {
				try {
					inputFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}


