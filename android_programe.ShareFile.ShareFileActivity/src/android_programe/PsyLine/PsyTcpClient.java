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
import android_programe.Util.*;


public class PsyTcpClient{

	private int timeout;
	private ExecutorService executorService = null;
	private final int POOL_SIZE = 2;				//线程池大小
	private PsyLine psyline;
	
	public PsyTcpClient(PsyLine p){
		timeout = 10000;
		int cpuCount = Runtime.getRuntime().availableProcessors();
		executorService = Executors.newFixedThreadPool(cpuCount*POOL_SIZE);
		psyline = p;
	}
	
	public boolean connect(String ip){
		if(psyline.getIndexByIp(ip) == -1){				//还未同该设备进行连接
			System.out.println("enter psytcpclient connect");
			Socket s = new Socket();
			try {
				s.connect(new InetSocketAddress(ip,FileConstant.TCPPORT), timeout);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			System.out.println("connected--------");
			SocketInf socketInf = new SocketInf(ip,s,0);
			//socketList.add(socketInf);
			psyline.addSocket(socketInf);
			
			Responser res = new Responser(s,psyline);
			executorService.execute(res);
			return true;
		}
		return false;
	}
	
	/*
	public PsyTcpClient(String ip){
		try {
			System.out.println("ip is"+ip);
			socket = new Socket(ip,FileConstant.TCPPORT);
			IP = ip;
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			
			tag = true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/
	
	public boolean sendFile(final DataOutputStream dos,final String absolutePath,final String relativePath){
		try {
			File file = new File(absolutePath);
			DataInputStream disfile = new DataInputStream(new BufferedInputStream(new FileInputStream(absolutePath)));
			dos.writeUTF(FileConstant.FILEDATA + "$SIZE$" + file.length() + "$PATH$" + relativePath+"\n");
			dos.flush();
			
			long length = file.length();
			long passedLength = 0;
			int bufferSize = 8192;
			byte[] buf = new byte[bufferSize];
			while(true){
				int read = 0;
				if(disfile != null){
					read = disfile.read(buf);
					//System.out.println(read);
				}
				if(read == -1){
					break;
				}
				
				dos.write(buf,0,read);
				dos.flush();
				passedLength +=read;
				}
			System.out.println("file finished sending");
			disfile.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
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
				
				/*
				//开始传输文件，发送消息显示进度条
				Message msg1 = new Message();
				msg1.what = AndroidSmsConstant.SHOW_PROGRESSDIALOG;
				handler.sendMessage(msg1);
				*/
				long length = file.length();
				long passedLength = 0;
				int bufferSize = 8192;
				byte[] buf = new byte[bufferSize];
				while(true){
					int read = 0;
					if(disfile != null){
						read = disfile.read(buf);
						//System.out.println(read);
					}
					if(read == -1){
						break;
					}
					
					dos.write(buf,0,read);
					dos.flush();
					passedLength +=read;
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
	
	
	/**向地址为ip的设备请求文件*/
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
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				si.releaseTag();
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
					System.out.println("send makedir----path is "+ relativePath);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				si.releaseTag();
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
	
	
	
	/*
	private void receiveFile(String line){
		try{
			boolean fileExist = false;
			String savePath = "/sdcard/";
			int passedLength = 0;
			int size = Integer.parseInt(line.substring(line.indexOf("$SIZE$")+6,line.indexOf("$NAME$")));
			String filename = line.substring(line.indexOf("$NAME$")+6,line.length()-1);
			File file = new File(savePath+filename);
			File file2 = new File(savePath+filename+".1");
			DataOutputStream fileOut;
			if(!file.exists()) {
				file.createNewFile();
				fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
				}
			else {
				fileExist = true;
			//file2 = new File(savePath+filename+".1");
				file2.createNewFile();
				fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file2)));
			}
			int bufferSize = 8192;
			byte[] buf = new byte[bufferSize];
			//DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))); 
			while(true){
				int read = 0;
				if (dis != null){
					try{
						read = dis.read(buf);
					}catch(IOException e){
						e.printStackTrace();
					}

					if(read != -1){
						passedLength +=read;
						try{
							fileOut.write(buf,0,read);
							fileOut.flush();
							System.out.println("write file---------------");
						}catch(IOException e){
							e.printStackTrace();
						}
					}
					if(passedLength >= size) break;
				}
				}
				fileOut.close();				//文件接受完成
				/*
				Message msg = new Message();
				msg.what = AndroidSmsConstant.FILE_RECEIVED_TEST;
				handler.sendMessage(msg);
				if(fileExist){												//需要比较两个文件是否相同
					if(getFileMD5(file).equals(getFileMD5(file2))){
						file2.delete();
						Message msg1 = new Message();
						msg1.what = AndroidSmsConstant.FILE_SAME_TEST;
						handler.sendMessage(msg1);
					}
					else{
						file.delete();
						file2.renameTo(file);
			
						Message msg1 = new Message();
						msg1.what = AndroidSmsConstant.FILE_DIF_TEST;
						handler.sendMessage(msg1);
					}
				}
		
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	*/
	
	
}
