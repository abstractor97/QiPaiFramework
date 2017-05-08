package com.yaowan.server.game.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.reflect.TypeToken;
import com.yaowan.ServerConfig;
import com.yaowan.constant.GameError;
import com.yaowan.constant.ItemEvent;
import com.yaowan.constant.MoneyEvent;
import com.yaowan.constant.ShopType;
import com.yaowan.core.base.GlobalConfig;
import com.yaowan.csv.cache.ItemCache;
import com.yaowan.csv.cache.ShopCache;
import com.yaowan.csv.entity.ShopCsv;
import com.yaowan.framework.util.Http;
import com.yaowan.framework.util.JSONObject;
import com.yaowan.framework.util.JsonUtil;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GLogin.GMsg_12001001;
import com.yaowan.protobuf.game.GShop.GMsg_12007006;
import com.yaowan.protobuf.game.GShop.GMsg_12007007;
import com.yaowan.protobuf.game.GShop.GMsg_12007008;
import com.yaowan.protobuf.game.GShop.GMsg_12007009;
import com.yaowan.server.game.function.ChargeFunction;
import com.yaowan.server.game.function.ItemFunction;
import com.yaowan.server.game.function.RoleFunction;
import com.yaowan.server.game.function.ShopFuction;
import com.yaowan.server.game.function.ShopLogFunction;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.entity.Charge;
import com.yaowan.server.game.u8.U8Platform;

@Component
public class ShopService {

	@Autowired
	RoleFunction roleFunction;
	@Autowired
	ShopCache shopCache;
	@Autowired
	ShopLogFunction shopLogFunction;
	@Autowired
	ItemFunction itemFunction;
	@Autowired
	ItemCache itemCache;
	@Autowired
	ChargeFunction chargeFunction;
	@Autowired
	ShopFuction shopFuction;

	// type 1.人民币 2.钻石 3.金币
	public void buyGoods(Player player, int goodsId, int type, int payType,
			int num, int channel) {
		ShopCsv shopCsv = shopCache.getConfig(goodsId);
		// 判断商品是否存在
		if (shopCsv == null) {
			GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
			builder.setFlag(GameError.WRONG_PARAMETER);
			player.write(builder.build());
			return;
		}
		// 判断是否有该充值方法
		if (getItemPrice(shopCsv.getItemPrice(), String.valueOf(type)) == null) {
			GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
			builder.setFlag(GameError.WRONG_PARAMETER);
			player.write(builder.build());
			return;
		}
		// 商店类型
		int shopType = shopCsv.getShopType();
		switch (shopType) {
		case 1:// 金币
			buyCoin(player, shopCsv, type, payType, channel);
			break;
		case 2:// 砖石
			buyDiamond(player, shopCsv, type, payType, channel);
			break;
		case 3:// vip
			buyVip(player, shopCsv, type, payType, channel);
			break;
		case 4:// 道具
			buyItem(player, shopCsv, type, num, payType);
			break;
		default:
			break;
		}
		// 1为人民币充值购买 2金币购买 3水晶购买
		if (type != 1) {
			GMsg_12007006.Builder builder = GMsg_12007006.newBuilder();
			builder.setFlag(0);
			builder.setType(type);
			player.write(builder.build());
			// 物品首购信息
			roleChargeInfo(player);
		}
	}

