/**
 * 
 */
package com.yaowan.framework.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author huangyuyuan
 *
 */
public class ExcelReader {
	
	private static int excelDataStartRow = 1;
	
	public static List<Map<String, String>> excelData(File file) {
		int version = ExcelVersion.getExcelType(file);
		if(version != 2007) {
			throw new RuntimeException(file.getName() + " is not excel2007 file");
		}
		return getData(file);
	}
	
	public static List<Map<String, String>> getData(File file) {
		try {
			InputStream inputFile = new FileInputStream(file);
			long fileSize = inputFile.available();
			inputFile.close();
			
			if (fileSize >= 4 * 1024 * 1024) {
				//超过一定大小的excel文件是无法通过Workbook正常读取的
				return getDataWithUserDefined(file);
			} else {
				return getDataWithWorkbook(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static List<Map<String, String>> getDataWithUserDefined(File file) {
		try {
			InputStream inputFile = new FileInputStream(file);
			
			BigExcelToData2007 data2007 = new BigExcelToData2007();
			//读第一个Sheet的内容
			data2007.processSheet(inputFile, 1, excelDataStartRow);
			
			List<List<String>> table = data2007.getDataTable();
			List<Map<String, String>> result = new ArrayList<Map<String, String>>();
			List<String> titles = null;
			
			for (List<String> row : table) {
				if (titles == null) {
					titles = row;
				} else {
					Map<String, String> map = new HashMap<String, String>();
					for (String title : titles) {
						map.put(title, "");
					}
					for (int i = 0; i < row.size(); i++) {
						map.put(titles.get(i), row.get(i));
					}
					result.add(map);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static List<Map<String, String>> getDataWithWorkbook(File file) {
		try {
			InputStream inputFile = new FileInputStream(file);
			XSSFWorkbook xwb = new XSSFWorkbook(OPCPackage.open(inputFile));
			return getDataWithSheet(xwb.getSheetAt(0));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static List<Map<String, String>> getDataWithSheet(XSSFSheet sheet) {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		// 定义 row、cell
		XSSFCell cell = null;
		String cellValue;
		int maxRow = sheet.getRow(excelDataStartRow).getLastCellNum();
		// 循环输出表格中的内容
		int rowSize = 0;
		String[] titles = null;
		for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {// 行
			XSSFRow row = sheet.getRow(rowIndex);
			if (row == null || row.getLastCellNum() == -1) {
				continue;
			}
			rowSize = row.getLastCellNum();
			if (rowSize > maxRow) {
				rowSize = maxRow;
			}
			String[] values = new String[maxRow];// 一条数据 [格1.格2.格3...]
			Arrays.fill(values, "");// 赋值前，全部为""
			boolean hasValue = false;
			for (int columnIndex = row.getFirstCellNum(); columnIndex < rowSize; columnIndex++) {// 格
				if (columnIndex < 0)
					break;
				cell = row.getCell(columnIndex);
				if (cell == null) {
					continue;
				}
				if (titles != null && titles[columnIndex].length() == 0) {
					continue;
				}
				cellValue = getCellValue(cell);
				cell = null;
				String value = rightTrim(cellValue);
				if (value.endsWith(".0")) {
					value = new StringBuilder(value).replace(
							value.length() - 2, value.length(), "").toString();
				}
				values[columnIndex] = value;
				if (value != null && !value.equals("") && !value.equals("0")) {
					hasValue = true;
				}
			}
			if (hasValue == true && !values[0].equals("")) {
				if (titles == null) {
					// 标题
					titles = values;
				} else {
					Map<String, String> data = new HashMap<String, String>(
							titles.length);
					for (String title : titles) {
						data.put(title, "");
					}
					for (int i = 0; i < values.length; i++) {
						data.put(titles[i], values[i].replaceAll("，", ","));
					}
					result.add(data);
				}
			}
		}
		return result;
	}

	/**
	 * 去掉字符串右边的空格号
	 * 
	 * @param input
	 *            当前字符串
	 * @return 删除空格号后的数据
	 */
	public static String rightTrim(String input) {
		if (input == null)
			return "";
		int length = input.length();
		for (int i = length - 1; i >= 0; i--) {
			if (input.charAt(i) != 0x20)
				break;
			length--;
		}
		return input.substring(0, length).trim();
	}

	/**
	 * @param cell
	 * @return
	 */
	public static String getCellValue(XSSFCell cell) {
		String value = "";
		if (cell != null) {
			if (cell.getCellType() == XSSFCell.CELL_TYPE_FORMULA) {
				try {
					value = String.valueOf(cell.getNumericCellValue());
				} catch (IllegalStateException e) {
					cell.setCellType(XSSFCell.CELL_TYPE_STRING);
					value = cell.getStringCellValue();
				}
			} else if (cell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
				value = String.valueOf(cell.getNumericCellValue());
			} else if (cell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
				value = String.valueOf(cell.getRichStringCellValue());
			}
		}
		return value;
	}
}
