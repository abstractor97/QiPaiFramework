/**
 * 
 */
package com.yaowan.framework.database.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author huangyuyuan
 * 
 * 数据库ID属性
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id {
	/**
	 * ID生成策略
	 * 
	 * @return
	 */
	public Strategy strategy() default Strategy.IDENTITY;
	/**
	 * ID的序数
	 * 
	 * @return
	 */
	public int sort() default 0;
	
	public static enum Strategy {
		/**
		 * 自定义ID
		 */
		IDENTITY,
		/**
		 * 自增长ID
		 */
		AUTO
	}
}
