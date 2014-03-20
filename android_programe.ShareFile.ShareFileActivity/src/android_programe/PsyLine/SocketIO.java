package android_programe.PsyLine;

import java.io.*;
import java.net.*;

import junit.framework.Assert;

import android_programe.FileSystem.VersionMap;
import android_programe.MemoryManager.FileMetaData;
import android_programe.Util.FileConstant;


public class SocketIO {
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private String targetID;
	private int type;							//0为client连接，1为server连接
	private boolean tag;
	private ObjectOutputStream oos;
	private int urgentData;
	private FileTransferCallBack callBack;
	
	
	public SocketIO(String targetID,Socket socket,int type,FileTransferCallBack callBack){
		try {
			this.socket = socket;
			this.type = type;
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			this.targetID = targetID;
			tag = true;
			oos = new ObjectOutputStream(socket.getOutputStream());
			urgentData = 0xff;
			this.callBack = callBack;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Socket getSocket(){
		return socket;
	}
	
	public DataInputStream getDataInputStream(){
		return dis;
	}
	
	public DataOutputStream getDataOutputStream(){
		return dos;
	}
	
	public synchronized boolean getTag(){
		if(tag == true){
			tag = false;
			return true;
		}
		else return false;
	}
	
	public synchronized void releaseTag(){
		tag = true;
	}
	
	private synchronized void testConnection(){
		try {
			socket.sendUrgentData(urgentData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				socket.connect(new InetSocketAddress(targetID,FileConstant.TCPPORT), 10000);
				type = 0;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				callBack.connectionFailure(targetID);
			}
		}
	}
	
	/**
	 * 发送文件的metaData
	 */
	public synchronized void sendFileMetaData(FileMetaData fileMetaData){
		try {
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送文件更新的通知（是收到来自别的设备的更新）
	 */
	public synchronized void sendFileUpdateInform(FileMetaData fileMetaData){
		try {
			testConnection();
			oos.writeUTF(FileTransferHeader.sendFileUpdateHeader());
			oos.writeUnshared(fileMetaData);
			oos.flush();
			oos.reset();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 发送文件数据,先发送文件的metaData，然后发送data
	 * @param fileVersionNum	文件的版本号
	 * @param fileID	文件的id
	 * @param relativePath	文件的相对路径
	 * @param absolutePath 文件的绝对路径
	 */
	public synchronized void sendFileData(FileMetaData metaData,String absolutePath){
		try {
			testConnection();
			File file = new File(absolutePath);
			DataInputStream disfile = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			Assert.assertNotNull("File inputStream for " + metaData.getFileID() + " is null",disfile);
			System.out.println("file's length that will be sended is" + file.length() + ",path is :" + metaData.getRelativePath());
			//发送文件前的处理
			oos.writeUTF(FileTransferHeader.sendFileDataHeader(file.length()));
			//先发送文件的metaData
			oos.writeUnshared(metaData);
			oos.flush();
			oos.reset();
			//发送文件的数据
			int bufferSize = 8192;
			byte[] buf = new byte[bufferSize];
			int read = 0;
			while((read = disfile.read(buf)) > -1){
				//System.out.println("send " + read);
				oos.write(buf,0,read);
				oos.flush();
				}
			System.out.println(metaData.getRelativePath() + " has been  finished sending");
			disfile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 *发送文件的versionMap
	 * @param versionMap
	 * @param fileID
	 * @param relativePath
	 */
	public synchronized void sendFileVersionMap(VersionMap versionMap,String fileID,String relativePath,String tag){
		try {
			testConnection();
			System.out.println("enter socketIO send versionMap,relativePath is " + relativePath);
			oos.writeUTF(FileTransferHeader.sendFileVersionMapHeader(fileID, relativePath,tag));
			oos.writeUnshared(versionMap);
			oos.flush();
			oos.reset();
			System.out.println("socketIO has sended versionMap,relativePath is" + relativePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送一条指令
	 * @param cmd
	 */
	public synchronized void sendCommand(String cmd){
		try {
			testConnection();
			oos.writeUTF(cmd);
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送断连信息
	 */
	public synchronized void sendDisconnectMsg(){
		try {
			oos.writeUTF(FileTransferHeader.disconnect());
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public String getTargetID(){
		return targetID;
	}
	
	public int getType(){
		return type;
	}
	
	/**
	 * 关闭socket
	 */
	public synchronized void close(){
		try {
			dis.close();
			dos.close();
			oos.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			System.out.println("has closed the socket");
		}
		
		
	}
	
}
