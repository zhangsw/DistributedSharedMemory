package android_programe.PsyLine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android_programe.Util.FileConstant;

public class Responser implements Runnable{
    private Socket socket = null;
    private String ip;
    private InputStream inputStream;
	private OutputStream outputStream;
	private String line;
	private StringBuffer sb;
	private DataInputStream dis;
	private DataOutputStream dos;
    private String savePath;
    private int bufferSize = 8192;
	private byte[] buf;
	private PsyLine psyline;
    
    public Responser(Socket socket,PsyLine psyline){
        this.socket=socket;
        this.psyline = psyline;
        ip = socket.getInetAddress().getHostAddress();
        try {
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
			dis = new DataInputStream(new BufferedInputStream(inputStream));
			//savePath = "/sdcard/wallpaper/";
			savePath = FileConstant.ROOTPATH;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    
    public void run() {
    	// TODO 
    	try{
    		while(true){
    			if(socket.isClosed()) System.out.println("socket is closed");
    			else{
    				line = dis.readUTF();
    				if(line!=null){
    					int state = Integer.parseInt(line.substring(0,2));
    					switch(state){
    					case FileConstant.FILEDATA:{
    						System.out.println("receive filedata------");
    						int passedLength = 0;
    						int size = Integer.parseInt(line.substring(line.indexOf("$SIZE$")+6,line.indexOf("$PATH$")));
    						String relativeFilePath = line.substring(line.indexOf("$PATH$")+6,line.length()-1);
    						String Path = savePath + relativeFilePath;
    						
    						File file = new File(Path);
    						File parent = file.getParentFile();
    						if(!parent.exists()) parent.mkdirs();
    						
    						System.out.println("size is " + size + "\n" + "relativeFilePath is " + relativeFilePath);
    						System.out.println("savePath is "+ Path);
    						buf = new byte[bufferSize];
    						try{
    							dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Path)));	
    						}catch(IOException e){
    							e.printStackTrace();
    						}
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
    								dos.write(buf,0,read);
    								dos.flush();
    								//System.out.println("write file---------------");
    							}catch(IOException e){
    								e.printStackTrace();
    							}
    							}
    							if(passedLength >= size) {
    								System.out.println("finished written");
    								break;
    							}
    						}
    						}
    						if(dos != null){
    							try{
    								dos.close();
    								System.out.println("dos has been closed");
    							}catch(IOException e){
    								e.printStackTrace();
    							}
    						}
    					}break;
    					
    					case FileConstant.FILEINF:{									//接收到的是文件信息
    						System.out.println("receive fileinf---------");
    						String relativePath = line.substring(line.indexOf("$PATH$")+6, line.indexOf("$MD5$"));
    						String MD5 = line.substring(line.indexOf("$MD5$")+5, line.length()-1); 
    						psyline.receiveFileInf(ip,relativePath, savePath+relativePath, MD5);
    					}break;
    					
    					case FileConstant.ASKFILE:{									//接收到的是请求文件信息
    						System.out.println("receive askfile---------");
    						String relativePath = line.substring(line.indexOf("$PATH$")+6, line.length()-1);
    						psyline.receiveAskFile(ip, relativePath, savePath+relativePath);
    					}break;
    					
    					case FileConstant.DELETEFILE:{								//接收到的是删除文件信息
    						System.out.println("receive deletefile-----");
    						String relativeFilePath = line.substring(line.indexOf("$PATH$")+6, line.length()-1);
    						psyline.receiveDeleteFile(ip, savePath+relativeFilePath);
    					}break;
    					
    					case FileConstant.RENAMEFILE:{								//收到的是重命名文件信息
    						System.out.println("receive renamefile-----");
    						String oldRelativeFilePath = line.substring(line.indexOf("$OLDPATH$")+9, line.indexOf("$NEWPATH$"));
    						String newRelativeFilePath = line.substring(line.indexOf("$NEWPATH$")+9, line.length()-1);
    						psyline.receiveRenameFile(ip,savePath+oldRelativeFilePath,savePath+newRelativeFilePath);
    					}break;
    					
    					case FileConstant.MAKEDIR:{
    						System.out.println("receive makedir--------");
    						String relativePath = line.substring(line.indexOf("$PATH$")+6,line.length()-1);
    						System.out.println("relativePath is "+relativePath);
    						psyline.receiveMakeDir(ip, savePath+relativePath);
    					}break;
    					}
    				}
    			}
    		}
    	}catch (IOException e){
    		e.printStackTrace();
    	}
    	
    }

}
