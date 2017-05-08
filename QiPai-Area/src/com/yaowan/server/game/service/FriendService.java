/**
 * 
 */
package com.yaowan.server.game.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameType;
import com.yaowan.model.struct.GameRole;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GFriend.FriendStatus;
import com.yaowan.protobuf.game.GFriend.GApplyFriend;
import com.yaowan.protobuf.game.GFriend.GFamilarFriend;
import com.yaowan.protobuf.game.GFriend.GFriendChat;
import com.yaowan.protobuf.game.GFriend.GFriendChatList;
import com.yaowan.protobuf.game.GFriend.GMsg_12004001;
import com.yaowan.protobuf.game.GFriend.GMsg_12004002;
import com.yaowan.protobuf.game.GFriend.GMsg_12004003;
import com.yaowan.protobuf.game.GFriend.GMsg_12004004;
import com.yaowan.protobuf.game.GFriend.GMsg_12004005;
import com.yaowan.protobuf.game.GFriend.GMsg_12004006;
import com.yaowan.protobuf.game.GFriend.GMsg_12004007;
import com.yaowan.protobuf.game.GFriend.GMsg_12004008;
import com.yaowan.protobuf.game.GFriend.GMsg_12004009;
import com.yaowan.protobuf.game.GFriend.GMsg_12004010;
import com.yaowan.protobuf.game.GFriend.GMsg_12004011;
import com.yaowan.protobuf.game.GFriend.GMsg_12004012;
import com.yaowan.protobuf.game.GFriend.GSimpleFriend;
import com.yaowan.server.game.function.CDMajiangDataFunction;
import com.yaowan.server.game.function.CDMajiangFunction;
import com.yaowan.server.game.function.DoudizhuDataFunction;
import com.yaowan.server.game.function.FriendChatLogFunction;
import com.yaowan.server.game.function.FriendFunction;
import com.yaowan.server.game.function.MajiangDataFunction;
import com.yaowan.server.game.function.MenjiDataFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ZTDoudizhuFunction;
import com.yaowan.server.game.function.ZTMajiangFunction;
import com.yaowan.server.game.function.ZTMenjiFunction;
import com.yaowan.server.game.function.ZXMajiangDataFunction;
import com.yaowan.server.game.function.ZXMajiangFunction;
import com.yaowan.server.game.model.data.dao.ReportDao;
import com.yaowan.server.game.model.data.dao.RoleDao;
import com.yaowan.server.game.model.data.entity.CDMajiangData;
import com.yaowan.server.game.model.data.entity.DoudizhuData;
import com.yaowan.server.game.model.data.entity.Friend;
import com.yaowan.server.game.model.data.entity.MajiangData;
import com.yaowan.server.game.model.data.entity.MenjiData;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.data.entity.ZXMajiangData;
import com.yaowan.server.game.model.log.dao.FriendChatLogDao;
import com.yaowan.server.game.model.log.entity.FriendChatLog;
import com.yaowan.server.game.model.struct.ZTDoudizhuRole;
import com.yaowan.server.game.model.struct.ZTMajiangRole;
import com.yaowan.server.game.model.struct.ZTMenjiRole;

/**
 * 好友模块
 * 
 * @author zane 2016年10月12日 下午8:36:20
 *
 */
@Component
public class FriendService {

	@Autowired
	private FriendFunction friendFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private FriendChatLogFunction friendChatLogFunction;

	@Autowired
	private FriendChatLogDao friendChatLogDao;

	@Autowired
	private MajiangDataFunction majiangDataFunction;
	
	@Autowired
	private ZXMajiangDataFunction zxmajiangDataFunction;
	
	@Autowired
	private CDMajiangDataFunction cdmajiangDataFunction;
	
	@Autowired
	private DoudizhuDataFunction doudizhuDataFunction;

	@Autowired
	private MenjiDataFunction menjiDataFunction;

	@Autowired
	private ReportDao reportDao;

	@Autowired
	private ZTMajiangFunction majiangFunction;

