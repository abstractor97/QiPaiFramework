/**
 * 
 */
package com.yaowan.excel.cache;

import org.springframework.stereotype.Component;

import com.yaowan.excel.ExcelCache;
import com.yaowan.excel.entity.StaffExcel;

/**
 * @author huangyuyuan
 *
 */
@Component
public class StaffCache extends ExcelCache<StaffExcel> {

	@Override
	public String getFileName() {
		return "Y-员工表.xlsx";
	}

	@Override
	public void loadOther() {
		for(StaffExcel excel : this.getAllList()) {
			System.out.println(excel.toString());
		}
	}
}
