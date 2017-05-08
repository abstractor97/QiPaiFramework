/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.service;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.csv.cache.DoudizhuRoomCache;
import com.yaowan.csv.entity.DoudizhuRoomCsv;
import com.yaowan.framework.core.GlobalVar;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GDouDiZhu.DouDiZhuAction;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012001;
import com.yaowan.protobuf.game.GDouDiZhu.GMsg_12012010;
import com.yaowan.protobuf.game.GGame.GMsg_12006005;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.FriendRoomFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.model.data.entity.FriendRoom;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTDoudizhuTable;

/**
 * 昭通麻将
 *
 * @author zane
 */
@Component
public class ZTDoudizhuService {
	
	@Autowired
	private DoudizhuRoomCache doudizhuRoomCache;

	@Autowired
	private SingleThreadManager manager;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private RoomFunction roomFunction;

	@Autowired
	private ZTDoudizhuFunction doudizhuFunction;
	
	@Autowired
	private FriendRoomFunction friendRoomFunction;

	@Autowired
	GlobalVar globalVar;

	public Map<Long, ZTDoudizhuTable> timerQueue = new ConcurrentHashMapV8<>();

	/**
	 * 玩家准备 (现在测试只要前端有一个玩家准备 就创建一桌牌局)
	 *
	 * @param player
	 */
	public void playerPrepare(Player player) {
		ZTDoudizhuRole role = doudizhuFunction.getRole(player.getRole()
				.getRid());
		if (role == null) {
			GMsg_12012001.Builder builder = GMsg_12012001.newBuilder();
			builder.setSeat(0);
			player.write(builder.build());
			return;
		}
		ZTDoudizhuTable table = doudizhuFunction.getTableByRole(player
				.getRole().getRid());
		if (table == null) {
			GMsg_12012001.Builder builder = GMsg_12012001.newBuilder();
			builder.setSeat(0);
			player.write(builder.build());
			return;
		}
		Game game = table.getGame();
		doudizhuFunction.resetOverTable(game);
		
		DoudizhuRoomCsv doudizhuRoomCsv = doudizhuRoomCache.getConfig(game
				.getRoomType());
		boolean flag = false;
		if(!game.isFriendRoom()){
			if (doudizhuRoomCsv.getEnterUpperLimit() == -1) {
				if (role.getRole().getRole().getGold() < doudizhuRoomCsv
						.getEnterLowerLimit()) {
					flag = true;
				}
			} else {
				if (role.getRole().getRole().getGold() < doudizhuRoomCsv
						.getEnterLowerLimit()
						|| role.getRole().getRole().getGold() > doudizhuRoomCsv
								.getEnterUpperLimit()) {
					flag = true;
				}
			}
			if (flag) {
				GMsg_12006005.Builder builder = GMsg_12006005
						.newBuilder();
				builder.setCurrentSeat(role.getRole().getSeat());
				builder.setRoomId(game.getRoomId());
				roleFunction.sendMessageToPlayers(
						game.getRoles(), builder.build());
				roomFunction.endGame(game);
				return;
			}
		}

		role.getRole().setStatus(PlayerState.PS_PREPARE_VALUE);

		GMsg_12012001.Builder builder = GMsg_12012001.newBuilder();
		builder.setSeat(role.getRole().getSeat());
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		boolean isAllPrepare = true;
		List<ZTDoudizhuRole> members = table.getMembers();
		for (ZTDoudizhuRole doudizhuRole : members) {
			if(doudizhuRole == null) {
				isAllPrepare = false;
				continue;
			}
			if (doudizhuRole.getRole()!=null&&doudizhuRole.getRole().getStatus() != PlayerState.PS_PREPARE_VALUE) {
				isAllPrepare = false;
			}
			
		}

		if (isAllPrepare) {
			// 全部已经准备 开始斗地主
			/*manager.executeTask(new SingleThreadTask(table) {
				@Override
				public void doTask(ISingleData singleData) {
					ZTDoudizhuTable table = (ZTDoudizhuTable) singleData;
					doudizhuFunction.startTable(table.getGame());
				}
			});*/
		}

	}

