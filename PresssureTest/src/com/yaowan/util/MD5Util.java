/**
 * 
 */
package com.yaowan.util;

import com.yaowan.core.base.GlobalConfig;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author zane
 *
 */
public class MD5Util {
	
	/**
	 * 生成sign值 除sign字段外，所有参数按照字段名的ascii码从小到大排序后使用key1=val1&key2=val2&key3=val3...格式拼接而成，空值不参与签名组串
	 *
	 * @return
	 */
	public static <T> String getOrderSign(Map<String, T> params,String secretKey)
    {
        StringBuffer content = new StringBuffer();

        // 按照key做排序
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++)
        {
            String key = (String) keys.get(i);
            if ("sign".equals(key))
            {
                continue;
            }
            String value = (String) params.get(key);
            if (value != null)
            {
                content.append((i == 0 ? "" : "&") + key + "=" + value);
            }
            else
            {
                content.append((i == 0 ? "" : "&") + key + "=");
            }

        }
        content.append("&appkey=").append(secretKey);
        return Security.MD5(content.toString().toLowerCase());
    }
	
	/**
	 * 生成sign值(系统密钥)
	 *
	 * @author ruan 2013-2-6
	 * @param data
	 * @param key
	 * @return
	 */
	public static String makeSign(Map<String, Object> data) {
		return makeSign(data, GlobalConfig.platformKey);
	}
	/**
	 * 生成sign值(自定义密钥)
	 *
	 * @author ruan 2013-2-6
	 * @param data
	 * @param key
	 * @return
	 */
	public static String makeSign(Map<String, Object> data, String key) {
		// sign生成规则，每个参数的按照key的顺序依次连接，最后连接通信key，整个字符串做一次md5计算
		// 例子：a=2, b=1, c=9
		// sign = md5(219key)
		StringBuilder sign = new StringBuilder();
		ArrayList<String> keyList = new ArrayList<String>();
		for (Entry<String, Object> e : data.entrySet()) {
			keyList.add(e.getKey());
		}
		Collections.sort(keyList, comp);
		for (String k : keyList) {
			sign.append(data.get(k));
		}
		sign.append(key);
		return Security.MD5(sign.toString());
	}
	
	private static Comparator<String> comp = new Comparator<String>() {
		public int compare(String o1, String o2) {
			if (o1.compareTo(o2) > 0) {
				return 1;
			} else if (o1.compareTo(o2) < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	};
	
	
	/**
	 * 使用MD5对原始字符串进行加密
	 * @param originalString	原始字符串
	 * @return	经MD5加密后的字符串
	 */
	public static String encodeByMD5(String originalString) {
		byte[] bytes = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			bytes = md.digest(originalString.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			LogUtil.error( e);
		} catch (UnsupportedEncodingException e) {
			LogUtil.error( e);
		}
		return byte2HexStr(bytes);
	}
	
	/**
	 * 字节转换为十六位字符串
	 * 
	 * @param bytes
	 * @return
	 */
	private static String byte2HexStr(byte[] bytes) {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hexStr = Integer.toHexString(bytes[i] & 0xFF);
			if (hexStr.length() == 1) {
				sb.append("0");
			}
			sb.append(hexStr);
		}
		return sb.toString();
	} 
}
