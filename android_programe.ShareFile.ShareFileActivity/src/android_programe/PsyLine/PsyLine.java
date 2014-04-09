package android_programe.PsyLine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.util.Log;
import android_programe.FileSystem.VersionMap;
import android_programe.LogLine.LogLine;
import android_programe.MemoryManager.FileMetaData;

public class PsyLine extends Observable implements FileTransfer,FileTransferCallBack{

	private int maxTCPConnections;
	private int TCPConCount;
	private PsyTcpClient psyTcpClient;
	private PsyTcpServer psyTcpServer;
	private LogLine logline;
	private List socketList;
	//private List psyTcpClients;
	public PsyLine(LogLine l) throws IOException{
		TCPConCount = 0;
		maxTCPConnections = 10;				//设置tcp连接最大个数
		logline = l;
		socketList = new ArrayList<SocketIO>();
		psyTcpClient = new PsyTcpClient(this);
		psyTcpServer = new PsyTcpServer(this);
		psyTcpServer.startServer();
		
	//	psyTcpClients = new ArrayList<PsyTcpClient>();
	}
	
	
	/** 通过ip进行设备连接(TCP)
	 * @throws IOException */
	public boolean connect(String ip) throws IOException{
		
		//psyTcpClient = new PsyTcpClient(ip);
		if(TCPConCount < maxTCPConnections){
			if (psyTcpClient.connect(ip)){
				TCPConCount ++;
				return true;
			}
			else return false;
		}
		else{
			System.out.println("已经达到最大连接个数，连接失败");
			return false;
		}
	}
	
