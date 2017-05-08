/**
 * 
 */
package com.yaowan.server.game.model.struct;




/**
 * 游戏服数据
 * @author zane
 *
 */
public class ServerInfo {
	

	private int id;
	

	private int platform;
	

	private String areaName;
	

	private String name;
	
	private String host;
	
	private int port;
	
	private int httpPort;
	

	private int status;
	

	private int openTime;
	
	private int startTime;
	
	private int rate;
	
	private String mysqlHost;
	
	private int mysqlPort;
	
	private String mysqlUser;
	
	private String mysqlPasswd;
	
	private String actorTable;
	
	private String logTable;
	
	private String configVersion;
	
	private int isIntranet;

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

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
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

	public String getActorTable() {
		return actorTable;
	}

	public void setActorTable(String actorTable) {
		this.actorTable = actorTable;
	}

	public String getLogTable() {
		return logTable;
	}

	public void setLogTable(String logTable) {
		this.logTable = logTable;
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
	



	
}
