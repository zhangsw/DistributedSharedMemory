package android_programe.Consistency;

import java.util.ArrayList;
import java.util.List;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android_programe.Util.FileConstant;
import android_programe.Util.FileUtil;

public class ShareInfo extends Thread{
	private String sharedFilePath;
	private String target;
	private int type;		//�������ͣ���ʱδsimple
	private ConsistencyRule conRule;
	private List fileList;
	private FileObserver fo;
	private Handler handlerFa;
	private IFileObserverManager fom;
	private Handler handlerCh;
	private Thread t;
	public ShareInfo(){
		
	}
	
	public ShareInfo(String sharedFilePath,String target,int type,Handler handler,IFileObserverManager fom){
		this.sharedFilePath = sharedFilePath;
		this.target = target;
		this.type = type;
		this.fom = fom;
		
		fileList =new ArrayList<FileInf>();
		FileUtil.getFileInfList(sharedFilePath,fileList);
		handlerFa = handler;
		
		switch(type){
		case FileConstant.SIMPLECM:{
			conRule = new SimpleConsistencyRule();
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
	
	
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		Looper.prepare();
		
		handlerCh = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				String obj = msg.obj.toString();
				switch(msg.what){
				case IEventTranslate.FILEMODIFIED:				//�ļ����޸�
				case IEventTranslate.FILEMOVETO:{					//�ļ�����
					if(conRule.sendFile(obj)){
						String relativePath = obj.substring(FileConstant.ROOTPATH.length());
						fileModifiedMessage(target,FileConstant.SENDFILEMESSAGE,obj,relativePath,handlerFa);
					}	
				}break;
				
				case IEventTranslate.FILEDELETE:				//�ļ�ɾ��
				case IEventTranslate.FILEMOVEFROM:{				//�ļ�����
					if(conRule.deleteFile(obj)){
						String relativePath = obj.substring(FileConstant.ROOTPATH.length());
						deleteMessage(target,FileConstant.DELETEFILEMESSAGE,relativePath,handlerFa);
					}
				}break;
				
				case IEventTranslate.FILERENAMEORMOVE:{			//�ļ������������ƶ�
					int index = obj.indexOf("$/@@/$");
					String oldPath = obj.substring(0, index);
					String newPath = obj.substring(index+6);
					if(getFatherPath(oldPath).equals(getFatherPath(newPath)) && conRule.renameFile(oldPath,newPath)){
						renameOrMoveMessage(target,FileConstant.RENAMEFILEMESSAGE,oldPath.substring(FileConstant.ROOTPATH.length()),newPath.substring(FileConstant.ROOTPATH.length()),handlerFa);
					}
					else if(!getFatherPath(oldPath).equals(getFatherPath(newPath)) && conRule.moveFile(oldPath,newPath))
						renameOrMoveMessage(target,FileConstant.MOVEFILEMESSAGE,oldPath.substring(FileConstant.ROOTPATH.length()),newPath.substring(FileConstant.ROOTPATH.length()),handlerFa);
					/*
					else if(isSubDirectory(oldPath) && conRule.deleteFile(oldPath)){
						
						deleteMessage(target,FileConstant.DELETEFILEMESSAGE,oldPath.substring(FileConstant.ROOTPATH.length()),handlerFa);
					}
					else if(isSubDirectory(newPath) && conRule.sendFile(newPath))
						fileModifiedMessage(target,FileConstant.SENDFILEMESSAGE,newPath,newPath.substring(FileConstant.ROOTPATH.length()),handlerFa);
						*/
				}break;
				
				case IEventTranslate.DIRCREATE:{			//�ļ��д���
					if(conRule.createDirectory(obj)){
						String relativePath = obj.substring(FileConstant.ROOTPATH.length());
						createDirMessage(target,FileConstant.CREATEDIRMESSAGE,obj,relativePath,handlerFa);
					}
				}break;
				
				case IEventTranslate.DIRDELETE:				//�ļ���ɾ��
				case IEventTranslate.DIRMOVEFROM:{			//�ļ����Ƴ�
					if(conRule.deleteDirectory(obj)){
						String relativePath = obj.substring(FileConstant.ROOTPATH.length());
						deleteMessage(target,FileConstant.DELETEDIRMESSAGE,relativePath,handlerFa);
					}
				}break;
				
				case IEventTranslate.DIRMOVETO:{			//�ļ�������
					if(conRule.sendDirectory(obj)){
						String relativePath = obj.substring(FileConstant.ROOTPATH.length());
						dirModifiedMessage(target,FileConstant.CREATEDIRMESSAGE,obj,relativePath,handlerFa);
					}
				}break;
				
				case IEventTranslate.DIRRENAMEORMOVE:{			//�ļ������������ƶ�
					int index = obj.indexOf("$/@@/$");
					String oldPath = obj.substring(0, index);
					String newPath = obj.substring(index+6);
					if(getFatherPath(oldPath).equals(getFatherPath(newPath)) && conRule.renameDirectory(oldPath,newPath)){
						renameOrMoveMessage(target,FileConstant.RENAMEDIRMESSAGE,oldPath.substring(FileConstant.ROOTPATH.length()),newPath.substring(FileConstant.ROOTPATH.length()),handlerFa);
					}
					else if(conRule.moveDirectory(oldPath,newPath))
						renameOrMoveMessage(target,FileConstant.MOVEDIRMESSAGE,oldPath.substring(FileConstant.ROOTPATH.length()),newPath.substring(FileConstant.ROOTPATH.length()),handlerFa);
					/*
					else if(isSubDirectory(oldPath) && conRule.deleteDirectory(oldPath))
						deleteMessage(target,FileConstant.DELETEDIRMESSAGE,oldPath.substring(FileConstant.ROOTPATH.length()),handlerFa);
					else if(isSubDirectory(newPath) && conRule.sendDirectory(newPath))
						dirModifiedMessage(target,FileConstant.CREATEDIRMESSAGE,newPath,newPath.substring(FileConstant.ROOTPATH.length()),handlerFa);
						*/
				}break;
				}
				
			}
			
		};
		
		fom.registerObserver(target, sharedFilePath, handlerCh, null);
		Looper.loop();
		
		
	}
	
