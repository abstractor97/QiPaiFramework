/**
 * 
 */
package com.yaowan.server.game.handler;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.GameRole;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GFriend.GMsg_11004002;
import com.yaowan.protobuf.game.GFriend.GMsg_11004003;
import com.yaowan.protobuf.game.GFriend.GMsg_11004004;
import com.yaowan.protobuf.game.GFriend.GMsg_11004005;
import com.yaowan.protobuf.game.GFriend.GMsg_11004006;
import com.yaowan.protobuf.game.GFriend.GMsg_11004007;
import com.yaowan.protobuf.game.GFriend.GMsg_11004009;
import com.yaowan.protobuf.game.GFriend.GMsg_11004010;
import com.yaowan.protobuf.game.GFriend.GMsg_11004011;
import com.yaowan.protobuf.game.GFriend.GMsg_11004012;
import com.yaowan.protobuf.game.GFriend.GMsg_11004013;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.struct.ZTMaJongTable;
import com.yaowan.server.game.model.struct.ZTMajiangRole;
import com.yaowan.server.game.service.FriendService;

/**
 * @author zane
 *
 */
@Component
public class FriendHandler extends GameHandler {

	@Autowired
	private FriendService friendService;
	
	@Autowired
	ZTMajiangFunction zTMajiangFunction;
	
	@Override
	public int moduleId() {
		return GameModule.FRIEND;
	}

	//注意点：好友之间的相互关系
	@Override
	public void register() {
		addExecutor(1, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				friendService.friendList(player);
			}
		});
		
		addExecutor(2, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11004002 msg =GMsg_11004002.parseFrom(data);
				friendService.applyFriend(player, msg.getRid());
			}
		});
		
		addExecutor(3, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11004003 msg =GMsg_11004003.parseFrom(data);
				friendService.removeFriend(player, msg.getRid(),msg.getType());
			}
		});
		
		addExecutor(4, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11004004 msg =GMsg_11004004.parseFrom(data);
				friendService.alarmPlayer(player, msg.getRid(), msg.getType());
			}
		});
		
		addExecutor(5, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11004005 msg =GMsg_11004005.parseFrom(data);
				friendService.agreeFriend(player, msg.getRid());
			}
		});
		
		addExecutor(6, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11004006 msg =GMsg_11004006.parseFrom(data);
				friendService.familarPeople(player,msg.getFamiliarPeopleListList());
			}
		});
		
		addExecutor(7, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11004007 msg =GMsg_11004007.parseFrom(data);
				friendService.seekPlayer(player, msg.getRid());
			}
		});
		
		addExecutor(8, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				friendService.getFriendChatList(player);
			}
		});
		
		addExecutor(9, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11004009 msg =GMsg_11004009.parseFrom(data);
				friendService.friendChat(player, msg.getRid(), msg.getText(), msg.getType());
			}
		});
		
		addExecutor(10, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11004010 msg =GMsg_11004010.parseFrom(data);
				friendService.playInfo(player, msg.getRid(),msg.getGameType());
			}
		});
		
		addExecutor(11, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11004011 msg =GMsg_11004011.parseFrom(data);
				friendService.getFriendChat(player, msg.getRid());
			}
		});
		
		addExecutor(12, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11004012 msg =GMsg_11004012.parseFrom(data);
				friendService.deleteFriendChat(player, msg.getRid());
			}
		});
		
		//测试
		addExecutor(13, 1000, false, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11004013 msg =GMsg_11004013.parseFrom(data);
				//桌子剩余的牌
				ZTMaJongTable table=new ZTMaJongTable();
				table.setLaiZiNum(33);
				List<Integer> a=new ArrayList<Integer>();
				a.add(33);
				a.add(13);
				a.add(13);
				a.add(23);
				table.getPais().addAll(a);
				//桌子剩余的牌
				
				//麻将用户的牌
				ZTMajiangRole ZTMajiangRole=new ZTMajiangRole(new GameRole(new Role(), 1));
				List<Integer> b=new ArrayList<Integer>();
				b.add(11);
				b.add(12);
				b.add(23);
				b.add(22);
				ZTMajiangRole.getPai().addAll(b);
				//麻将用户的牌
				zTMajiangFunction.moPai(table, ZTMajiangRole);
			}
		});
		
		
	}
}
