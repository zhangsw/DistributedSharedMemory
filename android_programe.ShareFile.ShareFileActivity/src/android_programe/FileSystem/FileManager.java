package android_programe.FileSystem;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android_programe.FileMonitor.AndEventTranslate;
import android_programe.FileMonitor.IEventTranslate;
import android_programe.Util.FileConstant;
import android_programe.Util.FileOperateHelper;

public class FileManager implements IFileManager {
	private HashMap<String,MyFileObserver> mObservers;
	private Handler globalMsgHandler;		//ȫ����Ϣhandler,���Ը���observer
	private HandlerThread handlerThread;		//ȫ����Ϣ��thread����Ҫ�������ṩlooper�ġ�
	
	private String localDeviceId;
	private String defaultRootPath;
	private static final String handlerThreadName = "HandlerThread";			//thread��name
	private static final String oldVersionSuffix = ".oldVersion";
	
	private boolean dispenseMsgTag;	//�Ƿ����ļ�������Ϣ��tag
	
	/**
	 * �����ļ������������ļ�����Լ��汾��Ϣ
	 * @param defaultRootPath	Ĭ��·��
	 * @param localDeviceId	�����豸id
	 */
	public FileManager(String defaultRootPath,String localDeviceId){
		mObservers = new HashMap<String,MyFileObserver>();
		handlerThread = new HandlerThread(handlerThreadName);
		handlerThread.start();		//����thread
		globalMsgHandler = new GlobalMsgHandler(handlerThread.getLooper());
		Assert.assertNotNull("globalMessageHandler is null,check it!!", globalMsgHandler);
		dispenseMsgTag = true;
		//System.out.println("before initializeObservers");
		this.localDeviceId = localDeviceId;
		this.defaultRootPath = defaultRootPath;
		//��ʼ������Ĭ��Ŀ¼���м���
		initializeObservers(defaultRootPath,localDeviceId);	
	}
	
	public void setDispenseMsgTag(boolean tag){
		dispenseMsgTag = tag;
	}
	
	/**
	 * �����յ��ļ����
	 * @param path	�ļ�·��
	 * @param fileID	�ļ�id
	 * @return
	 */
	public boolean createEmptyFileNode(String path,String fileID){
		if(path == null || mObservers.containsKey(path)) return false;
		else{
			//��ø����·��
			String fatherPath = getFatherPath(path);
			MyFileObserver fatherObserver = mObservers.get(fatherPath);
			if(fatherObserver == null){
				//�����ڸ����
				//�ȹ������ڵ�
				createEmptyFileNode(fatherPath,null);
				fatherObserver = mObservers.get(fatherPath);
			}
			//���ڸ����
			MyFileObserver m = new MyFileObserver(path,localDeviceId,globalMsgHandler,this,fatherObserver);
			fatherObserver.addChildObserver(m);
			mObservers.put(path, m);
			//TODO �ļ���id�Ƿ���Ҫ�д���֤ 
			return true;
		}
	}
	
	public boolean fileObserverExist(String path){
		return mObservers.containsKey(path);
	}
	
	
	/**
	 * �����ļ���metaData
	 * @param path	�ļ�·��
	 * @param metaData	�ļ���metaData
	 */
	public boolean updateMetaData(String path,FileMetaData metaData){
		if(mObservers.containsKey(path)){	//�����ļ����
			mObservers.get(path).setFileMetaData(metaData);
			return true;
		}
		else return false;
	}
	
	public boolean updateVectorClock(String path,String deviceId,Integer versionNumber){
		if(mObservers.containsKey(path)){	//�����ļ����
			//System.out.println("----FileManager----updateVectorClock:observer exists");
			mObservers.get(path).updateVectorClock(deviceId, versionNumber);
			return true;
		}else return false;
	}
	
