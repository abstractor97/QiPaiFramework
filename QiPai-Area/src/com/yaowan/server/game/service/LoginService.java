/**
 * 
 */
package com.yaowan.server.game.service;



import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.internal.LinkedTreeMap;
import com.yaowan.ServerConfig;
import com.yaowan.constant.ChannelConst;
import com.yaowan.constant.FriendRoomPayType;
import com.yaowan.constant.GameError;
import com.yaowan.core.function.FunctionManager;
import com.yaowan.framework.util.Http;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.center.CGame.CMsg_21100006;
import com.yaowan.protobuf.center.CGame.CRoleInfo;
import com.yaowan.protobuf.game.GGame.PlayerState;
import com.yaowan.protobuf.game.GLogin.GMsg_12001001;
import com.yaowan.protobuf.game.GLogin.GMsg_12001002;
import com.yaowan.protobuf.game.GLogin.GMsg_12001003;
import com.yaowan.protobuf.game.GLogin.GMsg_12001005;
import com.yaowan.protobuf.game.GRole.GRedPoint;
import com.yaowan.protobuf.game.GRole.GRoleAvatar;
import com.yaowan.protobuf.game.GRole.GRoleInfo;
import com.yaowan.server.game.function.DoudizhuDataFunction;
import com.yaowan.server.game.function.FriendRoomFunction;
import com.yaowan.server.game.function.LoginFunction;
import com.yaowan.server.game.function.MailFunction;
import com.yaowan.server.game.function.MajiangDataFunction;
import com.yaowan.server.game.function.MenjiDataFunction;
import com.yaowan.server.game.function.MissionFunction;
import com.yaowan.server.game.function.RecommendFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.function.ZXMajiangDataFunction;
import com.yaowan.server.game.main.NettyClient;
import com.yaowan.server.game.model.data.entity.FriendRoom;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.data.entity.TaskDaily;
import com.yaowan.server.game.model.struct.VoteActivityType;
import com.yaowan.server.game.u8.U8Platform;
import com.yaowan.util.MD5Util;

/**
 * @author zane
 *
 */
@Component
public class LoginService {
	
	@Autowired
	private LoginFunction loginFunction;
	
	@Autowired
	private RoleFunction roleFunction;
	
	@Autowired
	private RoomFunction roomFunction;
	
	@Autowired
	private DoudizhuDataFunction doudizhuDataFunction;
	
	@Autowired
	private MajiangDataFunction majiangDataFunction;
	
	@Autowired
	private ZXMajiangDataFunction zxmajiangDataFunction;
	
	@Autowired
	private MenjiDataFunction menjiDataFunction;
	
	@Autowired
	private MissionFunction missionFunction;
	
	@Autowired
	private MailFunction mailFunction;
	
	@Autowired
	private FriendRoomFunction friendRoomFunction;
	
	@Autowired
	private RecommendFunction recommendFunction;
	
