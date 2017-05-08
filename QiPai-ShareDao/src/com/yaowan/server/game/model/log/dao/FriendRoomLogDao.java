/**
 * 
 */
package com.yaowan.server.game.model.log.dao;

import org.springframework.stereotype.Component;

import com.yaowan.database.asyn.AsynContainer;
import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.server.game.model.log.entity.FriendRoomLog;

/**
 * @author zane
 *
 */
@Component
public class FriendRoomLogDao extends SingleKeyLogDao<FriendRoomLog,Long> {
	
	public void addMoney(FriendRoomLog friendRoomLog) {
		AsynContainer.add(getInsertSql(friendRoomLog));
	}

}
