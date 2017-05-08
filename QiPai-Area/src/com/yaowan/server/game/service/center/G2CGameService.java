package com.yaowan.server.game.service.center;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;

/**
 * 游戏服对中心服业务层
 * 
 * @author zane
 *
 */
@Component
public class G2CGameService {

	@Autowired
	private RoomFunction roomFunction;
	@Autowired
	private RoleFunction roleFunction;

	public void sendMessage() {
		
	}
}
