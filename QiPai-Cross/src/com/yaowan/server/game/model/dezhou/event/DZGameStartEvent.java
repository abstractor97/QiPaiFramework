package com.yaowan.server.game.model.dezhou.event;

import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.MoneyEvent;
import com.yaowan.csv.entity.DZConfigCsv;
import com.yaowan.framework.core.events.Event;
import com.yaowan.framework.core.events.handler.EventHandlerAdapter;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.server.game.event.type.HandleType;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.dezhou.DZRoom;
import com.yaowan.server.game.model.dezhou.function.DZCardFunction;

/**
 * 德州游戏开始时
 * @author flyingfan
 *
 */
@Component
public class DZGameStartEvent extends EventHandlerAdapter {
	@Autowired
	private DZCardFunction dzCardFunction;
	@Autowired
	private RoleFunction roleFunction;
	@Override
	public int execute(Event event) {
		
		//税金抽取
		
		if(event.getParam()==null || !(event.getParam()[0] instanceof DZRoom)){
			throw new RuntimeException("DZGameEndEvent,参数转换错误， param:"+event.getParam());
		}
		
		DZRoom dzRoom = (DZRoom) event.getParam()[0];
		DZConfigCsv csv = dzCardFunction.getDzConfigCsv(dzRoom.getCid());
		int taxes = csv.getTaxes();
		Game game = dzRoom.getGame();
		Iterator<GameRole> iterator = game.getSpriteMap().values().iterator();
		while (iterator.hasNext()) {
			GameRole gameRole = (GameRole) iterator.next();
			if(gameRole.getStatus() == PlayerState.PS_PLAY_VALUE){//扣税金
				roleFunction.goldSub(gameRole.getRole(), taxes, MoneyEvent.DEZHOU, true);
			}
		}
		
		return 0;
	}

	@Override
	public int getHandle() {
		return HandleType.DEZHOU_START;
	}

}
