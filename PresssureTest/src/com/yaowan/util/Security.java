package com.yaowan.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Security {
	private Security() {
	}

	/**
	 * MD5方法
	 * 
	 * @param string
	 * @return String
	 */
	public final static String MD5(String string) {
		try {
			return byteArrayToHexString(MessageDigest.getInstance("MD5").digest(string.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			
		}
		return null;
	}

	/**
	 * 哈希方法
	 * 
	 * @param string
	 * @return String
	 */
	public final static String SHA(String string) {
		try {
			return byteArrayToHexString(MessageDigest.getInstance("SHA").digest(string.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			
		}
		return null;
	}

	/**
	 * byteArrayToHexString
	 * 
	 * @author ruan 2013-7-17
	 * @param bytes
	 * @return
	 */
	private final static String byteArrayToHexString(byte[] bytes) {
		StringBuilder buf = new StringBuilder(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			if (((int) bytes[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString((int) bytes[i] & 0xff, 16));
		}
		return buf.toString();
	}
}
