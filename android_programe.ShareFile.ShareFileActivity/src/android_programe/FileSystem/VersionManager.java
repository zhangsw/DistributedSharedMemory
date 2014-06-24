package android_programe.FileSystem;

import java.util.HashMap;
import java.util.UUID;

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
	private VectorClock vectorClock;
	
	private FileMetaData fileMetaData;

	//Ĭ�ϵĳ�ʼ�汾�ţ�Ϊ0
	public static int INITIAL_VERSION_NUMBER = 0;
	
	//����ļ������ڣ���汾��Ϊ-1
	public static int FILENOTEXIST = -1;
	
	//Ĭ�ϵ�ÿ�θ������ӵ���ֵ��Ϊ1
	private static int DEFAULT_ADDITION = 1;
	
	
	public VersionManager(){
		
	}
	//��ʼ��vectorClock,������Ҫ���汾���ļ��汾�ţ���ˣ�vector��������һ���豸
	public VersionManager(String deviceName,String path){
		if(FileOperateHelper.fileExist(path)){
			vectorClock = new VectorClock();
			System.out.println("----VersionManager----File:" + path + " exists");
			vectorClock.put(deviceName, INITIAL_VERSION_NUMBER);
			//Ϊ�ļ�����ȫ��Ψһ��id
			String fileID = UUID.randomUUID().toString();
			long fileLength = FileOperateHelper.getFileLength(path);
			String relaitvePath = path.substring(FileConstant.DEFAULTSHAREPATH.length());
			fileMetaData = new FileMetaData(fileID,INITIAL_VERSION_NUMBER, relaitvePath, path, fileLength, deviceName,FileOperateHelper.getFileModifiedTime(path));
		}
		else{
			vectorClock = new VectorClock();
			vectorClock.put(deviceName, FILENOTEXIST);
			fileMetaData = new FileMetaData();
			fileMetaData.setVersionID(FILENOTEXIST);
		}
	}
	
	/*
	public VersionManager(String deviceName,int versionNumber){
		vectorClock = new vectorClock();
		vectorClock.put(deviceName, versionNumber);
	}
	
	*/
	
	//����map�е�һ���豸����汾��
	public void updateVersionNumber(String deviceName,int number){
		vectorClock.put(deviceName, number);
		System.out.println("local update version number,device: " + deviceName +" ,number is:" + number);
		fileMetaData.setVersionID(number);
	}
	
	//�����ļ��汾��������map��metaData
	public void updateVersionNumber(String deviceName){
		//����map
		addVersionNumber(deviceName,DEFAULT_ADDITION);
		//����metaData
		int old = fileMetaData.getVersionID();
		fileMetaData.setVersionID(++old);
		System.out.println("----VersionManager----updateVersionNumber----version number is:" + old);
	}
	
	public int getFileVersion(){
		return fileMetaData.getVersionID();
	}
	
	public void updateVectorClock(String deviceName,int number){
		vectorClock.put(deviceName, number);
	}
	
	public void updateVectorClock(VectorClock vectorClock){
		this.vectorClock = vectorClock;
	}
	
	//��map�����һ�����豸���汾��Ϊ�ļ�������
	public void addDevice(String deviceName){
		vectorClock.put(deviceName, FILENOTEXIST);	
	}
	
	//��map�����һ�����豸
	public void addDevice(String deviceName,int number){
		vectorClock.put(deviceName, number);
	}
	
	//��map��ɾ��һ���豸����汾��
	public void deleteDevice(String deviceName){
		vectorClock.remove(deviceName);
	}
	
	
	
	//��ָ�������versionNumber����number
	public void addVersionNumber(String deviceName,int number){
		Integer versionNumber = vectorClock.getVersionNumber(deviceName);
		assert versionNumber != null :"Device not existing,can't add its versionNumber";
		assert versionNumber >= 0 :"Device's versionNumber is illegal,it should be properly initialized";
		versionNumber += number;
		vectorClock.put(deviceName, versionNumber);
	}
	
	//��ʼ��map�е�һ���豸�İ汾��
	public void initialVersionNumber(String deviceName){
		vectorClock.put(deviceName, INITIAL_VERSION_NUMBER);
	}
	
	//��ȡָ���豸��versionNumber
	public Integer getVersionNumber(String deviceName){
		return vectorClock.getVersionNumber(deviceName);
	}
	
	/**
	 * merge����vectorClock
	 * @param vectorClock
	 */
	public void mergeVectorClock(VectorClock vectorClock){
		this.vectorClock.merge(vectorClock);
	}
	
	/**
	 * �����ļ���vectorClock
	 * @param vectorClock
	 */
	public void setVectorClock(VectorClock vectorClock){
		this.vectorClock = vectorClock;
	}
	
	/**
	 * ��ȡ�ļ���vectorClock
	 * @return
	 */
	public VectorClock getVectorClock(){
		return vectorClock;
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
