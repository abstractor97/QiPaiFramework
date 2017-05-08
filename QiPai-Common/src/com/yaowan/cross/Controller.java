package com.yaowan.cross;

import com.yaowan.model.struct.Player;

public abstract class Controller<T extends Player> {
	private short cmd;
	
	public Controller(short cmd){
		this.cmd = cmd;
		DispatchController.registe(this);
	}
	
	public short getCmd() {
		return cmd;
	}

	public abstract void execute(T player,BasePacket packet);
}
