package android_programe.Conflict;

import java.io.File;
import java.util.HashMap;

import android_programe.FileSystem.FileManager;
import android_programe.FileSystem.FileMetaData;
import android_programe.FileSystem.MyFileObserver;
import android_programe.FileSystem.VectorClock;
import android_programe.MemoryManager.IMemoryManager;

public class ConflictManager implements IResoluteOperator{
	private ConflictDetect conflictDetect;
	private ConflictResolute conflictResolute;
	private IMemoryManager imm;
	private FileManager fileManager;
	//�����ͻ�ļ�
	private HashMap<String,ConflictFileNode> conflictFiles;
	
	/**������ͻ*/
	public static int CONFLICT = 1;
	
	/**Զ�˻�δ��Ϥ���صİ汾����Ҫ�õ�����*/
	public static int REMOTENEEDUPDATE = 2;
	
	/**���ػ�δ��ϤԶ�˵İ汾����Ҫ�õ�����*/
	public static int LOCALNEEDUPDATE = 3;
	
	/**˫�����Ѿ���Ϥ�Է��İ汾��*/
	public static int BOTHKNOW = 4;
	
	public ConflictManager(IMemoryManager imm,FileManager fileManager){
		conflictDetect = new ConflictDetect();
		conflictResolute = new MyConflictResolute(fileManager);
		this.imm = imm;
		this.fileManager = fileManager;
		conflictFiles = new HashMap<String,ConflictFileNode>();
	}
	
	/**
	 * �������versionMap,�Ƿ�����ͻ
	 * @param localVersionMap
	 * @param localDeviceId
	 * @param remoteVersionMap
	 * @param remoteDeviceId
	 * @return
	 */
	public int detect(VectorClock localVersionMap,String localDeviceId,VectorClock remoteVersionMap,String remoteDeviceId){
		return conflictDetect.detect(localVersionMap, localDeviceId, remoteVersionMap, remoteDeviceId);
	}
	
	public int detect(VectorClock localVersionMap, String localDeviceId,
			VectorClock remoteVersionMap, String remoteDeviceId,
			FileMetaData localMetaData, FileMetaData remoteMetaData) {
		// TODO Auto-generated method stub
		return conflictDetect.detect(localVersionMap, localDeviceId, remoteVersionMap, remoteDeviceId, localMetaData, remoteMetaData);
	}
	
	public void resolute(String fileID,VectorClock localVersionMap,String localDeviceId,VectorClock remoteVersionMap,String remoteDeviceId,String relativePath){
		conflictResolute.resoluteConfliction(fileID,localVersionMap, localDeviceId, remoteVersionMap, remoteDeviceId, relativePath,this);
	}
	
	public void resolute(VectorClock localVersionMap, String localDeviceId,
			VectorClock remoteVersionMap, String remoteDeviceId, String relativePath,
			FileMetaData remoteMetaData) {
		// TODO Auto-generated method stub
		conflictResolute.resoluteConfliction(localVersionMap, localDeviceId, remoteVersionMap, remoteDeviceId, relativePath, remoteMetaData,this);
	}
	
	/**
	 * �ж��յ����ļ��Ƿ����Գ�ͻ��������������ĳ�ͻ�ļ�
	 */
	public boolean isConflictFile(String id,FileMetaData fileMetaData,File file){
		return conflictResolute.isConflictFile(id, fileMetaData, file);
	}
	
	/**
	 * �յ���ͻ�ļ�������
	 */
	public void receiveConflictFileData(String target,FileMetaData fileMetaData,File file){
		conflictResolute.receiveConflictFileData(target, fileMetaData, file,this);
	}
	
	public boolean conflictFileNodeExist(String path){
		if(conflictFiles.containsKey(path)) return true;
		else return false;
	}
	
	public void addConflictFile(MyFileObserver ob,String path){
		if(conflictFileNodeExist(path)){
			conflictFiles.get(path).add(ob);
		}
		else{
			ConflictFileNode node = new ConflictFileNode(path);
			node.add(ob);
			conflictFiles.put(path, node);
		}
	}
	
	public void addConflictNode(String path){
		if(!conflictFileNodeExist(path)){
			ConflictFileNode node = new ConflictFileNode(path);
			conflictFiles.put(path, node);
		}
	}
	
	public void removeConflictNode(String path){
		conflictFiles.remove(path);
	}
	

	public void deleteFile() {
		// TODO Auto-generated method stub
		
	}

	public void mergeFile() {
		// TODO Auto-generated method stub
		
	}

	public void fetchFile(String target, String fileID, String relativePath) {
		// TODO Auto-generated method stub
		imm.fetchFile(target, relativePath);
	}

	public void renameLocalFile(String oldRelativePath,
			String newRelativePath) {
		// TODO Auto-generated method stub
		System.out.println("----ConflictManager----enter renameLocalFile");
		imm.renameLocalFile(oldRelativePath, newRelativePath);
		
	}

	public void createEmptyFileNode(String path, String fileID) {
		// TODO Auto-generated method stub
		imm.createEmptyFileNode(path, fileID);
	}
	
	public ConflictFileNode getConflictFileNode(String path){
		return conflictFiles.get(path);
	}

	

	
	
	

}
