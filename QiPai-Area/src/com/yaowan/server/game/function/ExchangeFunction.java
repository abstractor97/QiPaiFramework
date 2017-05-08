package com.yaowan.server.game.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.ItemCache;
import com.yaowan.csv.entity.ItemCsv;
import com.yaowan.framework.util.Http;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.StringUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.protobuf.game.GExchange.GExchangeItem;
import com.yaowan.protobuf.game.GExchange.GExchangeLog;
import com.yaowan.server.game.model.data.dao.ExchangeDao;
import com.yaowan.server.game.model.data.entity.Exchange;
import com.yaowan.server.game.model.data.entity.PackItem;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.ExchangeLogDao;
import com.yaowan.server.game.model.log.entity.ExchangeLog;
import com.yaowan.util.MD5Util;

@Component
public class ExchangeFunction extends FunctionAdapter {
	@Autowired
	private ItemFunction itemFunction;
	
	@Autowired
	private MailFunction mailFunction;

	@Autowired
	private ExchangeDao exchangeDao;

	@Autowired
	private ExchangeLogDao exchangeLogDao;
	
	@Autowired
	private ItemCache itemCache;

	private Map<Long, Exchange> exchangeCache = new ConcurrentHashMap<>();

	/**
	 * 得到用户的兑换信息
	 * 
	 * @author G_T_C
	 * @return
	 */
	public List<GExchangeItem> getExchangeItem(Role role) {
		List<GExchangeItem> result = new ArrayList<GExchangeItem>();
		exchangeCache = getExchangeCache();
		int time = TimeUtil.time();
		if (null != exchangeCache && !exchangeCache.isEmpty()) {
			for (Long key : exchangeCache.keySet()) {
				Exchange exchange = exchangeCache.get(key);
				if(exchange.getStartTime() <= time && exchange.getEndTime()> time){
					GExchangeItem.Builder builder = GExchangeItem.newBuilder();
					builder.setExchangeType(exchange.getExchangeType());
					builder.setGoodsId(exchange.getId());
					builder.setItemId(exchange.getItemId());
					builder.setItemInfo(exchange.getItemInfo());
					builder.setItemOder(exchange.getItemOrder());
					builder.setName(exchange.getItemName());
					builder.setPrice(exchange.getItemPrice());
					builder.setQuantity(exchange.getOneQuotaNum());
					builder.setShopType(exchange.getGoodType());
					builder.setEndTime(exchange.getEndTime());
					builder.setStartTime(exchange.getStartTime());
					builder.setQuantity(exchange.getQuantity());
					builder.setOneQuotaNum(exchange.getOneQuotaNum());
					builder.setStock(exchange.getStock());
					int quotaTime = exchangeLogDao.findByItemIdAndRid(role.getRid(), exchange.getId());
					builder.setQuotaTime(quotaTime*exchange.getQuotaDay()*24*60*60);
					result.add(builder.build());
				}
				
			}
		}
		return result;
	}

	public Map<Long, Exchange> getExchangeCache() {
		if (exchangeCache.size() <= 0) {
			exchangeCache = exchangeDao.findAllMap();
		}
		return exchangeCache;
	}

	/**
	 * 得到用户的兑换记录信息
	 * 
	 * @author XSY
	 * @param role
	 * @return
	 */
	public List<GExchangeLog> getExchangeLogs(Role role) {
		List<ExchangeLog> list = exchangeLogDao.getExchangeItemListByRid(role
				.getRid());
		GExchangeLog.Builder GExchangelog = GExchangeLog.newBuilder();
		List<GExchangeLog> result = new ArrayList<GExchangeLog>();
		if(list == null){
			return result;
		}
		for (ExchangeLog log : list) {
			Exchange exchange = exchangeCache.get(log.getExchangeId());
			if (exchange != null) {
				GExchangelog.setItemName(exchange.getItemName());
				GExchangelog.setExchageNum(log.getStatus());
				GExchangelog.setCrystal(log.getExchangeNum()
						* log.getExchangePrice());
				GExchangelog.setExchangeTime(log.getExchangeTime());
				GExchangelog.setState(log.getStatus());
				result.add(GExchangelog.build());
			}
		}
		return result;

	}

	/**
	 * 新增兑换配置
	 * 
	 * @author G_T_C
	 * @param exchange
	 */
	public void addExchangeItem(Exchange exchange) {
		// 给配置缓存添加
		exchangeDao.insert(exchange);
		exchangeCache.put(exchange.getId(), exchange);
	}

	/**
	 * 更新兑换配置
	 * 
	 * @author G_T_C
	 * @param exchange
	 */
	public void updateExchangeItem(Exchange exchange) {
		// 修改配置缓存
		exchangeDao.updateProperty(exchange);
		exchangeCache.put(exchange.getId(),exchangeDao.findByKey(exchange.getId()));
	}

	/**
	 * 查询兑换的所有物品
	 * 
	 * @author G_T_C
	 * @return
	 */
	public List<Exchange> findExchanges() {
		return exchangeDao.findAll();
	}

