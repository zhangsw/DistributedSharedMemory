package android_programe.FileSystem;

import java.util.HashMap;
import java.util.UUID;

import android_programe.MemoryManager.FileMetaData;
import android_programe.Util.FileConstant;
import android_programe.Util.FileOperateHelper;

/**
 * �����ļ��İ汾��Ϣ
 * @author zhangsw
 *
 */
public class VersionManager {

	//�汾��ʷ
	private VersionHistory versionHistory;
	
	//�߼�ʱ�ӣ����ڼ�¼�Լ��Լ������豸���ļ��汾��
	private VersionMap versionMap;
	
	private FileMetaData fileMetaData;

	//Ĭ�ϵĳ�ʼ�汾�ţ�Ϊ0
	public static int INITIAL_VERSION_NUMBER = 0;
	
	//����ļ������ڣ���汾��Ϊ-1
	public static int FILENOTEXIST = -1;
	
	//Ĭ�ϵ�ÿ�θ������ӵ���ֵ��Ϊ1
	private static int DEFAULT_ADDITION = 1;
	
	
	public VersionManager(){
		
	}
	//��ʼ��versionMap,������Ҫ���汾���ļ��汾�ţ���ˣ�map��������һ���豸
	public VersionManager(String deviceName,String path){
		if(FileOperateHelper.fileExist(path)){
			System.out.println("file exist,initialize versionMap and fileMetaData");
			versionMap = new VersionMap();
			versionMap.put(deviceName, INITIAL_VERSION_NUMBER);
			//Ϊ�ļ�����ȫ��Ψһ��id
			String fileID = UUID.randomUUID().toString();
			long fileLength = FileOperateHelper.getFileLength(path);
			String relaitvePath = path.substring(FileConstant.DEFAULTROOTPATH.length());
			fileMetaData = new FileMetaData(fileID,INITIAL_VERSION_NUMBER, relaitvePath, path, fileLength, deviceName,FileOperateHelper.getFileModifiedTime(path));
		}
		else{
			versionMap = new VersionMap();
			versionMap.put(deviceName, FILENOTEXIST);
			fileMetaData = new FileMetaData();
			fileMetaData.setVersionID(FILENOTEXIST);
		}
	}
	
	public VersionManager(String deviceName,int versionNumber){
		versionMap = new VersionMap();
		versionMap.put(deviceName, versionNumber);
	}
	
	
	
	//����map�е�һ���豸����汾��
	public void updateVersionNumber(String deviceName,int number){
		versionMap.put(deviceName, number);
		System.out.println("local update version number,device: " + deviceName +" ,number is:" + number);
		fileMetaData.setVersionID(number);
	}
	
	//�����ļ��汾��������map�����ĸ���
	public void updateVersionNumber(String deviceName){
		//����map
		addVersionNumber(deviceName,DEFAULT_ADDITION);
		//������
	}
	
	//��map�����һ�����豸���汾��Ϊ�ļ�������
	public void addDevice(String deviceName){
		versionMap.put(deviceName, FILENOTEXIST);	
	}
	
	//��map�����һ�����豸
	public void addDevice(String deviceName,int number){
		versionMap.put(deviceName, number);
	}
	
	//��map��ɾ��һ���豸����汾��
	public void deleteDevice(String deviceName){
		versionMap.remove(deviceName);
	}
	
	
	
	//��ָ�������versionNumber����number
	public void addVersionNumber(String deviceName,int number){
		Integer versionNumber = versionMap.getVersionNumber(deviceName);
		assert versionNumber != null :"Device not existing,can't add its versionNumber";
		assert versionNumber >= 0 :"Device's versionNumber is illegal,it should be properly initialized";
		versionNumber += number;
		versionMap.put(deviceName, versionNumber);
	}
	
	//��ʼ��map�е�һ���豸�İ汾��
	public void initialVersionNumber(String deviceName){
		versionMap.put(deviceName, INITIAL_VERSION_NUMBER);
	}
	
	//��ȡָ���豸��versionNumber
	public Integer getVersionNumber(String deviceName){
		return versionMap.getVersionNumber(deviceName);
	}
	
	/**
	 * �����ļ���versionMap
	 * @param versionMap
	 */
	public void setVersionMap(VersionMap versionMap){
		this.versionMap = versionMap;
	}
	
	/**
	 * ��ȡ�ļ���versionMap
	 * @return
	 */
	public VersionMap getVersionMap(){
		return versionMap;
	}
	
	/**
	 * �����ļ���metaData
	 * @param metaData
	 */
	public void setFileMetaData(FileMetaData metaData){
		fileMetaData = metaData;
	}
	
	/**
	 * ��ȡ�ļ���metaData
	 * @return
	 */
	public FileMetaData getFileMetaData(){
		return fileMetaData;
	}
}
