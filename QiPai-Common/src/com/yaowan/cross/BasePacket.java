package com.yaowan.cross;


public class BasePacket{
	//协议头
	public static final byte HEAD= (byte)168;
	//跨服协议号
	private short cmd;
	//地区游戏请求的协议号
	private int gmsgID;
	//roleId
	private long rid;
	
	//请求的数据
	private byte[] data;
	
	private CrossPlayer crossPlayer;
	
	public BasePacket(short cmd,int gmsgID,long rid,byte[] data){
		this.cmd = cmd;
		this. gmsgID = gmsgID;
		this.data = data;
		this.rid = rid;
	}

	public short getCmd() {
		return cmd;
	}

	public void setCmd(short cmd) {
		this.cmd = cmd;
	}

	public int getGmsgID() {
		return gmsgID;
	}

	public void setGmsgID(int gmsgID) {
		this.gmsgID = gmsgID;
	}

	public long getRid() {
		return rid;
	}

	public void setRid(long rid) {
		this.rid = rid;
	}

	public byte[] getData() {
		if(data == null){
			data = new byte[0];
		}
		return data;
	}
	

	public void setData(byte[] data) {
		this.data = data;
	}

	public CrossPlayer getCrossPlayer() {
		return crossPlayer;
	}

	public void setCrossPlayer(CrossPlayer crossPlayer) {
		this.crossPlayer = crossPlayer;
	}
	
	
}
