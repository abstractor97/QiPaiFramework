package com.yaowan.framework.druid;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.util.IPRange;
import com.yaowan.framework.druid.DruidControllerSystemConfig;

public class DruidStatViewServlet extends StatViewServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DruidStatViewServlet() {
		super.username = DruidControllerSystemConfig.loginUsername;
		super.password = DruidControllerSystemConfig.loginPassword;
		List<IPRange> allowList = new ArrayList<>();
		String [] allows = DruidControllerSystemConfig.allow.trim().split(",") ;
		if(null != allows && allows.length>0){
			for(String s:allows){
				if(s.equals("")){
					continue;
				}
				allowList.add(new IPRange(s));
				super.allowList = allowList;
			}
		}	
		List<IPRange> denyList = new ArrayList<>();
		String [] denys = DruidControllerSystemConfig.deny.trim().split(",") ;
		if(null != denys && denys.length>0){
			for(String s:denys){
				if(s.equals("")){
					continue;
				}
				denyList.add(new IPRange(s));
				super.denyList = denyList;
			}
		}	
	}

}