	public boolean updateVectorClock(String path,VectorClock VectorClock){
		if(mObservers.containsKey(path)){
			mObservers.get(path).updateVectorClock(VectorClock);
			return true;
		}else return false;
	}
	
	
	private void updateVersion(MyFileObserver ob,String deviceId){
		ob.fileModified(deviceId);
		
		System.out.println(ob.getPath() + " has update its local version,version number is " + ob.getFileVersion());
	}
	
	public boolean updateLocalVersion(String path,int versionNumber){
		return updateVectorClock(path,localDeviceId,versionNumber);
	}
	
	/**
	 * �ƶ��ļ�
	 * @param oldPath �ɵ�·��
	 * @param newPath �µ�·��
	 */
	public void moveFile(String oldPath,String newPath){
		modifyObserverPath(oldPath,newPath);
		FileOperateHelper.renameFile(oldPath, newPath);
	}
	
	/**
	 * ���յ�����Զ�˵��������������ڱ��ؽ�������������Ҫͬ���ز���������
	 * @param oldPath
	 * @param newPath
	 */
	public boolean remoteMoveFile(String oldPath,String newPath){
		if(FileOperateHelper.fileExist(oldPath)){
			FileOperateHelper.renameFile(oldPath, oldPath + oldVersionSuffix);
			FileOperateHelper.renameFile(oldPath + oldVersionSuffix, newPath);
			modifyObserverPath(oldPath,newPath);
			return true;
		}
		else return false;
	}
	
	public void remoteDeleteFile(String path){
		if(FileOperateHelper.fileExist(path)){
			//�Ƚ��ļ�������Ϊ (�ļ���+��oldVersion)����ʽ
			FileOperateHelper.renameFile(path, path+oldVersionSuffix);
			System.out.println("----FileManager----has rename to oldversion");
			//ɾ���ļ�
			FileOperateHelper.deleteFile(path + oldVersionSuffix);
			System.out.println("----FileManager---has deleted oldversion");
			deleteObserver(path);
		}
	}
	
	public void startObserverFile(String path){
		if(mObservers.containsKey(path))
			mObservers.get(path).startWatching();
	}
	
	public void setLocalDeviceId(String localDeviceId){
		this.localDeviceId = localDeviceId;
		//���������ļ�����е�localDeviceId�����ݿ���ļ��е�localDeviceId�����߽�localDeviceId���ó�ȫ�ֿɼ���
		//TODO
	}
	
	/**
	 * �ļ��Ǿɰ汾�����°���ļ������ɰ汾�ļ�ɾ��.
	 * @param ob
	 */
	public void deleteOldFile(MyFileObserver ob) {
		// TODO Auto-generated method stub
		String path = ob.getPath();
		if(FileOperateHelper.fileExist(path)){
			//�Ƚ��ļ�������Ϊ (�ļ���+��oldVersion)����ʽ
			FileOperateHelper.renameFile(path, path+oldVersionSuffix);
			//ɾ���ļ�
			FileOperateHelper.deleteFile(path + oldVersionSuffix);
			//deleteObserver(path);
		}
	}
	
	/**
	 * �ļ��Ǿɰ汾�����°���ļ������ɰ汾�ļ�ɾ��.
	 * @param ob
	 */
	
	public void deleteOldFile(String path){
		if(FileOperateHelper.fileExist(path)){
			//�Ƚ��ļ�������Ϊ (�ļ���+��oldVersion)����ʽ
			FileOperateHelper.renameFile(path, path+oldVersionSuffix);
			//ɾ���ļ�
			FileOperateHelper.deleteFile(path + oldVersionSuffix);
			//deleteObserver(path);
		}
	}
	
	//��ʼ��file tree
	private void initializeObservers(String path,String localDeviceId){
		registerObserver(localDeviceId,path);
	}
	
	private MyFileObserver registerObserver(String localDeviceId,String absolutePath){
		String fatherPath = null;
		return registerObserver(localDeviceId,absolutePath,fatherPath);
	}
	
