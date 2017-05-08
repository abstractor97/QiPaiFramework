package com.yaowan.server.game.cross.controller;

import java.nio.ByteBuffer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.MoneyEvent;
import com.yaowan.cross.BasePacket;
import com.yaowan.cross.Controller;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.cmd.CMD.CrossCMD;
import com.yaowan.server.game.function.RoleFunction;
/**
 * 跨服扣减金币
 * @author YW0941
 *
 */
@Component
public class GoldSubController extends Controller<Player> {
	@Autowired
	private RoleFunction roleFunction;
	public GoldSubController() {
		super((short)CrossCMD.GoldSub_VALUE);
	}

	@Override
	public void execute(Player player, BasePacket packet) {
		byte[] bytes = packet.getData();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		int moneyEvent = buffer.getInt();
		int gold = buffer.getInt();
		roleFunction.goldSub(player.getRole(), gold, MoneyEvent.valueOf(moneyEvent), true);
	}

}
