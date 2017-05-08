package com.yaowan.server.game.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GGame.GMsg_12006011;
import com.yaowan.server.game.function.NoticeFunction;
import com.yaowan.server.game.model.data.dao.GameUpdateNoticeDao;
import com.yaowan.server.game.model.data.entity.GameUpdateNotice;

/**
 * 
 * @author G_T_C
 */
@Service
public class GameUpdateNoticeService {
	
	@Autowired
	private GameUpdateNoticeDao gameUpdateNoticeDao;
	@Autowired
	private NoticeFunction noticeFunction;
	
	/**
	 * 发送公告
	 * @author G_T_C
	 * @param player
	 * @param gameType
	 */
	public void getNotice(Player player, int gameType){
		GameUpdateNotice notice = gameUpdateNoticeDao.findByKey(gameType);
		if(notice == null){
			LogUtil.info(player.getRole().getRid()+"参数错误,数据库没有gameType="+gameType);
			return;
		}
		GMsg_12006011.Builder builder = GMsg_12006011.newBuilder();
		builder.setContent(notice.getContent());
		builder.setGameType(gameType);
		player.write(builder.build());
	}
	
	/**
	 * 添加或修改公告
	 * @author G_T_C
	 * @param gameType
	 * @param content
	 */
	public void addOrUpdate(int gameType, String content){
		GameUpdateNotice notice = gameUpdateNoticeDao.findByKey(gameType);
		if(notice == null){
			notice = new GameUpdateNotice();
			notice.setContent(content);
			notice.setGameType(gameType);
			notice.setTime(TimeUtil.time());
			gameUpdateNoticeDao.insert(notice);
		}else{
			notice.setContent(content);
			notice.setTime(TimeUtil.time());
			gameUpdateNoticeDao.update(notice);
		}
		
	
	}
}