	private MyFileObserver registerObserver(String localDeviceId,String absolutePath,String fatherPath){
		MyFileObserver observer = mObservers.get(absolutePath);
		if(observer != null){	//observer��Ϊnull�����Ѿ����ڼ�ص�observer
			//TODO
			return observer;
		}
		//Assert.assertNull("observer exists when first initialize the observer tree,check it",observer);
		
		if(fatherPath != null){
			//System.out.println("-----FileManager-----fatherPath is" + fatherPath);
			MyFileObserver fatherObserver = mObservers.get(fatherPath);
			observer = new MyFileObserver(absolutePath,localDeviceId,globalMsgHandler,this,fatherObserver);
			fatherObserver.addChildObserver(observer);
			}
		else
			observer = new MyFileObserver(absolutePath,localDeviceId,globalMsgHandler,this,null);
		mObservers.put(absolutePath, observer);
		if(FileOperateHelper.isDirectory(absolutePath)){
			//���ļ��У������ļ��ݹ�
			File []files = FileOperateHelper.subFiles(absolutePath);
			for(File f:files){
				registerObserver(localDeviceId,f.getAbsolutePath(),f.getParent());
			}
		}
		return observer;
	}
	
	private MyFileObserver registerObserverNoRecursion(String localDeviceId,String absolutePath,String fatherPath){
		MyFileObserver observer = mObservers.get(absolutePath);
		Assert.assertNull("observer exists when first initialize the observer tree,check it",observer);
		
		if(fatherPath != null)
			observer = new MyFileObserver(absolutePath,localDeviceId,globalMsgHandler,this,mObservers.get(fatherPath));
		else
			observer = new MyFileObserver(absolutePath,localDeviceId,globalMsgHandler,this,null);
		mObservers.put(absolutePath, observer);
		return observer;
	}
	
	/**
	 * ��ȡ�ļ���VectorClock
	 * @param fileID �ļ���id
	 * @return
	 */
	public VectorClock getVectorClock(String path){
		if(mObservers.containsKey(path))
			return mObservers.get(path).getVectorClock();
		else return null;
	}
	
	public int getLocalVersionNumber(String path){
		if(mObservers.containsKey(path))
			return mObservers.get(path).getVersionNumber(localDeviceId);
		else return -1;
	}
	
	public FileMetaData getFileMetaData(String path){
		if(mObservers.containsKey(path))
			return mObservers.get(path).getFileMetaData();
		else return null;
	}
	
	/**
	 * �����������ļ�
	 * @param oldRelativePath
	 * @param newRelativePath
	 */
	public boolean renameLocalFile(String oldRelativePath,String newRelativePath){
		System.out.println("----FileManager----enter renameLocalFile,oldpath is: " +defaultRootPath + oldRelativePath + ";newPath is:" + defaultRootPath + newRelativePath);
		boolean result =  FileOperateHelper.renameFile(defaultRootPath + oldRelativePath, defaultRootPath + newRelativePath);
		if(result) System.out.println("----FileManager----rename successful");
		return result;
	}
	
	public MyFileObserver registerObserver(Handler handler,String target,String absolutePath){
		return registerObserver(target,absolutePath,handler,null);
	}
	
	/**	
	 * ע��observer
	 * @param path: 
	 * @param target: 
	 * */
	public MyFileObserver registerObserver(String target,String absolutePath,Handler handler,String fatherPath){
		MyFileObserver observer= mObservers.get(absolutePath);
		if(observer != null){		//�Ѿ����ڼ��Ӹ�·����observer
			observer.addTarget(target,handler);
			if((fatherPath != null) && (!observer.hasFather()))
					observer.setFather(mObservers.get(fatherPath));
		}
		else{									//�����ڼ��Ӹ�·����observer����Ӧ�÷�������
			/*
			if(fatherPath != null)
				observer = new MyFileObserver(absolutePath,target,handler,globalMsgHandler,this,mObservers.get(fatherPath));
			else
				observer = new MyFileObserver(absolutePath,target,handler,globalMsgHandler,this,null);
			mObservers.put(absolutePath, observer);*/
		}
		File file = new File(absolutePath);
		if(file.isDirectory()){
			File []files = file.listFiles();
			for(File f:files){		//�����ļ��ݹ����ע��
				if(f.isDirectory())
					System.out.println(f.getAbsolutePath()+"------"+f.getParent());
				observer.addChildObserver(registerObserver(target,f.getAbsolutePath(),handler,f.getParent()));
			}
		}
		return observer;
	}
	