	public void login(Player player, String openId, int time, String sign, String imei,String platform,String token, byte deviceType,byte loginType, String deviceToken,int u8id) {
		LogUtil.info("openId="+openId+", sign="+sign+",imei="+imei+", platform="+platform+",token="+token+",deviceType="+deviceType+", loginTyp="+loginType+",deviceToken="+deviceToken);
		if (StringUtils.isBlank(openId)) {
			GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
			builder.setFlag(GameError.WRONG_PARAMETER);
			player.write(builder.build());
			return;
		}

		// 校验前后端MD5
		HashMap<String, Object> signmap = new HashMap<String, Object>();
		signmap.put("openId", openId);
		signmap.put("time", time);
		signmap.put("imei", imei);
		String signString = MD5Util.makeSign(signmap);
		String md5 = sign.trim();
		/*
		 * if (!signString.equals(md5)) { GMsg_12001001.Builder builder =
		 * GMsg_12001001.newBuilder();
		 * builder.setFlag(GameError.LOGIN_WRONG_SIGN);
		 * player.write(builder.build()); return; }
		 */
		String nickname = null;
		String headimgurl = null;
		if (token != null && token.length() > 0) {
			if("yaowan".equals(platform)){
				HashMap<String, Object> params = new HashMap<String, Object>();
				params.put("token", token);
				params.put("appid", "100001");
				params.put("account_id", openId);
				params.put("sign", MD5Util.getOrderSign(params, "c3t4bpfuup6efag7m"));
				String data = Http.sendPost(
						"http://phpintf.allrace.com/api.php/index/check_login_token",
						params, 2000);
				LogUtil.info("data=" + data);
			
				if(loginType == 3){//微信登录
					@SuppressWarnings("unchecked")
					LinkedTreeMap<String, String> user = (LinkedTreeMap<String, String>)JSONObject.decode(data).get("user");
					 nickname = user.get("nickname");
					 headimgurl = user.get("headimgurl");
				}
			}
		}
		
		if(u8id>0){//U8Server
			if(token == null) {
				token = "";
			}
			if(!U8Platform.getInstance().verifyAccount(u8id, token)){
				GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
				builder.setFlag(GameError.WRONG_PARAMETER);
				player.write(builder.build());
				return;
			}
		}
		
//		String openId, int time, String sign, String imei,
//		String platform,String token, byte deviceType,byte loginType, String deviceToken,int u8id
		Role role = loginFunction.loginRole(openId, imei, platform, player.getIp(),deviceType, loginType,nickname, headimgurl,deviceToken,u8id);

		GRoleInfo.Builder roleBuilder = dealLoginRole(role, player);

		if (roleBuilder == null) {
			return;
		}
		LogUtil.info("back"+role.getNick());
		GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
		builder.setFlag(0);

		builder.setRoleInfo(roleBuilder);
		builder.setForbidTime(role.getForbid());
		builder.setServerTime(System.currentTimeMillis());
		//builder.s
		player.write(builder.build());
		
		Channel channel = NettyClient.getChannel();
		if (channel != null && channel.isRegistered()) {
			CMsg_21100006.Builder centerBuilder = CMsg_21100006.newBuilder();
			CRoleInfo.Builder crole = CRoleInfo.newBuilder();
			crole.setCity(role.getCity());
			crole.setDiamond(role.getDiamond());
			crole.setExp(role.getExp());
			crole.setGold(role.getGold());
			crole.setLevel(role.getLevel());
			crole.setHead(role.getHead());
			crole.setNick(role.getNick());
			crole.setProvince(role.getProvince());
			crole.setRid(role.getRid());
			crole.setSex(role.getSex());
			crole.setOpenId(role.getOpenId());
			crole.setPlatform(role.getPlatform());
			crole.setServerId(ServerConfig.serverId);
			centerBuilder.setRole(crole);
			
			NettyClient.write(centerBuilder.build());
		}
	}
	
