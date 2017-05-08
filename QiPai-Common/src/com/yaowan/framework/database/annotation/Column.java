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
 * 数据库字段属性
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Column {
	/**
	 * 字段长度，仅对String生效
	 * 
	 * @return
	 */
	public int length() default 255;
	/**
	 * 备注
	 * 
	 * @return
	 */
	public String comment();
}