	public void buyCoin(Player player, ShopCsv shopCsv, int type, int payType,
			int channel) {
		Role role = roleFunction.getRoleByRid(player.getRole().getRid());
		boolean isFirst = true;
		int goodsId = shopCsv.getGoodsId();
		String goodsIdStr = String.valueOf(goodsId);
		int coinAdd = shopCsv.getQuantity();
		// 首冲奖励
		if (role.getHasChargeInfo().contains(goodsIdStr)) {
			isFirst = false;
		}
		// 首冲是否有奖励
		if (type != 1) {
			if(isFirst) {
				if ((!StringUtil.isStringEmpty(shopCsv.getBonus()))
						&& (!shopCsv.getBonus().equals("0")))// 判断有没加成
				{
					addCDChargeInfo(role, String.valueOf(goodsId));
					coinAdd = Integer.parseInt(StringUtil.split(shopCsv.getBonus(),
							"_")[1]) * coinAdd / 100 + coinAdd;
				}
			}else {
				if ((!StringUtil.isStringEmpty(shopCsv.getExtraGift()))
						&& (!shopCsv.getExtraGift().equals("0")))// 判断有没加成
				{
					addCDChargeInfo(role, String.valueOf(goodsId));
					coinAdd = Integer.parseInt(StringUtil.split(shopCsv.getExtraGift(),
							"_")[1]) * coinAdd / 100 + coinAdd;
				}
			}
			
		}
		switch (type) {
		case 1:// 人民币
				// 充值记录
			int rmb = Integer
					.parseInt(getItemPrice(shopCsv.getItemPrice(), "1"));
//			if (role.getPlatform().equals("yaowan")) {
				String id = requestOrder(role, rmb, goodsId, coinAdd, type,
						payType);
//			}
			break;
		case 2:// 砖石
			int diamondSub = Integer.parseInt(getItemPrice(
					shopCsv.getItemPrice(), "2"));
			if (role.getDiamond() < diamondSub) {
				GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
				builder.setFlag(GameError.ROLE_MONEY_LACK);
				player.write(builder.build());
				return;
			} else {
				// 花费砖石
				roleFunction.diamondSub(role, diamondSub, MoneyEvent.BUYITEM,
						false);
				// 增加金币
				roleFunction.goldAdd(role, coinAdd, MoneyEvent.BUYITEM, true);
				// 日志处理
				shopLogFunction.addShopLog(role.getRid(), shopCsv.getGoodsId(),
						coinAdd, type, diamondSub,
						TimeUtil.time(), ShopType.CoinShop.getValue());
			}
			break;
		case 3:// 金币(金币不能买金币,不执行)
			break;
		default:
			break;
		}
	}

	public String requestOrder(Role role, int rmb, int goodsId, int value,
			int type, int payType) {
		String orderId = "";
		String yaowanOrderId = "";
		String extension = "";
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("platform", role.getPlatform());
		data.put("channel", 1);
		data.put("open_id", role.getOpenId());
		data.put("money", rmb);
		data.put("ip", "127.0.0.1");
		data.put("server_id", ServerConfig.serverId);
		data.put("role_id", role.getRid());
		data.put("goods_id", goodsId);
		data.put("goods_money", value);
		String str = Http.sendGet(GlobalConfig.chargeBase+"/api/pay/charge", data);
		HashMap<String, String> map = JSONObject.decode(str,
				new TypeToken<HashMap<String, String>>() {
				}.getType());
		if(map.containsKey("order_id")){
			orderId = map.get("order_id");
			yaowanOrderId = orderId;
		}else {
			
			String info = "在平台创建订单失败(yaowan)："+JSONObject.encode(data);
			LogUtil.error(info);
			throw new RuntimeException(info);
			
		}
		if(orderId != null && orderId.length() > 0 ){
			if(role.getU8id()>0){//来自U8平台的账号
				Map<String, Object> extesion = new HashMap<String,Object>();
				extesion.put("vAdd", String.valueOf(value));
				extesion.put("rid", String.valueOf(role.getRid()));
				extesion.put("orderId", orderId);
				orderId = "";
				
				String res = U8Platform.getInstance().getOrderId(role, ServerConfig.serverId, rmb, String.valueOf(goodsId), shopCache.getConfig(goodsId).getItemName(), JSONObject.encode(extesion));
				
				Map response = (Map)JsonUtil.decodeJson(res, Map.class);
				boolean flag = false;
				if (response != null) {
					try {
						if("1.0".equals(response.get("state").toString()) || "1".equals(response.get("state").toString())){
							orderId = ((Map)response.get("data")).get("orderID").toString();
							extension = ((Map)response.get("data")).get("extension").toString();
							flag = true;
						}
					} catch (Exception e) {
						flag = false;
						LogUtil.error(e.getMessage());
					}
				}
				
				if (!flag) {
					String info = "在平台创建订单失败(U8)："+JSONObject.encode(data);
					LogUtil.error(info);
					throw new RuntimeException(info);
				}
				
//				if(orderId==null || orderId.length() ==0){
//					String info = "在平台创建订单失败(U8)："+JSONObject.encode(data);
//					LogUtil.error(info);
//					throw new RuntimeException(info);
//					
//				}
			}
			if(orderId != null && orderId.length() > 0 ){
				GMsg_12007006.Builder databBuilder = GMsg_12007006.newBuilder();
				databBuilder.setFlag(0);
				databBuilder.setPayType(payType);
				databBuilder.setType(type);
				databBuilder.setOrderId(orderId);
				databBuilder.setSdkOrderId(orderId);
				databBuilder.setExtension(extension);
				roleFunction.sendMessageToPlayer(role.getRid(), databBuilder.build());
	
				chargeFunction.addChargeLog(yaowanOrderId, role.getRid(),
						goodsId, rmb, (byte) 0, 0, 0, 1, role.getPlatform(),
						ServerConfig.serverId,role.getU8id()>0?orderId:"");
			}
		}
		
		return orderId;
	}

