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
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.protobuf.game.GDouDiZhu.DouDiZhuAction;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012004;
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
public class DoudizhuPaiOutAutoEvent extends EventHandlerAdapter{

	
	@Autowired
	private ZTDoudizhuFunction doudizhuFunction;

	@Autowired
	private RoleFunction roleFunction;
	
	@Autowired
	private DoudizhuRoomCache doudizhuRoomCache;


	@Override
	public int execute(Event event) {
		ZTDoudizhuTable table = (ZTDoudizhuTable) event.getParam()[0];
		
		table.setWaitAction(DouDiZhuAction.GA_PLAYING);
		
		int seat = table.getLastPlaySeat();
		DoudizhuRoomCsv doudizhuRoomCsv = doudizhuRoomCache.getConfig(table
				.getGame().getRoomType());
		
		ZTDoudizhuRole role = table.getMembers().get(seat - 1);

		GMsg_12012004.Builder builder = GMsg_12012004.newBuilder();
		builder.setCurrentSeat(seat);
		builder.setAction(DouDiZhuAction.GA_PLAYING);
		if(doudizhuRoomCsv == null){
			builder.setWaitTime(TimeUtil.time() + 1000);
		}else{
			builder.setWaitTime(TimeUtil.time() + doudizhuRoomCsv.getCommontime());
		}
		

		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		if (role.getRole().isAuto()) {
			int think = MathUtil.randomNumber(1200, 2000);
			if (Math.random() > 0.9) {
				think = MathUtil.randomNumber(2000, 4000);
			}
			if(doudizhuRoomCsv == null){
				table.setTargetTime(System.currentTimeMillis() + 1000);
			}else{
				table.setTargetTime(System.currentTimeMillis() + 1000
						* doudizhuRoomCsv.getCommontime());
			}
			
			if(role.getRole().isRobot()){
				doudizhuFunction.tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.DOUDIZHU_OUT_PAI,
						System.currentTimeMillis() + think);
			}else{
				doudizhuFunction.tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.DOUDIZHU_OUT_PAI,
						0);
			}
			
		}else {
			if(doudizhuRoomCsv == null){
				doudizhuFunction.tableToWait(
						table,
						table.getLastPlaySeat(),
						table.getNextPlaySeat(),
						HandleType.DOUDIZHU_OUT_PAI,
						System.currentTimeMillis() + 1000);
			}else{
				doudizhuFunction.tableToWait(
						table,
						table.getLastPlaySeat(),
						table.getNextPlaySeat(),
						HandleType.DOUDIZHU_OUT_PAI,
						System.currentTimeMillis() + 1000
								* doudizhuRoomCsv.getCommontime());
			}
			
		}
		
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.DOUDIZHU_OUT_PAI_AUTO;
	}


    

}
