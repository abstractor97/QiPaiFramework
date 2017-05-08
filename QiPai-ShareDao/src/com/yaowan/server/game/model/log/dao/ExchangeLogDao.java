package com.yaowan.server.game.model.log.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.yaowan.framework.database.SingleKeyLogDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.log.entity.ExchangeLog;

@Repository
public class ExchangeLogDao extends SingleKeyLogDao<ExchangeLog, Long>{

	public ExchangeLog insertLog(long exchangeId, String nick, long rid, int exchangeNum, int exchangePrice,int itemId, int serverId, String itemName, String iconInfo) {
		ExchangeLog log = new ExchangeLog();
		log.setId(TimeUtil.time()+"-"+rid);
		log.setExchangeId(exchangeId);
		log.setPhone("");
		log.setResultInfo("");
		log.setRid(rid);
		log.setSerialno("");
		log.setStatus(0);//0未充值， 1合作方受理中，2为充值成功，3充值失败
		log.setStatusInfo("");
		log.setNick(nick);
		log.setExchangeNum(exchangeNum);
		log.setExchangePrice(exchangePrice);
		log.setExchangeTime(TimeUtil.time());
		log.setItemId(itemId);
		log.setServerId(serverId);
		log.setItemName(itemName);
		log.setIconInfo(iconInfo);
		insert(log);
		return log;
	}

	public int findByItemIdAndRid(long rid, long exchangeId) {
		StringBuilder sql = new StringBuilder("select exchange_time from exchange_log where rid =");
		sql.append(rid).append(" and ").append("exchange_id=").append(exchangeId).append(" order by exchange_time desc limit 0,1");
		List<Object[]> list = findColumn(sql.toString());
		if(list == null){
			return 0;
		}
		if(list.size()>0 && list.get(0) != null && list.get(0)[0] != null){
			return Integer.parseInt(list.get(0)[0]+"");
		}else{
			return 0;
		}
		
	}

	public void insertLog(long exchangId, long rid, String nick, int itemId) {
		ExchangeLog log = new ExchangeLog();
		log.setAddress("");
		log.setConsignee("");
		log.setExchangeTime(TimeUtil.time());
		log.setExchangeId(exchangId);
		log.setNick(nick);
		log.setPhone("");
		log.setRemark("");
		log.setResultInfo("");
		log.setRid(rid);
		log.setSerialno("");
		log.setStatus(0);
		log.setUseTime(0);
		log.setItemId(itemId);
		insert(log);
	}

	public ExchangeLog findLog(int itemId, long rid) {
		String sql = "select * from exchange_log where item_id="+ itemId+" and rid="+ rid+" and status= 0 order by exchange_time limit 0,1";
		return find(sql);
	}

	public List<ExchangeLog> getExchangeItemListByRid(long rid) {
		String sql = "select * from exchange_log where  rid="+ rid+" order by exchange_time desc limit 0,10";
		return findList(sql);
	}

	
}
