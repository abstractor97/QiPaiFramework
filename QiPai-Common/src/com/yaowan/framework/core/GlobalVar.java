/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yaowan.framework.core;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.yaowan.framework.util.TimeUtil;

/**
 * 房间id
 * @author zane
 */
@Component
public class GlobalVar {

	//自增流水id 每几分秒钟重置一次 以免发生id重复
    public AtomicLong actionId = new AtomicLong(TimeUtil.millisTime());
}
