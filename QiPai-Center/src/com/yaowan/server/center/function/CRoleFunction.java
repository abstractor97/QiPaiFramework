/**
 * 
 */
package com.yaowan.server.center.function;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.csv.cache.AppGameCache;
import com.yaowan.server.center.model.data.CenterRole;
import com.yaowan.server.center.model.data.dao.CenterRoleDao;

/**
 * @author zane
 *
 */
@Component
public class CRoleFunction extends FunctionAdapter{
	
	@Autowired
	private AppGameCache appGameCache;
	@Autowired
	private CenterRoleDao centerRoleDao;

	private Map<Long, CenterRole> roleMap = new ConcurrentHashMap<Long, CenterRole>();

	public long nextTime = 0;
	
	@Override
	public void handleOnServerStart() {

	}

	public CenterRole getCenterRole(long rid) {
		CenterRole centerRole = roleMap.get(rid);
		if (centerRole == null) {
			centerRole = centerRoleDao.findByRid(rid);
		}
		return centerRole;
	}

	public void addCenterRole(CenterRole centerRole) {
		updateCenterRole(centerRole);
		centerRoleDao.insert(centerRole);
	}
	public void updateCenterRole(CenterRole centerRole) {
		roleMap.put(centerRole.getRid(), centerRole);
		if(centerRole.getId()>0){
			centerRoleDao.update(centerRole);
		}
	}
	
}
