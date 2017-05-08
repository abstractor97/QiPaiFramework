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
import com.yaowan.framework.util.LogUtil;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012010;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.model.struct.Card;
import com.yaowan.server.game.model.struct.CardType;
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
public class DoudizhuPaiOutEvent extends EventHandlerAdapter {

	@Autowired
	private ZTDoudizhuFunction doudizhuFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Override
	public int execute(Event event) {
		ZTDoudizhuTable table = (ZTDoudizhuTable) event.getParam()[0];
		// TODO 优化自动出牌方案
		int seat = table.getLastPlaySeat();
		ZTDoudizhuRole role = table.getMembers().get(seat - 1);
		int size = table.getLastPai().size();
		if (!role.getRole().isAuto()) {
			role.setPassCount(role.getPassCount() + 1);
			if (role.getPassCount() > 1) {
				role.setPassCount(0);
				role.getRole().setAuto(true);
				GMsg_12012010.Builder builder = GMsg_12012010.newBuilder();
				builder.setIsAuto(1);
				roleFunction.sendMessageToPlayer(role.getRole().getRole()
						.getRid(), builder.build());
			} else {
				// pass
				if (size == 0) {
					List<Integer> out = new ArrayList<Integer>();
					boolean success = ZTDoudizhuRule.selfOutPai(table, role.getPai(), 17, 0, table.getRecyclePai(), out);
					if(success){
						doudizhuFunction.dealPaiOut(table, out.size() == 0? null:out);
						return 0;
					}
					List<Integer> data = ZTDoudizhuRule.selfOutPai(
							role.getPai(), 17, 0, table.getRecyclePai());
					if (data.size() > 0) {
						List<Card> list = new ArrayList<Card>();
						for (Integer id : data) {
							list.add(new Card(id));
						}
						CardType cardType = ZTDoudizhuRule.getCardType(list);
						if (cardType == null) {
							doudizhuFunction.dealPaiOut(table, null);
						} else {
							doudizhuFunction.dealPaiOut(table, data);
						}
					} else {
						doudizhuFunction.dealPaiOut(table, data);
					}
				} else if (table.getLastOutPai() == table.getLastPlaySeat()) {
					
					
					List<Integer> out = new ArrayList<Integer>();
					boolean success = ZTDoudizhuRule.selfOutPai(table, role.getPai(), doudizhuFunction
							.getEnemyMinSize(table, role),
							doudizhuFunction.getFriendMinSize(table,
									role), table.getRecyclePai(), out);
					if(success){
						doudizhuFunction.dealPaiOut(table, out.size() == 0? null:out);
						return 0;
					}
					
					doudizhuFunction.dealPaiOut(table, ZTDoudizhuRule
							.selfOutPai(role.getPai(), doudizhuFunction
									.getEnemyMinSize(table, role),
									doudizhuFunction.getFriendMinSize(table,
											role), table.getRecyclePai()));
				} else {
					doudizhuFunction.dealPaiOut(table, null);
				}
				return 0;
			}

		}
		
		int relate = 1;// 敌人
		if (role.getRole().getSeat() == table.getOwner()) {
			relate = 1;
		} else if (table.getLastOutPai() == table.getOwner()) {
			relate = 1;
		} else {
			relate = 2;
		}
		
		if(role.getRole().isRobot() && role.getRole().isCheat()) {
			doudizhuFunction.dealPaiOut(table, ZTDoudizhuAiRule.getCheatPai(table, role, role.getPai(), table.getLastPai(), relate));
		}else if(role.getRole().isRobot() && !role.getRole().isCheat()) {
			doudizhuFunction.dealPaiOut(table, ZTDoudizhuAiRule.getNoCheatPai(table, role, role.getPai(), table.getLastPai(), relate));
		}
		
		
//		if (role.getRole().isAuto() && role.getRole().isRobot()) {
//			if (size == 0) {
//				List<Integer> out = new ArrayList<Integer>();
//				boolean success = ZTDoudizhuRule.selfOutPai(table, role.getPai(), doudizhuFunction
//						.getEnemyMinSize(table, role),
//						doudizhuFunction.getFriendMinSize(table,
//								role), table.getRecyclePai(), out);
//				if(success){
//					doudizhuFunction.dealPaiOut(table, out.size() == 0? null:out);
//					return 0;
//				}
//				
//				doudizhuFunction.dealPaiOut(
//						table,
//						ZTDoudizhuRule.selfOutPai(role.getPai(), 17, 0,
//								table.getRecyclePai()));
//				return 0;
//			}
//
//			if (table.getLastOutPai() == table.getLastPlaySeat()) {
//				List<Integer> out = new ArrayList<Integer>();
//				boolean success = ZTDoudizhuRule.selfOutPai(table, role.getPai(), doudizhuFunction
//						.getEnemyMinSize(table, role),
//						doudizhuFunction.getFriendMinSize(table,
//								role), table.getRecyclePai(), out);
//				if(success){
//					doudizhuFunction.dealPaiOut(table, out.size() == 0? null:out);
//					return 0;
//				}
//				
//				
//				doudizhuFunction.dealPaiOut(table, ZTDoudizhuRule.selfOutPai(
//						role.getPai(),
//						doudizhuFunction.getEnemyMinSize(table, role),
//						doudizhuFunction.getFriendMinSize(table, role),
//						table.getRecyclePai()));
//				return 0;
//			}
//			int relate = 1;// 敌人
//			if (role.getRole().getSeat() == table.getOwner()) {
//				relate = 1;
//			} else if (table.getLastOutPai() == table.getOwner()) {
//				relate = 1;
//			} else {
//				relate = 2;
//			}
//			
//			List<Integer> out = new ArrayList<Integer>();
//			boolean success = ZTDoudizhuRule.getOutPai(table, role.getPai(), relate, doudizhuFunction.getEnemyMinSize(table,
//										role), doudizhuFunction
//										.getFriendMinSize(table, role),
//								table.getRecyclePai(), out);
//			if(success){
//				doudizhuFunction.dealPaiOut(table,out.size() == 0?null:out);
//				return 0;
//			}
//			if (relate == 2) {
//				ZTDoudizhuRole ownerRole = table.getMembers().get(
//						table.getOwner() - 1);
//				// 友军
//				int grade = ZTDoudizhuRule.getGrade(table.getLastPai().get(0));
//
//				if (table.getLastPai().size() == 1) {
//					if(role.getPai().size() == 1){
//						LogUtil.info("剩下一张牌一定会出");
//						List<Integer> data = ZTDoudizhuRule.getOutPai(table
//								.getLastPai(), role.getPai(), relate,
//								doudizhuFunction.getEnemyMinSize(table,
//										role), doudizhuFunction
//										.getFriendMinSize(table, role),
//								table.getRecyclePai());
//						doudizhuFunction.dealPaiOut(table, data);
//					}else{
//						if (grade > 9 && grade < 13) {
//							if (Math.random() > 0.7) {
//								List<Integer> data = ZTDoudizhuRule.getOutPai(table
//										.getLastPai(), role.getPai(), relate,
//										doudizhuFunction.getEnemyMinSize(table,
//												role), doudizhuFunction
//												.getFriendMinSize(table, role),
//										table.getRecyclePai());
//								if (data.size() > 0) {
//									if (ZTDoudizhuRule.getGrade(data.get(0)) >= grade + 2) {
//										doudizhuFunction.dealPaiOut(table, null);
//									} else {
//										doudizhuFunction.dealPaiOut(table, data);
//									}
//								} else {
//									doudizhuFunction.dealPaiOut(table, null);
//								}
//							} else {
//								doudizhuFunction.dealPaiOut(table, null);
//							}
//						} else {
//							List<Integer> data = ZTDoudizhuRule.getOutPai(
//									table.getLastPai(), role.getPai(), relate,
//									doudizhuFunction.getEnemyMinSize(table, role),
//									doudizhuFunction.getFriendMinSize(table, role),
//									table.getRecyclePai());
//							if (data.size() > 0) {
//								if (ZTDoudizhuRule.getGrade(data.get(0)) > 11) {
//									if (grade < 8) {
//										doudizhuFunction.dealPaiOut(table, data);
//									} else {
//										doudizhuFunction.dealPaiOut(table, null);
//									}
//								} else {
//									doudizhuFunction.dealPaiOut(table, data);
//								}
//							} else {
//								doudizhuFunction.dealPaiOut(table, null);
//							}
//						}
//					}
//					
//				} else if (table.getLastPai().size() == 2) {
//					if (ownerRole.getPai().size() == 1) {
//						doudizhuFunction.dealPaiOut(table, null);
//					} else {
//
//						if (grade > 8 && grade < 11) {
//							if (Math.random() > 0.7) {
//								List<Integer> data = ZTDoudizhuRule.getOutPai(
//										table.getLastPai(), role.getPai(),
//										relate, doudizhuFunction
//												.getEnemyMinSize(table, role),
//										doudizhuFunction.getFriendMinSize(
//												table, role), table
//												.getRecyclePai());
//								if (data.size() > 0) {
//									if (ZTDoudizhuRule.getGrade(data.get(0)) > grade + 2) {
//										doudizhuFunction
//												.dealPaiOut(table, null);
//									} else {
//										doudizhuFunction
//												.dealPaiOut(table, data);
//									}
//								} else {
//									doudizhuFunction.dealPaiOut(table, null);
//								}
//							} else {
//								doudizhuFunction.dealPaiOut(table, null);
//							}
//						} else {
//							List<Integer> data = ZTDoudizhuRule.getOutPai(table
//									.getLastPai(), role.getPai(), relate,
//									doudizhuFunction.getEnemyMinSize(table,
//											role), doudizhuFunction
//											.getFriendMinSize(table, role),
//									table.getRecyclePai());
//							if (data.size() > 0) {
//								if (ZTDoudizhuRule.getGrade(data.get(0)) > 8) {
//									if (grade < 6) {
//										doudizhuFunction
//												.dealPaiOut(table, data);
//									} else {
//										doudizhuFunction
//												.dealPaiOut(table, null);
//									}
//								} else {
//									doudizhuFunction.dealPaiOut(table, data);
//								}
//							} else {
//								doudizhuFunction.dealPaiOut(table, null);
//							}
//						}
//					}
//
//				} else {
//					doudizhuFunction.dealPaiOut(table, null);
//				}
//			} else {
//				doudizhuFunction.dealPaiOut(table, ZTDoudizhuRule.getOutPai(
//						table.getLastPai(), role.getPai(), relate,
//						doudizhuFunction.getEnemyMinSize(table, role),
//						doudizhuFunction.getFriendMinSize(table, role),
//						table.getRecyclePai()));
//			}
//
//		}
		
		if(role.getRole().isAuto() && !(role.getRole().isRobot())){
			if(size == 0 || table.getLastOutPai() == table.getLastPlaySeat()){
			doudizhuFunction.dealPaiOut(table, ZTDoudizhuRule.tuoGuanselfOutPai(role.getPai()));			
			return 0;
		}else{
			doudizhuFunction.dealPaiOut(table,ZTDoudizhuRule.tuoGuanOutPai(
					table.getLastPai(), role.getPai()));
		}
//		doudizhuFunction.dealPaiOut(table, ZTDoudizhuAiRule.getCheatPai(table, role, role.getPai(), table.getLastPai(), relate));
//		doudizhuFunction.dealPaiOut(table, ZTDoudizhuAiRule.getNoCheatPai(table, role, role.getPai(), table.getLastPai(), relate));
			
		}

		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.DOUDIZHU_OUT_PAI;
	}

}
