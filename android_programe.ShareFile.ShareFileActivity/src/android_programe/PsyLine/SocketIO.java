package android_programe.PsyLine;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;

import android_programe.FileSystem.VersionMap;
import android_programe.MemoryManager.FileMetaData;
import android_programe.Util.FileConstant;


public class SocketIO implements Runnable{
	
	private static final int FILEDATA = 1;
	private static final int VERSIONMAP = 2;
	private static final int METADATA = 3;
	private static final int COMMAND = 4;
	private static final int FILEUPDATE = 5;
    private static final int DISCONNECT = 6;
	private static final int SYNREADY = 7;
	private static final int HEARTBEAT = 8;
	
	
	
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private String targetID;
	private int type;							//0为client连接，1为server连接
	private boolean tag;
	private ObjectOutputStream oos;
	private int urgentData;
	private FileTransferCallBack callBack;
	
	private LinkedBlockingQueue<IOMessage> messageQueue;
	
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
			
			messageQueue = new LinkedBlockingQueue<IOMessage>();
			
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
	
	/*
	private synchronized void testConnection(){
		try {
			socket.sendUrgentData(urgentData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				socket.connect(new InetSocketAddress(targetID,FileConstant.TCPPORT), 10000);
				type = 0;
				System.out.println("reconnect to " + targetID);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				callBack.connectionFailure(targetID);
			}
		}
	}*/
	
	private void reconnect(){
		try {
			
			socket.connect(new InetSocketAddress(targetID,FileConstant.TCPPORT), 10000);
			type = 0;

		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			callBack.connectionFailure(targetID);
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
		IOMessage msg = new IOMessage();
		msg.type = FILEUPDATE;
		msg.obj1 = fileMetaData;
		try {
			messageQueue.put(msg);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void sendFileUpdateInform(IOMessage msg){
		try {
			Assert.assertNotNull(msg.obj1);
			oos.writeUTF(FileTransferHeader.sendFileUpdateHeader());
			oos.writeUnshared((FileMetaData)msg.obj1);
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
		IOMessage msg = new IOMessage();
		msg.type = FILEDATA;
		msg.obj1 = metaData;
		msg.sArg1 = absolutePath;
		try {
			messageQueue.put(msg);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void sendFileData(IOMessage msg){
		try {
			//testConnection();
			Assert.assertNotNull(msg.obj1);
			Assert.assertNotNull(msg.sArg1);
			File file = new File(msg.sArg1);
			DataInputStream disfile = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			Assert.assertNotNull("File inputStream for " + ((FileMetaData)msg.obj1).getFileID() + " is null",disfile);
			//发送文件前的处理
			oos.writeUTF(FileTransferHeader.sendFileDataHeader(file.length()));
			//先发送文件的metaData
			oos.writeUnshared((FileMetaData)msg.obj1);
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
		IOMessage msg = new IOMessage();
		msg.type = VERSIONMAP;
		msg.obj1 = versionMap;
		msg.sArg1 = fileID;
		msg.sArg2 = relativePath;
		msg.sArg3 = tag;
		
		try {
			messageQueue.put(msg);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	private void sendFileVersionMap(IOMessage msg){
		try {
			//testConnection();
			Assert.assertNotNull(msg.obj1);
			Assert.assertNotNull(msg.sArg1);
			Assert.assertNotNull(msg.sArg2);
			Assert.assertNotNull(msg.sArg3);
			oos.writeUTF(FileTransferHeader.sendFileVersionMapHeader(msg.sArg1, msg.sArg2,msg.sArg3));
			oos.writeUnshared((VersionMap)msg.obj1);
			oos.flush();
			oos.reset();
			System.out.println("socketIO has sended versionMap,relativePath is" + msg.sArg2);
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
		IOMessage msg = new IOMessage();
		msg.type = COMMAND;
		msg.sArg1 = cmd;
		try {
			messageQueue.put(msg);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void sendCommand(IOMessage msg){
		Assert.assertNotNull(msg.sArg1);
		try {
			//testConnection();
			oos.writeUTF(msg.sArg1);
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
		IOMessage msg = new IOMessage();
		msg.type = DISCONNECT;
		msg.sArg1 = FileTransferHeader.disconnect();
		try {
			messageQueue.put(msg);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	private void sendDisconnectMsg(IOMessage msg){
		Assert.assertNotNull(msg.sArg1);
		try {
			//testConnection();
			oos.writeUTF(msg.sArg1);
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 发送同步就绪消息
	 */
	public synchronized void sendSynReady() {
		// TODO Auto-generated method stub
		IOMessage msg = new IOMessage();
		msg.type = SYNREADY;
		msg.sArg1 = FileTransferHeader.synReady();
		try {
			System.out.println("----SocketIO----put synready message");
			messageQueue.put(msg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendSynReady(IOMessage msg){
		try {
			oos.writeUTF(msg.sArg1);
			oos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void sendHeartBeat() {
		// TODO Auto-generated method stub
		IOMessage msg = new IOMessage();
		msg.type = HEARTBEAT;
		msg.sArg1 = FileTransferHeader.heartBeat();
		try {
			messageQueue.put(msg);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void sendHeartBeat(IOMessage msg){
		try {
			oos.writeUTF(msg.sArg1);
			oos.flush();
			System.out.println("----SocketIO----send heart beat");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("----SocketIO----send heart beat failure");
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
			tag = false;
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

	public void run() {
		// TODO Auto-generated method stub
		while(tag){
			try {
				IOMessage msg = messageQueue.take();
				switch(msg.type){
				case FILEDATA:{
					sendFileData(msg);
				}break;
				case FILEUPDATE:{
					sendFileUpdateInform(msg);
				}break;
				case VERSIONMAP:{
					sendFileVersionMap(msg);
				}break;
				case COMMAND:{
					sendCommand(msg);
				}break;
				case DISCONNECT:{
					sendDisconnectMsg(msg);
				}break;
				case SYNREADY:{
					sendSynReady(msg);
				}break;
				case HEARTBEAT:{
					sendHeartBeat(msg);
				}break;
				default:{
					
				}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	class IOMessage{
		
		int type;
		String sArg1,sArg2,sArg3;
		Object obj1,obj2,obj3;
		
		IOMessage(){
			
		}
	}

	
	
}
