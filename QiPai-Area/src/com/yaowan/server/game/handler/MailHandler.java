package com.yaowan.server.game.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameModule;
import com.yaowan.framework.core.handler.server.ServerExecutor;
import com.yaowan.framework.server.base.handler.GameHandler;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GMail.GMsg_11015002;
import com.yaowan.protobuf.game.GMail.GMsg_11015003;
import com.yaowan.server.game.service.MailService;

@Component
public class MailHandler extends GameHandler {
	
	@Autowired
	private MailService mailService;
	
	@Override
	public int moduleId() {
		return GameModule.MAIL;
	}

	@Override
	public void register() {
		
		// TODO Auto-generated method stub
		addExecutor(2, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11015002 msg =GMsg_11015002.parseFrom(data);
				mailService.readMail(player,msg.getId());
			}
		});	
		
		// TODO Auto-generated method stub
		addExecutor(3, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				GMsg_11015003 msg =GMsg_11015003.parseFrom(data);
				mailService.receiveMail(player,msg.getId());
			}
		});			
		
		// TODO Auto-generated method stub
		addExecutor(4, 1000, true, new ServerExecutor() {
			@Override
			public void doExecute(Player player, byte[] data) throws Exception {
				mailService.checkMail(player);
			}
		});	
		

	}
}
