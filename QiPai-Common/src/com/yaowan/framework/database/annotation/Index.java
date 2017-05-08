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
 * 数据库索引属性
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Index {
	/**
	 * 索引名字
	 * 
	 * @return
	 */
	public String[] names() default {};
	/**
	 * 索引引用的列名
	 * 
	 * @return
	 */
	public String[] indexs() default {};
	/**
	 * 索引的类型
	 * 
	 * @return
	 */
	public Type[] types() default {};
	/**
	 * 索引的方式
	 * 
	 * @return
	 */
	public Way[] ways() default {};
	
	public static enum Type {
		NORMAL(""),
		UNIQUE("UNIQUE"),
		FULLTEXT("FULLTEXT");
		
		private String v;
		
		private Type(String value) {
			this.v = value;
		}
		public String value() {
			return this.v;
		}
	}
	
	public static enum Way {
		BTREE("BTREE"),
		HASH("HASH");
		
		private String v;
		
		private Way(String value) {
			this.v = value;
		}
		public String value() {
			return this.v;
		}
	}
}
