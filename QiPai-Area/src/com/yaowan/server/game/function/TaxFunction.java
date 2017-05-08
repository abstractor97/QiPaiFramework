package com.yaowan.server.game.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.framework.util.LogUtil;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.dao.RoleDao;
import com.yaowan.server.game.model.data.dao.TaxDao;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.data.entity.Tax;
import com.yaowan.server.game.model.log.dao.ChargeDao;
import com.yaowan.server.game.model.log.dao.RoomCountLogDao;
import com.yaowan.server.game.model.log.dao.RoomRoleActiveDao;
import com.yaowan.server.game.model.log.dao.RoomRoleLoginDao;
import com.yaowan.server.game.model.log.dao.RoomRoleOnlineDao;
import com.yaowan.server.game.model.log.entity.Charge;
import com.yaowan.server.game.model.log.entity.RoomCountLog;
import com.yaowan.server.game.model.log.entity.RoomRoleLogin;

/**
 * 
 * @author G_T_C
 */
@Service
public class TaxFunction extends FunctionAdapter {

	private Map<Long, Tax> gameTaxMap = new ConcurrentHashMap<>();
	
	@Autowired
	private TaxDao taxDao;



	public Tax getTax(long id){
		Tax tax ;
		if(gameTaxMap.containsKey(id)){
			tax = gameTaxMap.get(id);
		}else{
			tax = new Tax();
			tax = taxDao.findByKey(id);
			if(tax != null){
				gameTaxMap.put(id, tax);
			}
			
		}
		return tax;
	}
	
	public Tax getTaxByTypes(byte gameType,int roomType){
		for(Tax tax : gameTaxMap.values()){
			if(tax.getGameType() == gameType && tax.getRoomType() == roomType){
				return tax;
			}
		}
		Tax tax = taxDao.findTax(gameType, roomType);
		if(tax != null){
			gameTaxMap.put(tax.getId(), tax);
			return tax;
		}else{
			return null;
		}
		
	}
	
	public List<Tax> getAllTax(){
		return taxDao.findAll();
	}
	
	public boolean updateTax(long id,int taxCount) {
		Tax tax = getTax(id);
		if (tax == null) {
			return false;
		} else {
			tax.setTaxCount(taxCount);
			taxDao.update(tax);
			gameTaxMap.put(id, tax);
			return true;
		}
	}
	
	public void addTax(byte gameType,int roomType,int taxCount){
		Tax tax = new Tax();
		tax.setGameType(gameType);
		tax.setRoomType(roomType);
		tax.setTaxCount(taxCount);
		taxDao.addTax(tax);
		LogUtil.info("id：" + tax.getId());
		gameTaxMap.put(tax.getId(), tax);
	}
}