	private boolean isSubDirectory(String path){
		if(path.startsWith(sharedFilePath + "/"))
			return true;
		else return false;
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
		Message m1 = handler.obtainMessage();
		MessageObj temp = new MessageObj(type);
		temp.setTarget(target);
		temp.setRelativeFilepath(relativeFilePath);
		temp.setNewRelativeFilepath(newRelativeFilePath);
		m1.obj = temp;
		handler.sendMessage(m1);
	}
	
	private void createDirMessage(String target,int type,String absoluteDirPath,String relativeDirPath,Handler handler){
		dirModifiedMessage(target,type,absoluteDirPath,relativeDirPath,handler);
	}
	
	private void dirModifiedMessage(String target,int type,String absoluteDirPath,String relativeDirPath,Handler handler){
		fileModifiedMessage(target,type,absoluteDirPath,relativeDirPath,handler);
	}
	
	private void deleteMessage(String target,int type,String relativeFilePath,Handler handler){
		Message m1 = handler.obtainMessage();
		MessageObj temp = new MessageObj(type);
		temp.setTarget(target);
		temp.setRelativeFilepath(relativeFilePath);
		m1.obj = temp;
		handler.sendMessage(m1);	
	}
	
	private String getFatherPath(String path){
		int index  = path.lastIndexOf("/");
		return path.substring(0, index);
	}
	



	/*
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
						}
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
								while(!pmf.isEmpty());
								fileModifiedMessage(target,FileConstant.SENDFILEMESSAGE,path.substring(0, path.lastIndexOf("$TIME$")),newPath,handlerFa);
								arg1 = null;
							}
							else{
								
								synchronized(lock){
									pmf.init();
								}
								
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
		
		
	}*/
}
