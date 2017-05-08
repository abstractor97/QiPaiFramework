package com.yaowan.server.game.service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.constant.ItemEvent;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.csv.cache.ItemCache;
import com.yaowan.csv.entity.ItemCsv;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GExchange.GExchangeItem;
import com.yaowan.protobuf.game.GExchange.GExchangeLog;
import com.yaowan.protobuf.game.GExchange.GMsg_12017001;
import com.yaowan.protobuf.game.GExchange.GMsg_12017002;
import com.yaowan.protobuf.game.GExchange.GMsg_12017003;
import com.yaowan.protobuf.game.GExchange.GMsg_12017004;
import com.yaowan.server.game.function.ExchangeFunction;
import com.yaowan.server.game.function.ItemFunction;
import com.yaowan.server.game.function.MissionFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.model.data.dao.ExchangeDao;
import com.yaowan.server.game.model.data.entity.Exchange;
import com.yaowan.server.game.model.data.entity.PackItem;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.ExchangeLogDao;
import com.yaowan.server.game.model.struct.MissionType;
import com.yaowan.server.game.model.struct.TaskType;

@Component
public class ExchangeService {

	@Autowired
	private ExchangeLogDao exchangeLogDao;

	@Autowired
	private ItemCache itemCache;

	@Autowired
	private ItemFunction itemFunction;

	@Autowired
	private RoleFunction roleFunction;

	@Autowired
	private ExchangeFunction exchangeFunction;

	@Autowired
	private ExchangeDao exchangeDao;
	
	@Autowired
	private MissionFunction missionFunction;

	/**
	 * 获取用户的兑换信息
	 * 
	 * @param player
	 */
	public void readExchangeItem(Player player) {
		GMsg_12017001.Builder builder = GMsg_12017001.newBuilder();
		List<GExchangeItem> exchangeItems = exchangeFunction
				.getExchangeItem(player.getRole());
		builder.addAllExchangeList(exchangeItems);
		player.write(builder.build());
	}

	/**
	 * 兑换道具
	 * 
	 * @author G_T_C
	 * @param player
	 * @param goodsId
	 * @param quantity
	 */
	public void exchangeItem(Player player, long goodsId, int quantity) {
		// 记录用户兑换道具所花费的水晶数量
		int usetotal = 0;
		Role role = player.getRole();
		boolean isSuccess = false;
		int itemId = 0;
		// ExchangeItem exchangeItem = new ExchangeItem();
		if (quantity <= 0) {
			return;
		}
		Exchange exchange = exchangeFunction.getExchangeCache().get(goodsId);
		if (null == exchange) {
			LogUtil.info(role.getRid() + "请求参数的数量错误" + goodsId);
			return;
		}
		usetotal = exchange.getItemPrice() * quantity;// 奖券
		int selfNum = 0;
		// int exchangType = exchange.getExchangeType();
		/*
		 * switch (exchangType) { case 1:
		 */// 奖券
		selfNum = role.getCrystal();
		itemId = exchange.getItemId();
		ItemCsv itemCsv = itemCache.getConfig(itemId);
		int itemType = itemCsv.getItemType();
		int subItemType = itemCsv.getItemTypeSub();
		if (selfNum < usetotal) {// 判断是否达到兑换数量
			send(player, goodsId, 1, exchange.getStock(), itemType);
			return;
		}

/*		if (quantity > exchange.getStock()) {
			send(player, goodsId, 2, exchange.getStock(), itemType);
			return;
		}
*/
	
		
		int lastTime = exchangeLogDao.findByItemIdAndRid(role.getRid(),
				exchange.getItemId());
		int quotaTime = lastTime * exchange.getQuotaDay() * 24 * 60 * 60;
		if (quotaTime > TimeUtil.time()) {// 判断是否还在限制兑换的时间内
			send(player, goodsId, 3, exchange.getStock(), itemType);
			return;
		}

		synchronized (this) {
			if (exchange.getStock() >= quantity) {
				exchange.setStock(exchange.getStock() - quantity);
			} else {
				send(player, goodsId, 4, exchange.getStock(), itemType);
				return;
			}
		}
		
		switch (itemType) {
		case 1: {
			switch (subItemType) {
			case 1: {
				break;
			}
			case 2: {
				break;
			}
			case 3: {
				break;
			}
			}
			break;
		}
		case 2: {// 获得金币,砖石，奖券
			switch (subItemType) {
			case 1: {
				roleFunction.goldAdd(role, quantity*exchange.getQuantity(), MoneyEvent.EXCHANGE, true);
				isSuccess = true;
				break;
			}
			case 2: {
				roleFunction.diamondAdd(role, quantity*exchange.getQuantity(), MoneyEvent.EXCHANGE);
				isSuccess = true;
				break;
			}
			case 3: {
				roleFunction.crystalAdd(role, quantity*exchange.getQuantity(), MoneyEvent.EXCHANGE);
				isSuccess = true;

				break;
			}
			}
			break;
		}
		case 4: {// 兑换道具
			switch (subItemType) {
			case 1: {// 充值卡
				Map<Long, PackItem> packItems = itemFunction
						.getPackItemListByRid(role);
				if (null != packItems && !packItems.isEmpty()) {
					int i = 1;
					int size = packItems.keySet().size();
					for (Long key : packItems.keySet()) {
						PackItem packItem = packItems.get(key);
						if (packItem.getItemId() == itemId) {
							if ((packItem.getCount() + quantity) < itemCsv
									.getAccumulateLimit()) {
								isSuccess = true;
							}
							break;
						}
						if (i == size) {
							if (quantity < itemCsv.getAccumulateLimit()) {// 判断是否超个拥有数量
								isSuccess = true;
							}
						}
						i++;

					}
				} else {
					if (quantity < itemCsv.getAccumulateLimit()) {// 判断是否超个拥有数量
						isSuccess = true;
					}
				}
				if (isSuccess) {
					// 发放实物
					itemFunction.addPackItem(role, itemId, quantity,
							ItemEvent.ExchangeItem, true);// 获得物品
				}
				break;
			}
			case 2: {
				break;
			}
			case 3: {
				break;
			}
			}

			break;
		}
		}
		if (isSuccess) {
			LogUtil.info("兑换物品成功：itemId=" + exchange.getItemId() + ", rid="
					+ role.getRid());
			roleFunction.crystalSub(role, usetotal, MoneyEvent.EXCHANGE);// 减少水晶

			send(player, goodsId, 0, exchange.getStock(), itemType);
			// 更新库存
			exchange.markToUpdate("stock");
			exchangeDao.updateProperty(exchange);
			// 插入日志
			exchangeLogDao.insertLog(exchange.getId(), role.getNick(),
					role.getRid(), quantity, exchange.getItemPrice(),
					exchange.getItemId(), exchange.getServerId(),
					exchange.getItemName(), exchange.getIconInfo());
			// 发送兑换记录
			readExchangeLog(player);
			//检测任务
			missionFunction.checkTaskFinish(role.getRid(), TaskType.main_task, MissionType.LOTTER_EXCHANGE, 1);
		}

	}

