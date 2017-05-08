package com.yaowan.server.game.model.data.dao;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.framework.util.TimeUtil;
import com.yaowan.server.game.model.data.entity.Report;

@Component
public class ReportDao extends SingleKeyDataDao<Report, Long>{
	
	public void addReport(long rid1,long rid2,int reportType){
		Report report=new Report();
		report.setRid1(rid1);
		report.setRid2(rid2);
		report.setReportType(reportType);
		report.setTime(TimeUtil.time());
		insert(report);
	}
}
