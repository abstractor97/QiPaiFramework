package com.yaowan.cross;

import com.yaowan.cross.BasePacket;
import com.yaowan.cross.DispatchController;

public abstract class PacketProcessor {
    

	protected PacketProcessor() {
	}

	/**
	 * 网络包加入处理队列
	 * 
	 * @param packet 网络包
	 */
	public abstract void pushPacket(BasePacket packet);
	
	/**
	 * 取出一个有效的网络包进行处理
	 * @return 网络包
	 */
	public abstract BasePacket getAvailablePacket();
	
	/**
	 * 关闭处理器
	 */
	public abstract void shutdown();

}