	public void buyDiamond(Player player, ShopCsv shopCsv, int type,
			int payType, int channel) {
		Role role = roleFunction.getRoleByRid(player.getRole().getRid());
		boolean isFirst = true;
		int goodsId = shopCsv.getGoodsId();
		String goodsIdStr = String.valueOf(goodsId);
		int diamondAdd = shopCsv.getQuantity();
		// 首冲奖励
		if (role.getHasChargeInfo().contains(goodsIdStr)) {
			isFirst = false;
		}
		if (isFirst == true && type != 1) {
			if ((!StringUtil.isStringEmpty(shopCsv.getBonus()))
					&& (!shopCsv.getBonus().equals("0")))// 判断有没加成
			{
				addCDChargeInfo(role, String.valueOf(goodsId));
				diamondAdd = Integer.parseInt(StringUtil.split(
						shopCsv.getBonus(), "_")[1])
						* diamondAdd / 100 + diamondAdd;
			}
		}
		switch (type) {
		case 1:
			// 充值记录
			int rmb = Integer
					.parseInt(getItemPrice(shopCsv.getItemPrice(), "1"));
//			if (role.getPlatform().equals("yaowan")) {
				String id = requestOrder(role, rmb, goodsId, diamondAdd, type,
						payType);
//			}
			break;
		case 2:
			break;
		case 3:
			int coinSub = Integer.parseInt(getItemPrice(shopCsv.getItemPrice(),
					"3"));
			if (role.getGold() < coinSub) {
				GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
				builder.setFlag(GameError.ROLE_MONEY_LACK);
				player.write(builder.build());
				return;
			}
			// 增加砖石
			roleFunction.diamondAdd(role, diamondAdd, MoneyEvent.BUYITEM);
			// 减少金币
			roleFunction.goldSub(role, coinSub, MoneyEvent.BUYITEM, true);
			// 日志处理
			shopLogFunction.addShopLog(role.getRid(), shopCsv.getGoodsId(),
					diamondAdd, type, coinSub,
					TimeUtil.time(), ShopType.DiaMondShop.getValue());
			break;
		default:
			break;
		}

	}

	public void buyVip(Player player, ShopCsv shopCsv, int type, int payType,
			int channel) {

		Role role = roleFunction.getRoleByRid(player.getRole().getRid());
		boolean isHasCharge = isHasCharge(role,
				String.valueOf(shopCsv.getGoodsId()));
		switch (type) {
		case 1:
			// 充值记录
			int rmb = Integer
					.parseInt(getItemPrice(shopCsv.getItemPrice(), "1"));
//			if (role.getPlatform().equals("yaowan")) {
				String id = requestOrder(role, rmb, shopCsv.getGoodsId(),
						shopCsv.getVipDuration(), type, payType);

//			}
			break;
		case 2:
			// 减少的砖石
			int diamondSub = Integer.parseInt(getItemPrice(
					shopCsv.getItemPrice(), "2"));
			// 花费砖石
			roleFunction.diamondSub(role, diamondSub, MoneyEvent.BUYVIP, true);
			// 增加金币
			if (isHasCharge == true) {
				if ((!StringUtil.isStringEmpty(shopCsv.getBonus()))
						&& (!shopCsv.getBonus().equals("0"))) {
					int bonusGold = Integer.parseInt(shopCsv.getBonus().split(
							"_")[1]);
					roleFunction.goldAdd(role, bonusGold, MoneyEvent.BUYVIP,
							true);
				}
			}
			// vip的信息
			addVipChargeInfo(role, shopCsv.getGoodsId(),
					shopCsv.getItemOrder(), shopCsv.getVipDuration()
							* TimeUtil.ONE_HOUR, isHasCharge);
			// 日志处理
			shopLogFunction.addShopLog(role.getRid(), shopCsv.getGoodsId(),
					1, type, diamondSub,
					TimeUtil.time(), ShopType.VipShop.getValue());
			break;
		case 3:
			break;
		default:
			break;
		}

	}

