package android_programe.PsyLine;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android_programe.Util.FileConstant;

public class PsyTcpServer{

	private ServerSocket serverSocket = null;
	private ExecutorService executorService = null;
	private final int POOL_SIZE = 2;				//线程池大小
	private boolean tag;
	private PsyLine psyline;
	
	//private String savePath;
	
	public PsyTcpServer(PsyLine p) throws IOException{
		 int cpuCount = Runtime.getRuntime().availableProcessors();
		 executorService = Executors.newFixedThreadPool(cpuCount*POOL_SIZE);
		 
		 serverSocket = new ServerSocket(FileConstant.TCPPORT);
		 //socketList = new ArrayList<SocketInf>();
		 tag = false;
		 psyline = p;
		 //savePath = "F:\\Test\\";
	}
	
	public void startServer(){
		if(tag == false){
			tag = true;
			new Thread(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					super.run();
				
					while(true){
						try {
							// 接收客户连接,只要客户进行了连接,就会触发accept()从而建立连接
							Socket socket = serverSocket.accept();
							InetAddress ia = socket.getInetAddress();
							String ip = ia.getHostAddress();
							int index = psyline.getIndexByIp(ip);			//只允许连接一次
							if(index == -1){
								SocketInf si = new SocketInf(ip,socket,1);
								psyline.addSocket(si);
								Responser res = new Responser(socket,psyline);
								executorService.execute(res);//Responser类的定义见后面
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				
			}.start();
		}
	}
	
	public boolean sendFile(final String ip,final String absolutePath,final String relativeFilePath){
		new Thread(){
			public void run() {
				try{
				//FileHelper helper = new FileHelper(context);
				File file = new File(absolutePath);
				
				System.out.println("sendFile 1------------------------");
				
				DataInputStream disfile = new DataInputStream(new BufferedInputStream(new FileInputStream(absolutePath)));
				
				int index = psyline.getIndexByIp(ip);
				SocketInf si = psyline.getSocketInf(index);
				
				while(!si.getTag());
				DataOutputStream dos = si.getDataOutputStream();
				DataInputStream dis = si.getDataInputStream();
				
				dos.writeUTF(FileConstant.FILEDATA + "$SIZE$" + file.length() + "$PATH$" + relativeFilePath+"\n");
				dos.flush();
				System.out.println("sendFile 3-----------------------");
				
				long length = file.length();
				long passedLength = 0;
				int bufferSize = 8192;
				byte[] buf = new byte[bufferSize];
				while(true){
					int read = 0;
					if(dis != null){
						read = disfile.read(buf);
						//System.out.println(read);
					}
					if(read == -1){
						break;
					}
					
					dos.write(buf,0,read);
					dos.flush();
					passedLength +=read;
						/*
						Message msg = new Message();
						msg.what = AndroidSmsConstant.SENDING_PROGRESS;
						msg.arg1 = (int)(passedLength*100/length);
						System.out.println(msg.arg1);
						handler.sendMessage(msg);
						System.out.println("sent message--------");
						*/
					}
				System.out.println("file finished sending");
				disfile.close();
				
				si.releaseTag();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}.start();
		return true;
	}
	
	public boolean deleteFile(final String ip,final String relativeFilePath){
		new Thread(){
			public void run(){
				try{
					System.out.println("deletefile 1------------------------");	
					int index = psyline.getIndexByIp(ip);
					SocketInf si = psyline.getSocketInf(index);
					while(!si.getTag());
					DataOutputStream dos = si.getDataOutputStream();
					dos.writeUTF(FileConstant.DELETEFILE + "$PATH$" + relativeFilePath+"\n");
					dos.flush();
					si.releaseTag();
					System.out.println("deletefile 3-----------------------");
				}catch(IOException e){
				e.printStackTrace();
				}
			}
		}.start();
		return true;
	}
	
	public boolean sendFileInf(final String ip,final String relativePath,final String MD5){
		new Thread(){
			public void run(){
				try{
					System.out.println("sendFileInf 1------------------------");
					int index = psyline.getIndexByIp(ip);
					SocketInf si = psyline.getSocketInf(index);
					while(!si.getTag());
					DataOutputStream dos = si.getDataOutputStream();
					dos.writeUTF(FileConstant.FILEINF + "$PATH$" + relativePath + "$MD5$" + MD5 + "\n");
					dos.flush();
					si.releaseTag();
					System.out.println("sendFileInf 3-----------------------");
					}catch(IOException e){
						e.printStackTrace();
					}
			}
		}.start();
		return true;
	}
	
	public boolean fetchFile(final String ip,final String relativePath){
		new Thread(){
			public void run(){
				System.out.println("send fetchfile ask---------");
				
				int index = psyline.getIndexByIp(ip);
				SocketInf si = psyline.getSocketInf(index);
				while(!si.getTag());
				DataOutputStream dos = si.getDataOutputStream();
				try {
					dos.writeUTF(FileConstant.ASKFILE + "$PATH$" + relativePath + "\n");
					dos.flush();
					si.releaseTag();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		return true;
	}

	public boolean renameFile(final String ip, final String relativeFilePath,final String newRelativeFilePath) {
		new Thread(){
			public void run(){
				try{
					System.out.println("sendFileInf 1------------------------");	
					int index = psyline.getIndexByIp(ip);
					SocketInf si = psyline.getSocketInf(index);
					while(!si.getTag());
					DataOutputStream dos = si.getDataOutputStream();
					dos.writeUTF(FileConstant.RENAMEFILE + "$OLDPATH$" + relativeFilePath + "$NEWPATH$" + newRelativeFilePath + "\n");
					dos.flush();
					si.releaseTag();
					System.out.println("sendFileInf 3-----------------------");
				}catch(IOException e){
				e.printStackTrace();
				}
			}
		}.start();
		return true;
	}
	
	public boolean makeDir(final String ip,final String relativePath){
		new Thread(){
			public void run(){
				int index = psyline.getIndexByIp(ip);
				SocketInf si = psyline.getSocketInf(index);
				while(!si.getTag());
				DataOutputStream dos = si.getDataOutputStream();
				try{
					dos.writeUTF(FileConstant.MAKEDIR + "$PATH$" + relativePath + "\n");
					dos.flush();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				si.releaseTag();
			}
		}.start();
		return true;
	}
}