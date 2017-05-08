package com.yaowan.simulator;

import java.awt.Toolkit;

/**
 * 配置文件
 */
public final class SimulatorConfig {
	/**
	 * 屏幕的宽
	 */
	public static final int SCREEN_WIDTH = (int) Toolkit.getDefaultToolkit()
			.getScreenSize().getWidth();
	/**
	 * 用户屏幕的高
	 */
	public static final int SCREEN_HEIGHT = (int) Toolkit.getDefaultToolkit()
			.getScreenSize().getHeight();
}
