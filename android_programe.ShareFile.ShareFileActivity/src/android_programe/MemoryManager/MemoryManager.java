package android_programe.MemoryManager;

import java.io.File;
import java.io.IOException;
import java.util.*;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android_programe.Conflict.ConflictManager;
import android_programe.FileSystem.FileManager;
import android_programe.FileSystem.MyFileObserver;
import android_programe.FileSystem.VersionManager;
import android_programe.FileSystem.VersionMap;
import android_programe.LogLine.LogLine;
import android_programe.Util.FileConstant;
import android_programe.Util.FileOperateHelper;
import android_programe.Util.FileUtil;


/**
 * �����ڴ������
 * @author zhangsw
 *
 */
public class MemoryManager implements IMemoryManager{

	private LogLine logLine; 
	private String defaultRootPath;		//Ĭ��·��������Ź����ļ����ļ���·��
	private String localDeviceId;				//��������
	private List <ShareInfo>shareInfList;	//�����豸��Ϣ��
	private HandlerThread fileTranManager;
	private Handler handler;
	private FileManager fileManager;
	private ConflictManager conflictManager;	//��ͻ���������ж��Ƿ�����ͻ���Լ�������ͻ����Ҫ���еĲ���
	
	private static String VERSIONMAP = "one";	//��һ�η���versionMap�ı��
	private static String VERSIONMAPACK = "two";	//�Ѿ����͹�versionMap,���յ����ǻ�Ӧ�������ж϶Է��Ƿ��Ѿ�֪�����Լ���versionMap
	
	public MemoryManager(String id) throws IOException{
		
		shareInfList = new ArrayList<ShareInfo>();
		logLine = new LogLine(this);
		//�½�һ����looper���߳�
		fileTranManager = new HandlerThread("fileTranManagerHandleThread");
		fileTranManager.start();
		//��looperͬhandler�󶨣���handler�ڽ�����Ϣ����ʱ������looper���߳��н���
		handler = new FileTranHandler(fileTranManager.getLooper());
		
		defaultRootPath = getRootPath();
		//localDeviceId = getLocalDeviceId();
		localDeviceId = id;
		fileManager = new FileManager(defaultRootPath,localDeviceId);
		conflictManager = new ConflictManager(this,fileManager);
	}
	
	/**
	 * ��ȡ�����豸��id
	 * @return
	 */
	private String getLocalDeviceId(){
		return "Device A";
	}
	
	/**
	 * ��ȡĬ�ϵĸ�Ŀ¼
	 * @return
	 */
	private String getRootPath(){
		String rootPath = FileConstant.DEFAULTROOTPATH;
		return rootPath;
	}
	
	private void deleteLocalFile(String filepath){
		System.out.println("enter consistency deletefile, path is " + filepath);
		File file = new File(filepath);
		if(file.exists()){
			if(file.isDirectory()){
				File []child = file.listFiles();
				for(int i=0;i<child.length;i++){
					deleteLocalFile(child[i].getAbsolutePath());
				}
				fileManager.remoteDeleteFile(file.getAbsolutePath());
			}
			else
				fileManager.remoteDeleteFile(file.getAbsolutePath());
		}
	}
	
	private void sendFileMetaData(String target,FileMetaData fileMetaData){
		
	}
	
	private void sendFileVersionMap(String target,String relativePath,String tag){
		MyFileObserver ob = fileManager.getMyFileObserver(defaultRootPath + relativePath);
		if(ob != null){
			System.out.println("before logline sendFileVersionMap");
			logLine.sendFileVersionMap(target, ob.getFileMetaData().getFileID(), ob.getVersionMap(), relativePath,tag);
		}
	}
	
	private void sendFileVersionMap(String target,MyFileObserver ob,String tag){
		if(ob != null){
			logLine.sendFileVersionMap(target, ob.getFileMetaData().getFileID(), ob.getVersionMap(), ob.getFileMetaData().getRelativePath(), tag);
		}
	}
	
	/**
	 * �����ļ���versionMap
	 * @param fileID	��Ҫ���͵��ļ�id
	 * @param versionMap	�汾map
	 * @param target	Ŀ�ĵ�
	 */
	private void sendFileVersionMap(String target,String fileID,VersionMap versionMap,String relativePath,String tag){
		logLine.sendFileVersionMap(target,fileID,versionMap,relativePath,tag);
	}
	
