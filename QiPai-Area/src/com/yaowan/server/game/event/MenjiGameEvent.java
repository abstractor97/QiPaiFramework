/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.csv.cache.MenjiRoomCache;
import com.yaowan.csv.entity.MenjiRoomCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.game.GMenJi.GMsg_12013004;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTMenjiFunction;
import com.yaowan.server.game.model.struct.ZTMenjiRole;
import com.yaowan.server.game.model.struct.ZTMenjiTable;

/**
 * 
 *
 * @author zane
 */
@Component
public class MenjiGameEvent extends EventHandlerAdapter{

	@Autowired
	private MenjiRoomCache menjiRoomCache;
	
	@Autowired
	private ZTMenjiFunction menjiFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Override
	public int execute(Event event) {
		ZTMenjiTable table = (ZTMenjiTable) event.getParam()[0];
		
		int seat = table.getLastPlaySeat();
		ZTMenjiRole role = table.getMembers().get(seat - 1);
		MenjiRoomCsv menjiRoomCsv = menjiRoomCache.getConfig(table.getGame()
				.getRoomType());
		GMsg_12013004.Builder builder = GMsg_12013004.newBuilder();
		table.setCoolDownTime(System.currentTimeMillis()+menjiRoomCsv.getTurnDuration() * 1000);
		LogUtil.info("当前时间：" + System.currentTimeMillis());
		LogUtil.info("通知" + table.getLastPlaySeat() + "号玩家操作");
		LogUtil.info(role.getRole().getRole().getNick());
		builder.setInfo(table.serialize());
		
		
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());
		if(role.getRole().isAuto()){
			menjiFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(), HandleType.MENJI_GAME_AUTO, System.currentTimeMillis()+2000);
			
		}else{
			menjiFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(), HandleType.MENJI_GAME_AUTO, System.currentTimeMillis()+menjiRoomCsv.getTurnDuration() * 1000);
			
		}
		//LogUtil.info(table.getGame().getRoomId()+" seat "+seat +" MENJI_GAME ");	
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.MENJI_GAME;
	}


    

}
