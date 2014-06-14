package android_programe.FileSystem;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android_programe.FileMonitor.SDFileObserver;
import android_programe.Util.FileConstant;
import android_programe.Util.FileOperateHelper;
import android_programe.Util.FileUtil;

/**
 * �ļ��࣬���ڹ���һ���ļ����������ļ��ļ�������ļ��İ汾��������ļ��Ķ����Լ��ļ��ĸ��ڵ㼰�ӽڵ�
 * @author zhangsw
 *
 */
public class MyFileObserver{
	private SDFileObserver observer;
	private String path;
	private HashMap <String,Handler> mTargets;
	private ArrayList<MyFileObserver> lChildObserver;
	private MyFileObserver fatherObserver;
	private IFileManager iFOManager;
	
	private Handler globalMessageHandler;
	
	private VersionManager versionManager; 		//�ļ��汾���Ƶ�Ԫ���������ļ���ʷ���汾�ŵ�
	
	/**whether file is opened*/
	private boolean isFileOpened = false;
	
	/**
	 * 
	 * @param path		�ļ���·��
	 * @param localDeviceId		�����豸��id
	 * @param globalMessageHandler		ȫ����Ϣ����handler
	 * @param i	�ļ�����ӿ�
	 * @param fatherObserver	���ڵ�
	 */
	public MyFileObserver(String path,String localDeviceId,Handler globalMessageHandler,IFileManager i,MyFileObserver fatherObserver){
		this.path = path;
		if(fatherObserver != null && fatherObserver.hasTarget()){
			mTargets = new HashMap<String,Handler>(fatherObserver.getTargetsAll());
		}
		else{ 
			mTargets = new HashMap<String,Handler>();
		}
		lChildObserver = new ArrayList<MyFileObserver>();
		iFOManager = i;
		this.fatherObserver = fatherObserver;
		this.globalMessageHandler = globalMessageHandler;
		//��Ӷ��ļ��ļ��
		if(FileOperateHelper.fileExist(path)){			//�ļ����ڣ��Ž��м��
			observer = new SDFileObserver(path,globalMessageHandler);
			observer.startWatching();
		}
		initializeVersionManager(localDeviceId,path,mTargets);
	}
	
	public MyFileObserver(String path,String target,Handler handler,Handler globalMessageHandler,IFileManager i,MyFileObserver fatherObserver){
		this.path = path;
		mTargets = new HashMap<String,Handler>();
		lChildObserver = new ArrayList<MyFileObserver>();
		mTargets.put(target, handler);
		iFOManager = i;
		this.fatherObserver = fatherObserver;
		this.globalMessageHandler = globalMessageHandler;
		//observer = new SDFileObserver(path,handler);
		initializeVersionManager();
	}
	
