package com.yaowan.server.game.model.log.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.FriendChatLog;

@Component
public class FriendChatLogDao extends SingleKeyLogDao<FriendChatLog,Long>{
		
	/*public List<FriendChatLog> getFriendChatNoRead(long rid){	
		StringBuilder sql = new StringBuilder();
		sql.append("select * from friend_chat_log where getter_id = '" + rid + "' and is_read=0" );
		return this.findList(sql.toString());
	}*/
	
	public void updatefriendChat(long rid){
		StringBuilder sql = new StringBuilder();
		sql.append("update friend_chat_log set is_read=1 where getter_id = "+rid);
		this.executeSql(sql.toString());
	}
	
	public void deletefriendChat(long rid,long removeId){
		StringBuilder sql = new StringBuilder();
		sql.append("delete from friend_chat_log where rid = "+rid+" and (sender_id = "+removeId+" or getter_id ="+removeId+")");
		this.executeSql(sql.toString());
	}
	
	/**
	 * 获取该用户和好友最新的一条记录（因为有发送者和接收者的关系，需要在程序中二次封装）
	 * @param rid
	 * @return
	 */
	public List<FriendChatLog> getOneChatListByRid(long rid){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM friend_chat_log WHERE rid = ");
		sql.append(rid);
		
		//sql.append(" group by getter_id,sender_id ORDER BY id");
		
		System.out.println(sql);
		return this.findList(sql.toString());
	}
	
	public List<FriendChatLog> getFriendChatByRid(long rid,long targetId){
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM friend_chat_log WHERE rid = "+rid+" and (sender_id = "+targetId+" or getter_id ="+targetId+")");
		return this.findList(sql.toString());
	}
	

}
