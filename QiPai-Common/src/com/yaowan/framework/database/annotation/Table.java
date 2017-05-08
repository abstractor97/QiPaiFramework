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
 * 数据库表的属性
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Table {
	/**
	 * 表名
	 * 
	 * @return
	 */
	public String name() default "";
	/**
	 * 分表数
	 * 
	 * @return
	 */
	public int catalog() default 0;
	/**
	 * 结合catalog的组合定义
	 * 例如catalog = 10，catalog_ = 0，分表数为0到9
	 * 例如catalog = 6，catalog_ = 1，分表数为1到5
	 * 
	 * @return
	 */
	public int catalog_() default 0;
	
	/**
	 * 分表根据字段
	 * 
	 * @return
	 */
	public String catalogby() default "";
	/**
	 * 表注释
	 * 
	 * @return
	 */
	public String comment() default "";
}
