package com.yaowan.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class CheckTest {

	private String WorkPath = "D:/yaowanwork/qipaiwork/QiPaiNew";
	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Test
	public void checkArea(){
		checkPjt("QiPai-Area");
	}
	@Test
	public void checkCross(){
		checkPjt("QiPai-Cross");
	}
	@Test
	public void testMoveFile(){
		copyfile("D:/tmp/bak/copyfile");
	}
	
	private void checkPjt(String projectName){
		String basePath = WorkPath+"/"+projectName;
		Map<String, String> commonMap = qipaiCommon();
		Map<String, String> shareDaoMap = qipaiShareDao();
		Map<String, String> areaMap = qipaiArea();
		
		
		System.out.println(commonMap.size());
		System.out.println(shareDaoMap.size());
		System.out.println(areaMap.size());
		
		for (Map.Entry<String, String> entry : areaMap.entrySet()) {
			String path = entry.getKey();
			String fileName = entry.getValue();
			
			if(path.endsWith("ActivityDao.java")){
				System.out.println();
			}
			if(commonMap.containsKey(path)){
				delete(basePath+path);
				System.out.println("Common:"+path);
			}
			if(shareDaoMap.containsKey(path)){
				delete(basePath+path);
				System.out.println("ShareDao:"+path);
			}
		}
		
		//检查同名的文件
		
		Collection<String> commonNames = commonMap.values();
		Collection<String> shareDaoNames = shareDaoMap.values();
		for (Map.Entry<String, String> entry : areaMap.entrySet()) {
			String path = entry.getKey();
			String fileName = entry.getValue();
			
			if(commonNames.contains(fileName)){
				System.out.println("重名(Common):	"+fileName+"----"+path);
			}
			if(shareDaoNames.contains(fileName)){
				System.out.println("重名(ShareDao):	"+fileName+"----"+path);
			}
		}
		
	}
	
	private void delete(String path){
		File file = new File(path);
		if(file.isFile()){
			file.deleteOnExit();
		}
	}
	
	private Map<String, String> qipaiCommon(){
		Map<String, String> fileMap = new HashMap<String, String>();
		allFile(fileMap, WorkPath+"/QiPai-Common");
		return fileMap;
	}
	
	private Map<String, String> qipaiShareDao(){
		Map<String, String> fileMap = new HashMap<String, String>();
		allFile(fileMap, WorkPath+"/QiPai-ShareDao");
		return fileMap;
	}
	
	private Map<String, String> qipaiArea(){
		Map<String, String> fileMap = new HashMap<String, String>();
		allFile(fileMap, WorkPath+"/QiPai-Area");
		return fileMap;
	}
	
	private Map<String, String> qipaiCross(){
		Map<String, String> fileMap = new HashMap<String, String>();
		allFile(fileMap, WorkPath+"/QiPai-Cross");
		return fileMap;
	}
	
	
	private void allFile(Map<String, String> fileMap,String dir){
		File file = new File(dir);
		if(file.isDirectory()){
			
			File[] files = file.listFiles();
			for (File file2 : files) {
				if(file2.getName().startsWith(".")){
					continue;
				}
				if(file2.isDirectory()){
					allFile(fileMap, file2.getPath());
				}else if(file2.getName().endsWith(".java") && file2.getPath().indexOf("\\test\\")==-1){
					try {
						
						fileMap.put(file2.getPath().substring(file2.getPath().indexOf("\\src\\")), file2.getName());
					} catch (Exception e) {
						System.out.println(file2.getPath());
					}
				}
			}
		}
	}
	
	private void copyfile(String toBasePath){
		String[] paths= "D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/csv/ConfigLoader.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/druid;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/druid/DruidStatViewServlet.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/protobuf;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/protobuf/game;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/protobuf/game/GBaseMahJong.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/protobuf/game/GFriendRoom.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/protobuf/game/GMahJong.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/protobuf/game/GZXMahJong.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/server/game/event/type;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/server/game/event/type/HandleType.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/server/game/model/data/dao/NoticeDao.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/server/game/model/data/dao/RoleDao.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/server/game/model/log/dao/DouniuLogDao.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/server/game/model/log/dao/FriendRoomLogDao.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/server/game/model/log/dao/RoleRemainMoneyLogDao.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/server/game/model/log/entity/DouniuLog.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/server/game/model/log/entity/FriendRoomLog.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/server/game/model/log/entity/RoleRemainMoneyLog.java;D:/yaowanwork/qipaiwork/QiPaiNew/QiPai-Area/src/com/yaowan/server/game/model/struct/Game.java".split(";");
		
		for (String path : paths) {
			int index = path.indexOf("/src/");
			if( index == -1){
				continue;
			}
			
			String fromBasePath = path.substring(0, index);
			System.out.println(fromBasePath);
		}
		
		
	}

}
