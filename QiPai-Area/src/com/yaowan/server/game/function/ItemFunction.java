package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import sun.net.www.content.image.gif;

import com.yaowan.constant.ItemEvent;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.ItemCache;
import com.yaowan.csv.entity.ItemCsv;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.ItemGet;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GItem.GMsg_12005002;
import com.yaowan.protobuf.game.GItem.GPackItem;
import com.yaowan.server.game.model.data.dao.PackItemDao;
import com.yaowan.server.game.model.data.entity.PackItem;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.GiftLogDao;
import com.yaowan.server.game.model.log.dao.ItemLogDao;
import com.yaowan.server.game.model.log.dao.ShopBuyDao;
import com.yaowan.server.game.model.log.entity.GiftLog;
import com.yaowan.server.game.model.log.entity.ItemLog;

@Component
public class ItemFunction extends FunctionAdapter {

	@Autowired
	private PackItemDao packItemDao;

	@Autowired
	private ItemLogDao itemLogDao;

	@Autowired
	private ItemCache itemCache;

	@Autowired
	private ShopBuyDao shopBuyDao;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private GiftLogDao giftLogDao;

	/**
	 * 缓存 Long 用户id Map<Long,PackItem> 背包id 背包id对应的PackItem
	 */
	private final ConcurrentHashMap<Long, Map<Long, PackItem>> mapCache = new ConcurrentHashMap<>();

	/**
	 * 背包数量(暂时写死)
	 */
	private static double bagNum = 50;

	/**
	 * 单个空格数量(暂时写死)
	 */
	private static double singleNum = 99;

	// 用户登录内存加载道具
	@Override
	public void handleOnRoleLogin(Role role) {
		// TODO Auto-generated method stub
		List<PackItem> PackItemList = packItemDao.getPackItemListByRid(role
				.getRid());

		Map<Long, PackItem> map = new HashMap<Long, PackItem>();
		for (PackItem packItem : PackItemList) {
			map.put(packItem.getId(), packItem);
		}
		mapCache.put(role.getRid(), map);
		LogUtil.info("Load Item size " + mapCache.get(role.getRid()).size());
	}

	// 用户退出内存释放道具
	@Override
	public void handleOnRoleLogout(Role role) {
		mapCache.remove(role.getRid());
	}

	/**
	 * 用户获取道具 1.内存 2.数据库
	 * 
	 * @param rid
	 * @return
	 */
	public Map<Long, PackItem> getPackItemListByRid(Role role) {
		long rid = role.getRid();
		Map<Long, PackItem> map = mapCache.get(rid);
		if (map != null)// 有缓存
		{
			LogUtil.info("Load Item size cache " + map.size());
			return map;
		} else// 无缓存
		{
			map = new HashMap<Long, PackItem>();
			List<PackItem> PackItemList = packItemDao.getPackItemListByRid(rid);
			for (PackItem packItem : PackItemList) {
				// 添加进缓存
				map.put(packItem.getId(), packItem);
			}
			mapCache.put(rid, map);
			return map;
		}
	}