	@Autowired
	private ZXMajiangFunction zxmajiangFunction;

	@Autowired
	private CDMajiangFunction cdmajiangFunction;

	@Autowired
	private ZTDoudizhuFunction doudizhuFunction;

	@Autowired
	private ZTMenjiFunction menjiFunction;

	/**
	 * 申请好友
	 * 互相申请的功能
	 * @param player
	 * @param targetId
	 */
	public void applyFriend(Player player, long targetId) {
		Friend friend = friendFunction.getFriend(player.getRole().getRid(),
				targetId);
		if (friend != null) {
			if(friend.getStatusList().contains(0)){
				return;
			}	
		}
		if (targetId == player.getRole().getRid()) {
			return;
		}
		// 避免加到ai
		if (roleFunction.getRoleByRid(targetId) == null) {
			return;
		}
		friendFunction.applyFriend(player.getRole().getRid(), targetId);
		// 申请好友（1.申请者看到 2.被申请者看到）
		// 2.被申请者
		Role role = player.getRole();
		Player player2 = roleFunction.getPlayer(targetId);
		if (player2 != null) {
			GMsg_12004002.Builder builder2 = GMsg_12004002.newBuilder();
			GApplyFriend.Builder applyBuilder = GApplyFriend.newBuilder();
			builder2.setFlag(0);
			applyBuilder.setRid(role.getRid());
			applyBuilder.setNick(role.getNick());
			applyBuilder.setHead(role.getHead());
			builder2.setApplyFriend(applyBuilder);
			player2.write(builder2.build());
		}
	}

	/**
	 * 获得好友列表和被申请列表
	 * 
	 * @param player
	 */
	public void friendList(Player player) {
		Role role = player.getRole();
		List<Role> roleList = friendFunction.listFriends(role.getRid());
		GMsg_12004001.Builder builder = GMsg_12004001.newBuilder();
		// 封装用户列表
		for (Role roleFriend : roleList) {
			GSimpleFriend.Builder friendsBuilder = GSimpleFriend.newBuilder();
			friendsBuilder.setRid(roleFriend.getRid());
			friendsBuilder.setNick(roleFriend.getNick());
			friendsBuilder.setHead(roleFriend.getHead());
			friendsBuilder.setLevel(roleFriend.getLevel());
			friendsBuilder.setExp(roleFriend.getExp());
			friendsBuilder.setGold(roleFriend.getGold());
			friendsBuilder.setDiamond(roleFriend.getDiamond());
			friendsBuilder.setProvince(roleFriend.getProvince());
			friendsBuilder.setCity(roleFriend.getCity());
			friendsBuilder.setSex(roleFriend.getSex());
			friendsBuilder.setOnline(roleFriend.getOnline());
			builder.addFriendList(friendsBuilder);
		}
		player.write(builder.build());
	}

	/**
	 * 删除好友 删除申请
	 * 
	 * @param player
	 * @param rid
	 */
	public void removeFriend(Player player, long removeId, int type) {
		long rid = player.getRole().getRid();
		Role role = roleFunction.getRoleByRid(removeId);	
		if(type==1){
			friendFunction.updateFriendApplys(rid,removeId);
		}else{
			friendFunction.deleteFriend(rid, removeId);
		}	
		GMsg_12004003.Builder builder = GMsg_12004003.newBuilder();
		builder.setFlag(0);
		builder.setRid(removeId);
		builder.setType(type);
		builder.setNick(role.getNick());
		player.write(builder.build());
		Player player2 = roleFunction.getPlayer(removeId);
		if (player2 != null) {
			GMsg_12004003.Builder builder2 = GMsg_12004003.newBuilder();
			builder2.setFlag(0);
			builder2.setRid(rid);
			builder2.setType(type);
			builder2.setNick(player.getRole().getNick());
			player2.write(builder2.build());
		}
		// 删除其聊天记录
		friendChatLogDao.deletefriendChat(rid,removeId);
	}

