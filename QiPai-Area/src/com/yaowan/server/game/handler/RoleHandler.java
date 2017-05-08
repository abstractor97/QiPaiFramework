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
import com.yaowan.protobuf.game.GRole.GMsg_11002001;
import com.yaowan.protobuf.game.GRole.GMsg_11002002;
import com.yaowan.protobuf.game.GRole.GMsg_11002003;
import com.yaowan.protobuf.game.GRole.GMsg_11002004;
import com.yaowan.protobuf.game.GRole.GMsg_11002005;
import com.yaowan.protobuf.game.GRole.GMsg_11002006;
import com.yaowan.protobuf.game.GRole.GMsg_11002007;
import com.yaowan.protobuf.game.GRole.GMsg_11002010;
import com.yaowan.protobuf.game.GRole.GMsg_11002011;
import com.yaowan.protobuf.game.GRole.GMsg_11002012;
import com.yaowan.protobuf.game.GRole.GMsg_11002013;
import com.yaowan.protobuf.game.GRole.GMsg_11002014;
import com.yaowan.protobuf.game.GRole.GMsg_11002016;
import com.yaowan.protobuf.game.GRole.GMsg_11002017;
import com.yaowan.server.game.service.RoleService;

/**
 * @author zane
 *
 */
@Component
public class RoleHandler extends GameHandler {

	@Autowired
	private RoleService roleService;
	
	@Override
	public int moduleId() {
		return GameModule.ROLE;
	}

	@Override
	public void register() {
		addExecutor(1, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002001 msg = GMsg_11002001.parseFrom(data);
			}
		});
		
		addExecutor(2, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002002 msg = GMsg_11002002.parseFrom(data);
				roleService.updateNick(player, msg.getNick());
			}
		});
		
		addExecutor(3, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002003 msg = GMsg_11002003.parseFrom(data);
				roleService.updateSex(player, msg.getSex());
			}
		});
		
		addExecutor(4, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002004 msg = GMsg_11002004.parseFrom(data);
				roleService.updateHead(player, msg.getHead());
			}
		});
		
		addExecutor(5, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002005 msg = GMsg_11002005.parseFrom(data);
				roleService.updateLocation(player, msg.getProvince(), msg.getCity());
			}
		});
		
		addExecutor(6, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002006 msg = GMsg_11002006.parseFrom(data);
				roleService.unlockAvatar(player, msg.getId());
			}
		});
		
		addExecutor(7, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002007 msg = GMsg_11002007.parseFrom(data);
				roleService.useAvatar(player, msg.getId());
			}
		});
		
		addExecutor(9, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				roleService.listGameSta(player);
			}
		});
		
		addExecutor(10, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002010 msg = GMsg_11002010.parseFrom(data);
				roleService.updateGoldPot(player, msg.getGold(),msg.getGoldPot());
			}
		});
		
		addExecutor(11, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002011 msg = GMsg_11002011.parseFrom(data);
				roleService.ApplyGoldResuce(player,msg.getType());
			}
		});
		
		addExecutor(12, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002012 msg = GMsg_11002012.parseFrom(data);
				int day = msg.getDay();
				roleService.userSign(player,day);
			}
		});
		
		addExecutor(13, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002013 msg = GMsg_11002013.parseFrom(data);
				roleService.userSignAgain(player, msg.getDate());
			}
		});
		
		addExecutor(14, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002014 msg = GMsg_11002014.parseFrom(data);
				roleService.getContinueSignReward(player, msg.getId());
			}
		});
		
		addExecutor(16, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002016 msg = GMsg_11002016.parseFrom(data);
				roleService.getGOtherRoleInfo(msg.getRid(), player);
				
			}
		});
		
		addExecutor(17, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11002017 msg = GMsg_11002017.parseFrom(data);
				roleService.doVoteResult(player, msg);
				
			}
		});
	}
}
