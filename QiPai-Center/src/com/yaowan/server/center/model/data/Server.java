/**
 * 
 */
package com.yaowan.server.center.model.data;

import com.yaowan.framework.database.annotation.Column;
import com.yaowan.framework.database.annotation.Id;
import com.yaowan.framework.database.annotation.Id.Strategy;
import com.yaowan.framework.database.annotation.Table;



/**
 * @author zane
 *
 */
@Table(name = "server", comment = "游戏服数据")
public class Server {
	
	@Id(strategy = Strategy.AUTO)
	@Column(comment = "id")
	private int id;
	
	@Column(comment = "渠道id")
	private int platform;
	
	@Column(comment = "区域")
	private String areaName;
	
	@Column(comment = "名称")
	private String name;
	
	@Column(comment = "主机")
	private String host;
	
	@Column(comment = "端口")
	private String port;
	
	@Column(comment = "http端口")
	private int httpPort;
	
	@Column(comment = "服务器状态(0.未生效1.启动2.推荐3.更新中)")
	private int status;
	
	@Column(comment = "开服时间")
	private int openTime;
	
	@Column(comment = "启动时间")
	private int startTime;
	
	@Column(comment = "导量权重")
	private int rate;
	
	@Column(comment = "数据库地址")
	private String mysqlHost;
	
	@Column(comment = "数据库端口")
	private int mysqlPort;
	
	@Column(comment = "数据库用户")
	private String mysqlUser;
	
	@Column(comment = "数据库密码")
	private String mysqlPasswd;
	
	@Column(comment = "游戏服数据库名")
	private String mysqlDataName;
	
	@Column(comment = "游戏服日志库名")
	private String mysqlLogName;
	
	@Column(comment = "配置版本号")
	private String configVersion;
	
	@Column(comment = "是否为内网服务器0是,1不是")
	private int isIntranet;
	@Column(comment = "服务器类型, 0:地区游戏服务;1:提审服;2:跨服游戏服")
	private int type;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPlatform() {
		return platform;
	}

	public void setPlatform(int platform) {
		this.platform = platform;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public int getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(int httpPort) {
		this.httpPort = httpPort;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getOpenTime() {
		return openTime;
	}

	public void setOpenTime(int openTime) {
		this.openTime = openTime;
	}

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public String getMysqlHost() {
		return mysqlHost;
	}

	public void setMysqlHost(String mysqlHost) {
		this.mysqlHost = mysqlHost;
	}

	public int getMysqlPort() {
		return mysqlPort;
	}

	public void setMysqlPort(int mysqlPort) {
		this.mysqlPort = mysqlPort;
	}

	public String getMysqlUser() {
		return mysqlUser;
	}

	public void setMysqlUser(String mysqlUser) {
		this.mysqlUser = mysqlUser;
	}

	public String getMysqlPasswd() {
		return mysqlPasswd;
	}

	public void setMysqlPasswd(String mysqlPasswd) {
		this.mysqlPasswd = mysqlPasswd;
	}



	public String getConfigVersion() {
		return configVersion;
	}

	public void setConfigVersion(String configVersion) {
		this.configVersion = configVersion;
	}

	public int getIsIntranet() {
		return isIntranet;
	}

	public void setIsIntranet(int isIntranet) {
		this.isIntranet = isIntranet;
	}

	public String getMysqlDataName() {
		return mysqlDataName;
	}

	public void setMysqlDataName(String mysqlDataName) {
		this.mysqlDataName = mysqlDataName;
	}

	public String getMysqlLogName() {
		return mysqlLogName;
	}

	public void setMysqlLogName(String mysqlLogName) {
		this.mysqlLogName = mysqlLogName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}


	
    

	
}
