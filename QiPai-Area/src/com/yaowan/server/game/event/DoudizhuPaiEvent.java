/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.csv.cache.DoudizhuRoomCache;
import com.yaowan.csv.entity.DoudizhuRoomCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.protobuf.game.GDouDiZhu.DouDiZhuAction;
import com.yaowan.protobuf.game.GDouDiZhu.GDouDiZhuPai;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012004;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012007;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTDoudizhuTable;

/**
 * 
 *
 * @author zane
 */
@Component
public class DoudizhuPaiEvent extends EventHandlerAdapter{

	
	@Autowired
	private ZTDoudizhuFunction doudizhuFunction;

	@Autowired
	private RoleFunction roleFunction;
	
	@Autowired
	private DoudizhuRoomCache doudizhuRoomCache;

	@Override
	public int execute(Event event) {
		//这是只处理一次的事件
		ZTDoudizhuTable table = (ZTDoudizhuTable) event.getParam()[0];
		table.setWaitAction(DouDiZhuAction.GA_PLAYING);
		
		int seat = table.getLastPlaySeat();
		
		DoudizhuRoomCsv doudizhuRoomCsv = doudizhuRoomCache.getConfig(table
				.getGame().getRoomType());
		
		GMsg_12012004.Builder builder = GMsg_12012004.newBuilder();
		builder.setCurrentSeat(seat);
		builder.setAction(DouDiZhuAction.GA_PLAYING);
		if(doudizhuRoomCsv != null){
			builder.setWaitTime(TimeUtil.time() + doudizhuRoomCsv.getFristTime());
		}else{
			builder.setWaitTime(TimeUtil.time() + 1000);
		}
				
		for(ZTDoudizhuRole role:table.getMembers()){
			GMsg_12012007.Builder bd = GMsg_12012007.newBuilder();
			GDouDiZhuPai.Builder value = GDouDiZhuPai.newBuilder();
			value.addAllPai(role.getPai());
			bd.setPai(value);
			roleFunction.sendMessageToPlayer(role.getRole().getRole().getRid(), bd.build());
		}
		
		ZTDoudizhuRole role = table.getMembers().get(table.getLastPlaySeat()-1);
		
		
		
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());
		
		long time = System.currentTimeMillis();
		if(doudizhuRoomCsv != null){
			time = time + 1000*doudizhuRoomCsv.getFristTime();
		}else{
			time = time + 1000;
		}
		if(role.getRole().isRobot()){
			time = System.currentTimeMillis() + 1000;
		}
		
		table.setLastPlaySeat(table.getOwner());
		if(table.getMembers().get(table.getLastPlaySeat()-1).getRole().isAuto()){
			doudizhuFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(),
					HandleType.DOUDIZHU_OUT_PAI, System.currentTimeMillis() + 3000);
		}else{
			doudizhuFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(),
					HandleType.DOUDIZHU_OUT_PAI, time);
		}
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.DOUDIZHU_PAI;
	}


    

}
