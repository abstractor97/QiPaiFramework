package com.yaowan.server.game.center;


public abstract class Receive {
	private int cmd;
	
	public int getCmd(){
		return cmd;
	}
	public Receive( int cmd){
		this.cmd = cmd;
		DispatchReceive.registe(this);
	}
	public abstract void execute(byte[] data);
	
	
}
