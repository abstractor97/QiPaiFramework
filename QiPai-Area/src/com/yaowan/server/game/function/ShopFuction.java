package com.yaowan.server.game.function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.model.struct.Player;
import com.yaowan.protobuf.game.GShop.GMsg_12007007;
import com.yaowan.protobuf.game.GShop.GMsg_12007008;
import com.yaowan.server.game.model.data.entity.Role;
import com.yaowan.server.game.model.log.dao.ChargeDao;
import com.yaowan.server.game.model.log.entity.Charge;

@Component
public class ShopFuction extends FunctionAdapter{
	
	@Autowired
	RoleFunction roleFunction;
	
	@Autowired
	ChargeDao chargeDao;
	
	@Override
	public void handleOnRoleLogin(Role role) {
		//default handle nothing
		//用户首冲记录
		Player player=roleFunction.getPlayer(role.getRid());
		GMsg_12007007.Builder builder = GMsg_12007007.newBuilder();
		builder.setFlag(0);
		builder.setHasChargeInfo(role.getHasChargeInfo());
		player.write(builder.build());
		//用户vip信息
		GMsg_12007008.Builder builder2 = GMsg_12007008.newBuilder();
		builder2.setFlag(0);
		builder2.setVipTime(role.getVipTime());
		builder2.setVipleftTime(role.getVipTime()-TimeUtil.time());
		player.write(builder2.build());
	}
	
	public Charge getCharge(String id) {
		return chargeDao.findByKey(id);
	}
}
