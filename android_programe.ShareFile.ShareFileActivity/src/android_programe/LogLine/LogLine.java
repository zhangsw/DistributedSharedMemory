package android_programe.LogLine;

import java.io.IOException;
import java.util.*;

import android.util.Log;
import android_programe.Consistency.Consistency;
import android_programe.PsyLine.*;

public class LogLine {

	
	private PsyLine psyLine;
	private Consistency consistency;
	private List devices;				//style分为二种，0:TCP,1:bluetooth,
	
	public LogLine(Consistency c) throws IOException{
		psyLine = new PsyLine(this);
		devices = new ArrayList<DevicesInf>();
		consistency = c;
	}
	
	/** 通过ip建立设备间的连接(TCP连接)
	 * @throws IOException */
	public boolean connect(String ip) throws IOException{
		
		return psyLine.connect(ip);
		
	}
	
	/** 通过蓝牙进行设备间的连接*/
	public void connect(){
		
	}
	
	public boolean sendFileInf(String target,String relativePath,String MD5){
		int index = getIndexByName(target);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			return psyLine.sendFileInf(di.getID(),relativePath,MD5);
		}
		return false;
	}
	
	/**给地址为参数为ip的设备发送文件*/
	public boolean sendFile(String target,final String absolutePath,final String relativeFilePath){
		
		int index = getIndexByName(target);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			return psyLine.sendFile(di.getID(),absolutePath,relativeFilePath);
		}
		return false;
		
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
			return consistency.receiveDeletaFile(((DevicesInf)devices.get(index)).getName(), filepath);
		}
		return false;
	}
	
	public boolean receiveFileInf(String targetIp, String relativePath, String absolutePath, String MD5) {
		int index = getIndexByID(targetIp);
		System.out.println("target ip is "+targetIp);
		if(index != -1){
			System.out.println("index is not -1");
			return consistency.receiveFileInf(((DevicesInf)devices.get(index)).getName(), relativePath, absolutePath, MD5);
		}
		return false;
	}
	
	public boolean receiveAskFile(String targetIp, String relativePath,String absolutePath) {
		int index = getIndexByID(targetIp);
		if(index != -1){
			return consistency.receiveAskFile(((DevicesInf)devices.get(index)).getName(), relativePath, absolutePath);
		}
		return false;
	}
	
	public boolean receiveRenameFile(String targetIp, String oldPath,String newPath) {
		int index = getIndexByID(targetIp);
		if(index != -1){
			return consistency.receiveRenameFile(((DevicesInf)devices.get(index)).getName(),oldPath,newPath);
		}
		return false;
	}
	
	public boolean receiveMakeDir(String targetIp, String absolutePath) {
		int index = getIndexByID(targetIp);
		if(index != -1){
			System.out.println("before enter consistency receiveMakeDir,absolutePath is "+ absolutePath);
			return consistency.receiveMakeDir(((DevicesInf)devices.get(index)).getName(), absolutePath);
		}
		return false;
	}
	
	/**从目标为taeget的设备请求文件*/
	public boolean fetchFile(String target,final String relativePath){
		
		int index = getIndexByName(target);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			return psyLine.fetchFile(di.getID(),di.getStyle(), relativePath);
		}
		else return false;
		
	}
	
	/**给所有同本设备相连接的设备发送文件*/
	public boolean sendFileEve(String filePath, String fileName){	
		for(int i=0;i<devices.size();i++){
			DevicesInf temp = (DevicesInf)devices.get(i);
			if(temp.getStyle() == 0)
				psyLine.sendFile(temp.getID(), filePath, fileName);
			else 
				return true;					//暂时不考虑蓝牙
		}
		return true;
	}
	
	/**向目标target发送修改文件名字信息*/
	public boolean renameFile(String target, String relativeFilePath,String newRelativeFilePath) {
		int index = getIndexByName(target);
		if(index != -1){
			DevicesInf di = (DevicesInf)devices.get(index);
			return psyLine.renameFile(di.getID(),relativeFilePath,newRelativeFilePath);
		}
		else return false;
		
	}
	
	/**向所有同本设备相连接的设备请求文件*/
	public boolean fetchFile(final String fileName){
		return true;
	}
	
	public synchronized  void addDevice(String name, String ip, int style){
		System.out.println("enter logline addDevice---");
		DevicesInf temp = new DevicesInf(name,ip,style);
		if(!devices.contains(temp)) {
			devices.add(temp);
			consistency.addShareDevice("/sdcard/wallpaper", name, 0);
			}
		}
	
	private int getIndexByName(String target){
		int i = 0;
		System.out.println("devices size is "+devices.size());
		for(;i<devices.size();i++){
			System.out.println(((DevicesInf)(devices.get(i))).getName()+"------------");
			System.out.println(target+"------------");
			if(((DevicesInf)(devices.get(i))).getName().equals(target))
				return i;
		}
		return -1;
	}
	
	private int getIndexByID(String ID){
		int i = 0;
		System.out.println("devices size is "+devices.size());
		System.out.println(((DevicesInf)(devices.get(i))).getID()+"$$"+ID+"$$");
		for(;i<devices.size();i++){
			System.out.println(((DevicesInf)(devices.get(i))).getID());
			if(((DevicesInf)(devices.get(i))).getID().equals(ID) ){
				System.out.println("存在设备---");
				return i;
			}
				
		}
		return -1;
	}

	

	

		
	}