	/**
	 * ���յ�����target��versionMap
	 * @param target	��Ϣ��Դ
	 * @param fileID	�ļ�id
	 * @param RemoteVersionMap �汾map
	 * 
	 */
	public boolean receiveVersionMap(String target,String fileID,VersionMap remoteVersionMap,String relativePath,String tag){
		System.out.println("receive versionMap,relativePath is " + relativePath);
		String path = defaultRootPath + relativePath;
		VersionMap localVersionMap = fileManager.getVersionMap(path);
		
		if(localVersionMap != null){	//���ڸ��ļ����ļ����
			//���յ���versinMap���г�ͻ��⣬����Ҫ���±��صİ汾�ţ�����и���
			int detectResult = conflictManager.detect(localVersionMap, localDeviceId, remoteVersionMap, target);
			//if������ͻ
			if(detectResult == ConflictManager.CONFLICT){
				//��ͻ����
				if(tag.equals(VERSIONMAP)){	//�Է�����֪���Լ���versionMap
					sendFileVersionMap(target,fileID,localVersionMap,relativePath,VERSIONMAPACK);
				}
				conflictManager.resolute(fileID,localVersionMap, localDeviceId, remoteVersionMap,target,relativePath);
			}
			// Զ���и���
			else if(detectResult == ConflictManager.LOCALNEEDUPDATE){
				//�޸�Զ�˷�������map���Լ��İ汾��
				remoteVersionMap.put(localDeviceId, localVersionMap.getVersionNumber(localDeviceId));
				//���޸ĵ�versionMapת���������豸
				List<String>targets = fileManager.getMyFileObserver(path).getTargetsList();
				//ת��versionMap
				//forwardVersionMap(targets,target,fileID,remoteVersionMap,relativePath);
				//��target������ļ�
				fetchFile(target,relativePath);
			}
			else if(detectResult == ConflictManager.REMOTENEEDUPDATE){
				//���Զ�˻�δ��ñ��ظ��£���Զ�˷���֪ͨ
				sendFileVersionMap(target,fileID,localVersionMap,relativePath,VERSIONMAP);
			}
		}
		else{	//���ز����ڸ��ļ����ļ����
			//TODO
			//�����µ��ļ����
			boolean success = fileManager.createEmptyFileNode(path, fileID);
			if(success){
				//��������ļ�����versionMap������versionΪ-1�������ڸ��ļ�����Զ��version��Ϊ���͹�����
				//���±��ر����Զ�˵İ汾��
				fileManager.updateVersionMap(path, target, remoteVersionMap.getVersionNumber(target));
				//�޸�Զ�˷�������map���Լ��İ汾��
				remoteVersionMap.put(localDeviceId, VersionManager.FILENOTEXIST);
				//���޸ĵ�versionMapת���������豸
				List<String>targets = fileManager.getMyFileObserver(path).getTargetsList();
				//ת��versionMap
				//forwardVersionMap(targets,target,fileID,remoteVersionMap,relativePath);
				//��target������ļ�
				fetchFile(target,relativePath);
			}	
		}
		
		//TODO
		return true;
	}
	
	/**
	 * ת���յ���versionMap
	 * @param targets	��Ҫת����Ŀ��
	 * @param sourceID	����Ҫת����Ŀ��
	 * @param fileID	�ļ���id
	 * @param versionMap	��Ҫת����versionMap
	 * @param relativePath	�ļ������·��
	 */
	public void forwardVersionMap(List<String> targets,String sourceID,String fileID,VersionMap versionMap,String relativePath){
		
	}
	
