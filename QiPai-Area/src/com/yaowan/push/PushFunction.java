package com.yaowan.push;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.framework.push.model.DPushNotification;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.dao.RoleDao;
import com.yaowan.server.game.model.data.entity.PushBean;


@Component
public class PushFunction {

	@Autowired
	private RoleDao roleDao;
	
	@Autowired
	private PushService pushService;
	
	private static final int threeDaysUnOnline = -3;
	private static final int sevenDaysUnOnline = -7;
	
	
	//查询连续不在线的用户
	public List<PushBean> getUnOnlineList(int diffDay, int startNumber){
		int start = this.getTime(diffDay-1);
		int end = this.getTime(diffDay);
		List<PushBean> pushList = roleDao.getUnOnlineRole(start, end, startNumber);
		return pushList;
	}
	
//	public static void main(String[] args) {
//		PushFunction pushFunction = new PushFunction();
//		pushFunction.getUnOnlineList(-7, 0);
//	}
	
	//得到时间戳
	public int getTime(int diffDay){
		String time = getAgoTime(diffDay);
		return TimeUtil.strToTime(time);
	}
	
	
	//获得几天前或几天后的日期
	public static String getAgoTime(int differDay){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, differDay);
		return simpleDateFormat.format(calendar.getTime());
	}
	
	public static void main(String[] args) {
		System.out.print(TimeUtil.strToTime(getAgoTime(-8)));
	}
	
	
	 //获取定时推送线程启动时间
	public static long getPushTime() {
		
		DateFormat df = new SimpleDateFormat("HH:mm:ss");
		Calendar cl = Calendar.getInstance();
		String time = df.format(cl.getTime());
		Date date;
		try {
			date = df.parse("19:00:00");
			if(df.parse(time).getTime() > date.getTime()){
				cl.add(Calendar.DATE, 1);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Calendar c = Calendar.getInstance();
		c.setTime(cl.getTime());
		c.set(Calendar.HOUR_OF_DAY, 19);
		c.set(Calendar.MINUTE, 00);
		c.set(Calendar.SECOND, 00);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTimeInMillis();
	}
	
	//创建Device_token
	public String createDeviceToken(List<PushBean> list){
		StringBuffer stringBuffer = new StringBuffer();
		for(PushBean pushBean : list){
			String device_token = pushBean.getDevice_token();
			if(device_token == null || device_token.equals("")){
				continue;
			}
			if(stringBuffer == null || stringBuffer.equals("")){
				stringBuffer.append(device_token);
				continue;
			}
			stringBuffer.append("," + device_token);
		}
		return stringBuffer.toString();
	}
	
	
	//根据设备类型分类
	public void classifyPhoneType(List<PushBean> iosList, List<PushBean> androidList, List<PushBean> allList){
		for(PushBean push : allList){
			if(push.getDevice_type() == 1){
				androidList.add(push);
			}else if(push.getDevice_type() == 2){
				iosList.add(push);
			}
		}
	}
	
	//得到连续不在线的人数
	public int getUnOnlineNumber(int diffDay){
		int start = this.getTime(diffDay-1);
		int end = this.getTime(diffDay);
		if(roleDao == null) {
			return 0;
		}
		return roleDao.countUnOnlineRole(start, end);
	}
	
	
	
	public int getSelectTime(int diffDay){
		
		int unOnlineRoleNumber = getUnOnlineNumber(diffDay);
		//三天没登录的人数
		if(unOnlineRoleNumber == 0 ) {
			return -1;
		}else {
			return unOnlineRoleNumber/500 ;
		}
	}
	
	//创建推送数据
	public DPushNotification createDPush(int diffDay, List<PushBean> pushList){
		DPushNotification dPushNotification = new DPushNotification();
		String device_token = createDeviceToken(pushList);
		if(pushList.size() == 0 ) {
			return null;
		}else if(pushList.size() == 1){
			if(pushList.get(0).getDevice_type() == 1) {
				dPushNotification.setType(PushHelper.ANDROIDUNICAST);
			}else if(pushList.get(0).getDevice_type() == 2) {
				dPushNotification.setType(PushHelper.IOSUNICAST);
			}
		}else {
			if(pushList.get(0).getDevice_type() == 1) {
				dPushNotification.setType(PushHelper.ANDROIDLISTCAST);
			}else if(pushList.get(0).getDevice_type() == 2) { 
				dPushNotification.setType(PushHelper.IOSLISTCAST);
			}
		}
		
		dPushNotification.setTitle("邵通棋牌");
		dPushNotification.setTicker("邵通棋牌提醒您：客官，好久没来玩牌了呢，大家好想你哦~");
		dPushNotification.setMsg("客官，好久没来玩牌了呢，大家好想你哦~");
		dPushNotification.setDeviceTokens(device_token);
		return dPushNotification;
	}
	
	
	//推送
	public void sendMessage(){
		List<PushBean> iosList = new ArrayList<PushBean>();
		List<PushBean> androidList = new ArrayList<PushBean>();
		
		int threeDayNumber = getSelectTime(threeDaysUnOnline);
		for(int i = 0; i <= threeDayNumber; i++) {
			List<PushBean> pushList = this.getUnOnlineList(threeDaysUnOnline, i*500);
			classifyPhoneType(iosList, androidList, pushList);
			DPushNotification androidNotification = createDPush(threeDaysUnOnline, androidList);
			androidList.clear();
			DPushNotification iosNotification = createDPush(threeDaysUnOnline, iosList);
			iosList.clear();
			try {
				pushService.send(androidNotification);
				pushService.send(iosNotification);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LogUtil.error("发送出错");
			}
		}
		
		int sevenDayNumber = getSelectTime(sevenDaysUnOnline);
		for(int i = 0; i <= sevenDayNumber; i++) {
			List<PushBean> pushList = this.getUnOnlineList(sevenDaysUnOnline, i*500);
			classifyPhoneType(iosList, androidList, pushList);
			DPushNotification androidNotification = createDPush(sevenDaysUnOnline, androidList);
			androidList.clear();
			DPushNotification iosNotification = createDPush(sevenDaysUnOnline, iosList);
			iosList.clear();
			try {
				pushService.send(androidNotification);
				pushService.send(iosNotification);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				LogUtil.error("发送出错");
			}
		}
	}
	
	
	
	
	
	
}
