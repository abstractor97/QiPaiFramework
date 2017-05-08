/**
 * 
 */
package com.yaowan.framework.server.base.handler;

import com.yaowan.framework.core.handler.AbstractHandler;
import com.yaowan.framework.core.handler.TransmitType;

/**
 * @author huangyuyuan
 *
 */
public abstract class GameHandler extends AbstractHandler {

	@Override
	public final int transmitType() {
		return TransmitType.GAME;
	}
}