	private GRoleInfo.Builder dealLoginRole(Role role,Player player){
		Player oldPlayer = roleFunction.getPlayer(role.getRid());
		// 已经登陆不处理
		if (oldPlayer == player) {
			return null;
		}
		// 重复登陆
		if(oldPlayer != null) {
			//发送重复登录通知
			
			oldPlayer.write(GMsg_12001003.newBuilder().setFlag(GameError.SAME_LOGIN).build());
			logout(oldPlayer);
		}
		
		roleFunction.playerOnline(player, role);
		
		
		
		// 处理玩家登录事件
		FunctionManager.doHandleOnRoleLogin(role);
		
		LogUtil.info("login role " + role.getRid());
		

		GRoleInfo.Builder roleBuilder = GRoleInfo.newBuilder();
		
		roleBuilder.setRid(role.getRid());
		roleBuilder.setNick(role.getNick());
		roleBuilder.setSex(role.getSex());
		roleBuilder.setHead(role.getHead());
		roleBuilder.setLevel(role.getLevel());
		roleBuilder.setGold(role.getGold());
		roleBuilder.setExp(role.getExp());
		roleBuilder.setDiamond(role.getDiamond());
		roleBuilder.setCrystal(role.getCrystal());
		roleBuilder.setHeadimgurl(role.getHeadimgurl());
		roleBuilder.setCity(role.getCity());
		roleBuilder.setProvince(role.getProvince());
		roleBuilder.setFavorGames(role.getFavorGames());
		roleBuilder.setGoldPot(role.getGoldPot());
		roleBuilder.setSignRecord(role.getSignRecord());
		roleBuilder.setMaxContinueSign(role.getMaxContinueSign());
		roleBuilder.setSignRewardGet(role.getSignRewardGet());
		//投票活动信息、
		Map<Long, Integer> voteInfoMap = role.getVoteInfoMap();
		if(!voteInfoMap.containsKey(VoteActivityType.MASCOT_ACTIVITY)){
			voteInfoMap.put(VoteActivityType.MASCOT_ACTIVITY, 0);//未投票
			role.setVoteInfo(StringUtil.mapToString(voteInfoMap));
		}
		roleBuilder.setVoteResult(role.getVoteInfo());

		
		for(Map.Entry<Integer, List<Integer>> entry:role.getAvatarMap().entrySet()){
			GRoleAvatar.Builder avatarBuilder = GRoleAvatar.newBuilder();
			avatarBuilder.setId(entry.getKey());
			avatarBuilder.setIslock(entry.getValue().get(0));
			avatarBuilder.setIsfight(entry.getValue().get(1));
			roleBuilder.addRoleAvatar(avatarBuilder);
		}
		/*GDoudizhuData.Builder doudizhuBuilder = GDoudizhuData.newBuilder();
		DoudizhuData doudizhuData = doudizhuDataFunction.getDoudizhuData(role
				.getRid());
		ObjectUtil.copyProperties(doudizhuData, doudizhuBuilder);
		roleBuilder.setDoudizhuData(doudizhuBuilder);

		GMenjiData.Builder menjiDataBuilder = GMenjiData.newBuilder();
		MenjiData menjiData = menjiDataFunction.getMenjiData(role.getRid());
		ObjectUtil.copyProperties(menjiData, menjiDataBuilder);
		//roleBuilder.setMenjiData(menjiDataBuilder);

		GMajiangData.Builder majiangDataBuilder = GMajiangData.newBuilder();
		MajiangData majiangData = majiangDataFunction.getMajiangData(role
				.getRid());
		ObjectUtil.copyProperties(majiangData, majiangDataBuilder);
		roleBuilder.setMajiangData(majiangDataBuilder);*/
		
		if(recommendFunction.findIsCanGetMoney(role.getRid())) {
			GRedPoint.Builder red = GRedPoint.newBuilder();
			red.setType(4);
			red.setStatus(1);
			roleBuilder.addRedPoints(red);
		}
		
		for (Map.Entry<Integer, TaskDaily> entry : missionFunction 
				.listRoleMissions(role.getRid()).entrySet()) { // 玩家缓存中所有的任务数据
			if (entry.getValue().getReward() == 1) {
				GRedPoint.Builder red = GRedPoint.newBuilder();
				red.setType(3);
				red.setStatus(1);
				roleBuilder.addRedPoints(red);
				break;
			}
		}
		
		Map<Integer, TaskDaily> map = missionFunction.getMainTaskByRid(role.getRid());
		if(map != null){
			for(int taskId:map.keySet()){
				if (map.get(taskId).getReward() == 1) {
					GRedPoint.Builder red = GRedPoint.newBuilder();
					red.setType(1);
					red.setStatus(1);
					roleBuilder.addRedPoints(red);
					break;
				}
			}
		}
		
		
		Game game = roomFunction.getGameByRole(role.getRid());
		if(game != null){
			GameRole gameRole = game.getSpriteMap().get(role.getRid());
			LogUtil.info("玩家的游戏状态..."+gameRole.getStatus());
			//玩家胡了之后返回大厅
			if (gameRole.getStatus() != PlayerState.PS_WATCH_VALUE) {
				roleBuilder.setIsMatching(game.getGameType());
			}
			
		}else{
			long roomId = friendRoomFunction.getRoomIdByRid(role.getRid());
			FriendRoom friendRoom = friendRoomFunction.getFriendRoom(roomId);
			if(friendRoom != null){
				//在好友房内
				roleBuilder.setFriendroomId(friendRoom.getId());
				if(friendRoom.getOwner() == role.getRid()){
					if(friendRoom.isOwnerExit() == FriendRoomPayType.EXIT){
						//房主退出时回到原来的地方
						roleBuilder.setIsFriendroom(2);
					}else{
						//房主未退出的时候回到游戏内
						roleBuilder.setIsFriendroom(1);
					}
				}else{
					//普通玩家回到游戏内
					roleBuilder.setIsFriendroom(1);
				}
				
			}
		}
		
		
		return roleBuilder;
	}

	public void heartbeat(Player player) {
		GMsg_12001002.Builder builder = GMsg_12001002.newBuilder();
		builder.setServerTime(System.currentTimeMillis());
		player.write(builder.build());
	}

	public void logout(Player player) {
		// 将连接通道上的玩家对象置空
		player.getChannel().attr(ChannelConst.PLAYER).set(null);
		if(player.getRole()!=null){
			Role role = player.getRole();
			LogUtil.info("role"+role.getNick());
			loginFunction.logout(role,player.getIp());
			
			roleFunction.playerOffline(player,role);
			
		}
		

	}
	
	/**
	 * 重连
	 * @param player
	 * @param rid
	 * @param openId
	 */
	public void reConnect(Player player,long rid,String openId) {
		// 将连接通道上的玩家对象置空
		Role role = roleFunction.getRoleByRid(rid);
		Role oRole = roleFunction.getRoleByOpenId(openId);
		LogUtil.info(role+":" + oRole);
		if(role != oRole){
			return;
		}
		
        GRoleInfo.Builder roleBuilder = dealLoginRole(role, player);
		
		if(roleBuilder==null){
			return;
		}
		
		LogUtil.info("relogin role " + role.getRid());
		
		GMsg_12001005.Builder builder = GMsg_12001005.newBuilder();
		builder.setServerTime(System.currentTimeMillis());
		builder.setRoleInfo(roleBuilder);
		builder.setStatus(0);
		player.write(builder.build());
		

	}
}