	/**
	 * 
	 * @param player
	 * @param rid
	 */
	public void agreeFriend(Player player, long rid) {
		long rid1 = player.getRole().getRid();
		long rid2 = rid;
		friendFunction.agreeFriend(rid1, rid2);
		// 同意申请（1.申请者看到 2.接受申请者看到）
		// 接受申请者者看到
		GMsg_12004005.Builder builder1 = GMsg_12004005.newBuilder();
		Role role1 = roleFunction.getRoleByRid(rid1);
		Role role2 = roleFunction.getRoleByRid(rid2);
		GSimpleFriend.Builder friendsBuilder2 = GSimpleFriend.newBuilder();
		friendsBuilder2.setRid(role2.getRid());
		friendsBuilder2.setNick(role2.getNick());
		friendsBuilder2.setHead(role2.getHead());
		friendsBuilder2.setLevel(role2.getLevel());
		friendsBuilder2.setExp(role2.getExp());
		friendsBuilder2.setGold(role2.getGold());
		friendsBuilder2.setDiamond(role2.getDiamond());
		friendsBuilder2.setProvince(role2.getProvince());
		friendsBuilder2.setCity(role2.getCity());
		friendsBuilder2.setSex(role2.getSex());
		friendsBuilder2.setOnline(role2.getOnline());
		builder1.setNewFriend(friendsBuilder2);
		player.write(builder1.build());
		// 申请者者看到
		Player player2 = roleFunction.getPlayer(rid2);
		if (player2 != null) {
			GMsg_12004005.Builder builder2 = GMsg_12004005.newBuilder();
			GSimpleFriend.Builder friendsBuilder1 = GSimpleFriend.newBuilder();
			friendsBuilder1.setRid(role1.getRid());
			friendsBuilder1.setNick(role1.getNick());
			friendsBuilder1.setHead(role1.getHead());
			friendsBuilder1.setLevel(role1.getLevel());
			friendsBuilder1.setExp(role1.getExp());
			friendsBuilder1.setGold(role1.getGold());
			friendsBuilder1.setDiamond(role1.getDiamond());
			friendsBuilder1.setProvince(role1.getProvince());
			friendsBuilder1.setCity(role1.getCity());
			friendsBuilder1.setSex(role1.getSex());
			friendsBuilder1.setOnline(role1.getOnline());
			builder2.setNewFriend(friendsBuilder1);
			player2.write(builder2.build());
		}
	}

	/**
	 * 获取认识的人（已邀请，可邀请，已添加，可添加）（未测）
	 */
	public void familarPeople(Player player,
			List<GFamilarFriend> familiarPeopleList) {
		GMsg_12004006.Builder builder = GMsg_12004006.newBuilder();
		if (familiarPeopleList.size() > 0) {// 开启了获取用户权限，获得了用户的联系人的处理
			List<Role> roleList = roleDao.findAll();// 获取role表用户记录
			HashMap<Integer, Long> hm = new HashMap<Integer, Long>();// Integer电话号码Long用户id
			for (Role role : roleList) {
				hm.put(role.getPhone(), role.getRid());
			}
			List<Role> friendList = friendFunction.listFriends(player.getRole()
					.getRid());// 获取该用户的朋友列表
			List<Role> askList = friendFunction.listAsks(player.getRole()
					.getRid());// 获取该用户邀请其他用户的列表
			for (int i = 0; i < familiarPeopleList.size(); i++) {
				boolean flag = false;
				GFamilarFriend.Builder familarFriend = GFamilarFriend
						.newBuilder();
				int phone = familiarPeopleList.get(i).getPhone();
				String name = familiarPeopleList.get(i).getName();
				familarFriend.setName(name);
				familarFriend.setPhone(phone);
				if (hm.containsKey(phone)) {// 说明数据库里有个人用户
					long rid = hm.get(phone);
					if (flag == false) {
						for (Role role2 : friendList) {// 朋友关系（已添加状态）
							if (rid == role2.getRid()) {
								familarFriend
										.setFriendStatus(FriendStatus.ADD_ALREADY);
								flag = true;
								break;
							}
						}
					}
					if (flag == false) {
						for (Role role2 : askList) {// 该用户申请了其他用户（已邀请状态）
							if (rid == role2.getRid()) {
								familarFriend
										.setFriendStatus(FriendStatus.INVITE_ALREADY);
								flag = true;
								break;
							}
						}
					}
					if (flag == false) {// 用户存在（不是朋友，没有邀请，可添加状态）
						familarFriend.setFriendStatus(FriendStatus.ADD_CAN);
						flag = true;
					}
				} else {// 说明数据库里没有个人用户
					familarFriend.setFriendStatus(FriendStatus.INVITE_CAN);
				}
				builder.addFamiliarPeopleList(familarFriend);
			}
			player.write(builder.build());
		} else {// 没有开启用户权限的处理

		}
	}

