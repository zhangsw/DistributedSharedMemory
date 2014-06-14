package android_programe.FileSystem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * �����˸����豸�Լ�ͬ�����Ӧ�İ汾��
 * @author zhangsw
 *
 */
public class VersionMap implements Serializable{
	//���ڱ����豸idͬ��汾��
	public static final int EQUAL = 0;
	public static final int GREATER = 1;
	public static final int LESSER = 2;
	public static final int UNDEFINED = 4;
	
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
	public synchronized Integer put(String deviceId,Integer versionNumber){
		return versionMap.put(deviceId, versionNumber);
	}
	
	/**
	 * ɾ���豸����汾��
	 * @param deviceId
	 */
	public synchronized void remove(String deviceId){
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
	
	/**
	 * �ϲ�����versionMap���ϲ���������vector clock
	 * @param m
	 */
	public synchronized void merge(VersionMap m){
		Iterator<Entry<String,Integer>> iter = m.versionMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String,Integer> entry = iter.next();
			Integer i = versionMap.get(entry.getKey());
			if( i==null ||i<entry.getValue())
				versionMap.put(entry.getKey(), entry.getValue());	
		}
	}
	
	/**
	 * �Ƚ�versionMap�Ĵ�С
	 * @param m ��Ҫ�ȽϵĶ���
	 * @return �ȽϽ����0��ʾ��ȣ�1��ʾ���ڣ�2��ʾС�ڣ�4��ʾ��ȷ��
	 */
	public int compareTo(VersionMap m){
		int result = -1; 
		Iterator<Entry<String,Integer>> iter = versionMap.entrySet().iterator();
		while(iter.hasNext()){
			Entry<String,Integer> entry = iter.next();
			Integer remote = m.versionMap.get(entry.getKey());
			if(remote != null){
				if(entry.getValue() > remote){
					if(result == -1 || result == EQUAL)
						result = GREATER;
					else if(result == LESSER)
						return UNDEFINED;
				}
				else if(entry.getValue() < remote){
					if(result == -1 || result == EQUAL)
						result = LESSER;
					else if(result == GREATER)
						return UNDEFINED;
				}
				else{
					if(result == -1)
						result = EQUAL;
				}
			}
		}
		return result;
	}
	
	
}
