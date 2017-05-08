package com.yaowan.server.game.function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.log.dao.FriendChatLogDao;
import com.yaowan.server.game.model.log.entity.FriendChatLog;

@Component
public class FriendChatLogFunction {
	
	@Autowired
	FriendChatLogDao friendChatLogDao;
	
	public void addFriendChatLog(long rid,long senderId,long getterId,String textLog,int type,byte isRead){
		FriendChatLog friendChatLog=new FriendChatLog();
		friendChatLog.setRid(rid);
		friendChatLog.setSenderId(senderId);
		friendChatLog.setGetterId(getterId);
		friendChatLog.setType(type);
		friendChatLog.setTextLog(textLog);
		friendChatLog.setTime(TimeUtil.time());
		friendChatLog.setVoiceLog("");
		friendChatLog.setIsRead(isRead);
		friendChatLogDao.insert(friendChatLog);
	}
}