	/**
	 * ���յ��ļ�����
	 * @param target	��Ϣ��Դ
	 * @param fileMetaData	�ļ���metaData
	 * @param file	���յ����ļ�
	 */
	public void receiveFileData(String target,FileMetaData fileMetaData,File file){
		//�����յ����ļ��Ǳ�����Ĭ�ϵ�cache�еģ�������Ҫ�����ƶ�������Ӧ���ڵ�λ�ã���relativePath�¡�
		//
		System.out.println("recevice metaData,relativePath is " + fileMetaData.getRelativePath());
		String relativePath = fileMetaData.getRelativePath();
		file.setLastModified(fileMetaData.getModifiedTime());
		if(fileManager.fileObserverExist(defaultRootPath + relativePath)){
			//�Ѿ�����observer
			
			MyFileObserver ob = fileManager.getMyFileObserver(defaultRootPath + relativePath);
			if(fileMetaData.equals(ob.getFileMetaData())){	//���ر�����ļ�ͬ�յ����ļ�����ͬ�ģ������������ݣ�����Ҫ
				file.delete();
				System.out.println("File redundancy");
			}
			else{	
				//�ȸ���metaData�Լ�versinMap
				System.out.println("receive file that is wanted,file version is " + fileMetaData.getVersionID());
				ob.setFileMetaData(fileMetaData);
				ob.updateVersionNumber(localDeviceId, fileMetaData.getVersionID());
				if(FileOperateHelper.fileExist(defaultRootPath + relativePath)){
					//�оɰ汾�ļ����ڣ�ɾ����
					//TODO
					fileManager.deleteOldFile(ob);
				}
				//�����ļ��Ƶ�ָ��Ŀ¼��
				FileOperateHelper.renameFile(file.getAbsolutePath(), defaultRootPath + fileMetaData.getRelativePath());
				System.out.println("file has been moved from cache");
				//�����յ����ļ�
				ob.startWatching();
				sendFileUpdateInform(ob.getTargetsList(),fileMetaData);
			}
			
		}
		else{
			//�����ǳ�ͻ�ļ�
			String fileName = file.getName();
			long time = fileMetaData.getModifiedTime();
			String sTime = "("+FileUtil.getTimeFromLong(time)+")";
			if(fileName.indexOf(sTime) == 0){
				//�ǰ��ճ�ͻ�����������������ļ�
				conflictManager.receiveConflictFileData(target, fileMetaData, file,fileName.substring(sTime.length()));
				//���ͱ����յ��ļ�֪ͨ
				sendFileUpdateInform(fileManager.getMyFileObserver(defaultRootPath + relativePath).getTargetsList(),fileMetaData);
				System.out.println("has sended file update inform");
			}
			else{	
				//TODO
				/*
				boolean success = fileManager.createEmptyFileNode(defaultRootPath + fileMetaData.getRelativePath(), fileMetaData.getFileID());
				if(success){
					MyFileObserver ob = fileManager.getMyFileObserver(defaultRootPath + relativePath);
					ob.setFileMetaData(fileMetaData);
					ob.updateVersionNumber(target, fileMetaData.getVersionID());
					ob.updateVersionNumber(localDeviceId, fileMetaData.getVersionID());
					if(FileOperateHelper.fileExist(defaultRootPath + relativePath)){
						//�оɰ汾�ļ����ڣ�ɾ����
						//TODO
						fileManager.deleteOldFile(ob);
					}
					//�����ļ��Ƶ�ָ��Ŀ¼��
					FileOperateHelper.renameFile(file.getAbsolutePath(), defaultRootPath + fileMetaData.getRelativePath());
					System.out.println("file has been moved from cache");
					//�����յ����ļ�
					ob.startWatching();
					sendFileUpdateInform(ob.getTargetsList(),fileMetaData);
				}*/
			}
		}
	}
	
	/**
	 * �յ����Ա���豸���ļ�������Ϣ
	 * @param target
	 * @param fileMetaData
	 */
	public void receiveFileUpdate(String target, FileMetaData fileMetaData) {
		// TODO Auto-generated method stub
		System.out.println("receive file update inform,relativePath is " + fileMetaData.getRelativePath()+",version number is " + fileMetaData.getVersionID());
		String absolutePath = defaultRootPath + fileMetaData.getRelativePath();
		if(fileManager.fileObserverExist(absolutePath)){	//���ش��ڸ��ļ��Ľ��
			System.out.println("local has file node");
			FileMetaData localMetaData = fileManager.getMyFileObserver(absolutePath).getFileMetaData();
			System.out.println("local: " + localMetaData.getFileSize() +", "+ localMetaData.getFileCreator() +"," + localMetaData.getFileID() + "," +localMetaData.getRelativePath() +", "+ localMetaData.getVersionID() +", "+localMetaData.getModifiedTime());
			System.out.println("remote: "+ fileMetaData.getFileSize() + ", "+ fileMetaData.getFileCreator() + "," + fileMetaData.getFileID() + ","+fileMetaData.getRelativePath() + ", "+ fileMetaData.getVersionID() + ", " +fileMetaData.getModifiedTime());
			if(localMetaData.equals(fileMetaData)){
				System.out.println("metaData is the same");
				fileManager.getMyFileObserver(absolutePath).updateVersionNumber(target, fileMetaData.getVersionID());
			}
			else{
				//TODO
			}
			
		}
		
	}
	
