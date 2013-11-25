package android_programe.Consistency;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android_programe.Util.FileConstant;
import android_programe.Util.FileUtil;

public class ShareInf{
	private String sharedFilePath;
	private String target;
	private int type;		//�������ͣ���ʱδsimple
	private List fileList;
	private FileObserver fo;
	private Handler handlerFa;
	private IFileObserverManager fom;
	//private Handler handlerCh;
	private Thread t;
	public ShareInf(){
		
	}
	
	public ShareInf(String sharedFilePath,String target,int type,Handler handler,IFileObserverManager fom){
		this.sharedFilePath = sharedFilePath;
		this.target = target;
		this.type = type;
		this.fom = fom;
		
		fileList =new ArrayList<FileInf>();
		FileUtil.getFileInfList(sharedFilePath,fileList);
		handlerFa = handler;
		
		switch(type){
		case FileConstant.SIMPLECM:{
			t = new SimpleCM();
			t.start();
			System.out.println("simpleCm has been started----");
		}break;
		}		
		if(handlerFa == null)
			System.out.println("handlerFa is null------");
		else System.out.println("handlerFa is not null-----");
	}
	
	public String getSharedFilePath(){
		return sharedFilePath;
	}
	
	public String getTarget(){
		return target;
	}
	
	public int getType(){
		return type;
	}
	
	public void setSharedFilePath(String sharedFilePath){
		this.sharedFilePath = sharedFilePath;
	}
	
