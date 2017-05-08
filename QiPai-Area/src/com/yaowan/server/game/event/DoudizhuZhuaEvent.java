/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.event;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.csv.cache.DoudizhuRoomCache;
import com.yaowan.csv.entity.DoudizhuRoomCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.protobuf.game.GDouDiZhu.DouDiZhuAction;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012004;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.model.struct.Card;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTDoudizhuTable;
import com.yaowan.server.game.rule.ZTDoudizhuRule;

/**
 * 
 *
 * @author zane
 */
@Component
public class DoudizhuZhuaEvent extends EventHandlerAdapter{

	
	@Autowired
	private ZTDoudizhuFunction doudizhuFunction;

	@Autowired
	private RoleFunction roleFunction;
	
	@Autowired
	private DoudizhuRoomCache doudizhuRoomCache;

	@Override
	public int execute(Event event) {
		ZTDoudizhuTable table = (ZTDoudizhuTable) event.getParam()[0];
		int seat = table.getLastPlaySeat();
		
		ZTDoudizhuRole role = table.getMembers().get(table.getLastPlaySeat()-1);
		
		DoudizhuRoomCsv doudizhuRoomCsv = doudizhuRoomCache.getConfig(table
				.getGame().getRoomType());
		
		// 三次不抓 直接闷抓
		
		if (table.getNoZhuaCount()>=2) {
			table.setWaitAction(DouDiZhuAction.GA_PLAYING);
			doudizhuFunction.tableToWait(table, table.getLastPlaySeat(),
					table.getNextPlaySeat(), HandleType.DOUDIZHU_LAST_MEN_ZHUA,
					System.currentTimeMillis() + 1000);
		} else {
			DouDiZhuAction action = DouDiZhuAction.GA_DARK_GRAB;
			if (role.getLookedPai() == 1) {
				action = DouDiZhuAction.GA_MING_GRAB;
			}
			table.setWaitAction(action);
			GMsg_12012004.Builder builder = GMsg_12012004.newBuilder();
			builder.setCurrentSeat(seat);
			builder.setAction(action);
			if(doudizhuRoomCsv != null){
				builder.setWaitTime(TimeUtil.time() + doudizhuRoomCsv.getCatchTime());
			}else{
				builder.setWaitTime(TimeUtil.time() + 1000);
			}
						
			roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
					builder.build());
			//LogUtil.info("GMsg_12012004"+GMsg_12012004);
			long time = System.currentTimeMillis();
			if(doudizhuRoomCsv != null){
				time= time + 1000*doudizhuRoomCsv.getCatchTime();
			}else{
				time= time + 1000;
			}
			if(role.getRole().isRobot()){
				time = System.currentTimeMillis() + 1000;
			}
			
			List<Card> cards = new ArrayList<Card>();
			for (Integer id : table.getMembers().get(seat-1).getPai()) {
				
				cards.add(new Card(id));
				
			}
			
			if(table.getMembers().get(seat-1).getRole().isRobot() && ZTDoudizhuRule.include2WangAnd4ErOfGt3(cards)){//有概率闷抓
				doudizhuFunction.tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.DOUDIZHU_MEN_ZHUA,
						time);
			}else{
				doudizhuFunction.tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.DOUDIZHU_ZHUA_NO,
						time);
			}
		}
		
		LogUtil.info(table.getGame().getRoomId()+" seat "+seat +" DoudizhuZhuaEvent ");	

		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.DOUDIZHU_ZHUA;
	}


    

}
