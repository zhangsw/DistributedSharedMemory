package android_programe.PsyLine;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import android.os.Message;
import android.util.Log;
import android_programe.FileSystem.VersionMap;
import android_programe.MemoryManager.FileMetaData;
import android_programe.Util.*;


public class PsyTcpClient implements FileTransfer{

	private int timeout;
	private ExecutorService executorService = null;
	private final int POOL_SIZE = 2;				//�̳߳ش�С
	private PsyLine psyline;
	
	public PsyTcpClient(PsyLine p){
		timeout = 10000;
		int cpuCount = Runtime.getRuntime().availableProcessors();
		executorService = Executors.newFixedThreadPool(cpuCount*POOL_SIZE);
		psyline = p;
	}
	
	public boolean connect(String ip){
		if(psyline.getIndexByTargetID(ip) == -1){				//��δͬ���豸��������
			System.out.println("enter psytcpclient connect");
			Socket s = new Socket();
			try {
				System.out.println("ip is " + ip + " test");
				s.connect(new InetSocketAddress(ip,FileConstant.TCPPORT), timeout);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			SocketIO socketInf = new SocketIO(ip,s,0,psyline);
			//socketList.add(socketInf);
			psyline.addSocket(socketInf);
			
			Responser res = new Responser(s,psyline);
			executorService.execute(res);
			return true;
		}
		return false;
	}
	
	/**
	 * �����ļ�
	 * @param ip	
	 * @param fileVerisonNumber �ļ��İ汾��
	 * @param absolutePath
	 * @param relativePath
	 * @return
	 */
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
	
	/**
	 * ɾ���ļ�ָ��
	 * @param ip
	 * @param relativePath
	 * @return
	 */
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
	
	/**���ַΪip���豸�����ļ�*/
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
	
	/**
	 * �����ļ���ָ��
	 * @param ip
	 * @param relativePath
	 * @return
	 */
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

	/**
	 * �������ļ�ָ��
	 * @param ip
	 * @param oldRelativePath
	 * @param newRelativePath
	 * @return
	 */
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

	/*
	private SocketIO getSocketInfByTargetID(Object target){
		int index = psyline.getIndexByTargetID((String)target);
		
	}
	*/
	public void sendFileMetaData(Object fileMetaData, Object target) {
		// TODO Auto-generated method stub
		
	}

	public void getFileMetaData(Object fileID, Object target) {
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

	public void getFileMetaData(String fileID, Object target) {
		// TODO Auto-generated method stub
		
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
