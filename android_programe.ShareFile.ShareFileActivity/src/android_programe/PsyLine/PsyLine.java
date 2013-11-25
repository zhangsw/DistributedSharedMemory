package android_programe.PsyLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import android.util.Log;
import android_programe.LogLine.LogLine;

public class PsyLine extends Observable{

	private int maxTCPConnections;
	private int TCPConCount;
	private PsyTcpClient psyTcpClient;
	private PsyTcpServer psyTcpServer;
	private LogLine logline;
	private  List socketList;
	//private List psyTcpClients;
	public PsyLine(LogLine l) throws IOException{
		TCPConCount = 0;
		maxTCPConnections = 10;				//设置tcp连接最大个数
		logline = l;
		socketList = new ArrayList<SocketInf>();
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
	
	public synchronized  void addSocket(SocketInf si){
		int index = getIndexByIp(si.getIp());
		if(index == -1){				//该ip不在连接表中
			socketList.add(si);
			logline.addDevice(si.getSocket().getInetAddress().getHostName(), si.getIp(), 0);
			System.out.println("add a device:"+si.getSocket().getInetAddress().getHostName()+"  ip is "+si.getIp());
		}
		
	} 
	
	public boolean sendFile(String ip,final String absolutePath,final String relativeFilePath){
		int index = getIndexByIp(ip);
		if(index != -1){
			int type = ((SocketInf)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.sendFile(ip,absolutePath,relativeFilePath);
			else if(type == 1) return psyTcpServer.sendFile(ip, absolutePath, relativeFilePath);
		}
		return false;
		
	}
	
	public boolean deleteFile(String ip,String relativeFilePath){
		int index = getIndexByIp(ip);
		if(index != -1){
			int type = ((SocketInf)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.deleteFile(ip, relativeFilePath);
			else if(type == 1) return psyTcpServer.deleteFile(ip, relativeFilePath);
		}
		return false;
	}
	
	public boolean renameFile(String ip, String relativeFilePath,String newRelativeFilePath) {
		int index = getIndexByIp(ip);
		if(index != -1){
			int type = ((SocketInf)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.renameFile(ip,relativeFilePath,newRelativeFilePath);
			else if(type == 1) return psyTcpServer.renameFile(ip, relativeFilePath,newRelativeFilePath);
		}
		return false;
	}
	
	public boolean sendFileInf(String ip,String relativePath,String MD5){
		int index = getIndexByIp(ip);
		if(index != -1){
			int type = ((SocketInf)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.sendFileInf(ip,relativePath,MD5);
			else if(type == 1) return psyTcpServer.sendFileInf(ip, relativePath, MD5);
		}
		return false;
	}
	
	public boolean makeDir(String ip,String relativePath){
		int index = getIndexByIp(ip);
		if(index != -1){
			int type = ((SocketInf)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.makeDir(ip, relativePath);
			else if(type == 1) return psyTcpServer.makeDir(ip, relativePath);
		}
		return false;
	}
	
	/**向设备ID请求文件*/
	public boolean fetchFile(String ID,int style,String relativePath){
		switch(style){
		case 0:{							//为TCP/IP连接
			int index = getIndexByIp(ID);
			if(index != -1){
				int type = ((SocketInf)socketList.get(index)).getType();
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
	
	
	
	public  int getIndexByIp(String ip){
		int i = 0;
		for(;i<socketList.size();i++){
			if(((SocketInf)(socketList.get(i))).getIp().equals(ip))
				return i;
		}
		return -1;
	}
	
	public SocketInf getSocketInf(int index){
		if(index < socketList.size())
			return (SocketInf)socketList.get(index);
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

	

	
}