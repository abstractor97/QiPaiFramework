package com.yaowan.framework.druid;
import javax.servlet.annotation.WebListener;

import com.alibaba.druid.support.http.WebStatFilter;

@WebListener
public class DruidWebStatFilter  extends WebStatFilter{


	public DruidWebStatFilter() {
		super();
	}
}