	public boolean receiveDeleteFile(String target, String filepath){					//ɾ���ļ���Ҫ�ж����ļ������ļ��У�ͬʱ��Ҫ�����Ƿ��������߳���ʹ�ø��ļ����µ��ļ�
		//TODO
		
		deleteLocalFile(filepath);
		return true;
	}
	
	public boolean receiveFileInf(String target,String relativePath,String absolutePath,String MD5){
		int index = getIndexByName(target);
		System.out.println("enter consistency receiveFileInf---------");
		if(index != -1){
			System.out.println("exist this device--------");
			String MD5OfLocalFile = FileUtil.getFileMD5(new File(absolutePath));
			System.out.println("receivefile md5 is " + MD5 + " , local is " + MD5OfLocalFile);
			if(MD5.equals(MD5OfLocalFile)){			//���ڸ��ļ�
				System.out.println("file has been existed-----");
				return false;
			}
			else{											//�������ļ������ո��ļ�
				//TODO
				System.out.println("want to receive file---"+relativePath);
				fetchFile(target,relativePath);
				return true;
			}
		}
		return false;
	}
	
	public boolean receiveAskFile(String target, String relativePath, String absolutePath) {
		// TODO Auto-generated method stub
		int index = getIndexByName(target);
		System.out.println("enter receiveAskFile,absolutePath is " + absolutePath);
		if(index != -1){
			File file = new File(absolutePath);
			if(file.exists()){				//����������ļ������Խ��з���
				//TODO
				System.out.println("file exist��before send");
				sendFile(target,absolutePath,relativePath);			//�ļ�·��������
				return true;
			}
			else{										//������������ļ�
				//�ж�������Ƿ��ǳ�ͻ�ļ�
				if(conflictManager.conflictFileNodeExist(absolutePath)){	//������ǳ�ͻ�ļ�
					//���ͳ�ͻ�ļ�
					MyFileObserver ob = conflictManager.getConflictFileNode(absolutePath).getLocalFileObserver();
					sendFile(target,ob.getPath(),ob.getFileMetaData().getRelativePath());
					return true;
				}
				return false;
			}
		}
		return false;
	}
	
	public boolean receiveRenameFile(String target, String oldPath, String newPath) {
		// TODO Auto-generated method stub
		return fileManager.remoteMoveFile(oldPath, newPath);
	}
	
	public boolean receiveMakeDir(String target, String absolutePath){
		System.out.println("enter consistency receiveMakeDir----path is "+ absolutePath);
		File file = new File(absolutePath);
		if(!file.exists()) return file.mkdir();
		else return false;
	}
	
	
	 
	private void sendFileUpdateInform(List<String> targets,FileMetaData fileMetaData){
		System.out.println("enter memory manager sendFileUpdateInform");
		logLine.sendFileUpdateInform(targets,fileMetaData);
	}
	
	/**
	 * �����ļ����ݣ�������meta data�Լ�concrete data
	 * @param target
	 * @param absolutePath
	 * @param relativeFilePath
	 */
	private void sendFile(String target,String absolutePath,String relativeFilePath){
		logLine.sendFile(target, fileManager.getFileMetaData(absolutePath),absolutePath);
		
	}
	
	private void deleteFile(String target,String relativeFilePath){
		logLine.deleteFile(target, relativeFilePath);
	}
	
	private void renameFile(String target,String relativeFilePath,String newRelativeFilePath){
		logLine.renameFile(target,relativeFilePath,newRelativeFilePath);
	}
	
	private void moveFile(String target,String relativeFilePath,String newRelativeFilePath){
		renameFile(target,relativeFilePath,newRelativeFilePath);
	}
	
	private void renameDir(String target,String relativeFilePath,String newRelativeFilePath){
		renameFile(target,relativeFilePath,newRelativeFilePath);
	}
	
	private void moveDir(String target,String relativeFilePath,String newRelativeFilePath){
		renameFile(target,relativeFilePath,newRelativeFilePath);
	}
	
