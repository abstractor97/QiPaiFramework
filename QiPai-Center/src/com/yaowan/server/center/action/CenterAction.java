package com.yaowan.server.center.action;

import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import com.yaowan.model.struct.GameServer;
import com.yaowan.protobuf.center.CGame;

public abstract class CenterAction {
	private int cmd;
	
	public int getCmd(){
		return cmd;
	}
	public CenterAction( int cmd){
		this.cmd = cmd;
		DispatchAction.registe(this);
	}
	public abstract void execute(GameServer gameServer,byte[] data);
	
	/**
	 * 转换成ByteString
	 * @param msg
	 * @return
	 */
	public static ByteString toByteString(MessageLite msg){
		return ByteString.copyFrom(msg.toByteArray());
	}
	
	public static void sendResponse(GameServer gameServer,int cmd,MessageLite msg){
		CGame.CMsg_22100007.Builder builder = CGame.CMsg_22100007.newBuilder();
		builder.setCmd(cmd);
		builder.setData(toByteString(msg));
		
		gameServer.write(builder.build());
	}
	
	public static void sendResponse(GameServer gameServer,int cmd,byte[] data){
		CGame.CMsg_22100007.Builder builder = CGame.CMsg_22100007.newBuilder();
		builder.setCmd(cmd);
		builder.setData(ByteString.copyFrom(data));
		gameServer.write(builder.build());
	}
	
	public void sendResponse(GameServer gameServer,MessageLite msg){
		sendResponse(gameServer,cmd, msg);
	}
	
}