	/**
	 * 寻找好友
	 * 
	 * @param player
	 * @param rid
	 */
	public void seekPlayer(Player player, long rid) {
		long playId = player.getRole().getRid();
		Role role = roleFunction.getRoleByRid(rid);
		if (playId != rid && role != null) {
			GMsg_12004007.Builder builder = GMsg_12004007.newBuilder();
			GApplyFriend.Builder apply_friend = GApplyFriend.newBuilder();
			apply_friend.setRid(role.getRid());
			apply_friend.setNick(role.getNick());
			apply_friend.setHead(role.getHead());
			builder.setSeekFriend(apply_friend);
			player.write(builder.build());
		}
	}

	/**
	 * 如果用户在线，直接返回信息，并且插入到数据库
	 * 
	 * @param player
	 * @param rid2
	 * @param text
	 * @param type
	 */
	public void friendChat(Player player, long rid2, String text, int type) {
		long rid = player.getRole().getRid();
		// 为发送者添加数据库记录
		friendChatLogFunction.addFriendChatLog(rid, rid, rid2, text, type,
				(byte) 1);
		GMsg_12004009.Builder builder1 = GMsg_12004009.newBuilder();
		builder1.setFlag(0);
		builder1.setRid(rid);
		player.write(builder1.build());
		byte isRead = 0;
		Player player2 = roleFunction.getPlayer(rid2);
		if (player2 != null) {// 直接返回信息
			GMsg_12004009.Builder builder2 = GMsg_12004009.newBuilder();
			builder2.setFlag(0);
			builder2.setRid(rid);
			builder2.setType(type);
			builder2.setText(text);
			player2.write(builder2.build());
			isRead = 1;
		}
		// 为接收者添加数据库记录
		friendChatLogFunction.addFriendChatLog(rid2, rid, rid2, text, type,
				isRead);
	}

