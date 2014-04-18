package android_programe.FileSystem;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 保存了各个设备以及同其相对应的版本号
 * @author zhangsw
 *
 */
public class VersionMap implements Serializable{
	
	//用于保存设备id同其版本号
	private HashMap<String,Integer> versionMap;
	
	public VersionMap(){
		versionMap = new HashMap<String,Integer>();
	}
	
	/**
	 * 添加设备及其版本号，若设备已经存在，则更新其版本号
	 * @param deviceId	设备号
	 * @param versionNumber	版本号
	 * @return	同之前该设备相对应的版本号，如果之前不存在，则为null
	 */
	public Integer put(String deviceId,Integer versionNumber){
		return versionMap.put(deviceId, versionNumber);
	}
	
	/**
	 * 删除设备及其版本号
	 * @param deviceId
	 */
	public void remove(String deviceId){
		versionMap.remove(deviceId);
	}
	
	/**
	 * 获取设备的版本号
	 * @param deviceId	设备号
	 * @return 版本号
	 */
	public Integer getVersionNumber(String deviceId){
		return versionMap.get(deviceId);
	}
	
	
}
