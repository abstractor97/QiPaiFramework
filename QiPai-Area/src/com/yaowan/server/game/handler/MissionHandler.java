/**
 * 
 */
package com.yaowan.server.game.handler;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GMission.GMsg_11016001;
import com.yaowan.protobuf.game.GMission.GMsg_11016002;
import com.yaowan.protobuf.game.GMission.GMsg_11016006;
import com.yaowan.server.game.service.MissionService;
/**
 * 
 * @author zane
 */
@Component
public class MissionHandler extends GameHandler {

	@Autowired
	private MissionService missionService;
	
	
	@Override
	public int moduleId() {
		return GameModule.MISSION;
	}

	@Override
	public void register() {
		/* 请求用户的任务完成情况  */
		/* 11016001 */
		addExecutor(1, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11016001 msg = GMsg_11016001.parseFrom(data);
				missionService.requestRoleMissions(player,msg.getType()); // 下发任务列表数据
			}
		});
		/* 11016002 */
		addExecutor(2, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11016002 msg = GMsg_11016002.parseFrom(data);
				missionService.requestRewards(player,msg.getTaskId(), msg.getType()); // 下发任务列表数据
			}
		});
		
		addExecutor(5, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				missionService.uploadPhotoTask(player); //上传头像任务
			}
		});
		addExecutor(6, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11016006 msg = GMsg_11016006.parseFrom(data);
				missionService.weiXinShare(player,msg.getType());
			}
		});
	}
}
