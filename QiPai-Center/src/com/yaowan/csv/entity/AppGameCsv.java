package com.yaowan.csv.entity;

import java.util.List;

import com.yaowan.framework.util.StringUtil;

//cfg_game_list
public class AppGameCsv {
	/**
	 * 拍卖道具ID
	 */
	private int appId;
	/**
	 * 初始竞价
	 */
	private String appName;
	/**
	 * 加价单位
	 */
	private String gameList;
	
	/**
	 *
	 */
	private List<Integer> gameListList;
	
	public int getAppId() {
		return appId;
	}
	public void setAppId(int appId) {
		this.appId = appId;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getGameList() {
		return gameList;
	}
	public void setGameList(String gameList) {
		this.gameList = gameList;
	}
	public List<Integer> getGameListList() {
		if(gameListList==null){
			gameListList = StringUtil.stringToList(gameList, "|", Integer.class);
		}
		return gameListList;
	}
	public void setGameListList(List<Integer> gameListList) {
		this.gameListList = gameListList;
	}
	

}

