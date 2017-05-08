package com.yaowan.server.game.model.data.dao;

import java.util.List;

import org.springframework.stereotype.Component;

import com.yaowan.framework.database.SingleKeyDataDao;
import com.yaowan.server.game.model.data.entity.PackItem;

@Component
public class PackItemDao extends SingleKeyDataDao<PackItem,Long> {
	

	/**
	 * 根据用户rid来获取相关的信息
	 * @param rid
	 * @return
	 */
	public List<PackItem> getPackItemListByRid(long rid)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("select * from pack_item where rid = " + rid);
		return this.findList(sql.toString());
	}
	
	/**
	 * 根据流水id来获取相关信息
	 * @param id
	 * @return
	 */
	public PackItem getPackItemById(long id)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("select * from pack_item where id = " + id);
		return this.find(sql.toString());
	}
	
	/**
	 * 根据用户id和itemId来获取相关信息
	 * @param rid
	 * @param itemId
	 * @return
	 */
	public PackItem getPackItemByIds(long rid,long itemId)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("select * from pack_item where rid = " + rid+" and item_Id ="+itemId);
		return this.find(sql.toString());
	}
	
	/**
	 * 通过背包id和用户id
	 * @param id
	 * @param rid
	 * @return
	 */
	//G_T_C 注释
/*	public boolean isExist(long id,long rid)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("select * from pack_item where id = " + id+" and rid ="+rid);
		System.out.println(sql);
		PackItem packItem=this.find(sql.toString());
		if(packItem!=null){
			return true;
		}else{
			return false;
		}
	}*/
	
	/**
	 * 是否存在比这个数量多的道具
	 * @param id
	 * @param rid
	 * @return
	 */
	public  boolean isExistNum(long itemId,long rid,int num){
		
		String sql = "select * from pack_item where item_id = " +itemId+" and rid ="+rid+" and count>="+num;
		int count = findNumber(sql, int.class);
		if(count > 0){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * 通过itemId来判断
	 * @param itemId
	 * @param rid
	 * @param num
	 * @return
	 */
	public boolean isExistNumByItemId(long itemId,long rid,int num){

		String sql = "select count(*) from pack_item where item_id = " + itemId +" and rid ="+rid+" and count>="+num;
		//System.out.println(sql);
		int count = findNumber(sql, int.class);
		if(count > 0){
			return true;
		}else{
			return false;
		}	
	}
	
	/**
	 * 添加背包
	 * @param packItem
	 */
	public void addPackItem(PackItem packItem)
	{
		 this.insert(packItem);
	}
	
	/**
	 * 修改背包
	 * @param packItem
	 */
	public void updatePackItem(PackItem packItem)
	{
		this.update(packItem);
	}
	
	/**
	 * 删除背包
	 * @param packItem
	 */
	public void deletePackItem(PackItem packItem)
	{
		 this.delete(packItem);
	}
	
	
}
