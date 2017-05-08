package com.yaowan.server.game.function;

import org.springframework.stereotype.Component;

import com.yaowan.core.function.FunctionAdapter;
import com.yaowan.util.KeywordUtil;

/**
 * 屏蔽字处理函数
 * @author G_T_C
 */
@Component
public class KeyWordFunction extends FunctionAdapter{
	
	@Override
	public void handleOnServerStart() {
		KeywordUtil.loadDirtyWord();
	}

}
