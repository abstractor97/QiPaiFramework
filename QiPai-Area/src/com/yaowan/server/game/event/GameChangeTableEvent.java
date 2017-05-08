package com.yaowan.server.game.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameType;
import com.yaowan.csv.cache.NiuniuRoomCache;
import com.yaowan.csv.entity.NiuniuRoomCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAddListenerAdapter;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.Player;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.DouniuFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.service.RoomService;
@Component
public class GameChangeTableEvent extends EventHandlerAddListenerAdapter {
	@Autowired
	private RoomFunction roomFunction;
	@Autowired
	private RoomService roomService;
	
	@Autowired
	private SingleThreadManager manager;
	@Autowired
	private NiuniuRoomCache niuniuRoomCache;
	@Autowired
	private DouniuFunction douniuFunction;
	
	@Override
	public int getHandle() {
		
		return HandleType.GAME_CHANGE_TABLE;
	}

	public int process(Event event) {
		
		
		Player player = (Player) event.getParam()[0];
		
		LogUtil.info("changeTable:" + player.getRole().getNick());
		Game game = roomFunction.getGameByRole(player.getRole().getRid());
		
		if(game!=null && game.getGameType() == GameType.DEZHOU){//德州不走下面的流程
			return 0;
		}
		
		roomService.exitTable(player);
		final Role role = player.getRole();
		/*
		 * if(game!=null){ manager.executeTask(new SingleThreadTask(game) {
		 * 
		 * @Override public void doTask(ISingleData singleData) {
		 * 
		 * } });
		 * 
		 * }
		 */
		if (role.getLastGameType() == GameType.DOUNIU) {
			manager.executeTask(new SingleThreadTask(game) {
				@Override
				public void doTask(ISingleData singleData) {
					NiuniuRoomCsv csv = niuniuRoomCache.getConfig(role.getLastRoomType());
					int level = csv.getRoomLv();
					douniuFunction.enterTable(douniuFunction.findDouniuRooms(level,role.getLatelyGames()), role);
					}
			});
			
		}else{
			roomService.joinGame(
					player,
					role.getLastGameType(),
					roomFunction.checkRoomType(role.getLastGameType(),
							role.getLastRoomType(), role.getGold()));
		}
		return 0;
	}
	
}
