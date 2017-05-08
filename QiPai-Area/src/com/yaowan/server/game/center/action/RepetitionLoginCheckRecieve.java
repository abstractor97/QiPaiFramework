package com.yaowan.server.game.center.action;

import java.nio.ByteBuffer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.ChannelConst;
import com.yaowan.constant.GameError;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.cmd.CMD.CenterCMD;
import com.yaowan.protobuf.game.GLogin.GMsg_12001003;
import com.yaowan.server.game.center.Receive;
import com.yaowan.server.game.function.LoginFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.data.entity.Role;
/**
 * 当同一个区服，分多个程序跑时，会用到
 * 如果在新的区服登录， 如果在本区服上有登录，则从本服上踢下线
 * @author YW0941
 *
 */
@Component
public class RepetitionLoginCheckRecieve extends Receive {
	@Autowired
	private RoleFunction roleFunction;
	@Autowired
	private LoginFunction loginFunction;
	public RepetitionLoginCheckRecieve() {
		super(CenterCMD.RepetitionLoginCheck_VALUE);
	}

	@Override
	public void execute(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		long rid = buffer.getLong();
		Player player = roleFunction.getPlayer(rid);
		LogUtil.info("重复登录:rid="+rid);
		if(player == null){
			return;
		}
		
		player.write(GMsg_12001003.newBuilder().setFlag(GameError.SAME_LOGIN).build());

		// 将连接通道上的玩家对象置空
		player.getChannel().attr(ChannelConst.PLAYER).set(null);
		try {
			if(player.getRole()!=null){
				Role role = player.getRole();
				LogUtil.info("role"+role.getNick());
				loginFunction.logout(role,player.getIp());
				roleFunction.playerOffline(player,role);
				
				LogUtil.info("重复登录(迫使离线):rid="+rid);
			}
		} catch (Exception e) {
			LogUtil.error(e);
		}finally{
			player.getChannel().close();
		}
		
		LogUtil.info("重复登录(处理完毕):rid="+rid);
	}

}
