package com.yaowan.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yaowan.ServerConfig;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;


/**
 * 
 * 处理/屏蔽关键字
 * 
 * @author Thomas Zheng
 * 
 */
public class KeywordUtil {

	// 屏蔽字文件路径
	private static final String DIRTY_WORDS_FILE = ServerConfig.configPath0+"/dirtyWords.txt";
	private static final String FILTER = "****";
	public static List<String> dirtyWords = new ArrayList<String>(1);
	public static List<Pattern> patterns = new ArrayList<Pattern>(1);// 缓存
	public static List<Pattern> replacePatterns = new ArrayList<Pattern>(1);// 缓存
	private static ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();// 防止在读的时候更新屏蔽字
	// private static ReadLock rLock = readWriteLock.readLock();
	private static WriteLock wLock = readWriteLock.writeLock();

	private static int maxLength;

	public static HashMap<String, String> filterStrs = new HashMap<String, String>(
			2000);
	public static Map<String, String> whiteStrs = new HashMap<String, String>(
			50);
	
	public static Map<String, String> whiteIps = new HashMap<String, String>(
			50);
	static {
		loadDirtyWord();
	}

	/**
	 * 过滤屏蔽字
	 * 
	 * @param message
	 * @return
	 */
	/*public static String filterDirtyWords(String message,Player player) {
		if (message == null || message.trim().length() == 0) {
			return message;
		}
		// 防止正在读取屏蔽字时更新屏蔽字而抛出异常
		// rLock.lock();
		// long curTime = DateUtil.getCurrentTime();
		if(ServerConfig.isQqVersion()){
			String rep = QqRule.filterDirtyWords(player, message);
			if(StringUtil.isStringEmpty(rep)){
				return filter(message);
			}
			return rep;
		}else{
			if(CollectionUtil.isEmpty(filterStrs)){
				return message;
			}
			//TODO 暂时不屏蔽
			if(ServerConfig.isCnVersion()){
				message = filter(message);
			}
			message = filter(message);
		}
	    */
		/*
		 * long time1 = DateUtil.getCurrentTime(); try { // String regex = null;
		 * for (int i = 0; i < dirtyWords.size(); i++) { String dirtyWord =
		 * dirtyWords.get(i); if (dirtyWord == null || dirtyWord.length() == 0)
		 * { continue; } // regex = ".*" + dirtyWord.trim() + ".*"; try { if
		 * (checkStr(patterns.get(i), message)) { message =
		 * replaceStr(replacePatterns.get(i), message); } } catch (Exception e)
		 * { log.error("过滤字符出错...", e); continue; } } } finally { //
		 * rLock.unlock(); } long time2 = DateUtil.getCurrentTime();
		 * System.out.print((time1 - curTime) + ":" + (time2 - time1));
		 */
/*		return message;
	}*/

	/**
	 * 检验姓名
	 * 
	 * @param name
	 *            姓名
	 * @return true-不在屏蔽范围内,false-在屏蔽范围内
	 */
//	public static boolean checkName(String name) {
//		if (name == null || name.trim().length() == 0 || name.equals("null")
//				|| name.equals("NULL")) {
//			return false;
//		}
//		if (name.indexOf(Tools.DELIMITER_BETWEEN_ITEMS) != -1
//				|| name.indexOf(Tools.ARGS_ITEMS_DELIMITER) != -1
//				|| name.indexOf(Tools.DELIMITER_INNER_ITEM) != -1
//				|| name.indexOf(Tools.DELIMITER_CAS) != -1
//				|| name.indexOf(" ") != -1) {
//			return false;
//		}
//		int len = 0;
//		for (int i = 0; i < name.length(); i++) {
//			char c = name.charAt(i);
//			if (c >= '0' && c <= '9') {
//				len++;
//			} else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
//				len++;
//			} else {
//				len += 2;
//			}
//		}
//		if (len > 12) {
//			return false;
//		}
//		
//		if (CollectionUtil.isEmpty(filterStrs)) {
//				return true;
//		}
//		
//		
//		// rLock.lock();
//		if (isFilter(name)) {
//			return false;
//		}
//		/*
//		 * try { for (int i = 0; i < dirtyWords.size(); i++) { String dirtyWord
//		 * = dirtyWords.get(i); if (dirtyWord == null || dirtyWord.length() ==
//		 * 0) { continue; } try { if (checkStr(patterns.get(i), name)) { return
//		 * false; } } catch (Exception e) { log.error("过滤字符出错...", e); continue;
//		 * } } } finally { // rLock.unlock(); }
//		 */
//		return true;
//	}

