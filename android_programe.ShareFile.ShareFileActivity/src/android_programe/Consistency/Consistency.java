package android_programe.Consistency;

import java.io.File;
import java.io.IOException;
import java.util.*;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android_programe.LogLine.LogLine;
import android_programe.Util.FileConstant;
import android_programe.Util.FileUtil;

public class Consistency {

	private  LogLine logLine; 
	private String filePath;
	private boolean tag;
	private List fileList;
	private List shareInfList;
	private Thread fileTranManager;
	private Handler handler;
	private FileObserverManager fom;
	
	public Consistency() throws IOException{
		System.out.println("enter Consistency()");
		tag = true;
		shareInfList = new ArrayList<ShareInfo>();
		logLine = new LogLine(this);
		System.out.println("logline has been created");
		fileTranManager = new FileTranManager();
		fileTranManager.start();
		System.out.println("filetranManager has been created");
		fom = new FileObserverManager();
		fom.start();
		System.out.println("Consistency has been created");
		
		//测试用
		//fo = new SDFileObserver("/sdcard/wallpaper",handler);
		//fo.startWatching();
		//ShareInf si = new ShareInf("/sdcard/wallpaper","Dell",FileConstant.SIMPLECM,handler);
		
	}
	
	public Consistency(String path,List fileList) throws IOException{
		//filePath = "/d";			//初始化filePath，即为监听的文件路径
		filePath = path;
		this.fileList = fileList;
		//logLine.connect("114.212.87.66");
		//System.out.println("connecting");
		tag = true;
		logLine = new LogLine(this);
		fom = new FileObserverManager();
		System.out.println(fileList.size());
		//SimpleCM();
	}
	
	private void deleteLocalFile(String filepath){
		System.out.println("enter consistency deletefile, path is " + filepath);
		File file = new File(filepath);
		if(file.exists()){
			if(file.isDirectory()){
				File []child = file.listFiles();
				for(int i=0;i<child.length;i++){
					deleteLocalFile(child[i].getAbsolutePath());
				}
				file.delete();
			}
			else
				file.delete();
				
		}
		
	}
	
	public boolean connect(String ip) throws IOException{
		return logLine.connect(ip);
	}
	
	private void sendFileInf(String target,String relativePath,String MD5){
		logLine.sendFileInf(target, relativePath, MD5);
	}
	
	public boolean receiveDeletaFile(String target, String filepath){					//删除文件需要判断是文件还是文件夹，同时需要考虑是否还有其他线程在使用该文件夹下的文件
		//TODO
		
		deleteLocalFile(filepath);
		return true;
	}
	
	public boolean receiveFileInf(String target,String relativePath,String absolutePath,String MD5){
		int index = getIndexByName(target);
		System.out.println("enter consistency receiveFileInf---------");
		if(index != -1){
			System.out.println("exist this device--------");
			String MD5OfLocalFile = FileUtil.getFileMD5(new File(absolutePath));
			System.out.println("receivefile md5 is " + MD5 + " , local is " + MD5OfLocalFile);
			if(MD5.equals(MD5OfLocalFile)){			//存在该文件
				System.out.println("file has been existed-----");
				return false;
			}
			else{											//不存在文件，接收该文件
				//TODO
				System.out.println("want to receive file---"+relativePath);
				fetchFile(target,relativePath);
				return true;
			}
		}
		return false;
	}
	
	public boolean receiveAskFile(String target, String relativePath, String absolutePath) {
		// TODO Auto-generated method stub
		int index = getIndexByName(target);
		System.out.println("enter");
		if(index != -1){
			File file = new File(absolutePath);
			if(file.exists()){				//存在请求的文件，可以进行发送
				//TODO
				sendFile(target,absolutePath,relativePath);			//文件路径？？？
				return true;
			}
			else{										//不存在请求的文件，拒绝发送
				return false;
			}
		}
		return false;
	}
	
	public boolean receiveRenameFile(String target, String oldPath, String newPath) {
		// TODO Auto-generated method stub
		File file = new File(oldPath);
		if(file.exists()) return file.renameTo(new File(newPath));
		else return false;
	}
	
	public boolean receiveMakeDir(String target, String absolutePath){
		System.out.println("enter consistency receiveMakeDir----path is "+ absolutePath);
		File file = new File(absolutePath);
		if(!file.exists()) return file.mkdir();
		else return false;
	}
	 
	
	
	private void sendFile(String target,String absolutePath,String relativeFilePath){
		logLine.sendFile(target, absolutePath, relativeFilePath);
	}
	
	private void deleteFile(String target,String relativeFilePath){
		logLine.deleteFile(target, relativeFilePath);
	}
	
	private void renameFile(String target,String relativeFilePath,String newRelativeFilePath){
		logLine.renameFile(target,relativeFilePath,newRelativeFilePath);
	}
	
	private void moveFile(String target,String relativeFilePath,String newRelativeFilePath){
		renameFile(target,relativeFilePath,newRelativeFilePath);
	}
	
	private void renameDir(String target,String relativeFilePath,String newRelativeFilePath){
		renameFile(target,relativeFilePath,newRelativeFilePath);
	}
	
