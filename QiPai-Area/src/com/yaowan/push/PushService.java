/**
 * Project Name:dfh3_server
 * File Name:PushService.java
 * Package Name:push
 * Date:2016年6月20日上午10:31:08
 * Copyright (c) 2016, zhaozhiheng@yaowan.com All Rights Reserved.
 *
*/

package com.yaowan.push;



import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.framework.android.AndroidListcast;
import com.yaowan.framework.android.AndroidUnicast;
import com.yaowan.framework.push.channel.AndroidNotification;
import com.yaowan.framework.push.channel.BaseNotification;
import com.yaowan.framework.push.ios.IOSListcast;
import com.yaowan.framework.push.ios.IOSUnicast;
import com.yaowan.framework.push.model.DPushNotification;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.data.entity.Role;



/**
 * 
 * @author zhaozhiheng 2016年6月20日
 */
@Component
public class PushService implements IPush{
	
	@Autowired
	private RoleFunction roleFunction;
	
	protected final static String USER_AGENT = "Mozilla/5.0";
	
	protected static HttpClient client = new DefaultHttpClient();
	
	
	private PushService() {

	}
	
	/**
	 * 推送服务地址
	 */
	private static String pushURL = "http://msg.umeng.com/api/send";
	
	/**
	 * Android推送appkey
	 */
	private static String androidAppkey = "587597eef43e4808fb002640";
	
	/**
	 * Android推送appMasterSecret
	 */
	private static String androidappMasterSecret = "ydc0niw7dtpokhahkjgsojavsfwaihoo";
	
	/**
	 * Ios推送appkey
	 */
	private static String IosAppkey = "5875a587f29d9830140025bb";

	/**
	 * Ios推送appMasterSecret
	 */
	private static String IosAppMasterSecret = "8eoaizcn7e3fzbuqzanoxrt4obv4tywh";

	@Override
	public void unicastPush(Role role, int msgId, Object... args) throws Exception{
		/*String title = PushConst.getMsg(role.getLang(), msgId + 1, args);
		String ticker = PushConst.getMsg(role.getLang(), msgId + 2, args);
		String msg = PushConst.getMsg(role.getLang(), msgId + 3, args);
		if(msg == null){
			LogUtil.error("push failure,msg is null.rid:" + role.getRid() 
				+ ",pushId:" + role.getPushId() + ",msgId:"+msgId);
			return;
		}
		unicastPush(role, msg, ticker, title);*/
	}

	@Override
	public void unicastPush(Role role, String msg, String ticker, String title) throws Exception{
		// TODO 
//		DPushNotification notification = null;
//		byte platform = role.getPlatform();
//		String pushId = role.getPushId();
//		if (!StringUtils.isEmpty(pushId) && (platform == RoleLoginPlatform.ANDROID ||
//				platform == RoleLoginPlatform.IOS) && role.getPushSwitch() == 1) {
//			
//			notification = new DPushNotification(); 
//			notification.setDeviceTokens(pushId);
//			notification.setMsg(msg);
//			notification.setRid(role.getRid());
//			
//			switch (platform) {
//			case RoleLoginPlatform.ANDROID:
//				notification.setType(PushHelper.ANDROIDUNICAST);
//				notification.setTicker(ticker);
//				notification.setTitle(title);
//				send(notification);
//				break;
//			case RoleLoginPlatform.IOS:
//				notification.setType(PushHelper.IOSUNICAST);
//				send(notification);
//				break;
//			default:
//				break;
//			}
//		}
	}
	
	@Override
	public void listcastPush(Collection<Role> roles, int msgId, Object...args) throws Exception{
		
		
		//TODO 暂时去掉
		return;
		/*Map<Lang, List<Role>> roleMap = PushHelper.splitLangRole(roles);		//拆分不同语言的用户

		for (Entry<Lang, List<Role>> entry : roleMap.entrySet()) {
			String title = PushConst.getMsg(entry.getValue().get(0).getLang(), msgId + 1, args);
			String ticker = PushConst.getMsg(entry.getValue().get(0).getLang(), msgId + 2, args);
			String msg = PushConst.getMsg(entry.getValue().get(0).getLang(), msgId + 3, args);
			if(msg == null){
				GameApp.logger.error("push failure,msg is null.rid:"+entry.getValue().get(0).getRid()
						+ ",pushId:"+entry.getValue().get(0).getPushId()+",msgId:"+msgId 
						+ ",lang:" + entry.getValue().get(0).getLang());
				return;
			}
			listcastPush(entry.getValue(), msg, ticker, title);
		}*/
	}
	