	/**
	 * 用户使用道具
	 * 
	 * @param rid用户id
	 * @param id背包id
	 * @param flag标志位
	 * @return 改变了的用户的背包的具体信息
	 */
	public PackItem usePackItem(Role role, long id, boolean flag) {
		Map<Long, PackItem> map = getPackItemListByRid(role);
		// 缓存,数据库
		PackItem packItem = map.get(id);
		int afterValue = packItem.getCount() - 1;
		if (afterValue >= 0) {
			if (packItem.getCount() == 1) {
				map.remove(id);
				packItem.setCount(afterValue);
				packItemDao.deletePackItem(packItem);
			} else {
				packItem.setCount(afterValue);
				packItemDao.updatePackItem(packItem);
			}
			// itemLogDao
			ItemLog itemLog = new ItemLog();
			itemLog.setItemId(packItem.getItemId());
			itemLog.setItemNum(1);
			itemLog.setRid(role.getRid());
			itemLog.setTime(TimeUtil.time());
			itemLog.setEvent(ItemEvent.UseItem.getValue());
			itemLog.setType((byte) 2);
			itemLog.setBeforeValue(afterValue + 1);
			itemLog.setAfterValue(afterValue);
			itemLogDao.addItemLog(itemLog);

			// 客户端接收背包变更的消息
			if (flag == true) {
				GPackItem.Builder itemBuild = GPackItem.newBuilder();
				itemBuild.setCount(packItem.getCount());
				itemBuild.setId(packItem.getId());
				itemBuild.setExpire(0);
				itemBuild.setItemId(packItem.getItemId());
				GMsg_12005002.Builder builder = GMsg_12005002.newBuilder();
				builder.setFlag(0);
				builder.setMark(1);
				builder.addPackItemList(itemBuild);
				roleFunction
						.sendMessageToPlayer(role.getRid(), builder.build());
			}
		} else {
			LogUtil.warn("道具使用数量多于自身的道具数量 rid=" + role.getRid() + ", itemId="
					+ packItem.getItemId());
		}
		return packItem;
	}

	/**
	 * 用户使用道具（根据道具id来使用）
	 * 
	 * @param role
	 * @param itemId
	 * @param flag
	 */
	public void useByItemId(Role role, int itemId, int num, boolean flag) {
		Map<Long, PackItem> map = getPackItemListByRid(role);
		for (Entry<Long, PackItem> entry : map.entrySet()) {
			PackItem packItem = entry.getValue();
			if (packItem.getItemId() != itemId) {
				continue;
			}
			int afterValue = packItem.getCount() - num;
			if (afterValue < 0) {
				LogUtil.warn("道具使用数量多于自身的道具数量 rid=" + role.getRid()
						+ ", itemId=" + itemId);
				return;
			}
			if (afterValue == 0) {
				map.remove(packItem.getId());
				packItem.setCount(afterValue);
				packItemDao.deletePackItem(packItem);
			} else {
				packItem.setCount(afterValue);
				packItemDao.updatePackItem(packItem);
			}
			// itemLogDao
			addLog(role.getRid(), itemId, num, ItemEvent.UseItem.getValue(), 2,
					packItem.getCount() + num, packItem.getCount());
			// 客户端接收背包变更的消息
			if (flag == true) {
				GPackItem.Builder itemBuild = GPackItem.newBuilder();
				itemBuild.setCount(packItem.getCount());
				itemBuild.setId(packItem.getId());
				itemBuild.setExpire(0);
				itemBuild.setItemId(packItem.getItemId());
				GMsg_12005002.Builder builder = GMsg_12005002.newBuilder();
				builder.setFlag(0);
				builder.setMark(1);
				builder.addPackItemList(itemBuild);
				roleFunction
						.sendMessageToPlayer(role.getRid(), builder.build());
			}
			break;
		}
	}

	/**
	 * 用户丢弃道具,同使用道具分开,默认数量为1
	 * 
	 * @param rid
	 * @param id
	 * @param num
	 * @param flag标志位
	 * @return 改变了的用户的背包的具体信息
	 */
	public PackItem giveUpPackItem(Role role, long id, int num, boolean flag) {
		Map<Long, PackItem> map = getPackItemListByRid(role);
		// 缓存,数据库
		PackItem packItem = map.get(id);
		if (packItem.getCount() <= num) {
			map.remove(id);
			packItem.setCount(0);
			packItemDao.deletePackItem(packItem);
		} else {
			packItem.setCount(packItem.getCount() - num);
			packItemDao.updatePackItem(packItem);
		}
		// itemLogDao
		addLog(role.getRid(), packItem.getItemId(), num,
				ItemEvent.DropItem.getValue(), 2, packItem.getCount() + num,
				packItem.getCount());
		// 客户端接收背包变更的消息
		if (flag == true) {
			GPackItem.Builder itemBuild = GPackItem.newBuilder();
			itemBuild.setCount(packItem.getCount());
			itemBuild.setId(packItem.getId());
			itemBuild.setExpire(0);
			itemBuild.setItemId(packItem.getItemId());
			GMsg_12005002.Builder builder = GMsg_12005002.newBuilder();
			builder.setFlag(0);
			builder.setMark(3);
			builder.addPackItemList(itemBuild);
			roleFunction.sendMessageToPlayer(role.getRid(), builder.build());
		}
		return packItem;
	}

