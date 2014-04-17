package android_programe.PsyLine;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android_programe.FileSystem.VersionMap;
import android_programe.MemoryManager.FileMetaData;
import android_programe.Util.FileConstant;

public class PsyTcpServer{

	private ServerSocket serverSocket = null;
	private ExecutorService executorServiceRe = null;
	private ExecutorService executorServiceSo = null;
	private final int POOL_SIZE = 8;				//线程池大小
	private boolean tag;
	private boolean serverStart;
	private PsyLine psyline;
	
	//private String savePath;
	
	public PsyTcpServer(PsyLine p) throws IOException{
		 int cpuCount = Runtime.getRuntime().availableProcessors();
		 executorServiceRe = Executors.newFixedThreadPool(cpuCount*POOL_SIZE);
		 executorServiceSo = Executors.newFixedThreadPool(cpuCount*POOL_SIZE);
		 serverSocket = new ServerSocket(FileConstant.TCPPORT);
		 tag = true;
		 serverStart = false;
		 psyline = p;
	}
	
	public synchronized void startServer(){
		if(serverStart == true) return;	
		new Thread(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					super.run();
					System.out.println("server has been started");
					while(tag){
						try {
							// 接收客户连接,只要客户进行了连接,就会触发accept()从而建立连接
							Socket socket = serverSocket.accept();
							System.out.println("accept a socket");
							InetAddress ia = socket.getInetAddress();
							String ip = ia.getHostAddress();
							int index = psyline.getIndexByTargetID(ip);			
							if(index != -1){
								//存在该设备
								SocketIO si= psyline.getSocketInf(index);
								si.close();	
							}
							SocketIO si = new SocketIO(ip,socket,1,psyline);
							executorServiceSo.execute(si);
							psyline.addSocket(si);
							
							Responser res = new Responser(socket,psyline);
							executorServiceRe.execute(res);//Responser类的定义见后面
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
			serverStart = true;
	}
	
	public synchronized void stopServer(){
		tag = false;
		serverStart = false;
	}
	
	/**
	 * 用于测试server状态
	 */
	public void serverState(){
		System.out.println(serverSocket);
		System.out.println(serverSocket.isClosed());
	}
	
	
	
	public boolean sendFile(final String ip,final FileMetaData metaData,final String absolutePath){
		new Thread(){
			public void run() {
				System.out.println("sendFile 1------------------------");
				int index = psyline.getIndexByTargetID(ip);
				SocketIO si = psyline.getSocketInf(index);
				si.sendFileData(metaData, absolutePath);
			}
		}.start();
		return true;
	}
	
	public boolean deleteFile(final String ip,final String relativePath){
		new Thread(){
			public void run(){
				System.out.println("deletefile 1------------------------");	
				int index = psyline.getIndexByTargetID(ip);
				SocketIO si = psyline.getSocketInf(index);
				si.sendCommand(FileTransferHeader.deleteFileCmd(relativePath));
			}
		}.start();
		return true;
	}
	
	public boolean sendFileInf(final String ip,final String relativePath,final String MD5){
		new Thread(){
			public void run(){
				System.out.println("sendFileInf 1------------------------");	
				int index = psyline.getIndexByTargetID(ip);
				SocketIO si = psyline.getSocketInf(index);
				DataOutputStream dos = si.getDataOutputStream();
				si.sendCommand(FileConstant.FILEINF + "$PATH$" + relativePath + "$MD5$" + MD5 + "\n");
					
				System.out.println("sendFileInf 3-----------------------");
			}
		}.start();
		return true;
	}
	
	public boolean fetchFile(final String ip,final String relativePath){
		new Thread(){
			public void run(){
				System.out.println("send fetchfile ask---------");
				int index = psyline.getIndexByTargetID(ip);
				SocketIO si = psyline.getSocketInf(index);
				si.sendCommand(FileTransferHeader.fetchFileCmd(relativePath));
			}
		}.start();
		return true;
	}

	public boolean renameFile(final String ip, final String oldRelativePath,final String newRelativePath) {
		new Thread(){
			public void run(){
				int index = psyline.getIndexByTargetID(ip);
				SocketIO si = psyline.getSocketInf(index);
				si.sendCommand(FileTransferHeader.renameFileCmd(oldRelativePath, newRelativePath));
			}
		}.start();
		return true;
	}
	
	public boolean makeDir(final String ip,final String relativePath){
		new Thread(){
			public void run(){
				int index = psyline.getIndexByTargetID(ip);
				SocketIO si = psyline.getSocketInf(index);
				si.sendCommand(FileTransferHeader.makeDirCmd(relativePath));
			}
		}.start();
		return true;
	}

	public void sendFileVersionMap(final SocketIO sio, final VersionMap versionMap,
			final String fileID, final String relativePath,final String tag) {
		// TODO Auto-generated method stub
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				sio.sendFileVersionMap(versionMap, fileID, relativePath,tag);
			}
			
		}.start();
	}

	public void sendFileUpdateInform(SocketIO sio, FileMetaData fileMetaData) {
		// TODO Auto-generated method stub
		sio.sendFileUpdateInform(fileMetaData);
	}
}