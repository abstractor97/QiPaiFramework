/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.service;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.csv.cache.MajiangChengDuRoomCache;
import com.yaowan.csv.entity.MajiangChengDuRoomCsv;
import com.yaowan.framework.core.GlobalVar;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GBaseMahJong;
import com.yaowan.protobuf.game.GBaseMahJong.OptionsType;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042001;
import com.yaowan.protobuf.game.GCDMahJong.GMsg_12042011;
import com.yaowan.protobuf.game.GGame.GMsg_12006005;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.CDMajiangFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;

/**
 * 成都麻将
 *
 * @author yangbin
 */
@Component
public class CDMajiangService{
	
	@Autowired
	private MajiangChengDuRoomCache majiangRoomCache;

	@Autowired
	private SingleThreadManager manager;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private RoomFunction roomFunction;
	
	@Autowired
	private CDMajiangFunction majiangFunction;

	@Autowired
	GlobalVar globalVar;

	

	public Map<Long, ZTMaJongTable> timerQueue = new ConcurrentHashMapV8<>();

	

	/**
	 * 玩家准备 (现在测试只要前端有一个玩家准备 就创建一桌牌局)
	 *
	 * @param player
	 */
	public void playerPrepare(Player player) {
		ZTMajiangRole role = majiangFunction.getRole(player.getRole().getRid());
		if (role == null) {
			GMsg_12042001.Builder builder = GMsg_12042001.newBuilder();
			builder.setSeat(0);
			player.write(builder.build());
			return;
		}
		ZTMaJongTable table = majiangFunction.getTableByRole(player.getRole().getRid());
		if (table == null) {
			GMsg_12042001.Builder builder = GMsg_12042001.newBuilder();
			builder.setSeat(0);
			player.write(builder.build());
			return;
		}
		
		Game game = table.getGame();
		majiangFunction.resetOverTable(game);
		
		MajiangChengDuRoomCsv majiangRoomCsv = majiangRoomCache.getConfig(game
				.getRoomType());
		boolean flag = false;
		if (majiangRoomCsv.getEnterUpperLimit() == -1) {
			if (role.getRole().getRole().getGold() < majiangRoomCsv
					.getEnterLowerLimit()) {
				flag = true;
			}
		} else {
			if (role.getRole().getRole().getGold() < majiangRoomCsv
					.getEnterLowerLimit()
					|| role.getRole().getRole().getGold() > majiangRoomCsv
							.getEnterUpperLimit()) {
				flag = true;
			}
		}
		if (flag) {
			GMsg_12006005.Builder builder = GMsg_12006005
					.newBuilder();
			builder.setCurrentSeat(role.getRole().getSeat());
			roleFunction.sendMessageToPlayers(
					game.getRoles(), builder.build());
			roomFunction.endGame(game);
			return;
		}

		role.getRole().setStatus(PlayerState.PS_PREPARE_VALUE);

		GMsg_12042001.Builder builder = GMsg_12042001.newBuilder();
		builder.setSeat(role.getRole().getSeat());
		roleFunction.sendMessageToPlayers(table.getGame().getRoles(),
				builder.build());

		boolean isAllPrepare = true;
		List<ZTMajiangRole> members = table.getMembers();
		for (ZTMajiangRole majiangRole : members) {
			if (majiangRole.getRole().getStatus() != PlayerState.PS_PREPARE_VALUE) {
				isAllPrepare = false;
			}
		}

		if (isAllPrepare) {
			// 全部已经准备 开始麻将
			/*manager.executeTask(new SingleThreadTask(table) {
				@Override
				public void doTask(ISingleData singleData) {
					// 全部已经准备 开始麻将
					ZTMaJongTable table = (ZTMaJongTable) singleData;
					majiangFunction.startTable(table.getGame());
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
	public void playHand(Player player, final GBaseMahJong.OptionsType optionsType,
			final List<Integer> pai) {

		LogUtil.error(optionsType+"player"+player.getRole().getNick());
		final ZTMajiangRole member = majiangFunction.getRole(player.getRole()
				.getRid());
		long tableId = member.getRole().getRoomId();
		ZTMaJongTable table = majiangFunction.getTable(tableId);
		if (table == null) {
			LogUtil.error("出牌操作 找不到对应牌局 table id " + tableId);
			return;
		}
		/*
		 * if (table.getLastPlaySeat() != member.getRole().getSeat()) {
		 * LogUtil.error(table.getLastPlaySeat() + "不是轮到此玩家" +
		 * member.getRole().getSeat()); return; }
		 */

		if (optionsType == OptionsType.DISCARD_TILE
				&& (pai == null || pai.size() == 0)) {
			LogUtil.error("出牌操作 空牌" + player.getId());
			return;
		}
		
		if (optionsType == OptionsType.SET_QUE_TYPE 
				&& (pai == null || pai.size() == 0 || (pai.get(0) != 1 && pai.get(0) != 2 && pai.get(0) != 3))) {
			LogUtil.error("定缺操作 空牌 or 缺牌类型不合法" + player.getId());
			return;
		}
		
		manager.executeTask(new SingleThreadTask(table) {
			@Override
			public void doTask(ISingleData singleData) {
				ZTMaJongTable table = (ZTMaJongTable) singleData;
				int type = table.getQueueWaitType();
				try {
					switch (optionsType.getNumber()) {
					case OptionsType.SET_QUE_TYPE_VALUE: {// 设置缺一门类型
						System.out.println("SET_QUE_TYPE table.getQueueWaitType()"
								+ table.getQueueWaitType());
						
						majiangFunction.setQueType(table, member, pai.get(0));
						break;
					}
					case OptionsType.PASS_VALUE: {// 过
						System.out.println("pass table.getQueueWaitType()"
								+ table.getQueueWaitType());
						majiangFunction.dealPass(table, member);
						break;
					}
					case OptionsType.PENG_VALUE: {// 碰
						System.out
								.println("PENG_VALUE table.getQueueWaitType()"
										+ table.getQueueWaitType());
						if (table.getQueueWaitType() == HandleType.MAJIANG_WAIT
								|| table.getQueueWaitType() == HandleType.MAJIANG_HU) {
							/*
							 * if(majiangFunction.checkOrder(table, role,
							 * type)){
							 * 
							 * }
							 */
							majiangFunction.dealPeng(table, member);
							majiangFunction.checkTingPai(table, member);
						}

						break;
					}	
					case OptionsType.EXPOSED_GANG_VALUE: {// 明杠
						if (table.getQueueWaitType() == HandleType.MAJIANG_WAIT
								|| table.getQueueWaitType() == HandleType.MAJIANG_HU) {
							
							majiangFunction.dealGang(table, member);						
							majiangFunction.checkTingPai(table, member);
						}

						break;
					}
					case OptionsType.DARK_GANG_VALUE: {// 暗杠
						if (table.getQueueWaitType() == HandleType.MAJIANG_OUT_PAI
								|| table.getQueueWaitType() == HandleType.MAJIANG_HU|| table.getQueueWaitType() == HandleType.MAJIANG_WAIT) {
							majiangFunction.dealAnGang(table, member,
									pai.get(0));
							majiangFunction.checkTingPai(table, member);
						}
						break;
					}
					case OptionsType.EXTRA_GANG_VALUE: {// 补杠
						if (table.getQueueWaitType() == HandleType.MAJIANG_OUT_PAI
								|| table.getQueueWaitType() == HandleType.MAJIANG_HU|| table.getQueueWaitType() == HandleType.MAJIANG_WAIT) {
							if (pai == null) {
								List<Integer> data = new ArrayList<Integer>();
								data.add(0);
								majiangFunction.dealExtraGang(table, member,
										data.get(0));
							} else {
								majiangFunction.dealExtraGang(table, member,
										pai.get(0));
							}
							majiangFunction.checkTingPai(table, member);
						}
						break;
					}
					case OptionsType.DISCARD_TILE_VALUE: {// 出牌
						if (table.getQueueWaitType() == HandleType.MAJIANG_OUT_PAI
								|| table.getQueueWaitType() == HandleType.MAJIANG_HU) {
							majiangFunction.dealDisCard(table, pai.get(0));
						}
						break;
					}
					case OptionsType.ANNOUNCE_WIN_VALUE: {// 胡牌
						if (table.getQueueWaitType() == HandleType.MAJIANG_HU) {
							majiangFunction.dealHu(table, member);
							table.getCanOptions().clear();
							table.getWaiter().clear();
						}
						break;
					}
					}
				} catch (Exception e) {
					e.printStackTrace();
					table.setQueueWaitType(type);
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
		ZTMajiangRole role = majiangFunction.getRole(player.getRole().getRid());
		if (role == null) {
			return;
		}
		ZTMaJongTable table = majiangFunction.getTableByRole(player.getRole().getRid());
		if (table == null) {
			return;
		}

		role.getRole().setStatus(PlayerState.PS_FLASH_VALUE);

		

		boolean isAllPrepare = true;
		List<ZTMajiangRole> members = table.getMembers();
		for (ZTMajiangRole otherRole : members) {
			if (otherRole.getRole().getStatus() != 3) {
				isAllPrepare = false;
			}
		}

		if (isAllPrepare) {
			// 全部已经准备 开始麻将
			//majiangFunction.startTable(doudizhuTable.getGame());
		}
		
		// 暂时不需要完成
	}
	
	
	public void isAuto(Player player, int isAuto) {
		ZTMajiangRole role = majiangFunction.getRole(player.getRole()
				.getRid());
		if (role == null) {
			return;
		}
		role.getRole().setAuto(isAuto == 1 ? true : false);
		ZTMaJongTable table = majiangFunction.getTableByRole(player.getRole()
				.getRid());
		if (table == null) {
			return;
		}
		if (role.getRole().isAuto()) {
			if (table.getLastPlaySeat() == role.getRole().getSeat()
					&& table.getQueueWaitType() == HandleType.MAJIANG_OUT_PAI) {
				majiangFunction.tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
						System.currentTimeMillis() + 300);
			}
		} else {
			if (table.getLastPlaySeat() == role.getRole().getSeat()
					&& table.getQueueWaitType() == HandleType.MAJIANG_OUT_PAI) {
				majiangFunction.tableToWait(table, table.getLastPlaySeat(),
						table.getNextPlaySeat(), HandleType.MAJIANG_OUT_PAI,
						table.getTargetTime());
			}
		}
		
		GMsg_12042011.Builder builder = GMsg_12042011.newBuilder();
		builder.setIsAuto(isAuto);
		roleFunction.sendMessageToPlayer(player.getRole().getRid(), builder.build());
	}



	public void endTable(Player player) {
		ZTMaJongTable table = majiangFunction.getTableByRole(player.getRole().getRid());
		majiangFunction.endTable(table);
	}



	public void tingPai(Player player) {
		ZTMaJongTable table = majiangFunction.getTableByRole(player.getRole().getRid());
		ZTMajiangRole role = majiangFunction.getRole(player.getRole().getRid());
		majiangFunction.tingPai(table, role);
	}
}