	public void setTarget(String target){
		this.target = target;
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public List getFileList(){
		return fileList;
	}
	
	public void setFileList(List fileList){
		this.fileList = fileList;
	}
	
	public boolean IsContainFile(String filename,String MD5){
		System.out.println("fileList's size is "+fileList.size());
		System.out.println(filename + "___" + MD5);
		for(int i=0;i<fileList.size();i++){
			FileInf fi = (FileInf)fileList.get(i);
			if((fi.getFileName().equals(filename)) && (fi.getFileMD5().equals(MD5)))
				return true;
		}
		return false;
	}
	
	public boolean IsContainFile(String filename){
		for(int i=0;i<fileList.size();i++){
			FileInf fi = (FileInf)fileList.get(i);
			if(fi.getFileName().equals(filename))
				return true;
		}
		return false;
	}
	
	/*
	private class ProcessMoveFrom extends Thread{
		String lock;
		String relativePath;
		long time;
		Handler handler;
		boolean tag;
		
		public ProcessMoveFrom(Handler handler,String lock){
			this.handler = handler;
			this.lock = lock;
			tag = false;
		}
		
		public void set(String relativePath,long time){
			this.relativePath = relativePath;
			this.time = time;
			tag = false;
		}
		
		private void deleteMessage(String target,int type,String relativeFilePath,Handler handler){
			Message m1 = handler.obtainMessage();
			MessageObj temp = new MessageObj(type);
			temp.setTarget(target);
			temp.setRelativeFilepath(relativeFilePath);
			m1.obj = temp;
			handler.sendMessage(m1);	
		}
		
		public void init(){
			relativePath = null;
			time = 0;
			tag = false;
		}
		
		public void push(){
			tag = true;
		}
		
		public boolean isEmpty(){
			return (relativePath == null);
		}
		
		public void run() {
			// TODO Auto-generated method stub
			try {
				while(true){
					synchronized(lock){
					if(relativePath == null)
						lock.wait();
					if(relativePath != null){
						Date date = new Date();
						if(tag || ((date.getTime() - time) >3000)){
							System.out.println("difference is " + (date.getTime()-time));
							deleteMessage(target,FileConstant.DELETEFILEMESSAGE,relativePath,handler);
							init();
						}
					}
					}
					
				}
			}catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}
	}
	

	*/
	private class SimpleCM extends Thread{
		private boolean isInterrupted = false;
		private String arg1;
		private String arg2;
		private long oldTime;
		private long newTime;
		private Handler handlerCh;
		//private ProcessMoveFrom pmf;
		private String lock = "lock";
		public SimpleCM(){
		}
		
		private void sendMessage(int type){
			Message m1 = handlerFa.obtainMessage();
			MessageObj temp = new MessageObj(type);
			temp.setTarget(target);
		}
		
		private void deleteMessage(String target,int type,String relativeFilePath,Handler handler){
			Message m1 = handler.obtainMessage();
			MessageObj temp = new MessageObj(type);
			temp.setTarget(target);
			temp.setRelativeFilepath(relativeFilePath);
			m1.obj = temp;
			handler.sendMessage(m1);	
		}
		
		private void createDirMessage(String target,int type,String absoluteDirPath,String relativeDirPath,Handler handler){
			dirModifiedMessage(target,type,absoluteDirPath,relativeDirPath,handler);
		}
		
		private void dirModifiedMessage(String target,int type,String absoluteDirPath,String relativeDirPath,Handler handler){
			fileModifiedMessage(target,type,absoluteDirPath,relativeDirPath,handler);
		}
		
		private void fileModifiedMessage(String target,int type,String absoluteFilePath,String relativeFilePath,Handler handler){
			Message m1 = handler.obtainMessage();
			MessageObj temp = new MessageObj(type);
			temp.setTarget(target);
			temp.setFilepath(absoluteFilePath);
			temp.setFileName(FileUtil.getFileNameFromPath(absoluteFilePath));
			temp.setRelativeFilepath(relativeFilePath);
			m1.obj = temp;
			handler.sendMessage(m1);
		}
		
		private void renameOrMoveMessage(String target,int type,String relativeFilePath,String newRelativeFilePath,Handler handler){
			Message m1 = handlerFa.obtainMessage();
			MessageObj temp = new MessageObj(type);
			temp.setTarget(target);
			temp.setRelativeFilepath(relativeFilePath);
			temp.setNewRelativeFilepath(newRelativeFilePath);
			m1.obj = temp;
			handlerFa.sendMessage(m1);
		}
		
		
		
		@Override
		public boolean isInterrupted() {
			// TODO Auto-generated method stub
			isInterrupted = true;
			return super.isInterrupted();
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			
			//pmf = new ProcessMoveFrom(handlerFa,lock);
			//pmf.start();
			
			Looper.prepare();
			
			handlerCh = new Handler(){

				@Override
				public void handleMessage(Message msg) {
					// TODO Auto-generated method stub
					super.handleMessage(msg);
					Message m = Message.obtain(msg);
					assert m.obj != null :"Message obj is null";
	
					String path = m.obj.toString();
					String relativePath = path.substring(FileConstant.ROOTPATH.length()); 
					
					
					if((arg1 != null) && (m.what != FileObserver.MOVED_TO)){			//ֻ��һ��movefrom,�����ļ��Ƶ�����δ������ļ�����ȥ������delete������
						/*if(!pmf.isEmpty()){
							pmf.push();	
						}*/
						arg1 = null;
					}
					
					if((arg2 !=null) && (m.what == FileObserver.MOVE_SELF || m.what == FileObserver.DELETE_SELF)){				//ֻ��һ��movefrom,���ļ������߻�ɾ����,��Ҫֹͣ�Ը��ļ��еļ���
						deleteMessage(target,FileConstant.DELETEDIRMESSAGE,arg2.substring(FileConstant.ROOTPATH.length()),handlerFa);
						fom.withdrowObserver(target, arg2);
						arg2 = null;
						
					}
					
					switch(m.what){
					case FileObserver.CLOSE_WRITE:{				//�ļ����޸�,�����ļ�����
						fileModifiedMessage(target,FileConstant.SENDFILEMESSAGE,path,relativePath,handlerFa);
						
					}break;
					case FileObserver.MOVED_FROM:{
						arg1 = relativePath.substring(0, relativePath.lastIndexOf("$TIME$"));
						oldTime = Long.parseLong(relativePath.substring(arg1.length()+6));
						System.out.println("arg1 is " + arg1);
						synchronized(lock){
							//pmf.set(arg1, oldTime);
							lock.notify();
						}
						
						
					}break;
					case FileObserver.MOVED_TO:{				//1.������,��������������;2.Ҳ�����Ǽ��в���
						String newPath = relativePath.substring(0, relativePath.lastIndexOf("$TIME$"));
						if(arg1 != null){
							newTime = Long.parseLong(relativePath.substring(newPath.length()+6));
							if((newTime - oldTime)>500){				//��Ϊmove_from��move_to���Ƕ�ͬһ���ļ��Ĳ�����delete��create
								/*if(!pmf.isEmpty()){
									pmf.push();	
								}
								while(!pmf.isEmpty());*/
								fileModifiedMessage(target,FileConstant.SENDFILEMESSAGE,path.substring(0, path.lastIndexOf("$TIME$")),newPath,handlerFa);
								arg1 = null;
							}
							else{
								/*
								synchronized(lock){
									pmf.init();
								}*/
								
								System.out.println("enter move_to pmf.init()");
								String filename1 = FileUtil.getFileNameFromPath(arg1);
								String filename2 = FileUtil.getFileNameFromPath(newPath);
								int type = 0;
								if((filename1 !=null) && filename1.equals(filename2)){ 			
									//�ļ�����ͬ��Ϊmovefile
									type = FileConstant.MOVEFILEMESSAGE;
								}
								else{
									//�ļ�����ͬ����������
									type  = FileConstant.RENAMEFILEMESSAGE;
								}
								
								renameOrMoveMessage(target,type,arg1,newPath,handlerFa);
								arg1 = null;
							}
							}
						else{
							//����close_write����
							System.out.println("only a move_to, send this file");
							fileModifiedMessage(target,FileConstant.SENDFILEMESSAGE,path.substring(0, path.lastIndexOf("$TIME$")),newPath,handlerFa);
						
						}
						
					}break;
					case FileObserver.DELETE:{					//�ļ���ɾ��
						deleteMessage(target,FileConstant.DELETEFILEMESSAGE,relativePath,handlerFa);
						
					}break;
					
					case FileObserver.CREATE|FileConstant.ISDIR:{		//�ļ��д���
						System.out.println(path + "has been created-------");
						System.out.println("create dir,path is " + path +", relativePath is "+relativePath);
						createDirMessage(target,FileConstant.CREATEDIRMESSAGE,path,relativePath,handlerFa);
						
						String fatherPath = path.substring(0, path.lastIndexOf("/"));
						System.out.println(path + "has been created------fatherpath is "+fatherPath);
						fom.registerObserver(target, path, handlerCh, fatherPath);
						System.out.println("target is "+target+ ",sharepath is "+path );
					}break;
					
					case FileObserver.DELETE|FileConstant.ISDIR:{		//�ļ���ɾ��
						
					}break;
					
					case FileObserver.MOVED_FROM|FileConstant.ISDIR:{
						arg2 = path;
					}break;
					
					case FileObserver.MOVED_TO|FileConstant.ISDIR:{
						if(arg2 != null){
							String dirname1 = FileUtil.getFileNameFromPath(arg2);
							String dirname2 = FileUtil.getFileNameFromPath(path);
							if(dirname1.equals(dirname2)){			//�ļ���������ͬ��Ϊ�ƶ��ļ���
								renameOrMoveMessage(target,FileConstant.MOVEDIRMESSAGE,arg2.substring(FileConstant.ROOTPATH.length()),relativePath,handlerFa);
								
							}
							else{				//�ļ������ֲ�ͬ����������
								renameOrMoveMessage(target,FileConstant.RENAMEDIRMESSAGE,arg2.substring(FileConstant.ROOTPATH.length()),relativePath,handlerFa);
							}
							
							fom.modifyObserverPath(arg2, path);
							arg2 = null;
						}
						else{						//��δ�����ļ�������
							dirModifiedMessage(target,FileConstant.CREATEDIRMESSAGE,path,relativePath,handlerFa);
							//TODO
							String fatherPath = path.substring(0, path.lastIndexOf("/"));
							fom.registerObserver(target, path, handlerCh, fatherPath);
							System.out.println("----");
						}
						
					}break;
					
					default:{

					}break;
					}
					
					
					
					
				}
				
			};
			fom.registerObserver(target, sharedFilePath, handlerCh, null);
			Looper.loop();
			
			
		}
		
	}
	

}