	/**
	 * 用户添加道具
	 * 
	 * @param itemId
	 *            道具id
	 * @param num
	 *            道具数量
	 * @param rid
	 *            用户id
	 */
	public PackItem addPackItem(Role role, int itemId, int num,
			ItemEvent itemEvent, boolean flag) {
		Map<Long, PackItem> map = getPackItemListByRid(role);
		// 缓存,数据库
		PackItem packItem = null;
		for (Long key : map.keySet()) {
			if (map.get(key).getItemId() == itemId) {
				packItem = map.get(key);
				break;
			}
		}
		if (packItem == null) {
			// 数据库，缓存
			packItem = new PackItem();
			packItem.setCount(num);
			packItem.setItemId(itemId);
			packItem.setRid(role.getRid());
			packItem.setExpire(0);
			packItemDao.insert(packItem);
			map.put(packItem.getId(), packItem);
			mapCache.put(role.getRid(), map);
		} else {
			packItem.setCount(packItem.getCount() + num);
			packItemDao.update(packItem);
		}
		// itemLogDao
		addLog(role.getRid(), itemId, num, itemEvent.getValue(), 1,
				packItem.getCount() - num, packItem.getCount());
		// 客户端接收背包变更的消息
		if (flag == true) {
			GPackItem.Builder itemBuild = GPackItem.newBuilder();
			itemBuild.setCount(packItem.getCount());
			itemBuild.setId(packItem.getId());
			itemBuild.setExpire(0);
			itemBuild.setItemId(packItem.getItemId());
			GMsg_12005002.Builder builder = GMsg_12005002.newBuilder();
			builder.setFlag(0);
			builder.setMark(2);
			builder.addPackItemList(itemBuild);
			roleFunction.sendMessageToPlayer(role.getRid(), builder.build());
		}
		return packItem;
	}

