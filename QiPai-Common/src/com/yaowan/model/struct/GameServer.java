package com.yaowan.model.struct;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.Channel;

import com.yaowan.framework.core.model.AbstractServer;

public class GameServer extends AbstractServer {

	//在线人数
	private AtomicInteger online = new AtomicInteger(0);
	//跨区游戏服上，跑的跨区游戏类型列表
	private Integer[] gameTypes;
	
	public GameServer(Channel channel) {
		super(channel);
		// TODO Auto-generated constructor stub
	}
	
	public AtomicInteger getOnline() {
		return online;
	}

	public Integer[] getGameTypes() {
		return gameTypes;
	}

	public void setGameTypes(Integer[] gameTypes) {
		this.gameTypes = gameTypes;
	}

	@Override
	public long getId() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void close(){
		if(getChannel()!=null && getChannel().isOpen()){
			getChannel().close();
		}
	}

}
