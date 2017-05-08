package com.yaowan.server.game.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.GameError;
import com.yaowan.csv.cache.ItemCache;
import com.yaowan.csv.entity.ItemCsv;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.model.struct.ItemGet;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GItem.GMsg_12005001;
import com.yaowan.protobuf.game.GItem.GMsg_12005002;
import com.yaowan.protobuf.game.GItem.GPackItem;
import com.yaowan.protobuf.game.GLogin.GMsg_12001001;
import com.yaowan.server.game.function.ItemFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.data.dao.PackItemDao;
import com.yaowan.server.game.model.data.entity.PackItem;
import com.yaowan.server.game.model.data.entity.Role;

@Component
public class ItemService {

	@Autowired
	private PackItemDao packItemDao;

	@Autowired
	private ItemFunction itemFunction;

	@Autowired
	private ItemCache itemCache;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private ShopService shopService;

	/**
	 * 获取用户的物品箱
	 * 
	 * @param player
	 */
	public void listPackItem(Player player) {
		GMsg_12005001.Builder builder = GMsg_12005001.newBuilder();
		Map<Long, PackItem> map = itemFunction.getPackItemListByRid(player.getRole());
		for (Map.Entry<Long, PackItem> entry : map.entrySet()) {
			PackItem packItem = entry.getValue();
			ItemCsv itemCsv = itemCache.getConfig(packItem.getItemId());// 缓存数据
			if(itemCsv==null){
				LogUtil.error("packItem.getItemId()"+packItem.getItemId());
				continue;
			}
			GPackItem.Builder itemBuild = GPackItem.newBuilder();
			itemBuild.setId(packItem.getId());
			itemBuild.setItemId(packItem.getItemId());// id
			itemBuild.setCount(packItem.getCount());// 数量
			itemBuild.setItemName(itemCsv.getItemName());// 名字
			itemBuild.setIcon(itemCsv.getIcon());// 图片
			itemBuild.setItemDescribe(itemCsv.getItemDescribe());// 描述
			itemBuild.setExpire(packItem.getExpire());
			builder.addPackItemList(itemBuild);
		}
		player.write(builder.build());
	}

	/**
	 * 使用物品
	 * 
	 * @param player
	 * @param id
	 */
	public void useItem(Player player, long id) {
		Role role = player.getRole();
		// 数据异常
		if (!itemFunction.isExist(id, role.getRid())) {
			GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
			builder.setFlag(GameError.WRONG_PARAMETER);
			player.write(builder.build());
			return;
		}
		// 使用+数据返回
		itemFunction.usePackItem(role, id,true);
	}

	/**
	 * 丢弃物品 
	 * @param player
	 * @param id
	 * @param num
	 */
	public void giveUpItem(Player player, long id, int num) {
		Role role=player.getRole();
		long rid = player.getRole().getRid();
		PackItem packItem = packItemDao.getPackItemById(id);
		// 数据异常
		if (!itemFunction.isExist(id, rid)) {
			GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
			builder.setFlag(GameError.WRONG_PARAMETER);
			player.write(builder.build());
			return;
		}
		// 返回数据
		if (packItem.getCount() < num) {
			num = packItem.getCount();
		}
		//丢弃+数据返回
		itemFunction.giveUpPackItem(role, id, num,true);
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
			if (itemCsv.getItemType() == 1) {
				hm.put(itemGet.getId(), itemGet.getNum());
			}
		}
		if (hm.size() > 0) {
			return itemFunction.bagIsFull(role.getRid(), hm);
		} else {
			return true;
		}
	}

	
	
	
	/**
	 * 有关用户背包变动的输出信息
	 * @param packItemList
	 */
	public void packItemMessge(Player player,ArrayList<PackItem> packItemList,GMsg_12005002.Builder builder)
	{
		//用户的背包信息
		for (PackItem packItem : packItemList) {
			ItemCsv itemCsv = itemCache.getConfig(packItem.getItemId());// 缓存数据
			GPackItem.Builder itemBuild = GPackItem.newBuilder();
			itemBuild.setCount(packItem.getCount());
			itemBuild.setIcon(itemCsv.getIcon());
			itemBuild.setItemDescribe(itemCsv.getItemDescribe());
			itemBuild.setId(packItem.getId());
			itemBuild.setExpire(-1);
			itemBuild.setItemName(itemCsv.getItemName());
			itemBuild.setItemId(packItem.getItemId());
			builder.addPackItemList(itemBuild);
		}
		player.write(builder.build());	
	}

	public void addLog(long rid, int itemId,int num, int event, int type, int beforValue, int afterValue) {
		itemFunction.addLog(rid,itemId,num, event,type,beforValue, afterValue);
	}
	
	/**
	 * 赠送道具流水日志
	 * @param buyerId 购买者id
	 * @param recipientId 接受者id
	 * @param itemId 道具id
	 * @param getTheCurrencyType 购买道具货币id
	 * @param buyerCurrencyType 接受道具货币id
	 * @param price 购买价格
	 * @param giftPrice 实际获得价格
	 * @param difference 差价
	 */
	public void addGiftLog(long buyerId,long recipientId,int itemId,int getTheCurrencyType,int buyerCurrencyType,int price,int giftPrice,int difference){
		itemFunction.addGiftLog(buyerId, recipientId, itemId, getTheCurrencyType, buyerCurrencyType, price, giftPrice, difference);
	}
	
}
