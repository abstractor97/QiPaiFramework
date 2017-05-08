package com.yaowan.server.game.function;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.framework.util.StringUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.protobuf.game.GChat.GMsg_12008001;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.struct.ZTMaJongTable;

/**
 * @author zane
 *
 */
@Component
public class ChatFunction {

	/**
	 * 聊天的缓存(暂时不用)
	 */
	private final ConcurrentHashMap<Long, String> chatCacheMap = new ConcurrentHashMap<>();
	
	@Autowired
	public RoomFunction roomFunction;
	
	@Autowired
	public ZTMajiangFunction majiangFunction;
	
	@Autowired
	public RoleFunction roleFunction;
	
	public void addMessage(long rid,String message)
	{
		chatCacheMap.put(rid, message);
	}
	
	public void sendMessage(Role role,int gameType,int type,String message) {
		Game game = roomFunction.getGameByRole(role.getRid());
		if (game == null) {
			GMsg_12008001.Builder builder = GMsg_12008001.newBuilder();
			builder.setRid(role.getRid());
			builder.setSex(role.getSex());
			builder.setGame(gameType);
			builder.setNick(role.getNick());
			builder.setType(type);
			builder.setMessage(message);
			roleFunction.sendMessageToPlayer(role.getRid(), builder.build());
			return;
		}
		List<Long> roleList = game.getRoles();
		if (roleList == null || StringUtil.isStringEmpty(message)) {
			return;
		}
		GMsg_12008001.Builder builder = GMsg_12008001.newBuilder();
		builder.setRid(role.getRid());
		builder.setSex(role.getSex());
		builder.setGame(gameType);
		builder.setNick(role.getNick());
		builder.setType(type);
		builder.setMessage(message);
		roleFunction.sendMessageToPlayers(roleList, builder.build());
	}
	
	public void changePai(long rid, int pai) {
		ZTMaJongTable table = majiangFunction.getTableByRole(rid);
		if (table != null) {
			table.setGmMoPai(pai);
			table.setGmrid(rid);
		}
	}

}