	public void buyItem(Player player, ShopCsv shopCsv, int type, int num,
			int payType) {
		switch (type) {
		case 1:
			break;
		case 2:
			break;
		case 3:
			Role role = roleFunction.getRoleByRid(player.getRole().getRid());
			String itemPrice[] = StringUtil.split(shopCsv.getItemPrice(), "_");
			int coinSub = Integer.parseInt(itemPrice[1]);
			int needCoin = coinSub * num;
			if (role.getGold() < coinSub * num) {
				GMsg_12001001.Builder builder = GMsg_12001001.newBuilder();
				builder.setFlag(GameError.ROLE_MONEY_LACK);
				player.write(builder.build());
				return;
			}
			itemFunction.addPackItem(role, shopCsv.getItemId(), num,
					ItemEvent.BuyItem, true);
			shopLogFunction.addShopLog(role.getRid(), shopCsv.getGoodsId(),
					num,type, needCoin, TimeUtil.time(),
					ShopType.ItemShop.getValue());
			roleFunction.goldSub(role, needCoin, MoneyEvent.BUYITEM, true);
			break;
		default:
			break;
		}

	}

	/**
	 * CD 指的是 c指coin d指diamond 1表示coin的充值信息 2表示diamond的信息 order表示充值的项
	 * 
	 * @param role
	 * @param str
	 */
	public void addCDChargeInfo(Role role, String str) {
		role.markToUpdate("has_charge_info");
		if (StringUtil.isStringEmpty(role.getHasChargeInfo())) {
			role.setHasChargeInfo(str);
		} else {
			role.setHasChargeInfo(role.getHasChargeInfo()
					+ StringUtil.DELIMITER_BETWEEN_ITEMS + str);
		}
		roleFunction.updatePropertys(role);
	}

	/**
	 * 修改用户vip时间,以及首冲记录
	 * 
	 * @param role
	 * @param vipDuration
	 */
	public void addVipChargeInfo(Role role, int goodsId, int vip,
			int vipDuration, boolean isHasCharge) {
		if (isHasCharge == true) {
			role.markToUpdate("hasChargeInfo");
			if (StringUtil.isStringEmpty(role.getHasChargeInfo())) {
				role.setHasChargeInfo(String.valueOf(goodsId));
			} else {
				role.setHasChargeInfo(role.getHasChargeInfo()
						+ StringUtil.DELIMITER_BETWEEN_ITEMS + goodsId);
			}
		}
		role.markToUpdate("vipLevel");
		role.setVipLevel((short) vip);
		role.markToUpdate("vipTime");
		if (role.getVipTime() <= TimeUtil.time()) {
			role.setVipTime(TimeUtil.time() + vipDuration);
		} else {
			role.setVipTime(role.getVipTime() + vipDuration);
		}
		roleFunction.updatePropertys(role);
		// vip
		Player player = roleFunction.getPlayer(role.getRid());
		if (player != null) {
			roleVip(player);
		}
	}

	/**
	 * 返回价格信息
	 * 
	 * @param strA
	 *            （格式 1_3|2_30）
	 * @param strB
	 *            （类型 传1返3 传2返30 传3返null）
	 * @return
	 */
	public static String getItemPrice(String strA, String strB) {
		String[] sourceStrArray = strA.split("\\|");
		for (int i = 0; i < sourceStrArray.length; i++) {
			String a[] = sourceStrArray[i].split("_");
			if (a[0].equals(strB)) {
				return a[1];
			}
		}
		return null;
	}