	private void sendDir(String target,String absolutePath,String relativePath){
		File file = new File(absolutePath);
		if(file.exists() && file.isDirectory()){
			makeDir(target,relativePath);
			System.out.println("senddir------relativePath is "+relativePath);
			File []child = file.listFiles();
			for(int i=0;i<child.length;i++){
				String ab = child[i].getAbsolutePath();
				String re = relativePath + ab.substring(absolutePath.length());
				if(child[i].isDirectory()) sendDir(target,ab,re);
				else{
					System.out.println("send dir --- send list files: " + child[i].getAbsolutePath());
					sendFile(target,ab,re);
				}
			}
		}
	}
	
	private void makeDir(String target,String relativePath){
		logLine.makeDir(target, relativePath);
	}
	
	
	public void fetchFile(String target,String relativePath){
		logLine.fetchFile(target,relativePath);
	}
	
	/**
	 * ����һ���ļ����
	 * @param fileID	�ļ���id
	 * @param relaitvePath	�ļ������·��
	 * @param nodePath �ļ�����·��
	 */
	public void constructFileNode(String fileID,String relaitvePath,String nodePath){
		//fileManager.registerObserver(fileID, relaitvePath, nodePath);
	}
	
	
	public synchronized void addShareDevice(String sharedFilePath,String target,int type){
		System.out.println("SharedFilePath is"+sharedFilePath+",target is "+target);
		ShareInfo si = new ShareInfo(sharedFilePath,target,type,handler,fileManager);
		System.out.println("enter Consistency addShareDevice-----");
		//TODO
		if(!shareInfList.contains(si)){						//������˼򵥵��жϣ�����
			shareInfList.add(si);
			si.start();
		}
	}
	
	public synchronized void removeShareDevice(String target){
		int index = getIndexByName(target);
		if(index != -1){
			saveShareInformation();
			ShareInfo sio =  shareInfList.get(index);
			fileManager.withdrowObserver(target, sio.getSharedFilePath());
			shareInfList.remove(index);
		}
	}
	
	/**
	 * ���粻���ã�ֹͣ�ļ��仯��Ϣ�ķ���
	 */
	public void networkDisabled(){
		fileManager.setDispenseMsgTag(false);
		//TODO
	}
	
	/**
	 * ����Ŀǰ���������ӣ�ͬ�����豸���½�������
	 * @param localIP 
	 */
	public void reconnectAll(String localIP){
		logLine.reconnectAll(localIP);
	}
	
	public boolean connect(String ip) throws IOException{
		if(logLine.connect(ip)){
			//TODO
			String targetName = logLine.getDeviceNameByID(ip);
			System.out.println("has connected: " + ip + ",before synchronzeFiles,targetname is :" + targetName);
			if(targetName != null){
				synchronizeFiles(targetName);
			}
			return true;
		}
		else return false;
			
	}
	
	/**
	 * ͬ���ļ�
	 * @param target
	 */
	private void synchronizeFiles(String target){
		int index = getIndexByName(target);
		if(index != -1){
			ShareInfo s = shareInfList.get(index);
			String path = s.getSharedFilePath();
			System.out.println("----MemoryManager---share path is :" + path);
			MyFileObserver m = fileManager.getMyFileObserver(path);
			if(path.equals(defaultRootPath)){
				//�����Ĭ��·������Ҫ�ų���cache�ļ���
				if(m.hasChild()){
					ArrayList<MyFileObserver> list = m.getChildAll();
					for(int i=0;i<list.size();i++){
						System.out.println(list.get(i).getPath());
						if(!list.get(i).getPath().equals(FileConstant.DEFAULTSAVEPATH))
							synchronizeFiles(target,list.get(i));
					}
				}
			}
			else if(m.hasChild()){
				List<MyFileObserver> list = m.getChildAll();
				for(int i=0;i<list.size();i++){
					synchronizeFiles(target,list.get(i));
				}
			}
		}
	}
	
	/**
	 * 
	 */
	private void synchronizeFiles(String target,MyFileObserver m){
		if(m != null){
			String path = m.getPath();
			if(FileOperateHelper.isDirectory(path)){
				//�ļ���
				makeDir(target,m.getFileMetaData().getRelativePath());
				if(m.hasChild()){
					ArrayList<MyFileObserver> list = m.getChildAll();
					for(int i=0;i<list.size();i++){
						synchronizeFiles(target,list.get(i));
					}
				}
			}else{
				//�ļ�
				sendFileVersionMap(target,m,VERSIONMAP);
			}
		}
	}
	
