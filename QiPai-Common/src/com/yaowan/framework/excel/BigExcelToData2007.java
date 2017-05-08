/**
 * 
 */
package com.yaowan.framework.excel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Excel2007的底层数据格式是XML，如果某一格子是空的，则读取会出现残缺，必须保证Excel文件中不存在空格子
 * 
 * @author YuYuan Huang
 * 
 */
public class BigExcelToData2007 extends DefaultHandler {
	/**
	 * Excel2007的数据表
	 */
	private SharedStringsTable sst;
	/**
	 * 格子的内容
	 */
	private String cellValue;
	/**
	 * 对于字符串，需要通过索引进行获取
	 */
	private boolean nextIsString;
	/**
	 * 忽略读取的行数
	 */
	private int ingoreRow = 0;
	/**
	 * 当前的行数
	 */
	private int currentRow = 0;
	/**
	 * 最后解释出来的数据表
	 */
	private List<List<String>> dataTable = new ArrayList<List<String>>();
	/**
	 * 行数据
	 */
	private List<String> rowlist = new ArrayList<String>();

	public BigExcelToData2007() {

	}

	/**
	 * 
	 * @param is
	 * @param sheetIndex 从1开始
	 * @param ingoreRow 数据内容（包括表头）开始的行数
	 * @throws Exception
	 */
	public void processSheet(InputStream is, int sheetIndex, int ingoreRow)
			throws Exception {
		this.ingoreRow = ingoreRow;
		this.currentRow = 0;
		OPCPackage pkg = OPCPackage.open(is);
		XSSFReader reader = new XSSFReader(pkg);
		this.sst = reader.getSharedStringsTable();

		XMLReader parser = XMLReaderFactory.createXMLReader();
		parser.setContentHandler(this);

		InputStream sheet = reader.getSheet("rId" + sheetIndex);
		InputSource sheetSource = new InputSource(sheet);
		parser.parse(sheetSource);
		sheet.close();
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		if ("c".equals(name)) {
			if ("s".equals(attributes.getValue("t"))) {
				nextIsString = true;
			} else {
				nextIsString = false;
			}
		}
		cellValue = "";
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (nextIsString) {
			try {
				int idx = Integer.parseInt(cellValue);
				cellValue = new XSSFRichTextString(sst.getEntryAt(idx))
						.toString();
			} catch (Exception e) {

			}
		}

		if ("v".equals(name)) {
			if (currentRow >= ingoreRow) {
				rowlist.add(cellValue);
			}
		} else {
			if ("row".equals(name)) {
				if (currentRow >= ingoreRow) {
					dataTable.add(rowlist);
					rowlist = new ArrayList<String>();
				}
				currentRow++;
			}
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		cellValue += new String(ch, start, length);
	}

	public List<List<String>> getDataTable() {
		return this.dataTable;
	}
}
