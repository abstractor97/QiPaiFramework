/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.server.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameError;
import com.yaowan.constant.GameStatus;
import com.yaowan.csv.cache.MenjiRoomCache;
import com.yaowan.csv.cache.NiuniuRoomCache;
import com.yaowan.csv.entity.NiuniuRoomCsv;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025001;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025003;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025008;
import com.yaowan.protobuf.game.GDouniu.GMsg_12025009;
import com.yaowan.server.game.function.DouniuFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.struct.DouniuRole;
import com.yaowan.server.game.model.struct.DouniuTable;
import com.yaowan.server.game.model.struct.DouniuXian;

/**
 * 斗牛
 *
 * @author zane
 */
@Component
public class DouniuService {
	
	@Autowired
	private MenjiRoomCache menjiRoomCache;

	@Autowired
	private SingleThreadManager manager;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private RoomFunction roomFunction;
	
	@Autowired
	private DouniuFunction douniuFunction;

	@Autowired
	private NiuniuRoomCache niuniuRoomCache;
	/**
	 * 玩家准备可进行投注
	 *
	 * @param player
	 */
	public void playerPrepare(Player player) {
		final DouniuRole member = douniuFunction.getRole(player.getRole().getRid());
		if (member == null) {
			GMsg_12025001.Builder builder = GMsg_12025001.newBuilder();
			builder.setFlag(GameError.MATCH_NOT_MATCHING);
			player.write(builder.build());
			return;
		}
		DouniuTable table = douniuFunction.getTableByRole(player.getRole().getRid());
		if (table == null) {
			GMsg_12025001.Builder builder = GMsg_12025001.newBuilder();
			builder.setFlag(GameError.MATCH_NOT_MATCHING);
			player.write(builder.build());
			return;
		}
		
		manager.executeTask(new SingleThreadTask(table) {

			@Override
			public void doTask(ISingleData singleData) {
				DouniuTable table = (DouniuTable) singleData;
				douniuFunction.playerPrepare(table.getGame(), member.getRole());

			}
		});
	}
	
	/**
	 * 客户端进入房间
	 *
	 * @param member
	 */
	public void enterTable(Player player,long roomId) {
		
		//LogUtil.debug("=======rid:" + player.getRole().getRid() + ", roomId:" + roomId);
		
		DouniuTable table = null;
		if (roomId == 0) {
			table = douniuFunction.getTableByRole(player.getRole().getRid());
			if (table == null) {
				return;
			}
		} else {
			table = douniuFunction.getTable(roomId);
			if (table == null) {
				return;
			}
		}
		final Role role = player.getRole();
		manager.executeTask(new SingleThreadTask(table) {

			@Override
			public void doTask(ISingleData singleData) {
				DouniuTable table = (DouniuTable) singleData;
				douniuFunction.enterTable(table.getGame(), role);
			}
		});
	}
	
	
	/**
	 * 玩家申请当王
	 *
	 * @param player
	 */
	public void applyOwner(Player player) {
		final DouniuRole member = douniuFunction.getRole(player.getRole().getRid());
		if (member == null) {
			GMsg_12025009.Builder builder = GMsg_12025009.newBuilder();
			builder.setFlag(GameError.MATCH_NOT_MATCHING);
			player.write(builder.build());
			return;
		}
		DouniuTable table = douniuFunction.getTableByRole(player.getRole().getRid());
		if (table == null) {
			GMsg_12025009.Builder builder = GMsg_12025009.newBuilder();
			builder.setFlag(GameError.MATCH_NOT_MATCHING);
			player.write(builder.build());
			return;
		}
		
		manager.executeTask(new SingleThreadTask(table) {

			@Override
			public void doTask(ISingleData singleData) {
				DouniuTable table = (DouniuTable) singleData;
				douniuFunction.applyOwner(table, member);

			}
		});
	}
	
	/**
	 * 玩家离开王
	 *
	 * @param player
	 */
	public void cancelOwner(Player player) {
		final DouniuRole member = douniuFunction.getRole(player.getRole().getRid());
		if (member == null) {
			return;
		}
		DouniuTable table = douniuFunction.getTableByRole(player.getRole().getRid());
		if (table == null) {
			return;
		}
		
		manager.executeTask(new SingleThreadTask(table) {

			@Override
			public void doTask(ISingleData singleData) {
				DouniuTable table = (DouniuTable) singleData;
				douniuFunction.cancelOwner(table, member);

			}
		});
	}


	/**
	 * 下注
	 *
	 * @param member
	 */
	public void putChip(Player player, int index, int chipIndex) {
		DouniuRole member = douniuFunction.getRole(player.getRole().getRid());
		if (member == null) {
			return;
		}
		DouniuTable table = douniuFunction.getTableByRole(player.getRole().getRid());
		if (table == null) {
			return;
		}

		if (table.getFightOwner().contains(player.getRole().getRid())) {
			return;
		}

		if (table.getGame().getStatus() != GameStatus.RUNNING) {
			return;
		}

		NiuniuRoomCsv niuniuRoomCsv = niuniuRoomCache.getConfig(table.getGame().getRoomType());
		DouniuXian douniuXian =table.getXians().get(index-1);
		int gold = niuniuRoomCsv.getBetValueList().get(chipIndex - 1);
		
		douniuXian.setTotalGold(douniuXian.getTotalGold() + gold);
		douniuXian.getChips().add(chipIndex);

		int total = 0;
		for (Map.Entry<Integer, Integer> entry : member.getChips().entrySet()) {
			total += entry.getValue();
		}
		total += gold;
			
		// 要拥有十倍注
		if (member.getRole().getRole().getGold() < total * 10) {
			GMsg_12025003.Builder builder = GMsg_12025003.newBuilder();
			builder.setChipIndex(chipIndex);
			builder.setIndex(index);
			builder.setChipResult(1); // 错误类型1
			player.write(builder.build());	
			
			return;
		}
		
		// 所有庄家下注和
		int zhuangTotalGold = 0;
		for (Long id : table.getFightOwner()) {
			DouniuRole zhuangMember = table.getMembers().get(id);
			Role zhuangRole = zhuangMember.getRole().getRole();
			zhuangTotalGold += zhuangRole.getGold();
		}
		
		// 所有闲家下注和不能超过所有庄家钱数和的1/10
		int xianTotalChip = table.getXianTotalChip();
		
		//LogUtil.debug("xianTotalChip: " + xianTotalChip + ", zhuangTotalGold: " + zhuangTotalGold);
		if (zhuangTotalGold < xianTotalChip * 10) {
			GMsg_12025003.Builder builder = GMsg_12025003.newBuilder();
			builder.setChipIndex(chipIndex);
			builder.setIndex(index);
			builder.setChipResult(2); // 错误类型2
			player.write(builder.build());	
			return;
		}
		
		//玩家
		Integer value = member.getChips().get(index);
		if (value == null) {
			member.getChips().put(index, gold);
		} else {
			member.getChips().put(index, value + gold);
		}
		
		table.addXianTotalChip(gold);
		
		GMsg_12025003.Builder builder = GMsg_12025003.newBuilder();
		builder.setChipIndex(chipIndex);
		builder.setIndex(index);
		builder.setChipResult(0);
		player.write(builder.build());

		Long rid = player.getId();
		List<Long> idList = new ArrayList<Long>();
		idList.addAll(table.getGame().getRoles());
		idList.remove(rid);
		GMsg_12025008.Builder zhubuilder = GMsg_12025008.newBuilder();
		zhubuilder.setChipIndex(chipIndex);
		zhubuilder.setIndex(index);
		roleFunction.sendMessageToPlayers(idList, zhubuilder.build());
	}
}