	/**
	 * ͬ�豸����
	 * @param target ��Ҫ�������豸����
	 */
	public boolean disconnect(String target){
		int index = getIndexByName(target);
		if(index != -1){
			//���ڸ��豸�����ж���
			System.out.println("before enter logline's disconnect");
			if(logLine.disconnect(target)){
				//TODO
				System.out.println("logline has disconnected");
				saveShareInformation();
				ShareInfo s = (ShareInfo)(shareInfList.get(index));
				fileManager.withdrowObserver(target, s.getSharedFilePath());
				shareInfList.remove(index);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * ���յ����������豸�Ķ�����Ϣ
	 * @param targetName
	 */
	
	public void receiveDisconnect(String targetName) {
		// TODO Auto-generated method stub
		int index = getIndexByName(targetName);
		if(index != -1){
			saveShareInformation();
			ShareInfo s = (ShareInfo)(shareInfList.get(index));
			fileManager.withdrowObserver(targetName, s.getSharedFilePath());
			shareInfList.remove(index);
		}
	}
	
	private void saveShareInformation(){
		System.out.println("sava share information");
	}
	
	private int getIndexByName(String target){
		int i = 0;
		for(;i<shareInfList.size();i++){
			System.out.println("----" + shareInfList.get(i).getTarget());
			if(((ShareInfo)(shareInfList.get(i))).getTarget().equals(target))
				return i;
		}
		return -1;
	}
	
	
	
	private class FileTranHandler extends Handler{
		
		public FileTranHandler(Looper looper){
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			MessageObj mo = (MessageObj)msg.obj;
			switch(mo.getType()){
			case FileConstant.SENDFILEMESSAGE:{											//�����ļ�����,��ʵ�Ƿ���versionMap
				
				System.out.println("-----sendfilemessage------relativeFilePath is " + mo.getRelativeFilepath() + ",target is " + mo.getTarget());
				sendFileVersionMap(mo.getTarget(),mo.getRelativeFilepath(),VERSIONMAP);
				
				//sendFile(mo.getTarget(),mo.getFilepath(),mo.getRelativeFilepath());
				
			}break;
			
			case FileConstant.DELETEFILEMESSAGE:{									//ɾ���ļ�
				System.out.println("-----deletefilemessage----");
				deleteFile(mo.getTarget(),mo.getRelativeFilepath());
				
			}break;
			
			case FileConstant.RENAMEFILEMESSAGE:{								//�������ļ�
				System.out.println("-----renamefilemessage----");
				renameFile(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
			}break;
			
			case FileConstant.MOVEFILEMESSAGE:{									//�ƶ��ļ�,ͬ�������ļ�
				System.out.println("----movefilemessage-------");
				moveFile(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
			}break;
			
			case FileConstant.CREATEDIRMESSAGE:{						//�����ļ���
				System.out.println("----createDirMessage------");
				makeDir(mo.getTarget(),mo.getRelativeFilepath());
				//sendDir(mo.getTarget(),mo.getFilepath(),mo.getRelativeFilepath());
			}break;
			
			case FileConstant.DELETEDIRMESSAGE:{						//ɾ���ļ���
				System.out.println("---deleteDirMessage-------");
				deleteFile(mo.getTarget(),mo.getRelativeFilepath());
			}break;
			
			case FileConstant.MOVEDIRMESSAGE:{							//�ƶ��ļ���
				System.out.println("---moveDirMessage---------");
				moveDir(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
			}break;
			
			case FileConstant.RENAMEDIRMESSAGE:{						//�������ļ���
				System.out.println("---renameDirMessage-------");
				renameDir(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
			}break;
			case FileConstant.SENDDIRMESSAGE:{					//�����ļ���
				System.out.println("dir moved to,send dir message");
				sendDir(mo.getTarget(),mo.getFilepath(),mo.getRelativeFilepath());
			}break;
			}
		}
	}



	public void renameLocalFile(String fileID, String oldRelativePath,
			String newRelativePath) {
		// TODO Auto-generated method stub
		fileManager.renameLocalFile(fileID, oldRelativePath, newRelativePath);
		
	}

	public void createEmptyFileNode(String path, String fileID) {
		// TODO Auto-generated method stub
		fileManager.createEmptyFileNode(path, fileID);
	}

	

		
}
