package android_programe.LogLine;

import java.io.File;
import java.io.IOException;
import java.util.*;

import android.util.Log;
import android_programe.FileSystem.VersionMap;
import android_programe.MemoryManager.FileMetaData;
import android_programe.MemoryManager.MemoryManager;
import android_programe.PsyLine.*;
import android_programe.Util.FileConstant;

public class LogLine {

	
	private PsyLine psyLine;
	private MemoryManager memoryManager;
	private List devices;				//style分为二种，0:TCP,1:bluetooth,
	
	public LogLine(MemoryManager c) throws IOException{
		psyLine = new PsyLine(this);
		devices = new ArrayList<DevicesInf>();
		memoryManager = c;
	}
	
	/** 通过ip建立设备间的连接(TCP连接)
	 * @throws IOException */
	public boolean connect(String ip) throws IOException{
		
		return psyLine.connect(ip);
		
	}
	
	/**
	 * 同之前连接的设备重新建立连接
	 * @param localIP 
	 */
	public void reconnectAll(String localIP){
		psyLine.reconnectAll(localIP);
	}
	
	/** 通过蓝牙进行设备间的连接*/
	public void connect(){
		
	}
	
	/**
	 * 同对象设备断开连接
	 * @param target 需要断开连接的设备名
	 */
	public boolean disconnect(String target){
		int index = getIndexByName(target);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			System.out.println("before enter psyline's disconnect");
			if(psyLine.disconnect(di.getID())){
				System.out.println("psyline has disconnected");
				devices.remove(index);
				return true;
			}
			else return false;
		}
		else return false;
	}
	
	public boolean sendFileInf(String target,String relativePath,String MD5){
		int index = getIndexByName(target);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			return psyLine.sendFileInf(di.getID(),relativePath,MD5);
		}
		return false;
	}
	
	public void sendFile(String target, FileMetaData metaData,
			String absolutePath) {
		// TODO Auto-generated method stub
		int index = getIndexByName(target);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			psyLine.sendFile(di.getID(), metaData, absolutePath);
		}	
	}
	
	public boolean makeDir(String target,String relativePath){
		int index = getIndexByName(target);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			return psyLine.makeDir(di.getID(), relativePath);
		}
		return false;
		
	}
	
	
	public boolean deleteFile(String target,String relativeFilePath){
		int index = getIndexByName(target);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			return psyLine.deleteFile(di.getID(), relativeFilePath);
		}
		return false;
	}
	
	public boolean receiveDeleteFile(String targetIp, String filepath){
		int index = getIndexByID(targetIp);
		if(index != -1){
			return memoryManager.receiveDeleteFile(((DevicesInf)devices.get(index)).getName(), filepath);
		}
		return false;
	}
	
	public boolean receiveFileInf(String targetIp, String relativePath, String absolutePath, String MD5) {
		int index = getIndexByID(targetIp);
		System.out.println("target ip is "+targetIp);
		if(index != -1){
			System.out.println("index is not -1");
			return memoryManager.receiveFileInf(((DevicesInf)devices.get(index)).getName(), relativePath, absolutePath, MD5);
		}
		return false;
	}
	
	public boolean receiveAskFile(String targetIp, String relativePath,String absolutePath) {
		int index = getIndexByID(targetIp);
		if(index != -1){
			return memoryManager.receiveAskFile(((DevicesInf)devices.get(index)).getName(), relativePath, absolutePath);
		}
		return false;
	}
	
	public boolean receiveRenameFile(String targetIp, String oldPath,String newPath) {
		int index = getIndexByID(targetIp);
		if(index != -1){
			return memoryManager.receiveRenameFile(((DevicesInf)devices.get(index)).getName(),oldPath,newPath);
		}
		return false;
	}
	
	public boolean receiveMakeDir(String targetIp, String absolutePath) {
		int index = getIndexByID(targetIp);
		if(index != -1){
			System.out.println("before enter consistency receiveMakeDir,absolutePath is "+ absolutePath);
			return memoryManager.receiveMakeDir(((DevicesInf)devices.get(index)).getName(), absolutePath);
		}
		return false;
	}
	
	/**
	 * 发送文件的versionMap
	 * @param target	目标
	 * @param fileID	文件的id
	 * @param versionMap	
	 * @param relativePath	文件的相对路径
	 */
	public void sendFileVersionMap(String target, String fileID,
			VersionMap versionMap, String relativePath,String tag) {
		// TODO Auto-generated method stub
		int index = getIndexByName(target);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			psyLine.sendFileVersionMap(di.getID(), fileID,versionMap,relativePath,tag);
		}
	}
	
	public void receiveFileData(String targetIp, FileMetaData fileMetaData,
			File file) {
		// TODO Auto-generated method stub
		int index = getIndexByID(targetIp);
		if(index != -1)
			memoryManager.receiveFileData(((DevicesInf)devices.get(index)).getName(), fileMetaData, file);
	}
	
	/**
	 * 接收到文件的versionMap
	 * @param targetIp	目标
	 * @param versionMap	
	 * @param fileID	文件的id
	 * @param relativePath	文件的相对路径
	 * @return
	 */
	
	public boolean receiveVersionMap(String targetIp,VersionMap versionMap,String fileID,String relativePath,String tag){
		int index = getIndexByID(targetIp);
		if(index != -1){
			return memoryManager.receiveVersionMap(((DevicesInf)devices.get(index)).getName(),fileID, versionMap, relativePath,tag);
		}
		else return false;
	}
	
	/**
	 * 收到文件的更新
	 * @param ip
	 * @param fileMetaData
	 */
	public void receiveFileUpdate(String ip, FileMetaData fileMetaData) {
		// TODO Auto-generated method stub
		int index = getIndexByID(ip);
		if(index != -1)
			memoryManager.receiveFileUpdate(((DevicesInf)devices.get(index)).getName(),fileMetaData);
	}
	
	/**
	 * 收到断连信息
	 * @param ip
	 */
	public void receiveDisconnect(String ip) {
		// TODO Auto-generated method stub
		int index = getIndexByID(ip);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			memoryManager.receiveDisconnect(di.getName());
			devices.remove(index);
		}
	}

	
	/**从目标为target的设备请求文件*/
	public boolean fetchFile(String target,final String relativePath){
		
		int index = getIndexByName(target);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			return psyLine.fetchFile(di.getID(),di.getStyle(), relativePath);
		}
		else return false;
		
	}
	
	/*
	给所有同本设备相连接的设备发送文件
	public boolean sendFileEve(String filePath, String fileName){	
		for(int i=0;i<devices.size();i++){
			DevicesInf temp = (DevicesInf)devices.get(i);
			if(temp.getStyle() == 0)
				psyLine.sendFile(temp.getID(), filePath, fileName);
			else 
				return true;					//暂时不考虑蓝牙
		}
		return true;
	}*/
	
	/**向目标target发送修改文件名字信息*/
	public boolean renameFile(String target, String relativeFilePath,String newRelativeFilePath) {
		int index = getIndexByName(target);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			return psyLine.renameFile(di.getID(),relativeFilePath,newRelativeFilePath);
		}
		else return false;
		
	}
	
	public void sendFileUpdateInform(List<String> targets,
			FileMetaData fileMetaData) {
		// TODO Auto-generated method stub
		List <String>deviceId = new ArrayList<String>();
		for(int i=0;i<targets.size();i++){
			int index = getIndexByName(targets.get(i));
			if(index != -1)
				deviceId.add(((DevicesInf)devices.get(index)).getID());
		}
		if(deviceId.size()>0)
			psyLine.sendFileUpdateInform(deviceId,fileMetaData);
	}
	
	/**向所有同本设备相连接的设备请求文件*/
	public boolean fetchFile(final String fileName){
		return true;
	}
	
	public synchronized  void addDevice(String name, String ip, int style){
		System.out.println("enter logline addDevice---");
		DevicesInf temp = new DevicesInf(name,ip,style);
		if(!devices.contains(temp)){
			devices.add(temp);
			memoryManager.addShareDevice(FileConstant.DEFAULTROOTPATH, name, 0);
		}
	}
	
	
	public synchronized void removeDevice(String id){
		int index = getIndexByID(id);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			devices.remove(index);
			memoryManager.removeShareDevice(di.getName());
		}
	}
	
	private int getIndexByName(String target){
		int i = 0;
		System.out.println("devices size is "+devices.size());
		for(;i<devices.size();i++){
			if(((DevicesInf)(devices.get(i))).getName().equals(target))
				return i;
		}
		return -1;
	}
	
	private int getIndexByID(String ID){
		int i = 0;
		System.out.println(((DevicesInf)(devices.get(i))).getID()+"$$"+ID+"$$");
		for(;i<devices.size();i++){
			System.out.println(((DevicesInf)(devices.get(i))).getID());
			if(((DevicesInf)(devices.get(i))).getID().equals(ID) ){
				return i;
			}		
		}
		return -1;
	}

	public String getDeviceNameByID(String ID){
		int i = getIndexByID(ID);
		if(i != -1){
			DevicesInf dif = (DevicesInf)devices.get(i);
			return dif.getName();
		}
		else return null;
	}
	

	
	

	

	

	

	

	

		
	}