	private void send(Player player, long goodsId, int flag, int stock,
			int shopType) {
		GMsg_12017002.Builder builder = GMsg_12017002.newBuilder();
		builder.setFlag(flag);
		builder.setGoodsId(goodsId);
		builder.setItemNum(stock);
		builder.setShopType(shopType);
		player.write(builder.build());
	}

	/*
	 * 下发兑换信息记录
	 */
	public void readExchangeLog(Player player) {
		GMsg_12017003.Builder builder = GMsg_12017003.newBuilder();
		List<GExchangeLog> exchangeLogs = exchangeFunction
				.getExchangeLogs(player.getRole());
		builder.addAllExchangeLogList(exchangeLogs);
		player.write(builder.build());
	}

	/**
	 * 使用充值卡
	 * 
	 * @author G_T_C
	 * @param role
	 * @param itemId
	 * @param num
	 */
	public void useRechargeCard(Player player, long packId, String phone) {

		try {
			boolean result = false;
			if (!isMobileNO(phone)) {
				LogUtil.info(player.getRole().getRid() + " 请求参数的数量错误 ," + packId);
				return;
			}
			result = exchangeFunction.useChargeCard(player.getRole(), packId, phone);
			GMsg_12017004.Builder builder = GMsg_12017004.newBuilder();
			builder.setPackId(packId);
			builder.setPhone(phone);
			if(result){
				builder.setFlag(0);
			}else{
				builder.setFlag(1);
			}
			player.write(builder.build());
		} catch (Exception e) {
			LogUtil.error(e);
		}

	}

	public boolean isMobileNO(String mobiles) {

		Pattern p = Pattern
				.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.find();
	}

	/**
	 * 新增兑换配置
	 * 
	 * @author G_T_C
	 * @param exchange
	 */
	public boolean addExchangeItem(Exchange exchange) {
		// 给配置缓存添加
		int itemId = exchange.getItemId();
		if (itemCache.getConfig(itemId) == null) {
			return false;
		}
		ItemCsv itemCsv = itemCache.getConfig(itemId);
		exchange.setGoodType(itemCsv.getItemType());
		exchangeFunction.addExchangeItem(exchange);
		return true;
	}

	/**
	 * 更新兑换配置
	 * 
	 * @author G_T_C
	 * @param exchange
	 */
	public void updateExchangeItem(Exchange exchange) {
		// 修改配置缓存
		exchangeFunction.updateExchangeItem(exchange);
	}

	/**
	 * 查询兑换的所有物品
	 * 
	 * @author G_T_C
	 * @return
	 */
	public List<Exchange> findExchanges() {
		return exchangeFunction.findExchanges();
	}

	public void updateStatusInfo(int status, String statusInfo, String id) {
		exchangeFunction.updateStatusInfo(status, statusInfo, id);

	}

	public void del(long id) {
		exchangeFunction.delById(id);
		
	}

}