	/**
	 * 是否首冲
	 * 
	 * @param role
	 * @param str
	 * @return
	 */
	public boolean isHasCharge(Role role, String str) {
		if (role.getHasChargeInfo().contains(str)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 商场的http充值回调方法
	 * 
	 * @param rid
	 * @param id
	 */
	public void reChargeCallBack(long rid, String id) {
		Role role = roleFunction.getRoleByRid(rid);
		Charge charge = shopFuction.getCharge(id);
		if (charge != null && charge.getStatus() == 0) {
			getGoods(role, charge);
			roleFunction.updatePlayerMoney(rid,charge.getRmb());
			GMsg_12007009.Builder builder = GMsg_12007009.newBuilder();
			builder.setGoodsId(charge.getType());
			roleFunction.sendMessageToPlayer(role.getRid(), builder.build());
		} else {
			Player player = roleFunction.getPlayer(role.getRid());
			if (player != null) {
				LogUtil.info(rid + " 没有订单");
			}
		}
	}

	/**
	 * 充值回调获得物品
	 * 
	 * @param role
	 * @param charge
	 */
	public void getGoods(Role role, Charge charge) {
		Player player = roleFunction.getPlayer(role.getRid());
		int goodsId = charge.getType();
		ShopCsv shopCsv = shopCache.getConfig(goodsId);
		boolean isFirst = isHasCharge(role, String.valueOf(charge.getType()));// 是否首冲
		int count = 0;
		switch (shopCsv.getShopType()) {
		case 1:// 金币的充值
			int coinAdd = shopCsv.getQuantity();
			// 首冲是否有奖励
			if (isFirst) {
				if ((!StringUtil.isStringEmpty(shopCsv.getBonus()))
						&& (!shopCsv.getBonus().equals("0")))// 判断有没加成
				{
					addCDChargeInfo(role, String.valueOf(goodsId));
					coinAdd = Integer.parseInt(StringUtil.split(
							shopCsv.getBonus(), "_")[1])
							* coinAdd / 100 + coinAdd;
				}
			}else {
				if ((!StringUtil.isStringEmpty(shopCsv.getExtraGift()))
						&& (!shopCsv.getExtraGift().equals("0")))// 判断有没加成
				{
					addCDChargeInfo(role, String.valueOf(goodsId));
					coinAdd = Integer.parseInt(StringUtil.split(
							shopCsv.getExtraGift(), "_")[1])
							* coinAdd / 100 + coinAdd;
				}
			}
			roleFunction.goldAdd(role, coinAdd, MoneyEvent.CHARGE, true);
			count = coinAdd;
			break;
		case 2:// 砖石的充值
			int diamondAdd = shopCsv.getQuantity();
			if (isFirst == true) {
				if ((!StringUtil.isStringEmpty(shopCsv.getBonus()))
						&& (!shopCsv.getBonus().equals("0")))// 判断有没加成
				{
					addCDChargeInfo(role, String.valueOf(goodsId));
					diamondAdd = Integer.parseInt(StringUtil.split(
							shopCsv.getBonus(), "_")[1])
							* diamondAdd / 100 + diamondAdd;
				}
			}
			// 增加砖石
			roleFunction.diamondAdd(role, diamondAdd, MoneyEvent.CHARGE);
			count = diamondAdd;
			break;
		case 3:// vip的充值
			if (isFirst == true) {
				if ((!StringUtil.isStringEmpty(shopCsv.getBonus()))
						&& (!shopCsv.getBonus().equals("0"))) {
					int bonusGold = Integer.parseInt(shopCsv.getBonus().split(
							"_")[1]);
					roleFunction.goldAdd(role, bonusGold, MoneyEvent.CHARGE,
							true);
				}
			}
			// vip
			addVipChargeInfo(role, shopCsv.getGoodsId(),
					shopCsv.getItemOrder(), shopCsv.getVipDuration()
							* TimeUtil.ONE_HOUR, isFirst);
			count = 1;
			break;
		default:
			break;
		}
		// 返回用户的首冲记录
		roleChargeInfo(player);
		// 修改充值记录
		chargeFunction.updateCharge(charge);
		// 日志处理
		shopLogFunction.addShopLog(role.getRid(), shopCsv.getGoodsId(),
				count, 1, charge.getRmb(),
				TimeUtil.time(),shopCsv.getShopType());
	}

	/**
	 * 用户的首冲信息
	 * 
	 * @param player
	 */
	public void roleChargeInfo(Player player) {
		if (player != null) {// 判断是否为null,防止用户不在线情况
			Role role = roleFunction.getRoleByRid(player.getRole().getRid());
			GMsg_12007007.Builder builder = GMsg_12007007.newBuilder();
			builder.setHasChargeInfo(role.getHasChargeInfo());
			player.write(builder.build());
		}
	}

	/**
	 * 用户的vip信息
	 * 
	 * @param player
	 */
	public void roleVip(Player player) {
		if (player != null) {// 判断是否为null,防止用户不在线情况
			Role role = roleFunction.getRoleByRid(player.getRole().getRid());
			GMsg_12007008.Builder builder = GMsg_12007008.newBuilder();
			builder.setFlag(0);
			builder.setVipTime(role.getVipTime());
			builder.setVipleftTime(role.getVipTime() - TimeUtil.time());
			player.write(builder.build());
		}
	}

	
}
