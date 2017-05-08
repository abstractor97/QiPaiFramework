package com.yaowan.server.center.model.data.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.center.model.data.CenterRole;

@Component
public class CenterRoleDao extends SingleKeyDataDao<CenterRole,Long> {
	

	
	public CenterRole findByRid(long rid){
		return this.find("select * from role where rid ="+rid);
	}
	
}