	public MyFileObserver(String path,Handler globalMessageHandler,IFileManager i,MyFileObserver fatherObserver){
		this.path = path;
		this.globalMessageHandler = globalMessageHandler;
		this.fatherObserver = fatherObserver;
		lChildObserver = new ArrayList<MyFileObserver>();
		mTargets = new HashMap<String,Handler>(fatherObserver.getTargetsAll());
		iFOManager = i;
		
		initializeVersionManager();
		
	}
	
	
	private void initializeVersionManager(){
		//�ӱ������ݿ�����ļ��в鿴���Ƿ��и��ļ��İ汾��Ϣ	
		if(versionExist()){			//���ڰ汾��Ϣ
			versionManager = getVersion();
		}
		else{			//�����ڰ汾��Ϣ
			
		}
	}
	
	
	//��ʼ���汾���
	private void initializeVersionManager(String localDeviceId,String path,HashMap<String,Handler> targets){
		//�ӱ������ݿ�����ļ��в鿴���Ƿ��и��ļ��İ汾��Ϣ	
		if(versionExist()){			//���ڰ汾��Ϣ
			versionManager = getVersion();
		}
		else{			//�����ڰ汾��Ϣ
			versionManager = new VersionManager(localDeviceId,path);
			if(targets.size() > 0){
				Iterator<Entry<String, Handler>> iter = mTargets.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry<String,Handler> entry =(Map.Entry<String,Handler>)iter.next();
					versionManager.addDevice(entry.getKey());
				}
			}
		}
	}
	
	private boolean versionExist(){
		return false;
	}
	
	//��ȡ���ļ����У��İ汾��Ϣ
	private VersionManager getVersion(){
		//TODO
		return new VersionManager();
	}
	
	//��ȡ���ش洢��id�豸�İ汾��
	public int getVersionNumber(String deviceId){
		return versionManager.getVersionNumber(deviceId);
	}
	
	public void updateVersionNumber(String deviceId,Integer versionNumber){
		versionManager.updateVersionNumber(deviceId,versionNumber);
	}
	
	public void updateVersionMap(String deviceId,Integer versionNumber){
		versionManager.updateVersionMap(deviceId, versionNumber);
	}
	
	public void updateVersionMap(VersionMap versionMap){
		versionManager.updateVersionMap(versionMap);
	}
	
	
	/**
	 * �����ļ���versionMap
	 * @param versionMap
	 */
	public void setVersionMap(VersionMap versionMap){
		versionManager.setVersionMap(versionMap);
	}
	/**
	 * ��ȡ�ļ��İ汾Map
	 * @return
	 */
	public VersionMap getVersionMap(){
		return versionManager.getVersionMap();
	}
	
	public void setModifiedTime(long time){
		versionManager.getFileMetaData().setModifiedTime(time);
	}
	
	public long getModifiedTime(){
		return versionManager.getFileMetaData().getModifiedTime();
	}
	
	
	/**
	 * ��ȡ�ļ���metaData
	 * @return
	 */
	public FileMetaData getFileMetaData(){
		return versionManager.getFileMetaData();
	}
	
	/**
	 * �����ļ���metaData
	 * @param metaData
	 * @return
	 */
	public boolean setFileMetaData(FileMetaData metaData){
		versionManager.setFileMetaData(metaData);
		return true;
	}
	
	//�ļ����޸ģ���Ҫ����version
	public void fileModified(String deviceId){
		versionManager.updateVersionNumber(deviceId);
	}
	
	public void startWatching(){
		if(FileOperateHelper.fileExist(path)){
			if(observer == null){
				observer = new SDFileObserver(path,globalMessageHandler);
				observer.startWatching();
			}
			else{
				System.out.println("----MyFileObserver----start watching----");
				//observer.stopWatching();
				observer.startWatching();
			}
		}
	}

	
	public void stopWatching(){
		Iterator<MyFileObserver> iter = lChildObserver.iterator();
		while(iter.hasNext()){
			MyFileObserver o = iter.next();
			o.stopWatching();
		}
		if(observer != null){
			observer.stopWatching();
		}
	}
	
	public void modifyPath(String path){
		iFOManager.updateObserverMap(this.path, path);
		this.path = path;
		if(observer != null){//����observer��û��ʼ���������п��ܵģ����տ�ʼ��������emptynodeʱ����Ҫ��ص��ļ���������ʱ��observer���ǿյ�
			observer.updatePath(path);
		}
		//����metaData�е�relativePath
		versionManager.getFileMetaData().setRelativePath(path.substring(FileConstant.DEFAULTSHAREPATH.length()));
		
		Iterator<MyFileObserver> iter = lChildObserver.iterator();
		while(iter.hasNext()){
			//System.out.println("has child");
			MyFileObserver o = iter.next();
			String name = FileUtil.getFileNameFromPath(o.getPath());
			o.modifyPath(path + "/" + name);
			//System.out.println(path + "/" + name);
		}
		
	}
	
	public String getPath(){
		return path;
	}
	
	public boolean hasFather(){
		if(fatherObserver != null) return true;
		else return false;
	}
	
	public void setFather(MyFileObserver observer){
		fatherObserver = observer;
	}
	
	public MyFileObserver getFather(){
		return fatherObserver;
	}
	
	public boolean hasChild(){
		if(lChildObserver.isEmpty()) return false;
		else return true;
	}
	
	public ArrayList<MyFileObserver> getChildAll(){
		return lChildObserver;
	}
	
	public HashMap<String,Handler> getTargetsAll(){
		return mTargets;
	}
	
	/**
	 * ��ù�����ļ������ж���
	 * @return
	 */
	 
	public List<String> getTargetsList(){
		List <String>targets = new ArrayList<String>();
		Iterator<Entry<String, Handler>> iter = mTargets.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,Handler> entry =(Map.Entry<String,Handler>)iter.next();
			targets.add(entry.getKey());
		}
		return targets;
	}
	
	public boolean hasTarget(){
		if(mTargets.isEmpty()) return false;
		else return true;
	}
	
	
	public void addChildObserver(MyFileObserver childObserver){
		for(MyFileObserver fileObserver:lChildObserver)
			if(fileObserver.getPath().equals(childObserver.getPath())) return;
		lChildObserver.add(childObserver);
		//System.out.println("----MyFileObserver----" + path + "add a child observer:" + childObserver.getPath());
	}
	
	public void deleteChildObserver(String path){
		
	}
	
	public void deleteChildObserver(MyFileObserver childObserver){
		lChildObserver.remove(childObserver);
	}
	
	public boolean addTarget(String target,Handler handler){
		if(mTargets.containsKey(target))
			return false;
		else{
			mTargets.put(target, handler);
			//��versionMap������豸
			versionManager.addDevice(target);
			Iterator<MyFileObserver> iter = lChildObserver.iterator();
			while(iter.hasNext()){
				MyFileObserver o = iter.next();
				o.addTarget(target, handler);
			}
			return true;
		}	
	}
	
	public void deleteTarget(String target){
		mTargets.remove(target);
	}

	/*
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(o == this) return true;
		if(o == null) return false;
		else if(path.equals(((MyFileObserver)o).getPath())) return true;
		else return false;
	}
	*/
}
