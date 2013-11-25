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
		
		//������
		//fo = new SDFileObserver("/sdcard/wallpaper",handler);
		//fo.startWatching();
		//ShareInf si = new ShareInf("/sdcard/wallpaper","Dell",FileConstant.SIMPLECM,handler);
		
	}
	
	public Consistency(String path,List fileList) throws IOException{
		//filePath = "/d";			//��ʼ��filePath����Ϊ�������ļ�·��
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
	
	public boolean receiveDeletaFile(String target, String filepath){					//ɾ���ļ���Ҫ�ж����ļ������ļ��У�ͬʱ��Ҫ�����Ƿ��������߳���ʹ�ø��ļ����µ��ļ�
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
			if(MD5.equals(MD5OfLocalFile)){			//���ڸ��ļ�
				System.out.println("file has been existed-----");
				return false;
			}
			else{											//�������ļ������ո��ļ�
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
			if(file.exists()){				//����������ļ������Խ��з���
				//TODO
				sendFile(target,absolutePath,relativePath);			//�ļ�·��������
				return true;
			}
			else{										//������������ļ����ܾ�����
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
		if(!shareInfList.contains(si)){						//������˼򵥵��жϣ�����
			shareInfList.add(si);
			si.start();
		}
	}
	
	
	
	/*
	public void SimpleCM(){				//�������ļ����޸ĺ������������뱾�����ӵ��û����ͱ��޸ĺ���ļ���
		new Thread(){
			public void run(){
				while(tag){
					try {
						List temp = new ArrayList<FileInf>();
						sleep(5000);				//�趨Ϊÿ��5�����һ�β�ѯ�ļ��Ƿ�仯
						FileUtil.getFileInfList(filePath, temp);
						System.out.println(temp.size());
						for(int i=0;i<temp.size();i++){
							FileInf fi = (FileInf)(temp.get(i));
							if(fileList.contains(fi) == false){			//˵������ļ�ʱԭ���ļ��б���û�еĻ��߱��޸ĵ�
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
			handler = new Handler(){									//���ڷ�����Ϣ������Ϣ����һ��ͳһ��handler���ͣ��Ϳ��Ա������ͬʱռ�����ݷ��Ͷ˵����

				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					super.handleMessage(msg);
					MessageObj mo = (MessageObj)msg.obj;
					switch(mo.getType()){
					case FileConstant.SENDFILEMESSAGE:{											//�����ļ�����,��ʵ�Ƿ���FileInf
						
						System.out.println("-----sendfilemessage------relativeFilePath is " + mo.getRelativeFilepath());
						String MD5 = FileUtil.getFileMD5(new File(mo.getFilepath()));
						sendFileInf(mo.getTarget(),mo.getRelativeFilepath(),MD5);
						sendFile(mo.getTarget(),mo.getFilepath(),mo.getRelativeFilepath());
						
					}break;
					
					case FileConstant.DELETEFILEMESSAGE:{									//ɾ���ļ�
						System.out.println("-----deletefilemessage----");
						deleteFile(mo.getTarget(),mo.getRelativeFilepath());
						
					}break;
					
					case FileConstant.RENAMEFILEMESSAGE:{								//�������ļ�
						System.out.println("-----renamefilemessage----");
						renameFile(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
					}break;
					
					case FileConstant.MOVEFILEMESSAGE:{									//�ƶ��ļ�,ͬ�������ļ�
						System.out.println("----movefilemessage-------");
						moveFile(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
					}break;
					
					case FileConstant.CREATEDIRMESSAGE:{						//�����ļ���
						System.out.println("----createDirMessage------");
						sendDir(mo.getTarget(),mo.getFilepath(),mo.getRelativeFilepath());
					}break;
					
					case FileConstant.DELETEDIRMESSAGE:{						//ɾ���ļ���
						System.out.println("---deleteDirMessage-------");
						deleteFile(mo.getTarget(),mo.getRelativeFilepath());
					}break;
					
					case FileConstant.MOVEDIRMESSAGE:{							//�ƶ��ļ���
						System.out.println("---moveDirMessage---------");
						moveDir(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
					}break;
					
					case FileConstant.RENAMEDIRMESSAGE:{						//�������ļ���
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