	/**
	 * 获取用户的未读信息（最新1条）
	 * 
	 * @param player
	 */
	public void getFriendChatList(Player player) {
		GMsg_12004008.Builder builder = GMsg_12004008.newBuilder();
		// 封装申请
		long rid = player.getRole().getRid();
		List<Role> roleList = friendFunction.listApplys(rid);
		// 封装申请列表
		for (Role roleApply : roleList) {
			GApplyFriend.Builder applyBuilder = GApplyFriend.newBuilder();
			applyBuilder.setRid(roleApply.getRid());
			applyBuilder.setNick(roleApply.getNick());
			applyBuilder.setHead(roleApply.getHead());
			builder.addApplyList(applyBuilder);
		}
		List<FriendChatLog> friendChatLogList = friendChatLogDao
				.getOneChatListByRid(player.getRole().getRid());
		// 通过sql语句无法判别getterId和senderId的关系，需要进一步封装
		HashMap<Long, FriendChatLog> hm = new HashMap<Long, FriendChatLog>();
		for (FriendChatLog friendChatLog : friendChatLogList) {
			long senderId = friendChatLog.getSenderId();
			long getterId = friendChatLog.getGetterId();
			if (senderId != rid) {
				if (!hm.containsKey(senderId)) {
					hm.put(senderId, friendChatLog);
				} else {
					FriendChatLog friendChatLogBefore = hm.get(senderId);
					if (friendChatLog.getTime() > friendChatLogBefore.getTime()) {
						hm.put(senderId, friendChatLog);
					}
				}
			}
			if (getterId != rid) {
				if (!hm.containsKey(senderId)) {
					hm.put(getterId, friendChatLog);
				} else {
					FriendChatLog friendChatLogBefore = hm.get(senderId);
					if (friendChatLog.getTime() > friendChatLogBefore.getTime()) {
						hm.put(getterId, friendChatLog);
					}
				}
			}
		}
		// 封装好友的一条聊天记录
		for (Entry<Long, FriendChatLog> friendChat : hm.entrySet()) {
			long friendId = friendChat.getKey();
			FriendChatLog friendChatLog = friendChat.getValue();
			GFriendChatList.Builder friendChatList = GFriendChatList
					.newBuilder();
			friendChatList.setFriendId(friendId);
			Role role = roleFunction.getRoleByRid(friendId);
			friendChatList.setOnline(role.getOnline());
			friendChatList.setText(friendChatLog.getTextLog());
			friendChatList.setTime(friendChatLog.getTime());
			friendChatList.setType(friendChatLog.getType());
			builder.addFriendChatList(friendChatList);
		}
		player.write(builder.build());
	}

	/**
	 * 举报玩家
	 * 
	 * @param player
	 * @param rid
	 */
	public void alarmPlayer(Player player, long rid, int reportType) {
		reportDao.addReport(player.getRole().getRid(), rid, reportType);
		GMsg_12004004.Builder builder = GMsg_12004004.newBuilder();
		builder.setFlag(0);
		player.write(builder.build());
	}

