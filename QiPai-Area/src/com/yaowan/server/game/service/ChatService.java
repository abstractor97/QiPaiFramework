package com.yaowan.server.game.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Branch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.protobuf.ByteString;
import com.mysql.jdbc.log.LogUtils;
import com.yaowan.constant.GameStatus;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.constant.MoneyType;
import com.yaowan.csv.cache.ItemCache;
import com.yaowan.csv.cache.PropsGiftCache;
import com.yaowan.csv.cache.ShopCache;
import com.yaowan.csv.entity.ItemCsv;
import com.yaowan.csv.entity.PropsGiftCsv;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Game;
import com.yaowan.model.struct.GameRole;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GChat.GMsg_12008001;
import com.yaowan.protobuf.game.GChat.GMsg_12008002;
import com.yaowan.protobuf.game.GChat.GMsg_12008003;
import com.yaowan.protobuf.game.GChat.GMsg_12008004;
import com.yaowan.protobuf.game.GChat.NoticeInfo;
import com.yaowan.server.game.function.ChatFunction;
import com.yaowan.server.game.function.FriendRoomFunction;
import com.yaowan.server.game.function.ItemFunction;
import com.yaowan.server.game.function.NoticeFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.RoomFunction;
import com.yaowan.server.game.model.data.dao.NoticeDao;
import com.yaowan.server.game.model.data.dao.PackItemDao;
import com.yaowan.server.game.model.data.entity.Notice;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.util.KeywordUtil;

@Component
public class ChatService
{

	// 限制对话内容长度
	public static final int LimitByte = 100;

	@Autowired
	RoleFunction roleFunction;

	@Autowired
	RoomFunction roomFunction;

	@Autowired
	ItemService itemService;

	@Autowired
	PackItemDao packItemDao;

	@Autowired
	ItemFunction itemFunction;

	@Autowired
	ItemCache itemCache;

	@Autowired
	ChatFunction chatFunction;

	@Autowired
	private NoticeFunction noticeFunction;

	@Autowired
	private ShopCache shopCache;

	@Autowired
	private NoticeDao noticeDao;

	@Autowired
	private FriendRoomFunction friendRoomFunction;

	@Autowired
	private PropsGiftCache propsGiftCache;

	public void sendMessageInRoom(Player player, int type, String message, int Game,
			ByteString sound, int soundLen)
	{
		if (type != 4 && message.length() > ChatService.LimitByte)
		{
			return;
		}
		// 测试可以变牌
		// int pai = 0;
		// if(GlobalConfig.isTest){
		// if(message.startsWith("changePai:")){
		// pai =Integer.valueOf(message.split(":")[1]);
		// }
		// if(message.startsWith("gmAddGold:")){
		// int gold =Integer.valueOf(message.split(":")[1]);
		// roleFunction.goldAdd(player.getRole(), gold, MoneyEvent.GM_MAIL,
		// true);
		// }
		// }

		Game game = roomFunction.getGameByRole(player.getRole().getRid());
		if (game == null)
		{
			Long roomId = friendRoomFunction.getRoomIdByRid(player.getRole().getRid());
			game = friendRoomFunction.getGame(roomId);
		}
		if (type != 4 && type != 3)
		{
			message = KeywordUtil.filter(message);
		}
		if (game == null)
		{
			GMsg_12008001.Builder builder = GMsg_12008001.newBuilder();
			builder.setRid(player.getRole().getRid());
			builder.setSex(player.getRole().getSex());
			builder.setGame(Game);
			builder.setNick(player.getRole().getNick());
			builder.setType(type);
			builder.setMessage(message);
			builder.setSoundLen(soundLen);
			if (sound != null)
			{
				builder.setSound(sound);
			}
			player.write(builder.build());
			return;
		}
		List<Long> roleList = game.getRoles();
		if (roleList == null || StringUtil.isStringEmpty(message))
		{
			return;
		}
		GMsg_12008001.Builder builder = GMsg_12008001.newBuilder();
		builder.setRid(player.getRole().getRid());
		builder.setSex(player.getRole().getSex());
		builder.setGame(Game);
		builder.setNick(player.getRole().getNick());
		builder.setType(type);
		builder.setMessage(message);
		builder.setSoundLen(soundLen);
		if (sound != null)
		{
			builder.setSound(sound);
		}
		roleFunction.sendMessageToPlayers(roleList, builder.build());
		LogUtil.info("发送聊天");

		// if(pai>0){
		// chatFunction.changePai(player.getRole().getRid(), pai);
		// }
	}

	public void sendMessageInHall(Player player, int type, String message)
	{
		// vip表情判定（还没有）
		Role role = roleFunction.getRoleByRid(player.getRole().getRid());
		roleFunction.goldSub(role, 1000, MoneyEvent.USEITEM, true);
		if (message.length() > ChatService.LimitByte)
		{
			return;
		}
		if (StringUtil.isStringEmpty(message))
		{
			return;
		}
		message = KeywordUtil.filter(message);
		GMsg_12008002.Builder builder = GMsg_12008002.newBuilder();
		builder.setRid(player.getRole().getRid());
		builder.setNick(player.getRole().getNick());
		builder.setType(1);
		builder.setMessage(message);
		roleFunction.sendMessageToAll(builder.build());
	}

