package com.yaowan.server.game.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GActivity.GMsg_11019002;
import com.yaowan.protobuf.game.GActivity.GMsg_11019003;
import com.yaowan.server.game.service.ActivityService;

/**
 * 活动handler
 * 
 * @author G_T_C
 */
@Component
public class ActivityHandler extends GameHandler {

	@Autowired
	private ActivityService activityService;

	@Override
	public int moduleId() {
		return GameModule.ACTIVITY;
	}

	@Override
	public void register() {
		addExecutor(1, 1000, true, new ServerExecutor() {

			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				activityService.sendMessage(player);
			}
		});

		addExecutor(2, 1000, true, new ServerExecutor() {

			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11019002 msg = GMsg_11019002.parseFrom(data);
				activityService.requestRewards(player, msg.getAid());
			}
		});
		
		addExecutor(3, 1000, true, new ServerExecutor() {

			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11019003 msg = GMsg_11019003.parseFrom(data);
				activityService.addClickNum(player, msg.getAid());
			}
		});
	}

}
