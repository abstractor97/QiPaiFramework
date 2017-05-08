/**
 * 
 */
package com.yaowan.framework.server.base.handler;

import com.yaowan.framework.core.handler.AbstractHandler;
import com.yaowan.framework.core.handler.TransmitType;

/**
 * @author zane
 *
 */
public abstract class CenterHandler extends AbstractHandler {

	@Override
	public int transmitType() {
		return TransmitType.CENTER;
	}
}