	/**
	 * 根据赠送的道具类型扣除对应的货币
	 * 
	 * @param player
	 * @param itemCsv
	 * @param price
	 *            价格
	 * @return
	 */
	public Boolean deductionCurrencyByType(Player player, int currencyType, int price)
	{
		boolean flag = false;
		Role role = player.getRole();
		switch (currencyType)
		{
		case 1:
			if (role.getGold() < price)
			{
				LogUtil.warn("玩家id:" + role.getRid() + "金币不足，不能赠送礼物");
			} else
			{
				flag = roleFunction.goldSub(role, price, MoneyEvent.BUYITEM, true);
				break;
			}
		case 2:
			if (role.getDiamond() < price)
			{
				LogUtil.warn("玩家id:" + role.getRid() + "钻石不足，不能赠送礼物");
			} else
			{
				flag = roleFunction.diamondSub(role, price, MoneyEvent.BUYITEM, true);
				break;
			}
		case 3:
			if (role.getCrystal() < price)
			{
				LogUtil.warn("玩家id:" + role.getRid() + "奖券不足，不能赠送礼物");
			} else
			{
				flag = roleFunction.crystalSub(role, price, MoneyEvent.BUYITEM);
			}
			break;
		default:
			break;
		}

		return flag;
	}

	/**
	 * 根据物品类型获得对应的货币 处理数据同步，防止多人给同一个玩家送礼导致的数据异常
	 * 
	 * @param gameRole
	 * @param itemCsv
	 * @param price
	 *            获得价格
	 * @return
	 */
	public synchronized Boolean addCurrencyByType(GameRole gameRole, int currencyType, int price)
	{
		boolean flag = false;
		Role role = gameRole.getRole();
		if (role != null)
		{
			if (!gameRole.isRobot())
			{// 玩家获得道具增加货币
				switch (currencyType)
				{
				case 1:// 金币
					flag = roleFunction.goldAdd(role, price, MoneyEvent.BEUSEITEM, true);
					break;
				case 2:// 钻石
					flag = roleFunction.diamondAdd(role, price, MoneyEvent.BEUSEITEM);
					break;
				case 3:// 奖券
					flag = roleFunction.crystalAdd(role, price, MoneyEvent.BEUSEITEM);
					break;
				default:
					break;
				}
			} else
			{// 机器人获得道具增加货币
				switch (currencyType)
				{
				case 1:// 金币
					role.setGold(role.getGold() + price);
					flag = true;
					break;
				case 2:// 钻石
					role.setDiamond(role.getDiamond() + price);
					flag = true;
					break;
				case 3:// 奖券
					role.setCrystal(role.getCrystal() + price);
					flag = true;
					break;
				default:
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * 获取根据名字货币的类型
	 * 
	 * @param currencyName
	 * @return
	 */
	public int getCurrencyType(String currencyName)
	{
		int currencyType = 0;
		if (null != currencyName && currencyName.length() > 0)
		{
			switch (currencyName)
			{
			case "钻石":
				currencyType = MoneyType.Diamond.byteValue();
				break;
			case "金币":
				currencyType = MoneyType.Gold.byteValue();
				break;
			case "奖券":
				currencyType = MoneyType.Cristal.byteValue();
				break;
			default:
				break;
			}
		}
		return currencyType;
	}

	/**
	 * 游戏中赠送道具功能
	 * 
	 * @param player
	 * @param goodsId
	 *            道具id
	 * @param sendSeat
	 *            发送者座位
	 * @param targetSeat
	 *            接受者座位
	 * @param itemIndex
	 *            道具位置
	 */
	public void sendPropMessage(Player player, int goodsId, int sendSeat, int targetSeat,
			int itemIndex)
	{
		Role role = player.getRole();
		Game game = roomFunction.getGameByRole(player.getRole().getRid());
		// 游戏不存在
		if (game == null)
		{
			game = friendRoomFunction.getGame(friendRoomFunction.getRoomIdByRid(player.getRole()
					.getRid()));
		}
		if (game == null)
		{
			return;
		}
		if (game.getStatus() != GameStatus.RUNNING)
		{
			GMsg_12008003.Builder builder = GMsg_12008003.newBuilder();
			builder.setFlag(1);// 游戏还没开始
			player.write(builder.build());
			return;
		}

		// 获取道具信息
		PropsGiftCsv propsGiftCsv = propsGiftCache.getConfig(goodsId);
		if (propsGiftCsv == null)
		{
			LogUtil.warn("没有这个道具goodsId=" + goodsId + ", rid=" + role.getRid());
			return;
		}

		// 购买的道具信息
		String[] expressionPrice = propsGiftCsv.getExpressionPrice().split("\\|");
		if (expressionPrice == null || expressionPrice.length < 2)
		{
			LogUtil.warn("获取购买的道具货币价格信息失败");
			return;
		}
		// 购买的货币id
		int expressionId = Integer.parseInt(expressionPrice[0]);
		// 购买所需价格
		int price = Integer.parseInt(expressionPrice[1]);
		// 购买道具花费的货币信息
		ItemCsv itemCsv = itemCache.getConfig(expressionId);
		if (itemCsv == null)
		{
			LogUtil.warn("没有这个货币类型goodsId=" + expressionId + ", rid=" + role.getRid());
			return;
		}
		// 获取购买道具花费的货币类型
		int currencyType = getCurrencyType(itemCsv.getItemName());

		// 获得的道具信息
		String[] giftPriceInfo = propsGiftCsv.getGiftPrice().split("\\|");
		if (giftPriceInfo == null || giftPriceInfo.length < 2)
		{
			LogUtil.warn("获取获得的道具货币价格信息失败");
			return;
		}
		// 获得的货币id
		int giftPriceId = Integer.parseInt(giftPriceInfo[0]);
		// 实际获得的价格
		int giftPrise = Integer.parseInt(giftPriceInfo[1]);
		// 获得礼物差价
		int difference = propsGiftCsv.getDifference();

		ItemCsv itemCsv2 = itemCache.getConfig(giftPriceId);
		if (itemCsv2 == null)
		{
			LogUtil.warn("没有这个货币类型goodsId=" + giftPriceId + ", rid=" + role.getRid());
			return;
		}
		// 获取得到道具增加的货币类型
		int currencyType1 = getCurrencyType(itemCsv2.getItemName());
		// 接受道具者id
		long tagetId = 0l;
		boolean flag = deductionCurrencyByType(player, currencyType, price);
		if (!flag)
		{
			LogUtil.warn("扣减玩家货币不成功，rid=" + player.getRole().getRid());
			return;
		}

		// 对被使用的玩家增加金币
		Map<Long, GameRole> gameRoles = game.getSpriteMap();
		if (gameRoles.isEmpty() || gameRoles == null)
		{
			LogUtil.warn("玩家不存在");
			return;
		}

		for (Long key : gameRoles.keySet())
		{
			GameRole gameRole = gameRoles.get(key);
			if (!gameRole.isRobot() && gameRole.getSeat() == targetSeat)
			{
				Role targetRole = gameRole.getRole();
				if (targetRole != null)
				{
					tagetId = targetRole.getRid();
					flag = addCurrencyByType(gameRole, currencyType1, giftPrise);
					if (!flag)
					{
						LogUtil.warn("玩家获得道具增加货币不成功rid=" + tagetId);
					}
				}
				break;
			} else if (gameRole.isRobot() && gameRole.getSeat() == targetSeat)
			{
				Role targetRole = gameRole.getRole();
				tagetId = targetRole.getRid();
				if (targetRole != null)
				{
					// 如果是ai由客户端自己增减数值
					addCurrencyByType(gameRole, currencyType1, giftPrise);
				}
			}
		}

		// 给所在房间用户发送信息
		GMsg_12008003.Builder builder = GMsg_12008003.newBuilder();
		builder.setFlag(0);
		builder.setGoodsId(goodsId);
		builder.setSenderSeat(sendSeat);
		builder.setTargetSeat(targetSeat);
		builder.setItemIndex(itemIndex);
		List<Long> roleList = game.getRoles();
		roleFunction.sendMessageToPlayers(roleList, builder.build());
		// 插入道具流水日志
		itemService.addGiftLog(role.getRid(), tagetId, goodsId, currencyType1, currencyType, price,
				giftPrise, difference);
	}

	public void addClickNum(Player player, int aid)
	{
		noticeFunction.addClickNum(player.getRole(), aid);
	}

	public void sendMessage(Player player)
	{
		if (player.getRole() == null)
		{
			return;
		}
		Role role = player.getRole();
		List<Notice> notices = noticeDao.findAllStartTimeDesc();
		GMsg_12008004.Builder builder = GMsg_12008004.newBuilder();
		List<NoticeInfo> activityInfos = new ArrayList<NoticeInfo>();
		int time = TimeUtil.time();
		if (null != notices && !notices.isEmpty())
		{
			for (Notice notice : notices)
			{
				System.out.println(notice.toString());
				if (notice.getEtime() < time)
				{
					// activityMap.remove(notice.getId());
					continue;
				}
				NoticeInfo info = createActivityInfo(notice, role);
				activityInfos.add(info);
			}
		}
		builder.addAllNoticeInfo(activityInfos);
		player.write(builder.build());
	}

	private NoticeInfo createActivityInfo(Notice notice, Role role)
	{
		NoticeInfo.Builder builder = NoticeInfo.newBuilder();
		builder.setActivityUrl(notice.getContent());
		builder.setAid(notice.getId());
		builder.setIconUrl(notice.getImg());
		builder.setName(notice.getTitle());
		builder.setType(notice.getType());
		// builder.setExplain(notice.getMark());
		Map<Long, Integer> infoMap = role.getVoteInfoMap();
		int click = 0;
		if (infoMap != null && infoMap.get(notice.getId()) != null)
		{
			click = infoMap.get(notice.getId());
		}
		builder.setIsClick(click);
		return builder.build();
	}
}