	/**
	 * 同之前连接的设备重新建立连接
	 * @param localIP 
	 */
	public void reconnectAll(String localIP){
		psyTcpServer.serverState();
		int i=0;
		for(;i<socketList.size();i++){
			SocketIO sio = ((SocketIO)socketList.get(i));
			sio.close();
			socketList.remove(sio);
			try {
				if(connect(sio.getTargetID())){
					
				}else{
					logline.removeDevice(sio.getTargetID());
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	/**
	 * 断开连接
	 * @param id 需要断开连接的设备id
	 */
	public boolean disconnect(String id){
		int index = getIndexByTargetID(id);
		if(index != -1){
			System.out.println("enter psyline's disconnect");
			SocketIO sio = (SocketIO)socketList.get(index);
			sio.sendDisconnectMsg();
			sio.close();
			socketList.remove(index);
			return true;
		}
		else return false;
	}
	
	public synchronized  void addSocket(SocketIO si){
		int index = getIndexByTargetID(si.getTargetID());
		if(index == -1){				//该ip不在连接表中
			socketList.add(si);
			logline.addDevice(si.getSocket().getInetAddress().getHostName(), si.getTargetID(), 0);
			System.out.println("add a device:"+si.getSocket().getInetAddress().getHostName()+"  ip is "+si.getTargetID());
		}
	}
	
	public synchronized void removeSocket(SocketIO si){
		int index = getIndexByTargetID(si.getTargetID());
		if(index != -1){
			//存在该socket
			socketList.remove(index);
		}
	}
	
	
	
	public boolean sendFile(String ip,final FileMetaData metaData,final String absolutePath){
		int index = getIndexByTargetID(ip);
		if(index != -1){
			int type = ((SocketIO)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.sendFile(ip, metaData, absolutePath);
			else if(type == 1) return psyTcpServer.sendFile(ip, metaData, absolutePath);
		}
		return false;
		
	}
	
	public boolean deleteFile(String ip,String relativeFilePath){
		int index = getIndexByTargetID(ip);
		if(index != -1){
			int type = ((SocketIO)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.deleteFile(ip, relativeFilePath);
			else if(type == 1) return psyTcpServer.deleteFile(ip, relativeFilePath);
		}
		return false;
	}
	
	public boolean renameFile(String ip, String relativeFilePath,String newRelativeFilePath) {
		int index = getIndexByTargetID(ip);
		if(index != -1){
			int type = ((SocketIO)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.renameFile(ip,relativeFilePath,newRelativeFilePath);
			else if(type == 1) return psyTcpServer.renameFile(ip, relativeFilePath,newRelativeFilePath);
		}
		return false;
	}
	
	public boolean sendFileInf(String ip,String relativePath,String MD5){
		int index = getIndexByTargetID(ip);
		if(index != -1){
			int type = ((SocketIO)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.sendFileInf(ip,relativePath,MD5);
			else if(type == 1) return psyTcpServer.sendFileInf(ip, relativePath, MD5);
		}
		return false;
	}
	
	public boolean makeDir(String ip,String relativePath){
		int index = getIndexByTargetID(ip);
		if(index != -1){
			int type = ((SocketIO)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.makeDir(ip, relativePath);
			else if(type == 1) return psyTcpServer.makeDir(ip, relativePath);
		}
		return false;
	}
	
	/**向设备ID请求文件*/
	public boolean fetchFile(String ID,int style,String relativePath){
		switch(style){
		case 0:{							//为TCP/IP连接
			int index = getIndexByTargetID(ID);
			if(index != -1){
				int type = ((SocketIO)socketList.get(index)).getType();
				if(type == 0) return psyTcpClient.fetchFile(ID, relativePath);
				else if(type == 1) return psyTcpServer.fetchFile(ID, relativePath);
			}
		}break;
		//TODO
		}
		return false;
	}
	
	public boolean sendFileEve(String filePath, String fileName){
		return true;
	}
	
	public  int getIndexByTargetID(String targetID){
		int i = 0;
		for(;i<socketList.size();i++){
			if(((SocketIO)(socketList.get(i))).getTargetID().equals(targetID))
				return i;
		}
		return -1;
	}
	
	public SocketIO getSocketInf(int index){
		if(index < socketList.size())
			return (SocketIO)socketList.get(index);
		else return null;
	}
	
	public boolean receiveFileInf(String targetIp,String relativePath,String absolutePath,String MD5){
		System.out.println("enter psyline receiveFileInf---------");
		return logline.receiveFileInf(targetIp, relativePath, absolutePath, MD5);
	}
	
	public boolean receiveAskFile(String targetIp,String relativePath,String absolutePath){
		return logline.receiveAskFile(targetIp,relativePath,absolutePath);
	}
	
	public boolean receiveDeleteFile(String targetIp,String filepath){
		return logline.receiveDeleteFile(targetIp, filepath);
	}

	public boolean receiveRenameFile(String targetIp, String oldPath, String newPath) {
		return logline.receiveRenameFile(targetIp,oldPath,newPath);
		
	}
	
	public boolean receiveMakeDir(String targetIp, String absolutePath){
		return logline.receiveMakeDir(targetIp, absolutePath);
	}
	
	public boolean receiveVersionMap(String targetIp,VersionMap versionMap,String fileID,String relativePath,String tag){
		return logline.receiveVersionMap(targetIp,versionMap,fileID,relativePath,tag);
	}
	
	public void receiveFileData(String targetIp, FileMetaData fileMetaData,
			File file) {
		// TODO Auto-generated method stub
		logline.receiveFileData(targetIp,fileMetaData,file);
	}
	
	/**
	 * 收到断连信息，断开同目标设备的连接
	 * @param ip
	 */
	public void receiveDisconnect(String ip) {
		// TODO Auto-generated method stub
		int index = getIndexByTargetID(ip);
		if(index != -1){
			SocketIO sio = (SocketIO)socketList.get(index);
			sio.close();
			logline.receiveDisconnect(ip);
			socketList.remove(index);
		}
	}
	
	
	
	/**
	 *收到文件更新通知
	 * @param ip
	 * @param fileMetaData
	 */
	public void receiveFileUpdate(String ip, FileMetaData fileMetaData) {
		// TODO Auto-generated method stub
		logline.receiveFileUpdate(ip,fileMetaData);
	}

	public void sendFileMetaData() {
		// TODO Auto-generated method stub
		
	}

	public void getFileMetaData() {
		// TODO Auto-generated method stub
		
	}

	public void sendFileData() {
		// TODO Auto-generated method stub
		
	}

	public void getFileData() {
		// TODO Auto-generated method stub
		
	}

	public void sendFileVersionMap(VersionMap versionMap, String target) {
		// TODO Auto-generated method stub
		
	}

	public void sendCommand(int command) {
		// TODO Auto-generated method stub
		
	}


	public void sendFileMetaData(Object fileMetaData, Object target) {
		// TODO Auto-generated method stub
		
	}


	public void getFileMetaData(String fileID, Object target) {
		// TODO Auto-generated method stub
		
	}


	public void sendFileData(Object fileData, Object target) {
		// TODO Auto-generated method stub
		
	}


	public void getFileData(Object fileID, Object target) {
		// TODO Auto-generated method stub
		
	}


	public void sendFileVersionMap(Object versionMap, Object target) {
		// TODO Auto-generated method stub
		
	}


	public void sendCommand(String command) {
		// TODO Auto-generated method stub
		
	}


	public void sendFileVersionMap(String id, String fileID,
			VersionMap versionMap, String relativePath,String tag) {
		// TODO Auto-generated method stub
		System.out.println("enter psyline send versionMap");
		int index = getIndexByTargetID(id);
		if(index != -1){
			SocketIO sio = (SocketIO)socketList.get(index);
			int type = sio.getType();
			if(type == 0)  psyTcpClient.sendFileVersionMap(sio,versionMap,fileID,relativePath,tag);
			else if(type == 1) psyTcpServer.sendFileVersionMap(sio,versionMap,fileID,relativePath,tag);

		}
		
	}


	public void sendFileUpdateInform(List<String> deviceId,
			FileMetaData fileMetaData) {
		// TODO Auto-generated method stub
		for(int i=0;i<deviceId.size();i++){
			int index = getIndexByTargetID(deviceId.get(i));
			if(index != -1){
				SocketIO sio = (SocketIO)socketList.get(index);
				int type = sio.getType();
				if(type == 0)  psyTcpClient.sendFileUpdateInform(sio,fileMetaData);
				else if(type == 1) psyTcpServer.sendFileUpdateInform(sio,fileMetaData);
			}
		}
	}
	
	public void connectionFailure(String id) {
		// TODO Auto-generated method stub
		System.out.println("-----PsyLine-----connection failure,delete connection information");
		receiveDisconnect(id);
	}


	public void sendSynReady(String id) {
		// TODO Auto-generated method stub
		int index = getIndexByTargetID(id);
		if(index != -1){
			SocketIO sio = (SocketIO)socketList.get(index);
			sio.sendSnyReady();
		}
	}
	
	public void receiveSynReady(String id){
		logline.receiveSynReady(id);
	}
	


	


	


	

	

	
}