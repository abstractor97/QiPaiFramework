/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.event;


import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.csv.cache.MenjiRoomCache;
import com.yaowan.csv.entity.MenjiRoomCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.MathUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.protobuf.game.GMenJi.MJCardType;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTMenjiFunction;
import com.yaowan.server.game.model.struct.ZTMenjiAI;
import com.yaowan.server.game.model.struct.ZTMenjiRole;
import com.yaowan.server.game.model.struct.ZTMenjiTable;
import com.yaowan.server.game.rule.ZTMenji;

/**
 * 
 *
 * @author zane
 */
@Component
public class MenjiAIEvent extends EventHandlerAdapter {
	
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
		if(role.getRole().isRobot()){
			if(!role.isGoodPai()){
				//非作弊
				if(menjiFunction.menCompete(table, role)){
					LogUtil.info(role.getRole().getRole().getNick() + "蒙牌比牌");
					Event event2 = new Event(HandleType.MENJI_COMPETE,table);
					DispatchEvent.dispacthEvent(event2);
				}else{
					if(role.getLook() == 0){
						//还没看过牌
						if(menjiFunction.isLookPai(table, role)){
							LogUtil.info(role.getRole().getRole().getNick() + "非作弊看牌");
							Event event2 = new Event(HandleType.MENJI_LOOK,table);
							DispatchEvent.dispacthEvent(event2);
							long coolDownTime = System.currentTimeMillis() + MathUtil.randomNumber(1500, 3000);
							if(menjiFunction.lookCompete(table, role)){
								LogUtil.info(role.getRole().getRole().getNick() + "非作弊看牌比牌");
								menjiFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(), HandleType.MENJI_COMPETE, coolDownTime);
								return 0;
							}
							if(menjiFunction.isFold(role)){
								LogUtil.info(role.getRole().getRole().getNick() + "非作弊看牌弃牌");
								menjiFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(), HandleType.MENJI_QI_PAI, coolDownTime);
								return 0;
							}
							if(menjiFunction.isAddBet(table, role)){
								LogUtil.info(role.getRole().getRole().getNick() + "非作弊看牌加注");
								menjiFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(), HandleType.MENJI_ADD, coolDownTime);
								return 0;
							}
							if(menjiFunction.isFollow(table, role)){
								LogUtil.info(role.getRole().getRole().getNick() + "非作弊看牌跟注");
								menjiFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(), HandleType.MENJI_FOLLOW, coolDownTime);
								return 0;
							}
							LogUtil.info(role.getRole().getRole().getNick() + "非作弊最后看牌比牌");
							menjiFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(), HandleType.MENJI_COMPETE, coolDownTime);
							return 0;
						}else{
							//不选择看牌，优先进行加注判断，接着跟注，如果钱不够，选择比牌，此阶段无弃牌操作
							if(menjiFunction.isAddBet(table, role)){
								LogUtil.info(role.getRole().getRole().getNick() + "非作弊不看牌加注");
								Event event2 = new Event(HandleType.MENJI_ADD,table);
								DispatchEvent.dispacthEvent(event2);
								return 0;
							}
							if(menjiFunction.isFollow(table, role)){
								LogUtil.info(role.getRole().getRole().getNick() + "非作弊不看牌跟注");
								Event event2 = new Event(HandleType.MENJI_FOLLOW,table);
								DispatchEvent.dispacthEvent(event2);
								return 0;
							}
							LogUtil.info(role.getRole().getRole().getNick() + "非作弊不看牌最后比牌");
							Event event2 = new Event(HandleType.MENJI_COMPETE,table);
							DispatchEvent.dispacthEvent(event2);
							return 0;
						}
					}else{
						//看过牌了,优先进行比牌判断，接着加注，跟注，如果钱不够，选择比牌，此阶段无弃牌操作
						if(menjiFunction.lookCompete(table, role)){
							LogUtil.info(role.getRole().getRole().getNick() + "非作弊已看牌后比牌");
							Event event2 = new Event(HandleType.MENJI_COMPETE,table);
							DispatchEvent.dispacthEvent(event2);
							return 0;
						}
						if(menjiFunction.isAddBet(table, role)){
							LogUtil.info(role.getRole().getRole().getNick() + "非作弊已看牌后加注");
							Event event2 = new Event(HandleType.MENJI_ADD,table);
							DispatchEvent.dispacthEvent(event2);
							return 0;
						}
						if(menjiFunction.isFollow(table, role)){
							LogUtil.info(role.getRole().getRole().getNick() + "非作弊已看牌后跟注");
							Event event2 = new Event(HandleType.MENJI_FOLLOW,table);
							DispatchEvent.dispacthEvent(event2);
							return 0;
						}
						LogUtil.info(role.getRole().getRole().getNick() + "非作弊已看牌最后比牌");
						Event event2 = new Event(HandleType.MENJI_COMPETE,table);
						DispatchEvent.dispacthEvent(event2);
						return 0;
					}
				}
			}else{
				//作弊
				if(menjiFunction.isChectCompete(table, role)){
					LogUtil.info(role.getRole().getRole().getNick() + "作弊比牌");
					Event event2 = new Event(HandleType.MENJI_COMPETE,table);
					DispatchEvent.dispacthEvent(event2);
					return 0;
				}
				if(menjiFunction.isLookPai(table, role)){
					LogUtil.info(role.getRole().getRole().getNick() + "作弊看牌");
					Event event2 = new Event(HandleType.MENJI_LOOK,table);
					DispatchEvent.dispacthEvent(event2);
				}
				long coolDownTime = System.currentTimeMillis() + MathUtil.randomNumber(1500, 3000);
				if(menjiFunction.isAddBet(table, role)){
					LogUtil.info(role.getRole().getRole().getNick() + "作弊加注");
					menjiFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(), HandleType.MENJI_ADD, coolDownTime);
					return 0;
				}
				if(menjiFunction.isFollow(table, role)){
					LogUtil.info(role.getRole().getRole().getNick() + "作弊跟注");
					menjiFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(), HandleType.MENJI_FOLLOW, coolDownTime);
					return 0;
				}
				LogUtil.info(role.getRole().getRole().getNick() + "作弊最后比牌");
				menjiFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(), HandleType.MENJI_COMPETE, coolDownTime);
				return 0;
			}
		}else{
			menjiFunction.dealFold(table, role);
		}

		return 0;
	}
	
	

	@Override
	public int getHandle() {
		return HandleType.MENJI_GAME_AUTO;
	}

	
}