	/**
	 * @param rid
	 * @param hm
	 *            Integer为道具的id,Integer为
	 * @return
	 */
	public boolean bagIsFull(Long rid, HashMap<Integer, Integer> hm) {
		ArrayList<Integer> al = new ArrayList<Integer>();// 目前物品箱没有的
		Map<Long, PackItem> map = mapCache.get(rid);
		double BagUsed = 0;
		int count = 0;
		int itemId = 0;
		for (Map.Entry<Long, PackItem> entry : map.entrySet()) {
			PackItem packItem = entry.getValue();
			count = packItem.getCount();
			itemId = packItem.getItemId();
			if (hm.containsKey(itemId)) {
				al.add(itemId);
				count += hm.get(itemId);
			}
			BagUsed += Math.ceil(count / singleNum);
		}
		for (Map.Entry<Integer, Integer> entry : hm.entrySet()) {
			itemId = entry.getKey();
			count = entry.getValue();
			if (!al.contains(itemId)) {
				BagUsed += Math.ceil(count / singleNum);
			}
		}
		if (BagUsed < bagNum) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 新用户添加一个道具
	 * 
	 * @param role
	 */
	public void newRolePackItem(Role role) {
		this.addPackItem(role, 1030001, 1, ItemEvent.GetItem, false);
	}

	/**
	 * 邮件物品判断背包是否溢出
	 * 
	 * @param role
	 * @param items
	 * @return
	 */
	public boolean bagIsFull(Role role, String items) {
		List<ItemGet> mailItemList = JSONObject.decodeJsonArray(items,
				ItemGet[].class);
		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		for (ItemGet itemGet : mailItemList) {
			ItemCsv itemCsv = itemCache.getConfig(itemGet.getId());
			if (itemCsv == null) {
				System.out.println("ItemCsv" + itemGet.getId());
				continue;
			}
			if (itemCsv.getItemType() == 1) {
				hm.put(itemGet.getId(), itemGet.getNum());
			}
		}
		if (hm.size() > 0) {
			return bagIsFull(role.getRid(), hm);
		} else {
			return true;
		}
	}

	/**
	 * 通过邮件json获取奖励
	 * 
	 * @param type
	 * @param Id
	 * @param num
	 */
	public boolean getItems(Role role, String items, MoneyEvent event) {
		Player player = roleFunction.getPlayer(role.getRid());
		// 是否可以添加物品的标志,默认可以
		boolean flag = bagIsFull(role, items);
		if (flag == true) {
			List<ItemGet> mailItemList = JSONObject.decodeJsonArray(items,
					ItemGet[].class);
			List<ItemGet> itemList = new ArrayList<ItemGet>();
			for (ItemGet itemGet : mailItemList) {
				ItemCsv itemCsv = itemCache.getConfig(itemGet.getId());
				if (itemCsv.getItemType() == 1) {
					itemList.add(itemGet);
				}
			}
			// 背包没有满
			for (ItemGet itemGet : mailItemList) {
				switch (itemGet.getId()) {
				case 2010001:// 金币
					roleFunction.goldAdd(role, itemGet.getNum(), event, true);
					break;
				case 2020002:// 砖石
					roleFunction.diamondAdd(role, itemGet.getNum(), event);
					break;
				case 2030003:// 水晶
					roleFunction.crystalAdd(role, itemGet.getNum(), event);
					break;
				default:
					break;
				}
			}
			// 用户的资源信息
			roleFunction.sendBaseChangeMsg(role);
			// 道具
			if (itemList.size() > 0) {
				ArrayList<PackItem> packItemList = new ArrayList<PackItem>();
				for (ItemGet itemGet : itemList) {
					PackItem packItem = addPackItem(role, itemGet.getId(),
							itemGet.getNum(), ItemEvent.GetItem, true);
					packItemList.add(packItem);
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 通过背包id和用户id验证背包物品存不存在
	 * 
	 * @author G_T_C
	 * @param packId
	 * @param rid
	 * @return
	 */
	public boolean isExist(long packId, long rid) {
		Map<Long, PackItem> packItemMap = mapCache.get(rid);
		if (null != packItemMap && null != packItemMap.get(packId)) {
			return true;
		} else {
			return false;
		}
	}

	public void addLog(long rid, int itemId, int num, int event, int type,
			int beforValue, int afterValue) {
		// itemLogDao
		ItemLog itemLog = new ItemLog();
		itemLog.setItemId(itemId);
		itemLog.setItemNum(num);
		itemLog.setRid(rid);
		itemLog.setTime(TimeUtil.time());
		itemLog.setEvent(event);
		itemLog.setType((byte) type);
		itemLog.setBeforeValue(beforValue);
		itemLog.setAfterValue(afterValue);
		itemLogDao.addItemLog(itemLog);

	}

	/**
	 * 道具赠送相关日志
	 * @param buyerId 购买者id
	 * @param recipientId 接受者id
	 * @param itemId 道具id
	 * @param getTheCurrencyType 获得的货币类型
	 * @param buyerCurrencyType 购买的货币类型
	 * @param price 价格
	 * @param giftPrice 获得价格
	 * @param difference 差价
	 */
	public void addGiftLog(long buyerId, long recipientId, int itemId,
			int getTheCurrencyType, int buyerCurrencyType, int price,
			int giftPrice, int difference) {
		GiftLog giftLog = new GiftLog();
		giftLog.setBuyerId(buyerId);
		giftLog.setRecipientId(recipientId);
		giftLog.setItemId(itemId);
		giftLog.setGetTheCurrencyType(getTheCurrencyType);
		giftLog.setBuyerCurrencyType(buyerCurrencyType);
		giftLog.setPrice(price);
		giftLog.setGiftPrice(giftPrice);
		giftLog.setDifference(difference);
		giftLog.setEventTime(TimeUtil.time());
		giftLogDao.addGiftLog(giftLog);
	}

}
