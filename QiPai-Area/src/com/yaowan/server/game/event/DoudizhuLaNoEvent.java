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

import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.model.struct.Card;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTDoudizhuTable;
import com.yaowan.server.game.rule.ZTDoudizhuAiRule;
import com.yaowan.server.game.rule.ZTDoudizhuRule;

/**
 * 
 *
 * @author zane
 */
@Component
public class DoudizhuLaNoEvent extends EventHandlerAdapter{

	
	@Autowired
	private ZTDoudizhuFunction doudizhuFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Override
	public int execute(Event event) {
		ZTDoudizhuTable table = (ZTDoudizhuTable) event.getParam()[0];
		int seat = table.getLastPlaySeat();
		ZTDoudizhuRole ztDoudizhuRole = table.getMembers().get(seat - 1);
		List<Card> cards = new ArrayList<Card>();
		for (Integer id : table.getMembers().get(seat-1).getPai()) {
			cards.add(new Card(id));
		}
//		if(ZTDoudizhuRule.include2WangAnd4ErOfGt3(cards) && ztDoudizhuRole.getRole().isRobot()){
//			doudizhuFunction.dealLa(table, 1);
//		}
		//是否是机器人
		boolean isRobot = ztDoudizhuRole.getRole().isRobot();
		//是否机器人作弊
		boolean isCheat = ztDoudizhuRole.getRole().isCheat();
		//是否大牌大于5
		boolean isBigCardOverFive = ZTDoudizhuAiRule.getBigCardNum(table.getMembers().get(seat-1).getPai()) >= 5;
		//是否权重大于5
		boolean isWeightOverFive = ZTDoudizhuAiRule.getWeight(table.getMembers().get(seat-1).getPai(), table.getPais(), table, table.getMembers().get(seat-1)) >= 5;
		if(isRobot && !isCheat && isBigCardOverFive){
			doudizhuFunction.dealLa(table, 1);
		}else if(isRobot && isCheat && isWeightOverFive){
			doudizhuFunction.dealLa(table, 1);
		}else{
			doudizhuFunction.dealLa(table, 0);
		}
		
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.DOUDIZHU_LA_NO;
	}


    

}
