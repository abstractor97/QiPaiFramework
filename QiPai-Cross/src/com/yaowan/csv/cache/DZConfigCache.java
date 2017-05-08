package com.yaowan.csv.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.yaowan.csv.ConfigCache;
import com.yaowan.csv.entity.DZConfigCsv;

@Component
public class DZConfigCache extends ConfigCache<DZConfigCsv> {
	
	private Map<Integer, DZConfigCsv> caches = new HashMap<Integer, DZConfigCsv>();
	
	private Map<DZConfigCsv, Integer> dzConfig2RoomType = new HashMap<DZConfigCsv, Integer>(); 
	
	private Map<Integer, List<DZConfigCsv>> roomType2DZConfigs = new HashMap<Integer, List<DZConfigCsv>>();
	private List<Integer> roomTypes = new ArrayList<Integer>();
	
	
	@Override
	public String getFileName() {
		return "cfg_dzconfig";
	}
	@Override
	public void loadIndexsMap() {
		this.loadMap(new String[]{ "ID" }, caches);
		dzConfig2RoomType.clear();
		roomTypes.clear();
		roomType2DZConfigs.clear();
		for (DZConfigCsv config : caches.values()) {
			dzConfig2RoomType.put(config,config.getRoomType());
			if(!roomTypes.contains(config.getRoomType())){
				roomTypes.add(config.getRoomType());
			}
			if(!roomType2DZConfigs.containsKey(config.getRoomType())){
				roomType2DZConfigs.put(config.getRoomType(), new ArrayList<DZConfigCsv>());
			}
			roomType2DZConfigs.get(config.getRoomType()).add(config);
		}
	}
	/**
	 * 获取指定类型的房间配置列表
	 * @param roomType
	 * @return
	 */
	public List<DZConfigCsv> getDzConfigCsvs(int roomType){
		if(!roomType2DZConfigs.containsKey(roomType)){
			throw new RuntimeException("DZConfigCsv 配置项错误，不存在此配置项 :roomType="+roomType);
		}
		return roomType2DZConfigs.get(roomType);
	}
	/**
	 * 获取单条配置项
	 * @param id
	 * @return
	 */
	public DZConfigCsv getDzConfigCsv(int id){
		if(!caches.containsKey(id)){
			throw new RuntimeException("DZConfigCsv 配置项错误， 不存在此配置项 : id="+id);
		}
		return caches.get(id);
	}
	/**
	 * 获取此房间类型的所有配置项
	 * @param roomType
	 * @return
	 */
	public int getRoomType(int id){
		if(!caches.containsKey(id)){
			throw new RuntimeException("DZConfigCsv 配置项错误， 不存在此配置项 : id="+id);
		}
		return dzConfig2RoomType.get(caches.get(id));
	}
	
	public Collection<Integer> getRoomCids(){
		return caches.keySet();
	}
	public List<Integer> getRoomTypes(){
		return roomTypes;
	}
}