	/**
	 * 玩家操作
	 *
	 * @param player
	 * @param member
	 *            谁出牌
	 * @param optionsType
	 * @param pai
	 * @param laizi
	 */
	public void playHand(Player player, final DouDiZhuAction action,
			final List<Integer> pai) {
		ZTDoudizhuRole member = doudizhuFunction.getRole(player.getRole()
				.getRid());
		LogUtil.info("玩家操作");
		if (member == null) {
			LogUtil.error("出牌操作 找不到对应牌局 table id ");
			return;
		}
		long tableId = member.getRole().getRoomId();
		ZTDoudizhuTable table = doudizhuFunction.getTable(tableId);
		if (table == null) {
			LogUtil.error("出牌操作 找不到对应牌局 table id " + tableId);
			return;
		}
		if (table.getLastPlaySeat() != member.getRole().getSeat()) {
			LogUtil.error(table.getLastPlaySeat() + "不是轮到此玩家"
					+ member.getRole().getSeat());
			return;
		}

		manager.executeTask(new SingleThreadTask(table) {
			@Override
			public void doTask(ISingleData singleData) {
				ZTDoudizhuTable table = (ZTDoudizhuTable) singleData;
				if(table.getGame().isFriendRoom()){
					 FriendRoom friendRoom = friendRoomFunction.getFriendRoom(table.getGame().getRoomId());
					 if(friendRoom != null){
						 friendRoom.setStartTime(System.currentTimeMillis());
					 }
				}
				if (action.equals(DouDiZhuAction.GA_DARK_GRAB)) {
					if (table.getQueueWaitType() == HandleType.DOUDIZHU_ZHUA_NO
							|| table.getQueueWaitType() == HandleType.DOUDIZHU_LOOK) {
						doudizhuFunction.dealZhua(table, 1);
					}
				} else if (action.equals(DouDiZhuAction.GA_MING_GRAB)) {
					if (table.getQueueWaitType() == HandleType.DOUDIZHU_ZHUA_NO
							|| table.getQueueWaitType() == HandleType.DOUDIZHU_LOOK) {
						doudizhuFunction.dealZhua(table, 2);
					}

				} else if (action.equals(DouDiZhuAction.GA_PASS_GRAB)) {
					if (table.getQueueWaitType() == HandleType.DOUDIZHU_ZHUA_NO
							|| table.getQueueWaitType() == HandleType.DOUDIZHU_LOOK) {
						doudizhuFunction.dealZhua(table, 0);
					}
				} else if (action.equals(DouDiZhuAction.GA_UPSIDE)) {
					if (table.getQueueWaitType() == HandleType.DOUDIZHU_DAO_NO) {
						doudizhuFunction.dealDao(table, 1);
					}
				} else if (action.equals(DouDiZhuAction.GA_PASS_UPSIDE)) {
					if (table.getQueueWaitType() == HandleType.DOUDIZHU_DAO_NO) {
						doudizhuFunction.dealDao(table, 0);
					}
				} else if (action.equals(DouDiZhuAction.GA_LOOK_CARD)) {
					if (table.getQueueWaitType() == HandleType.DOUDIZHU_ZHUA_NO
							|| table.getQueueWaitType() == HandleType.DOUDIZHU_LOOK) {
						doudizhuFunction.dealLook(table);
					}
				} else if (action.equals(DouDiZhuAction.GA_PULL)) {
					if (table.getQueueWaitType() == HandleType.DOUDIZHU_LA_NO) {
						doudizhuFunction.dealLa(table, 1);
					}
				} else if (action.equals(DouDiZhuAction.GA_PASS_PULL)) {
					if (table.getQueueWaitType() == HandleType.DOUDIZHU_LA_NO) {
						doudizhuFunction.dealLa(table, 0);
					}
				} else if (action.equals(DouDiZhuAction.GA_PLAYING)) {
					if (table.getQueueWaitType() == HandleType.DOUDIZHU_OUT_PAI) {
						LogUtil.info("pai" + pai);
						doudizhuFunction.dealPaiOut(table, pai);
					}
				} else if (action.equals(DouDiZhuAction.GA_PASS_PLAYING)) {
					if (table.getQueueWaitType() == HandleType.DOUDIZHU_OUT_PAI) {
						if (table.getLastOutPai() != table.getLastPlaySeat()) {
							doudizhuFunction.dealPaiOut(table, null);
						}

					}
				}
			}
		});

	}

	/**
	 * 客户端动画播放完成
	 *
	 * @param member
	 */
	public void clientFlashFinish(Player player) {
		ZTDoudizhuRole role = doudizhuFunction.getRole(player.getRole()
				.getRid());
		if (role == null) {
			return;
		}
		ZTDoudizhuTable doudizhuTable = doudizhuFunction.getTableByRole(player
				.getRole().getRid());
		if (doudizhuTable == null) {
			return;
		}

		role.getRole().setStatus(PlayerState.PS_FLASH_VALUE);

		boolean isAllPrepare = true;
		List<ZTDoudizhuRole> members = doudizhuTable.getMembers();
		for (ZTDoudizhuRole majiangRole : members) {
			if(majiangRole != null) {
				if (majiangRole.getRole().getStatus() != 3) {
					isAllPrepare = false;
				}
			}
		}

		if (isAllPrepare) {
			// 全部已经准备 开始麻将
			// doudizhuFunction.startTable(doudizhuTable.getGame());

		}

		// 暂时不需要完成
	}

	public void isAuto(Player player, int isAuto) {
		ZTDoudizhuRole role = doudizhuFunction.getRole(player.getRole()
				.getRid());
		if (role == null) {
			return;
		}
		role.getRole().setAuto(isAuto == 1 ? true : false);
		ZTDoudizhuTable table = doudizhuFunction.getTableByRole(player.getRole()
				.getRid());
		if(role.getRole().isAuto()){
			if (table.getLastPlaySeat() ==role.getRole().getSeat()&& table.getQueueWaitType() == HandleType.DOUDIZHU_OUT_PAI) {
				doudizhuFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(),
						HandleType.DOUDIZHU_OUT_PAI, System.currentTimeMillis()+300);
			}
		}else{
			if (table.getLastPlaySeat() ==role.getRole().getSeat()&& table.getQueueWaitType() == HandleType.DOUDIZHU_OUT_PAI) {
				doudizhuFunction.tableToWait(table, table.getLastPlaySeat(), table.getNextPlaySeat(),
						HandleType.DOUDIZHU_OUT_PAI, table.getTargetTime());
			}
		}
		
		GMsg_12012010.Builder builder = GMsg_12012010.newBuilder();
		builder.setIsAuto(isAuto);
		roleFunction.sendMessageToPlayer(player.getRole().getRid(),
				builder.build());
	}

}
