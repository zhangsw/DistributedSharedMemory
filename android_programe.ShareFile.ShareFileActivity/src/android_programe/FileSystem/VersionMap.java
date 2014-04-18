package android_programe.FileSystem;

import java.io.Serializable;
import java.util.HashMap;

/**
 * �����˸����豸�Լ�ͬ�����Ӧ�İ汾��
 * @author zhangsw
 *
 */
public class VersionMap implements Serializable{
	
	//���ڱ����豸idͬ��汾��
	private HashMap<String,Integer> versionMap;
	
	public VersionMap(){
		versionMap = new HashMap<String,Integer>();
	}
	
	/**
	 * ����豸����汾�ţ����豸�Ѿ����ڣ��������汾��
	 * @param deviceId	�豸��
	 * @param versionNumber	�汾��
	 * @return	֮ͬǰ���豸���Ӧ�İ汾�ţ����֮ǰ�����ڣ���Ϊnull
	 */
	public Integer put(String deviceId,Integer versionNumber){
		return versionMap.put(deviceId, versionNumber);
	}
	
	/**
	 * ɾ���豸����汾��
	 * @param deviceId
	 */
	public void remove(String deviceId){
		versionMap.remove(deviceId);
	}
	
	/**
	 * ��ȡ�豸�İ汾��
	 * @param deviceId	�豸��
	 * @return �汾��
	 */
	public Integer getVersionNumber(String deviceId){
		return versionMap.get(deviceId);
	}
	
	
}