	/**
	 * ע��observer
	 * @param target
	 * @param path
	 */
	public void withdrowObserver(String target,String path){
		MyFileObserver observer = mObservers.get(path);
		moveTarget(target,observer);
	}
	
	public void deleteObserver(String path) {
		// TODO Auto-generated method stub
		MyFileObserver observer = mObservers.get(path);
		if(observer != null){
			observer.stopWatching();
			moveObserver(observer);
		}
	}
	
	public void updateObserverMap(String path,String newPath){
		MyFileObserver observer = mObservers.get(path);
		if(observer != null){
			mObservers.remove(path);
			MyFileObserver newObserver = mObservers.get(newPath);
			if(newObserver == null) mObservers.put(newPath, observer);
			else{
				//TODO
				HashMap<String,Handler> map = observer.getTargetsAll();
				Iterator<Entry<String, Handler>> iter = map.entrySet().iterator();
				while(iter.hasNext()){
					Map.Entry<String,Handler> entry =(Map.Entry<String,Handler>)iter.next();
					newObserver.addTarget(entry.getKey(), entry.getValue());
				}
			}
		}
	}
	
	/**
	 * �޸�observer��·�������������Լ��ƶ��ļ�ʱ����
	 */
	public void modifyObserverPath(String path,String newPath){
		MyFileObserver observer = mObservers.get(path);
		if(observer != null){
			//��mObservers��ɾ��·��Ϊpath��observer
			String parentPath = path.substring(0, path.lastIndexOf("/"));
			String newParentPath = newPath.substring(0, newPath.lastIndexOf("/"));
			if(parentPath.equals(newParentPath)){				//��Ŀ¼��ͬ��Ϊ������
				observer.modifyPath(newPath);
				System.out.println("----FileManager----observer has modifyPath,newPath is : " + newPath);
			}
			else{								//��Ŀ¼��ͬ�����ṹ�����仯
				MyFileObserver parent = mObservers.get(parentPath);
				if(parent != null){
					parent.deleteChildObserver(observer);
					observer.setFather(null);
				}
				MyFileObserver newParent = mObservers.get(newParentPath);
				if(newParent != null){
					observer.setFather(newParent);
					newParent.addChildObserver(observer);
				}
				observer.modifyPath(newPath);
			}
			//observer.startWatching();
		}
	}
	
	private void moveObserver(String path){			//��·��Ϊpath��observer������Ӽ�����Ƴ�
		MyFileObserver observer = mObservers.get(path);
		moveObserver(observer);
	}
	
	private void moveObserver(MyFileObserver observer){
		if(observer != null){
			Iterator<MyFileObserver> iter = observer.getChildAll().iterator();
			while(iter.hasNext()){
				MyFileObserver o = iter.next();
				moveObserver(o);
			}
			MyFileObserver father = observer.getFather();
			if(father != null){
				father.deleteChildObserver(observer);	
			}
			mObservers.remove(observer.getPath());
		}
	}
	