	/**
	 * 检验句子
	 * 
	 * @param name
	 *            姓名
	 * @return true-不在屏蔽范围内,false-在屏蔽范围内
	 */
	public static boolean checkWord(String word) {
		if (filterStrs == null) {
			return true;
		}
		if (filterStrs.size() == 0) {
			return true;
		}
		if (isFilter(word)) {
			return false;
		}
		return true;
	}
	
	public static void loadWhite() {
		whiteIps.clear();
		whiteStrs.clear();
	/*	String data = HttpUtil.getWhiteAccounts();
		if(!StringUtil.isStringEmpty(data)){
			String[] datas =data.split(",");
			for(String white:datas){
				if(white.indexOf(".")!=-1){
					whiteIps.put(white.substring(0,white.lastIndexOf(".")), white);
					LogUtil.error(white.substring(0,white.lastIndexOf(".")));
				}else{
					whiteStrs.put(white, white);
				}
				
			}
		    System.out.println("load white num "+whiteStrs.size());
		}*/
		
	}
	/**
	 * 将文件里的屏蔽字加载到内存中
	 */
	public static void loadDirtyWord() {
		String words = null;
		// words = HttpUtil.getDirtyWord();
		if (words == null) {
			BufferedReader reader = null;
			try {
				StringBuffer result = new StringBuffer();
				File file = new File(DIRTY_WORDS_FILE);
				reader = new BufferedReader(
						new InputStreamReader(new FileInputStream(file), "UTF-8"));
				String line = null;
				while ((line = reader.readLine()) != null) {
					result.append(line+",");
				}
				words = result.toString();
			} catch (FileNotFoundException e) {
				//LogUtil.error("加载屏蔽字出错,文件不存在！", e);
				System.out.println("加载屏蔽字出错,文件不存在！"+e);
			} catch (Exception e) {
				//LogUtil.error("加载屏蔽字出错！", e);
				System.out.println("加载屏蔽字出错"+e);
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						//LogUtil.error("加载屏蔽字出错！", e);
						System.out.println("加载屏蔽字出错！"+e);
					}
				}
			}
		}
		if (words != null && words.length() > 0) {
			words = words.trim();
			words = words.replaceAll("，", ",").replaceAll("\\*", "\\\\*")
					.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)")
					.replaceAll("\\+", "\\\\+");
			String[] dirtyWordArr = words.split(",");
			// wLock.lock();
			StringBuffer sb = null;
			try {

				String trim = null;
				for (String word : dirtyWordArr) {
					trim = word.trim();
					if (trim.length() > 0) {
						// sb = new StringBuffer();
						// sb.append(".*").append(trim).append(".*");
						// dirtyWords.add(trim);
						// patterns.add(Pattern.compile(sb.toString()));
						// sb = new StringBuffer();
						// sb.append(trim).append("+");
						// replacePatterns.add(Pattern.compile(sb.toString()));
						put(trim);
					}
				}
			} catch (Exception e) {
			} finally {
				// wLock.unlock();
			}
		}
	}

	/**
	 * 刷新屏蔽字
	 */
	public static void refreshDirtyWord() {
		wLock.lock();
		try {
			dirtyWords.clear();
		} finally {
			wLock.unlock();
		}
		loadDirtyWord();
	}

	/**
	 * 替换屏蔽字
	 * 
	 * @param pattern
	 *            编译了的正则表达式
	 * @param input
	 *            内容
	 * @return
	 */
	private static String replaceStr(Pattern pattern, String input) {
		Matcher m = pattern.matcher(input);
		return m.replaceAll(FILTER);
	}

	/**
	 * 判断是否存在屏蔽字
	 * 
	 * @param regex
	 *            正则表达式
	 * @param input
	 *            内容
	 * @return true-存在,false-不存在
	 */
	private static boolean checkStr(Pattern pattern, String input) {
		return pattern.matcher(input).matches();
	}

	public static void updateDirtyWord(final String word) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String words = word.replaceAll("，", ",");
					BufferedReader br = new BufferedReader(
							new InputStreamReader(KeywordUtil.class
									.getResourceAsStream(DIRTY_WORDS_FILE),
									"UTF-8"));
					StringBuffer sf = new StringBuffer();
					String s = null;
					while ((s = br.readLine()) != null) {
						sf.append(s);
					}
					if (words.startsWith(",")) {
						sf.append(words);
					} else {
						sf.append(",").append(words);
					}
					BufferedWriter bw = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(
									new File(KeywordUtil.class.getResource(
											DIRTY_WORDS_FILE).getFile())
											.getCanonicalPath()), "UTF-8"));
					bw.write(sf.toString());
					bw.close();
					refreshDirtyWord();
					loadDirtyWord();
				} catch (Exception e) {
					//LogUtil.error("修改屏蔽字出错！", e);
					System.out.println(e);
				}
			}

		}).start();
	}

	public static void deleteDirtyWord(final String word) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String words = null;
					if (word != null && word.length() > 0) {
						words = word.replaceAll("，", ",");
					}
					List<String> deleteList = StringUtil.stringToList(words,StringUtil.DELIMITER_COMMA,String.class);
					BufferedReader br = new BufferedReader(
							new InputStreamReader(KeywordUtil.class
									.getResourceAsStream(DIRTY_WORDS_FILE),
									"UTF-8"));
					StringBuffer sf = new StringBuffer();
					String s = null;
					while ((s = br.readLine()) != null) {
						sf.append(s);
					}
					List<String> contentList = StringUtil.stringToList(sf.toString(),StringUtil.DELIMITER_COMMA,String.class);
					if (!collectionIsEmpty(contentList)
							&& !collectionIsEmpty(deleteList)) {
						for (String str : deleteList) {
							if (contentList.contains(str)) {
								contentList.remove(str);
							}
						}
						String result = StringUtil.listToString(contentList, StringUtil.DELIMITER_COMMA);
						BufferedWriter bw = new BufferedWriter(
								new OutputStreamWriter(new FileOutputStream(
										new File(KeywordUtil.class.getResource(
												DIRTY_WORDS_FILE).getFile())
												.getCanonicalPath()), "UTF-8"));
						bw.write(result);
						bw.close();
						refreshDirtyWord();
						loadDirtyWord();
					}
				} catch (Exception e) {
					//LogUtil.error("删除屏蔽字出错！", e);
					System.out.println(e);
				}
			}

		}).start();
	}

	/**
	 * 
	 * 初始化需要过滤掉*的数量
	 */

	private static String initStr(int n) {

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < n; i++) {

			sb.append('*');

		}

		return sb.toString();

	}

	/**
	 * 
	 * str-被过滤得字符串
	 * 
	 * s-需要过滤得字符串
	 * 
	 * 获得剩下未过滤的字符串
	 */

	private static String getNextStr(String str, int start, int slength) {

		if (start == 0) {

			str = str.substring(slength);

		} else if (start + slength < str.length()) {

			str = str.substring(start + slength);

		} else {
			str = "";
		}

		return str;

	}

	/**
	 * 
	 * str-被过滤得字符串
	 * 
	 * s-需要过滤得字符串
	 * 
	 * 获得过滤后的字符串
	 */

	private static StringBuffer getFilterStr(StringBuffer sb, String str,
			int start, String s) {

		if (start != 0) {

			sb.append(str.substring(0, start));

		}

		sb.append(filterStrs.get(s));

		return sb;

	}

	/**
	 * 
	 * str-被过滤的字符串
	 * 
	 * 是否被过滤
	 */

	public static boolean isFilter(String str) {

		for (int start = 0; start < str.length(); start++) {
			int tempLength = str.length() > start + maxLength ? start
					+ maxLength : str.length();
			int minLength = start + 1;
			for (int end = tempLength; end >= minLength; end--) {
				String s = str.substring(start, end);
				if (filterStrs.containsKey(s)) {
					return true;
				}
			}
		}
		return false;

	}

	/**
	 * 
	 * str-被过滤的字符串
	 * 
	 * 过滤，并组合过滤后的字符串
	 */

	public static String filter(String str) {
		long curTime = TimeUtil.time();
		StringBuffer resultStr = new StringBuffer();

		for (int start = 0; start < str.length(); start++) {
			int tempLength = str.length() > start + maxLength ? start
					+ maxLength : str.length();
			int minLength = start + 1;
			for (int end = tempLength; end >= minLength; end--) {

				String s = str.substring(start, end);

				int slength = s.length();

				if (filterStrs.containsKey(s)) {

					resultStr = getFilterStr(resultStr, str, start, s);

					str = getNextStr(str, start, slength);
					if (TimeUtil.time() - curTime > 100000) {
						// 防止死循环
						return str;
					}

					start = 0;
					tempLength = str.length() > start + maxLength ? start
							+ maxLength : str.length();
					end = tempLength + 1;

				}

			}

		}
		if (str.length() > 0) {
			resultStr.append(str);
		}

		return resultStr.toString();

	}

	public static void put(String key) {

		int keyLength = key.length();
		filterStrs.put(key, FILTER);// initStr(keyLength)

		if (keyLength > maxLength)

			maxLength = keyLength;

	}
	
	private static boolean collectionIsEmpty(List<String> deleteList){
		if(null != deleteList && deleteList.size()>0){
			return false;
		}else{
			return true;
		}
	}
}