	/**
	 * 使用充值卡
	 * @author G_T_C
	 * @param role
	 * @param itemId
	 * @param num
	 * @param phone
	 * @throws Exception
	 */
	public boolean useChargeCard(Role role, Long  packId, String phone) {
		Map<Long, PackItem> packItems = itemFunction.getPackItemListByRid(role);
		if(packItems == null){
			LogUtil.error("packItem is null ! rid=" + role.getRid());
			return false;
		}
		if(packItems.get(packId) == null){
			LogUtil.error("该玩家不存在该物品 ! rid=" + role.getRid()+", packId="+packId);
			return false;
		}
		int itemId = packItems.get(packId).getItemId();
		ExchangeLog log = exchangeLogDao.findLog(itemId,
				role.getRid());
		if(log == null){
			LogUtil.error("不存在未使用的充值卡：rid="+ role.getRid()+", itemId="+itemId);
			return false;
		}
		
		ItemCsv itemCsv = itemCache.getConfig(itemId);
		String xmlResult = sendTopUp(phone, log, itemCsv);
		Map<String, Object> resultMap;
		try {
			resultMap = xmlStrToMap(xmlResult);
			if (null != resultMap && resultMap.size() > 0) {
				Integer reqcode = Integer.parseInt(resultMap.get("reqcode")+"");
				if(reqcode != null && reqcode == 0){
					log.setStatus(1);
				}else if(reqcode != null && reqcode == 1){
					log.setStatus(2);
				}else{
					log.setStatus(3);
				}
				
			}
			log.setResultInfo(xmlResult == null ? "" : xmlResult);
			log.markToUpdate("status");
			log.markToUpdate("resultInfo");
			log.setUseTime(TimeUtil.time());
			log.markToUpdate("useTime");
			log.setPhone(phone);
			log.markToUpdate("phone");
		} catch (Exception e) {
			LogUtil.error(e);
		}
		itemFunction.usePackItem(role, packId, true);
		//发送邮件
		String content = "您使用了"+itemCsv.getGeneral01()+"充费卡，系统将于30分钟内充值到您的提交号码中。\n如遇到充值不到账，请联系客服：400-00-00\n祝您\n游戏愉快\n\t\t\t——昭通棋牌";
		mailFunction.insertMailByInside(role.getRid(), -1, content, "", "充值卡使用消息", 0);
		exchangeLogDao.updateProperty(log);
		return true;
	}

	/**
	 * 发送充值
	 * @author G_T_C
	 * @param phone
	 * @param log
	 * @param itemCsv
	 * @return
	 */
	private String sendTopUp(String phone, ExchangeLog log, ItemCsv itemCsv) {
		String channelNo = "338363";
		String userId = "tariffe_gzywwl";
		String userpws = MD5Util.encodeByMD5("tariffe_gzyw321");
		System.out.println(userpws);
		String tariffeValue = itemCsv.getGeneral01();
		String orderId = log.getId();
		String txnDate = TimeUtil.date("yyyyMMdd");
		String version = "1.0";
		byte isLarge = 1;
		String url = "http://tariffe.tx.phone580.com/fzsTariffe/api/external/tariffeOrderApi";

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("channelNo",channelNo);
		//params.put("userId", userId);
	//	params.put("userpws", userpws);
		params.put("phone", phone);
		params.put("tariffeValue",tariffeValue);
		params.put("orderId",orderId);
		params.put("retUrl", "http://yw-qipai-platform.allrace.com/external/rechargecard/callback");
		params.put("txnDate", txnDate);
		params.put("version", version);
		params.put("isLarge", isLarge);
		String md5Str = MD5Util.encodeByMD5(channelNo+userpws+phone+tariffeValue+isLarge+orderId+txnDate+version+userId);
		params.put("md5Str", md5Str);
		String xmlResult = Http.sendGet(url, params);
		LogUtil.info("充值返回信息：" + xmlResult);
		return xmlResult;
	}
	

	/**
	 * 充值回调时，返回的信息。
	 * @author G_T_C
	 */
	public void updateStatusInfo(int status, String statusInfo, String id){
		ExchangeLog log  = new ExchangeLog();
		log.setId(id);
		if(status == 1){
			log.setStatus(2);
		}else{
			log.setStatus(3);
		}
		log.setStatusInfo(statusInfo == null ? "" : statusInfo);
		log.markToUpdate("status");
		log.markToUpdate("statusInfo");
		exchangeLogDao.updateProperty(log);
	}

	/**
	 * 将xml格式的字符串转换成Map对象
	 * 
	 * @param xmlStr
	 *            xml格式的字符串
	 * @return Map对象
	 * @throws Exception
	 *             异常
	 */
	public static Map<String, Object> xmlStrToMap(String xmlStr)
			throws Exception {
		if (StringUtil.isStringEmpty(xmlStr)) {
			return null;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		// 将xml格式的字符串转换成Document对象
		Document doc = DocumentHelper.parseText(xmlStr);
		// 获取根节点
		Element root = doc.getRootElement();
		// 获取根节点下的所有元素
		@SuppressWarnings("unchecked")
		List<Element> children = root.elements();
		// 循环所有子元素
		if (children != null && children.size() > 0) {
			for (int i = 0; i < children.size(); i++) {
				Element child =  children.get(i);
				map.put(child.getName(), child.getTextTrim());
			}
		}
		return map;
	}

	public void delById(long id) {
		exchangeDao.delById(id);
		exchangeCache.remove(id);
	}

}