	private void moveTarget(String target,MyFileObserver observer){
		if(observer != null){
			observer.deleteTarget(target);
			if(observer.hasChild()){
				List <MyFileObserver> list = observer.getChildAll();
				for(MyFileObserver fo:list){
					moveTarget(target,fo);
				}
			}
			/*
			if(!observer.hasTarget()){
				MyFileObserver father = observer.getFather();
				if(father != null){
					father.deleteChildObserver(observer);
				}
				if(observer.hasChild()){
					List <MyFileObserver> list = observer.getChildAll();
					for(MyFileObserver fo:list){
						fo.setFather(null);
						moveTarget(target,fo);
					}
				}
				observer.stopWatching();
				mObservers.remove(observer.getPath());	
			}*/
		}
		
	}
	
	public MyFileObserver getMyFileObserver(String path){
		return mObservers.get(path);
	}
	
	//�����µ��ļ�����·��Ϊpath
	private boolean createFile(String path){
		MyFileObserver observer = mObservers.get(path);
		if(observer != null){
			//�Ѿ����ڸ��ļ�������ʧ��
			return false;
		}else{
			registerObserver(localDeviceId,path,getFatherPath(path));
			return true;
		}
	}
	
	private void createDir(String path) {
		// TODO Auto-generated method stub
		registerObserverNoRecursion(localDeviceId,path,getFatherPath(path));
	}
	
	private void addDirectory(String path,MyFileObserver fatherObserver){
		registerObserver(localDeviceId,path,fatherObserver.getPath());
		/*
		MyFileObserver observer = new MyFileObserver(path,localDeviceId,globalMsgHandler,this,fatherObserver);
		mObservers.put(path, observer);
		File file = new File(path);
		if(file.isDirectory()){
			File []files = file.listFiles();
			for(File f:files){		//�����ļ��еݹ����ע��
				addDirectory(f.getAbsolutePath(),observer);
			}
		}*/
		
	}
	
	private void deleteFile(String path){
		deleteObserver(path);
	}
	
	private String getFatherPath(String path){
		//System.out.println("FileManager------Path is:"+path+"------");
		int index  = path.lastIndexOf("/");
		return path.substring(0, index);
	}
	
	private boolean dispenseMessage(int result, MyFileObserver o,String s){
		if(dispenseMsgTag == false){
			System.out.println("----FileManager----dispenseMessage----dispenseMsgTag is false");
			return false;
		}
		if(o != null){
			System.out.println("----FileManager----dispenseMessage----observer is not null");
			HashMap<String,Handler> targets = o.getTargetsAll();
			Iterator<Entry<String, Handler>> iter = targets.entrySet().iterator();
			while(iter.hasNext()){
				 Map.Entry<String,Handler> entry =(Map.Entry<String,Handler>)iter.next();
				 System.out.println("----FileManager----dispenseMessage----target is " + entry.getKey());
				 Handler handler = (Handler)entry.getValue();
				 handler.sendMessage(Message.obtain(handler, result, s));
			 }
			return true;
		}
		else return false;
	}
	
