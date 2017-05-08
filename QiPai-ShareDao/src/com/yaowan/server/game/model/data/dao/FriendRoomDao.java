package com.yaowan.server.game.model.data.dao;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.DataDao;
import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.entity.FriendRoom;
import com.yaowan.server.game.model.data.entity.Tax;
import com.yaowan.server.game.model.log.entity.RoomCountLog;

/**
 * 游戏数据统计
 * @author G_T_C
 */
@Component
public class FriendRoomDao extends DataDao<FriendRoom>{
	
	public void addFriendRoom(FriendRoom friendRoom){
		String spriteToString;
		if(friendRoom.getSpriteMap() == null || friendRoom.getSpriteMap().size() == 0){
			spriteToString = "0";
		}else{
			spriteToString = StringUtil.mapToString(friendRoom.getSpriteMap());
		}
		friendRoom.setSpriteToString(spriteToString);
		insert(friendRoom);
	}

	public void deleteFriendRoom(long roomId) {
		LogUtil.info("删除好友房");
		executeSql("delete from friend_room where id="+roomId );
	}
	
	public void updateSpriteToString(FriendRoom friendRoom){
		String spriteToString;
		if(friendRoom.getSpriteMap() == null || friendRoom.getSpriteMap().size() == 0){
			spriteToString = "0";
		}else{
			spriteToString = StringUtil.mapToString(friendRoom.getSpriteMap());
		}
		friendRoom.setSpriteToString(spriteToString);
		updateAllColumn(friendRoom);
	}
	
}