	public void playInfo(Player player, long rid, int gameType) {
		
		GMsg_12004010.Builder builder = GMsg_12004010.newBuilder();
		GameRole gameRole = null;
		switch (gameType) {
		case GameType.MENJI:
			ZTMenjiRole ztMenjiRole = menjiFunction.getRole(rid);
			if(ztMenjiRole != null){
				gameRole = ztMenjiRole.getRole();
			}
			break;
		case GameType.DOUDIZHU:
			ZTDoudizhuRole ztDoudizhuRole = doudizhuFunction.getRole(rid);
			if(ztDoudizhuRole != null){
				gameRole = ztDoudizhuRole.getRole();
			}
			break;
		case GameType.MAJIANG:
			ZTMajiangRole ztMajiangRole = majiangFunction.getRole(rid);
			if(ztMajiangRole != null){
				gameRole = ztMajiangRole.getRole();
			}
			break;
		case GameType.ZXMAJIANG:
			ZTMajiangRole zxmajiangRole = zxmajiangFunction.getRole(rid);
			if(zxmajiangRole != null){
				gameRole = zxmajiangRole.getRole();
			}
			break;
		case GameType.CDMAJIANG:
			ZTMajiangRole cdmajiangRole = cdmajiangFunction.getRole(rid);
			if(cdmajiangRole != null){
				gameRole = cdmajiangRole.getRole();
			}
			break;
		default:
			break;
		}
		if(gameRole != null && gameRole.isRobot() && gameRole.getRole() != null){
			Role robot = gameRole.getRole();
			builder.setRid(robot.getRid());
			builder.setNick(robot.getNick());
			builder.setGold(robot.getGold());
			builder.setDiamond(0);
			builder.setLevel(robot.getLevel());
			builder.setWinWeek(gameRole.getWinWeek());
			builder.setWinTotal(gameRole.getWinTotal());
			builder.setCountWeek(gameRole.getCountWeek());
			builder.setCountTotal(gameRole.getCountTotal());
			builder.setIsFriend(2);
		}else {
			Role role = roleFunction.getRoleByRid(rid);
			if(role != null){
				builder.setRid(rid);
				builder.setNick(role.getNick());
				builder.setGold(role.getGold());
				builder.setDiamond(role.getDiamond());
				builder.setLevel(role.getLevel());
				switch (gameType) {
				case GameType.MENJI:// 焖鸡
					MenjiData menjiData = menjiDataFunction.getMenjiData(rid);
					builder.setWinWeek(menjiData.getWinWeek());
					builder.setWinTotal(menjiData.getWinTotal());
					builder.setCountWeek(menjiData.getCountWeek());
					builder.setCountTotal(menjiData.getCountTotal());
					break;
				case GameType.DOUDIZHU:// 斗地主
					DoudizhuData doudizhuData = doudizhuDataFunction
							.getDoudizhuData(rid);
					builder.setWinWeek(doudizhuData.getWinWeek());
					builder.setWinTotal(doudizhuData.getWinTotal());
					builder.setCountWeek(doudizhuData.getCountWeek());
					builder.setCountTotal(doudizhuData.getCountTotal());
					break;
				case GameType.MAJIANG:// 麻将
					MajiangData majiangData = majiangDataFunction
							.getMajiangData(rid);
					builder.setWinWeek(majiangData.getWinWeek());
					builder.setWinTotal(majiangData.getWinTotal());
					builder.setCountWeek(majiangData.getCountWeek());
					builder.setCountTotal(majiangData.getCountTotal());
					break;
				case GameType.ZXMAJIANG:// 镇雄麻将
					ZXMajiangData zxmajiangData = zxmajiangDataFunction
							.getZXMajiangData(rid);
					builder.setWinWeek(zxmajiangData.getWinWeek());
					builder.setWinTotal(zxmajiangData.getWinTotal());
					builder.setCountWeek(zxmajiangData.getCountWeek());
					builder.setCountTotal(zxmajiangData.getCountTotal());
					break;
				case GameType.CDMAJIANG:// 成都麻将
					CDMajiangData cdmajiangData = cdmajiangDataFunction
							.getCDMajiangData(rid);
					builder.setWinWeek(cdmajiangData.getWinWeek());
					builder.setWinTotal(cdmajiangData.getWinTotal());
					builder.setCountWeek(cdmajiangData.getCountWeek());
					builder.setCountTotal(cdmajiangData.getCountTotal());
					break;
				default:
					break;
				}
				Friend friend = friendFunction.getFriend(player.getRole().getRid(),
						rid);
				if (friend == null) {// 没有好友，没有申请，没有被申请
					builder.setIsFriend(2);
				} else {
					if (friend.getStatusList().contains(2)) {// 已成为好友
						builder.setIsFriend(1);
					} else {// 有申请或者有被申请的显示
						builder.setIsFriend(2);
					}
				}
			}
		}	
		player.write(builder.build());
	}

	public void getFriendChat(Player player,long targetId) {
		// 通过用户id和getterId或者senderId对应的id or
		GMsg_12004011.Builder builder = GMsg_12004011.newBuilder();
		builder.setRid(targetId);
		List<FriendChatLog> friendChatLogList=friendChatLogDao.getFriendChatByRid(player.getRole().getRid(), targetId);
		for (FriendChatLog friendChatLog : friendChatLogList) {
			GFriendChat.Builder friendChat = GFriendChat.newBuilder();
			friendChat.setGetterId(friendChatLog.getGetterId());
			friendChat.setSenderId(friendChatLog.getSenderId());
			friendChat.setText(friendChatLog.getTextLog());
			friendChat.setTime(friendChatLog.getTime());
			friendChat.setType(friendChatLog.getType());
			friendChat.setVoice(friendChatLog.getVoiceLog());
			builder.addFriendChat(friendChat);
		}
		player.write(builder.build());
	}

	public void deleteFriendChat(Player player,long removeId) {
		GMsg_12004012.Builder builder = GMsg_12004012.newBuilder();
		long rid=player.getRole().getRid();
		friendChatLogDao.deletefriendChat(rid,removeId);
		builder.setRid(removeId);
		player.write(builder.build());
	}
}
