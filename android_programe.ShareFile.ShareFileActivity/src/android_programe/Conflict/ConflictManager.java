package android_programe.Conflict;

import java.io.File;
import java.util.HashMap;

import android_programe.FileSystem.FileManager;
import android_programe.FileSystem.MyFileObserver;
import android_programe.FileSystem.VersionMap;
import android_programe.MemoryManager.FileMetaData;
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
	public int detect(VersionMap localVersionMap,String localDeviceId,VersionMap remoteVersionMap,String remoteDeviceId){
		return conflictDetect.detect(localVersionMap, localDeviceId, remoteVersionMap, remoteDeviceId);
	}
	
	public void resolute(String fileID,VersionMap localVersionMap,String localDeviceId,VersionMap remoteVersionMap,String remoteDeviceId,String relativePath){
		conflictResolute.resoluteConfliction(fileID,localVersionMap, localDeviceId, remoteVersionMap, remoteDeviceId, relativePath,this);
	}
	
	/**
	 * �յ���ͻ�ļ�������
	 */
	public void receiveConflictFileData(String target,FileMetaData fileMetaData,File file,String orginalName){
		conflictResolute.receiveConflictFileData(target, fileMetaData, file,orginalName,this);
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

	public void renameLocalFile(String fileID, String oldRelativePath,
			String newRelativePath) {
		// TODO Auto-generated method stub
		imm.renameLocalFile(fileID, oldRelativePath, newRelativePath);
		
	}

	public void createEmptyFileNode(String path, String fileID) {
		// TODO Auto-generated method stub
		imm.createEmptyFileNode(path, fileID);
	}
	
	public ConflictFileNode getConflictFileNode(String path){
		return conflictFiles.get(path);
	}
	
	

}