	private boolean dispenseMessage(int result,MyFileObserver o1,MyFileObserver o2,String s1,String s2){
		if(dispenseMsgTag == false) return false;
		if(o1 != null && o2 != null){
			HashMap<String,Handler> targets1 = o1.getTargetsAll();
			HashMap<String,Handler> targets2 = o2.getTargetsAll();
			Iterator<Entry<String, Handler>> iter1 = targets1.entrySet().iterator();
			Iterator<Entry<String, Handler>> iter2 = targets2.entrySet().iterator();
			while(iter2.hasNext()){
				Map.Entry<String,Handler> entry =(Map.Entry<String,Handler>)iter2.next();
				String target = entry.getKey();
				Handler handler = (Handler)entry.getValue();
				if(!targets1.containsKey(target)){
					if(result == IEventTranslate.FILERENAMEORMOVE){
						handler.sendMessage(Message.obtain(handler, IEventTranslate.FILEMOVETO, s2));
						registerObserver(target,s2,handler,getFatherPath(s2));
					}
					else{
						handler.sendMessage(Message.obtain(handler, IEventTranslate.DIRMOVETO, s2));
						registerObserver(target,s2,handler,getFatherPath(s2));
					}
				}
			}
			while(iter1.hasNext()){
				Map.Entry<String,Handler> entry =(Map.Entry<String,Handler>)iter1.next();
				String target = entry.getKey();
				Handler handler = (Handler)entry.getValue();
				if(targets2.containsKey(target)){
					handler.sendMessage(Message.obtain(handler, result, s1 + "$/@@/$" + s2));
				}
				else{
					if(result == IEventTranslate.FILERENAMEORMOVE){
						handler.sendMessage(Message.obtain(handler,IEventTranslate.FILEDELETE,s1));
						withdrowObserver(target,s2);				//ע��target�Ը��ļ��ļ��
					}
					else{
						handler.sendMessage(Message.obtain(handler,IEventTranslate.DIRDELETE,s1));
						withdrowObserver(target,s2);
					}
				}
			}
			
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * ȫ����Ϣ�����ڽ��յ����Ը���observer�ļ����Ϣ�������¼����������ó��ļ�������
	 * ���������ļ��������͵������豸�Ŀ��ƶ��У���shareinfo�У��ö�Ӧ��һ���Կ��������о��ߡ�
	 * ���⣬����Ҫ���ݲ�ͬ���ļ����������Ա仯���ļ����а汾�ĸ��£��汾������Ҫ�����˶�Ӧ�ļ���observer��
	 * �汾��Ϣ�ĸ����Լ����ݿ��а汾��Ϣ�ĸ��£�
	 * �ڸ��°汾��Ϣʱ����Ҫע���Ǳ��ظ��»�������Զ�˵ĸ��¡�
	 * */
	private class GlobalMsgHandler extends Handler{
		private IEventTranslate eventTranslate;
		
		public GlobalMsgHandler(Looper looper){
			super(looper);
			eventTranslate = new AndEventTranslate();
		}
		
		//�����ļ��仯��Ϣ
		private void handleFileModifiedMsg(String path,int result){
			if(subFileOfCache(path)){			//��cacheĿ¼�µ��ļ��õ����޸�
				//�ж��Ƿ������ļ�
				//���°汾��Ϣ
			}
			else{			//����cacheĿ¼�µ��ļ��õ����޸�
				MyFileObserver ob = getMyFileObserver(path);
				if(ob == null){		//�ļ�δ����
					//System.out.println("----FileManager----HandleFileModifiedMsg:file not exists,create file observer");
					createFile(path);
				}
				else{		//�ļ��Ѿ�����
					long time = FileOperateHelper.getFileModifiedTime(path);
					if(time != ob.getModifiedTime()){ 	//�޸�ʱ��仯��˵��ȷʵ�޸����ļ�
						System.out.println("file modified,modify time also changed");
						updateVersion(ob,localDeviceId);
						//����metaData�е�version��
						ob.getFileMetaData().setVersionID(ob.getVersionNumber(localDeviceId));
						//�����ļ����޸�ʱ��
						ob.getFileMetaData().setModifiedTime(FileOperateHelper.getFileModifiedTime(path));
						//�����ļ��Ĵ�С
						ob.getFileMetaData().setFileSize(FileOperateHelper.getFileLength(path));
						}
					else{	//�޸�ʱ��û�仯��˵��ֻ�Ǵ��ˣ�������δ�޸�
						return;
						}
					}
				dispenseMessage(result,getMyFileObserver(path),path);
				}
		}
		
		//�����ļ�������Ϣ
		private void handleFileMoveToMsg(String path,int result){
			if(subFileOfCache(path)){				//���ļ����뵽cache�У������ϲ�Ӧ�ô����������
				//TODO
				createFile(path);
			}
			else{			//���ļ����룬�Ҳ���cache�ļ���
				
				if(createFile(path)){
					dispenseMessage(result,getMyFileObserver(path),path);
				}
			}
		}
		
		//�����ļ��еĴ���
		private void handleCreateDirMsg(String path,int result){
			if(subFileOfCache(path)){			//cache���ļ��еĴ���
				//TODO
			}
			else{
				createDir(path);
				dispenseMessage(result,getMyFileObserver(path),path);
			}
		}
		
		

		//�����ļ����Ƴ�����Ŀ���ļ��в��ڼ�ⷶΧ��
		private void handleFileMoveFromMsg(String path,int result){
			if(subFileOfCache(path)){			//���ļ���cache���Ƴ��������ϲ�Ӧ�ô����������
				//TODO
				deleteFile(path);
			}
			else{
				dispenseMessage(result,getMyFileObserver(path),path);
				deleteFile(path);
			}
		}
		
		//�����ļ���ɾ��
		private void handleDeleteFileMsg(String path,int result){
			if(path.endsWith(oldVersionSuffix)){	//�Ǿɰ汾�ļ���ɾ��
				System.out.println("old version :" + path + " has been deleted");
			}
			else{	//���Ǿɰ汾�ļ���ɾ��
				handleFileMoveFromMsg(path,result);
			}
		}
		
		//�����ļ������������ƶ�
		private void handleFileRenameOrMoveMsg(String oldPath,String newPath,int result){
			boolean oldInCache = subFileOfCache(oldPath);
			boolean newInCache = subFileOfCache(newPath);
			if(oldInCache && newInCache){	//��·���Լ���·������cache��
				modifyObserverPath(oldPath,newPath);
			}
			else if(oldInCache && !newInCache){		//��·����cache�У���·������cache��
				//TODO
				modifyObserverPath(oldPath,newPath);
				//�������豸��ȡ���ļ���Ĳ���
			}
			else if(!oldInCache && newInCache){			//�Ӽ��Ŀ¼���ƶ��ļ���cache�У�Ŀǰ������
				
			}
			else if(newPath.endsWith(oldVersionSuffix) || oldPath.endsWith(oldVersionSuffix)){	//���ļ����ΪoldVersion����������Ҫ����,��Ҫ���������Ǳ��ز����������������豸�Ĳ���
				
			}
			else{
				modifyObserverPath(oldPath,newPath);
				String oldFatherPath = getFatherPath(oldPath);
				String newFatherPath = getFatherPath(newPath);
				MyFileObserver ob1 = getMyFileObserver(oldFatherPath);
				MyFileObserver ob2 = getMyFileObserver(newFatherPath);
				dispenseMessage(result,ob1,ob2,oldPath,newPath);
			}	
		}
		
		private void handleDirRenameOrMoveMsg(String oldPath,String newPath,int result){
			handleFileRenameOrMoveMsg(oldPath,newPath,result);
		}
		
		/**
		 * ���ڴ����ļ��е�ɾ��
		 * @param path
		 * @param result
		 */
		private void handleDeleteDirMsg(String path,int result){
			if(subFileOfCache(path)){			//ɾ�����ļ�����cache�е�
				deleteFile(path);
			}
			else{
				MyFileObserver ob = getMyFileObserver(path);
				dispenseMessage(result,ob,path);
				deleteFile(path);
			}
		}
		
		private void handleDirMoveFromMsg(String path,int result){
			handleDeleteDirMsg(path,result);
		}
		
		/**
		 * ���ڴ����ļ��е�����
		 * @param path
		 * @param result
		 */
		private void handleDirMoveToMsg(String path,int result){
			if(subFileOfCache(path)){		//�ļ��д��ⲿ���뵽cache�У���Ӧ�ó����������
				//TODO
			}
			else{
				MyFileObserver ob = getMyFileObserver(getFatherPath(path));
				
				if(getMyFileObserver(path) == null){ 		//�ļ������������ڼ���Ŀ¼��
					addDirectory(path,ob);
				}   
				dispenseMessage(result,ob,path);
			}
		}
		
		/**
		 * 
		 */
		private void handleCoverFileMsg(String path,int result){
			MyFileObserver ob = getMyFileObserver(path);
			if(ob != null){
				ob.stopWatching();		//ֹͣ��ǰ�ļ���
				ob.startWatching();		//���¿�ʼ����
				long time = FileOperateHelper.getFileModifiedTime(path);
				if(time != ob.getModifiedTime()){ 	//�޸�ʱ��仯��˵��ȷʵ�޸����ļ�
					System.out.println("file modified,modify time also changed");
					updateVersion(ob,localDeviceId);
					//����metaData�е�version��
					ob.getFileMetaData().setVersionID(ob.getVersionNumber(localDeviceId));
					//�����ļ����޸�ʱ��
					ob.getFileMetaData().setModifiedTime(FileOperateHelper.getFileModifiedTime(path));
					//�����ļ��Ĵ�С
					ob.getFileMetaData().setFileSize(FileOperateHelper.getFileLength(path));
					}
				else{	//�޸�ʱ��û�仯��˵��ֻ�Ǵ��ˣ�������δ�޸�
					return;
					}
				dispenseMessage(result,getMyFileObserver(path),path);
			}
		}
		
		private boolean subFileOfCache(String path){
			if(path.startsWith(FileConstant.DEFAULTSAVEPATH+"/")) return true;
			else return false;
		}
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Message m = Message.obtain(msg);
			String path = m.obj.toString();
			int result = eventTranslate.translate(path, m.what);
			switch(result){
			case IEventTranslate.FILEMODIFIED:{				//�ļ����޸ģ�����Ϣ���͵�������ļ��Ķ���.
				//System.out.println(path + " has been modified");
				handleFileModifiedMsg(path,result);
			}break;
			
			case IEventTranslate.FILEMOVETO:{			//�����ļ��ƶ������ܼ�ص��ļ����У���Ҫ�����ļ���Ϊ���ļ����observer
				handleFileMoveToMsg(path,result);
			}break;
			
			case IEventTranslate.DIRCREATE:{				//�ļ��д���		
				handleCreateDirMsg(path,result);																				
			}break;
			
			case IEventTranslate.FILEMOVEFROM:{			//�ļ��Ӽ��Ŀ¼�����ߣ���Ŀ���ļ��в��ڼ�ⷶΧ��
				handleFileMoveFromMsg(path,result);
			}break;
			
			case IEventTranslate.FILEDELETE:{							//�ļ�ɾ��				
				handleDeleteFileMsg(path,result);
			}break;
			
			case IEventTranslate.FILERENAMEORMOVE:{		//�ļ������������ƶ�
				handleFileRenameOrMoveMsg(eventTranslate.getOldPath(),eventTranslate.getNewPath(),result);
			}break;
			
			case IEventTranslate.DIRRENAMEORMOVE:{					//�ļ������������ƶ�
				handleDirRenameOrMoveMsg(eventTranslate.getOldPath(),eventTranslate.getNewPath(),result);
			}break;
			
			case IEventTranslate.DIRDELETE:{				//�ļ���ɾ��
				handleDeleteDirMsg(eventTranslate.getOldPath(),result);
			}break;
			
			case IEventTranslate.DIRMOVEFROM:{			//�ļ����ƶ���δ���Ŀ¼�£�ͬ�ļ���ɾ��
				handleDirMoveFromMsg(eventTranslate.getOldPath(),result);
			}break;
			
			case IEventTranslate.DIRMOVETO:{			//�ļ���������Ŀ¼
				handleDirMoveToMsg(path,result);
			}break;
			
			case IEventTranslate.COVERFILE:{			//�����ļ�ʱ������ͬ�����ֵ��ļ�
				handleCoverFileMsg(path,result);
			}break;
			}
		}
	}

	
}
