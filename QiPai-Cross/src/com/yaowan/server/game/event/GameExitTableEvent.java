package com.yaowan.server.game.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameStatus;
import com.yaowan.constant.GameType;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAddListenerAdapter;
import com.yaowan.framework.core.thread.ISingleData;
import com.yaowan.framework.core.thread.SingleThreadManager;
import com.yaowan.framework.core.thread.SingleThreadTask;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GGame.GMsg_12006005;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.model.data.entity.Role;
@Component
public class GameExitTableEvent extends EventHandlerAddListenerAdapter {
	@Autowired
	private RoomFunction roomFunction;
	@Autowired
	private RoleFunction roleFunction;
	@Autowired
	private SingleThreadManager singleThreadManager;
	@Override
	public int getHandle() {
		
		return HandleType.GAME_EXIT_TABLE;
	}

	public int process(Event event) {
		
		if(event.getParam() == null || !(event.getParam()[0] instanceof Role)){
			throw new RuntimeException("GameExitTableEvent ，传参错误 ,param [Role] : "+event.getParam()[0].getClass());
		}
		final Role role = (Role)event.getParam()[0];
		Game game = roomFunction.getGameByRole(role.getRid());
		
		if(game!=null && game.getGameType() == GameType.DEZHOU){//德州不走下面的流程
			return 0; 
		}
		
		if (game != null) {
			// 为了换桌顺利
			if (game.getGameType() == GameType.MENJI) {
				Long gameId = roomFunction.getRoleGameMap().remove(role.getRid());
				if (gameId != null) {
					roomFunction.decrementOnline(game.getGameType(), game.getRoomType());
				}
			} else if ((game.getGameType() == GameType.MAJIANG || game.getGameType() == GameType.ZXMAJIANG || game.getGameType() == GameType.CDMAJIANG)
					&& (game.getStatus() == GameStatus.RUNNING||game.getStatus() == GameStatus.END_REWARD||game.getStatus() == GameStatus.WAIT_READY)) {
				Long gameId = roomFunction.getRoleGameMap().remove(role.getRid());
				if (gameId != null) {
					roomFunction.decrementOnline(game.getGameType(), game.getRoomType());
				}
			} else if (game.getGameType() == GameType.DOUDIZHU
					&& game.getStatus() != GameStatus.RUNNING) {
				Long gameId = roomFunction.getRoleGameMap().remove(role.getRid());
				if (gameId != null) {
					roomFunction.decrementOnline(game.getGameType(), game.getRoomType());
				}
			}else if (game.getGameType() == GameType.DOUNIU) {
				Long gameId = roomFunction.getRoleGameMap().remove(role.getRid());
				if (gameId != null) {
					roomFunction.decrementOnline(game.getGameType(), game.getRoomType());
				}
			}
			role.setLatelyGames(game.getRoomId());
			singleThreadManager.executeTask(new SingleThreadTask(game) {
				@Override
				public void doTask(ISingleData singleData) {
					Game game = (Game) singleData;
					if (game.getGameType() == GameType.MENJI|| game.getGameType() == GameType.DOUNIU) {
						roomFunction.quitRole(game, role);
					}  else if (game.getGameType() == GameType.DOUDIZHU
							&& game.getStatus() == GameStatus.WAIT_READY) {
						roomFunction.quitRole(game, role);
					} else if ((game.getGameType() == GameType.MAJIANG || game.getGameType() == GameType.ZXMAJIANG || game.getGameType() == GameType.CDMAJIANG)
							&& game.getStatus() == GameStatus.RUNNING) {
						GameRole gameRole = game.getSpriteMap().get(
								role.getRid());
						if (gameRole != null
								&& gameRole.getStatus() == PlayerState.PS_WATCH_VALUE) {
							roomFunction.quitRole(game, role);
						} else if (gameRole == null) {
							GMsg_12006005.Builder builder = GMsg_12006005
									.newBuilder();
							builder.setCurrentSeat(0);
							roleFunction.sendMessageToPlayer(role.getRid(),
									builder.build());
						} else {
							gameRole.setAuto(true);
							LogUtil.error("gameRole.getStatus()"
									+ gameRole.getStatus());
						}

					}else if((game.getGameType() == GameType.MAJIANG || game.getGameType() == GameType.ZXMAJIANG || game.getGameType() == GameType.CDMAJIANG)
						&& game.getStatus() == GameStatus.WAIT_READY){
						roomFunction.quitRole(game, role);
					} else if (game.getStatus() == GameStatus.WAIT_READY
							|| game.getStatus() == GameStatus.END_REWARD) {
						// 标记游戏已超时
						game.setStatus(GameStatus.CLEAR);

						LogUtil.info("Game exitTable " + game.getRoomId()
								+ " end!");

						roomFunction.endGame(game);
					} else if (game.getGameType() == GameType.DOUDIZHU
							&& game.getStatus() == GameStatus.RUNNING) {
						LogUtil.info("斗地主 很傻" + game.getRoomId() + "");
					} else {
						roomFunction.cancelReadyToGame(role);
						GMsg_12006005.Builder builder = GMsg_12006005
								.newBuilder();
						builder.setCurrentSeat(0);
						roleFunction.sendMessageToPlayer(role.getRid(),
								builder.build());
						LogUtil.info("Game  try cancelReadyToGame " + game
								+ " " + role.getNick());
					}
				}
			});

		} else {
			roomFunction.cancelReadyToGame(role);
			GMsg_12006005.Builder builder = GMsg_12006005.newBuilder();
			builder.setCurrentSeat(0);
			roleFunction.sendMessageToPlayer(role.getRid(), builder.build());
			LogUtil.info("null Game  try cancelReadyToGame " + game + " "
					+ role.getNick());
		}
		return 0;
	}
	
}