	/**
	 * 列播推送
	 * @author zhaozhiheng 2016年6月20日
	 * @param url
	 * @param roles
	 * @param msg
	 * @throws Exception
	 */
	private void listcastPush(List<Role> roles, String msg, String ticker, String title) throws Exception {
		byte platform = -1;
		List<String> androider = new ArrayList<>();;
		List<String> ioser = new ArrayList<>();
		String pushId = "";
		
		int android_count = 1;
		int ios_count = 1;
		
		DPushNotification notification = new DPushNotification();
		notification.setMsg(msg);
		// TODO
		/*for(Role role : roles) {
			if (ghRole.isOnline(role.getRid()) || role.getPushSwitch() != 1) {
				continue;
			}
			pushId = role.getPushId();
			platform = role.getPlatform();
			
			if (!StringUtils.isEmpty(pushId) && (platform == RoleLoginPlatform.ANDROID ||
					platform == RoleLoginPlatform.IOS)) {
				
				switch (platform) {
				case RoleLoginPlatform.ANDROID:
					androider.add(role.getPushId());
					android_count++;
					break;
				case RoleLoginPlatform.IOS:
					ioser.add(role.getPushId());
					ios_count++;
					break;
				default:
					break;
				}
				
				if (android_count % 500 == 0) {
					notification.setDeviceTokens(StringUtil.arrayJoin(androider, ","));
					notification.setTicker(ticker);
					notification.setTitle(title);
					notification.setType(PushHelper.ANDROIDLISTCAST);
					send(notification);
					android_count = 1;
				}
				if (ios_count % 500 == 0) {
					notification.setDeviceTokens(StringUtil.arrayJoin(ioser, ","));
					notification.setType(PushHelper.IOSLISTCAST);
					send(notification);
					ios_count = 1;
				}
			}
		}
		if (android_count > 1) {
			notification.setDeviceTokens(StringUtil.arrayJoin(androider, ","));
			notification.setTicker(ticker);
			notification.setTitle(title);
			notification.setType(PushHelper.ANDROIDLISTCAST);
			send(notification);
		}
		if (ios_count > 1) {
			notification.setDeviceTokens(StringUtil.arrayJoin(ioser, ","));
			notification.setType(PushHelper.IOSLISTCAST);
			send(notification);
		}*/
	}
	
	
	@Override
	public void send(DPushNotification dPushNotification) throws Exception{
		BaseNotification msg = null;
		if(dPushNotification == null) {
			return;
		}
		if (PushHelper.ANDROIDUNICAST == dPushNotification.getType()) {
			msg = getUnicastAndroid(dPushNotification);
		} else if (PushHelper.IOSUNICAST == dPushNotification.getType()) {
			msg = getUnicastIos(dPushNotification);
		} else if (PushHelper.ANDROIDLISTCAST == dPushNotification.getType()) {
			msg = getListcastAndroid(dPushNotification);
		} else if (PushHelper.IOSLISTCAST == dPushNotification.getType()) {
			msg = getListcastIos(dPushNotification);
		}
		if (null != msg) {
			send(pushURL, msg);
		}
	}
	
	/**
	 * 推送
	 * @author zhaozhiheng 2016年6月27日
	 * @param url
	 * @param msg
	 * @return
	 * @throws Exception
	 */
	private String send(String url, BaseNotification msg) throws Exception {
		String timestamp = Integer.toString((int)(System.currentTimeMillis() / 1000));
		msg.setPredefinedKeyValue("timestamp", timestamp);
        String postBody = msg.getPostBody();
        String sign = DigestUtils.md5Hex(("POST" + url + postBody + msg.getAppMasterSecret()).getBytes("utf8"));
        url = url + "?sign=" + sign;
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        StringEntity se = new StringEntity(postBody, "UTF-8");
        post.setEntity(se);
        // Send the post request and get the response
        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
	}

	/**
	 * android单播推送
	 * @author zhaozhiheng 2016年6月20日
	 * @param msg
	 * @throws Exception
	 */
	private AndroidUnicast getUnicastAndroid(DPushNotification msg) throws Exception{
		AndroidUnicast unicast = new AndroidUnicast(androidAppkey, androidappMasterSecret);
		unicast.setDeviceToken(msg.getDeviceTokens());
		unicast.setTicker(msg.getTicker());
		unicast.setTitle(msg.getTitle()); 
		unicast.setText(msg.getMsg());
		unicast.goAppAfterOpen();
		unicast.setDisplayType(AndroidNotification.DisplayType.NOTIFICATION);
		unicast.setProductionMode();
		return unicast;
	}
	
	/**
	 * ios单播推送
	 * @author zhaozhiheng 2016年6月20日
	 * @param msg
	 * @throws Exception
	 */
	private IOSUnicast getUnicastIos(DPushNotification msg) throws Exception {
		IOSUnicast unicast = new IOSUnicast(IosAppkey, IosAppMasterSecret);
		unicast.setDeviceToken(msg.getDeviceTokens());
		unicast.setAlert(msg.getMsg());
		unicast.setBadge(0);
		unicast.setSound("default");
		unicast.setProductionMode();
		return unicast;
	}
	
	/**
	 * android列播推送
	 * @author zhaozhiheng 2016年6月20日
	 * @param msg
	 * @throws Exception
	 */
	private AndroidListcast getListcastAndroid(DPushNotification msg) throws Exception {
		AndroidListcast listcast = new AndroidListcast(androidAppkey, androidappMasterSecret);
		listcast.setDeviceToken(msg.getDeviceTokens());
		listcast.setTicker(msg.getTicker());
		listcast.setTitle(msg.getTitle());
		listcast.setText(msg.getMsg());
		listcast.goAppAfterOpen();
		listcast.setDisplayType(AndroidNotification.DisplayType.NOTIFICATION);
		listcast.setProductionMode();
		return listcast;
	}
	
	/**
	 * ios列播推送
	 * @author zhaozhiheng 2016年6月20日
	 * @param arrayJoin
	 * @param msg
	 * @throws Exception
	 */
	private IOSListcast getListcastIos(DPushNotification msg) throws Exception {
		IOSListcast listcast = new IOSListcast(IosAppkey, IosAppMasterSecret);
		listcast.setDeviceToken(msg.getDeviceTokens());
		listcast.setAlert(msg.getMsg());
		listcast.setBadge(0);
		listcast.setSound("default");
		listcast.setProductionMode();
		return listcast;
	}
	
}

