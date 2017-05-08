package com.yaowan.server.game.model.dezhou.event;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameStatus;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.csv.entity.DZConfigCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.DispatchEvent;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.dezhou.DZPlayer;
import com.yaowan.server.game.model.dezhou.DZRoom;
import com.yaowan.server.game.model.dezhou.DZRoom.ProcessPer;
import com.yaowan.server.game.model.dezhou.function.DZCardFunction;

/**
 * 德州游戏结束时
 * @author flyingfan
 *
 */
@Component
public class DZGameEndEvent extends EventHandlerAdapter {
	@Autowired
	private RoomFunction roomFunction;
	@Autowired
	private RoleFunction roleFunction;
	@Autowired
	private DZCardFunction dzCardFunction;
	
	@Override
	public int execute(Event event) {
		if(event.getParam()==null || !(event.getParam()[0] instanceof DZRoom)){
			throw new RuntimeException("DZGameEndEvent,参数转换错误， param:"+event.getParam());
		}
		DZRoom dzRoom = (DZRoom) event.getParam()[0];
		final Game game = dzRoom.getGame();
		//TODO 处理牌局结束
		//1.设置游戏和玩家状态为非游戏状态
		//2.处理筹码上限， 多出的部分转为金币；不足筹码进行补足；无法补足者，踢出游戏
		Iterator<GameRole> iterator = game.getSpriteMap().values().iterator();
		while (iterator.hasNext()) {
			GameRole gameRole = (GameRole) iterator.next();
			gameRole.setStatus(PlayerState.PS_PREPARE_VALUE);
		}
		game.setStatus(GameStatus.END_REWARD);
		
		final DZConfigCsv csv = dzCardFunction.getDzConfigCsv(dzRoom.getCid());
		
		//TODO 处理
		dzRoom.iteratorAllDzPlayer(new ProcessPer() {
			
			@Override
			public void process(DZPlayer dzPlayer) {
				Role role = game.getSpriteMap().get(dzPlayer.getRid()).getRole();
				if(dzPlayer.getJeton() > csv.getHighestLimit()){ //多出的部分转换成金币
					int changeValue = csv.getHighestLimit() - dzPlayer.getJeton();
					roleFunction.goldAdd(role, changeValue , MoneyEvent.DEZHOU, true);
					dzPlayer.setJeton(dzPlayer.getJeton() - changeValue);
				}else if(dzPlayer.getJeton()>csv.getLowestLimit()){//高于最低限制，补充筹码
					int changeValue = csv.getHighestLimit() - dzPlayer.getJeton();
					if(changeValue>role.getGold()){
						changeValue = role.getGold();
					}
					roleFunction.goldSub(role, changeValue , MoneyEvent.DEZHOU, true);
					dzPlayer.setJeton(dzPlayer.getJeton()+changeValue);
				}else if(dzPlayer.getJeton()>csv.getBigBlind()){//重新选择房间
					DispatchEvent.dispacthEvent(new Event(HandleType.GAME_CHANGE_TABLE, dzPlayer));
				}else{//结束游戏
					DispatchEvent.dispacthEvent(new Event(HandleType.GAME_EXIT_TABLE, dzPlayer));
				}	
			}
		});
		
		if(game.getSpriteMap().size()<=1){//只剩下一个人时，把房间解散
			dzRoom.iteratorAllDzPlayer(new ProcessPer() {
				@Override
				public void process(DZPlayer dzPlayer) {
					DispatchEvent.dispacthEvent(new Event(HandleType.GAME_CHANGE_TABLE, dzPlayer));
				}
			});
		}else { //定时启动游戏
			dzCardFunction.startScheduler(dzRoom.getGid(),csv.getOperationTime()*1000 );
		}
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.DEZHOU_END;
	}

}
