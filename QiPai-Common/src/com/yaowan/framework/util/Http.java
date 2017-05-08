package com.yaowan.framework.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;



/**
 * http类(基于httpclient实现)
 *
 * @author ruan
 *
 */
public final class Http {
	
	/**
	 * 字符集
	 */
	private final static String charset = "utf-8";
	/**
	 * http连接池
	 */
	// private static PoolingHttpClientConnectionManager cm;
	/**
	 * 请求配置
	 */
//	private static RequestConfig.Builder requestConfigBuilder;
	/**
	 * 超时时间
	 */
	private final static int timeout = 3000;

	static {
		// 支持http和https
//		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", new SSLConnectionSocketFactory(SSLContexts.createSystemDefault(), new BrowserCompatHostnameVerifier())).build();
//
//		// 初始化连接池
//		cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
//		cm.setMaxTotal(200);
//		cm.setDefaultMaxPerRoute(10);
//
//		// 设置字符集
//		ConnectionConfig.Builder connectionConfigBuilder = ConnectionConfig.custom();
//		connectionConfigBuilder.setCharset(Charset.forName(charset));
//		cm.setDefaultConnectionConfig(connectionConfigBuilder.build());
//
//		// 设置socket连接选项
//		SocketConfig.Builder socketConfigBuilder = SocketConfig.custom();
//		socketConfigBuilder.setTcpNoDelay(true);
//		socketConfigBuilder.setSoKeepAlive(false);
//		cm.setDefaultSocketConfig(socketConfigBuilder.build());

		// 请求超时设置
//		requestConfigBuilder = RequestConfig.custom();
//		requestConfigBuilder.setConnectTimeout(timeout);
//		requestConfigBuilder.setConnectionRequestTimeout(timeout);
//		requestConfigBuilder.setSocketTimeout(timeout);

		// TODO ---------------------------------------------------------------------------------------------
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "stdout");
		// TODO ---------------------------------------------------------------------------------------------
	}

	/**
	 * 获取一个HttpClient对象
	 * @author ruan
	 * @return
	 */
	private final static CloseableHttpClient getHttpClient() {
//		cm.closeExpiredConnections();
//		if (Config.debug) {
//			GameApp.logger.info("http connectionManager total stats : " + JSONObject.encode(cm.getTotalStats()).replace("{", "").replace("}", "").replace("\"", ""));
//		}
//
		return HttpClients.createDefault();

		//return HttpClients.custom().setConnectionManager(cm).build();
	}

	/**
	 * 发送http请求
	 * @author ruan
	 * @param request
	 * @return
	 */
	private final static String doRequest(HttpUriRequest request) {
		try {
			CloseableHttpResponse response = getHttpClient().execute(request);
			return EntityUtils.toString(response.getEntity(), charset);
		} catch (IOException e) {
			
		}
		return null;
	}

	/**
	 * 发送post请求
	 * @author ruan
	 * @param url 请求地址
	 * @param params 请求参数
	 */
	public static String sendPost(String url, Map<String, Object> params) {
		return sendPost(url, params, timeout);
	}

	/**
	 * 发送post请求
	 * @author ruan
	 * @param url 请求地址
	 * @param params 请求参数
	 */
	public static String sendPost(String url, Map<String, Object> params, int timeout) {
		try {
			LogUtil.debug("HTTP POST: " + url);
			LogUtil.debug("PARAMS:");

			RequestConfig.Builder builder = RequestConfig.custom();
			builder.setConnectTimeout(timeout);
			builder.setConnectionRequestTimeout(timeout);
			builder.setSocketTimeout(timeout);

			HttpPost httppost = new HttpPost(url);
			httppost.setConfig(builder.build());

			ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();

			// 添加参数
			for (Entry<String, Object> e : params.entrySet()) {

				if (e.getValue() == null) {
					continue;
				}

				paramList.add(new BasicNameValuePair(e.getKey(), e.getValue().toString()));

				LogUtil.debug(e.getKey() + " = " + e.getValue().toString());
			}

			LogUtil.debug("PARAMS END.");

			httppost.setEntity(new UrlEncodedFormEntity(paramList, charset));

			return doRequest(httppost);

		} catch (ParseException | IOException e) {
			
		}
		return "";
	}

	public static String sendPost(String url, String content, int timeout) throws UnsupportedEncodingException {
		try {
			LogUtil.debug("HTTP POST: " + url);
			LogUtil.debug("CONTENT: " + content);

			RequestConfig.Builder builder = RequestConfig.custom();
			builder.setConnectTimeout(timeout);
			builder.setConnectionRequestTimeout(timeout);
			builder.setSocketTimeout(timeout);

			HttpPost httppost = new HttpPost(url);
			httppost.setConfig(builder.build());

			httppost.setEntity(new StringEntity(content, charset));

			return doRequest(httppost);

		} catch (ParseException e) {
			
		}
		return "";
	}


	public static String sendGet(String url, Map<String, Object> params) {
		return sendGet(url, params, timeout);
	}

	/**
	 * 发送get请求
	 * @author ruan
	 * @param url 请求的地址
	 * @param params 请求的参数
	 * @return
	 */
	public static String sendGet(String url, Map<String, Object> params, int timeout) {
		try {

			RequestConfig.Builder builder = RequestConfig.custom();
			builder.setConnectTimeout(timeout);
			builder.setConnectionRequestTimeout(timeout);
			builder.setSocketTimeout(timeout);

			HttpGet httpget = new HttpGet();
			httpget.setConfig(builder.build());
			
			String uri = url;
			if (params != null && !params.isEmpty()) {
				uri = makeUrl(url, params);
			}
			httpget.setURI(new URI(uri));

			LogUtil.debug("HTTP GET: " + uri);

			return doRequest(httpget);

		} catch (Exception e) {
			
		}
		return null;
	}

	public static String makeUrl(String url, Map<String, Object> params) {
		StringBuilder uri = new StringBuilder();
		uri.append(url);
		uri.append("?");

		try {
			// 分析参数
			for (Entry<String, Object> e : params.entrySet()) {
				if (e.getValue() == null) {
					continue;
				}

				uri.append(e.getKey());
				uri.append("=");

					uri.append(URLEncoder.encode(e.getValue().toString(), charset));

				uri.append("&");
			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		// 发送http请求
		int len = uri.length();
		uri.delete(len - 1, len);
		return uri.toString();
	}
}