	private void moveDir(String target,String relativeFilePath,String newRelativeFilePath){
		renameFile(target,relativeFilePath,newRelativeFilePath);
	}
	
	private void sendDir(String target,String absolutePath,String relativePath){
		File file = new File(absolutePath);
		if(file.exists() && file.isDirectory()){
			makeDir(target,relativePath);
			System.out.println("senddir------relativePath is "+relativePath);
			File []child = file.listFiles();
			for(int i=0;i<child.length;i++){
				String ab = child[i].getAbsolutePath();
				String re = relativePath + ab.substring(absolutePath.length());
				if(child[i].isDirectory()) sendDir(target,ab,re);
				else sendFile(target,ab,re);
			}
		}
	}
	
	private void makeDir(String target,String relativePath){
		logLine.makeDir(target, relativePath);
	}
	
	
	private void sendFileEve(String filePath,String fileName){
		logLine.sendFileEve(filePath,fileName);
	}
	
	private void fetchFile(String target,String relativePath){
		logLine.fetchFile(target,relativePath);
	}
	
	private int getIndexByName(String target){
		int i = 0;
		for(;i<shareInfList.size();i++){
			if(((ShareInfo)(shareInfList.get(i))).getTarget() == target)
				return i;
		}
		return -1;
	}
	
	public synchronized void addShareDevice(String sharedFilePath,String target,int type){
		System.out.println("SharedFilePath is"+sharedFilePath+",target is "+target);
		ShareInfo si = new ShareInfo(sharedFilePath,target,type,handler,fom);
		

		System.out.println("enter Consistency addShareDevice-----");
		//TODO
		if(!shareInfList.contains(si)){						//不能如此简单的判断！！！
			shareInfList.add(si);
			si.start();
		}
	}
	
	
	
	/*
	public void SimpleCM(){				//当发现文件被修改后，立即对所有与本地连接的用户发送被修改后的文件。
		new Thread(){
			public void run(){
				while(tag){
					try {
						List temp = new ArrayList<FileInf>();
						sleep(5000);				//设定为每隔5秒进行一次查询文件是否变化
						FileUtil.getFileInfList(filePath, temp);
						System.out.println(temp.size());
						for(int i=0;i<temp.size();i++){
							FileInf fi = (FileInf)(temp.get(i));
							if(fileList.contains(fi) == false){			//说明这个文件时原来文件列表中没有的或者被修改的
								//sendFileEve(fi.getFilePath(),fi.getFileName());
								System.out.println(i+"changed");
								sendFile("114.212.87.66",fi.getFilePath(),fi.getFileName());
							}
						}
						if(temp.size()!=fileList.size())
							System.out.println("file changed");
						fileList = temp;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	*/
	
	private class FileTranManager extends Thread{
		private boolean tag;
		
		public FileTranManager(){
			tag = true;
			
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			Looper.prepare();
			handler = new Handler(){									//用于发送信息或者消息，用一个统一的handler发送，就可以避免出现同时占用数据发送端的情况

				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					super.handleMessage(msg);
					MessageObj mo = (MessageObj)msg.obj;
					switch(mo.getType()){
					case FileConstant.SENDFILEMESSAGE:{											//发送文件数据,其实是发送FileInf
						
						System.out.println("-----sendfilemessage------relativeFilePath is " + mo.getRelativeFilepath());
						String MD5 = FileUtil.getFileMD5(new File(mo.getFilepath()));
						sendFileInf(mo.getTarget(),mo.getRelativeFilepath(),MD5);
						sendFile(mo.getTarget(),mo.getFilepath(),mo.getRelativeFilepath());
						
					}break;
					
					case FileConstant.DELETEFILEMESSAGE:{									//删除文件
						System.out.println("-----deletefilemessage----");
						deleteFile(mo.getTarget(),mo.getRelativeFilepath());
						
					}break;
					
					case FileConstant.RENAMEFILEMESSAGE:{								//重命名文件
						System.out.println("-----renamefilemessage----");
						renameFile(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
					}break;
					
					case FileConstant.MOVEFILEMESSAGE:{									//移动文件,同重命名文件
						System.out.println("----movefilemessage-------");
						moveFile(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
					}break;
					
					case FileConstant.CREATEDIRMESSAGE:{						//创建文件夹
						System.out.println("----createDirMessage------");
						sendDir(mo.getTarget(),mo.getFilepath(),mo.getRelativeFilepath());
					}break;
					
					case FileConstant.DELETEDIRMESSAGE:{						//删除文件夹
						System.out.println("---deleteDirMessage-------");
						deleteFile(mo.getTarget(),mo.getRelativeFilepath());
					}break;
					
					case FileConstant.MOVEDIRMESSAGE:{							//移动文件夹
						System.out.println("---moveDirMessage---------");
						moveDir(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
					}break;
					
					case FileConstant.RENAMEDIRMESSAGE:{						//重命名文件夹
						System.out.println("---renameDirMessage-------");
						renameDir(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
					}break;
					}
				}
				
			};
			
			Looper.loop();
		}
		
		
	}


	
	

}
