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
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.protobuf.game.GDouDiZhu.DouDiZhuAction;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012004;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
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
public class DoudizhuDaoEvent extends EventHandlerAdapter {

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
		ZTDoudizhuRole role = table.getMembers().get(
				table.getLastPlaySeat() - 1);

		GMsg_12012004.Builder builder = GMsg_12012004.newBuilder();

		DouDiZhuAction action = null;
		int handleType = 0;
		if (seat == table.getOwner()) {
			if (table.getDaoCount() >= 1) {// 只有倒了才能拉
				action = DouDiZhuAction.GA_PULL;
				
				
				
				handleType = HandleType.DOUDIZHU_LA_NO;
				
			} else {
				action = DouDiZhuAction.GA_PLAYING;
				handleType = HandleType.DOUDIZHU_PAI;

			}
		} else {
//			List<Integer> pai = role.getPai();
//			List<Card> cardList = new ArrayList<Card>();
//			for (Integer integer : pai) {
//				cardList.add(new Card(integer));
//			}
//			if (table.getZhuaType() != 1) {// 闷抓不倒
//				/*if (ZTDoudizhuRule.dao(cardList)) {
//					
//					 * doudizhuFunction.dealDao(table, 1); // 必倒通知
//					 * GMsg_12012011.Builder builder2 =
//					 * GMsg_12012011.newBuilder(); builder2.setAction(2);
//					 * roleFunction
//					 * .sendMessageToPlayer(role.getRole().getRole().getRid(),
//					 * builder2.build());
//					 
//
//					doudizhuFunction.processAction(table, DouDiZhuAction.GA_LOOK_CARD, seat,
//							HandleType.DOUDIZHU_BI_DAO, System.currentTimeMillis() + 300);
//					return 0;
//				}*/
//			}
			action = DouDiZhuAction.GA_UPSIDE;
			
//			if(table.getMembers().get(seat-1).getRole().isRobot() && ZTDoudizhuRule.include2WangAnd4ErOfGt3(table.getMembers().get(seat-1).getCards())){
//				handleType = HandleType.DOUDIZHU_TO_DAO;
//			}
			//是否机器人
			boolean isRobot = table.getMembers().get(seat-1).getRole().isRobot();
			//机器人是否作弊
			boolean isCheat = table.getMembers().get(seat-1).getRole().isCheat();
			//是否大牌大于3
			boolean isBigCardOverThree = ZTDoudizhuAiRule.getBigCardNum(table.getMembers().get(seat-1).getPai()) >= 4;
			//是否权重大于3
			boolean isWeightOverThree = ZTDoudizhuAiRule.getWeight(table.getMembers().get(seat-1).getPai(), table.getPais(), table, role) >= 3;
			
			if(isRobot && !isCheat && isBigCardOverThree){
				handleType = HandleType.DOUDIZHU_TO_DAO;
			}else if(isRobot && isCheat && isWeightOverThree){
				handleType = HandleType.DOUDIZHU_TO_DAO;
			} else{
				handleType = HandleType.DOUDIZHU_DAO_NO;
			}
		}
		DoudizhuRoomCsv doudizhuRoomCsv = doudizhuRoomCache.getConfig(table
				.getGame().getRoomType());
		
		
		builder.setCurrentSeat(seat);
		builder.setAction(action);
		if(doudizhuRoomCsv != null){
			builder.setWaitTime(TimeUtil.time() + doudizhuRoomCsv.getCatchTime());
		}else{
			builder.setWaitTime(TimeUtil.time() + 1000);
		}
		

		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		
		long time = System.currentTimeMillis() + 1000;
		if(doudizhuRoomCsv != null){

			time = System.currentTimeMillis() + 1000 * doudizhuRoomCsv.getCatchTime();
		}else{
			time = time + 1000;
		}

		if (role.getRole().isRobot()) {
			time = System.currentTimeMillis() + 1000;
		}

		if (handleType == HandleType.DOUDIZHU_PAI) {
			time = System.currentTimeMillis() + 500;
		}

		doudizhuFunction.tableToWait(table, table.getLastPlaySeat(),
				table.getNextPlaySeat(), handleType, time);

		LogUtil.info(table.getGame().getRoomId() + " seat " + seat + " "
				+ action);

		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.DOUDIZHU_DAO;
	}

}
