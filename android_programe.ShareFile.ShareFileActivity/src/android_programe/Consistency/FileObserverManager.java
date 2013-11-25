package android_programe.Consistency;


import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class FileObserverManager extends Thread implements IFileObserverManager {
	private HashMap<String,MyFileObserver> mObservers;
	private Handler globalMessageHandler;		//ȫ����Ϣhandler
	private IEventTranslate eventTranslate;
	
	public FileObserverManager(){
		mObservers = new HashMap<String,MyFileObserver>();
		eventTranslate = new AndEventTranslate();
	}
	
	public MyFileObserver registerObserver(String target,String absolutePath,Handler handler){
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
		else{									//�����ڼ��Ӹ�·����observer
			if(fatherPath != null)
				observer = new MyFileObserver(absolutePath,target,handler,globalMessageHandler,this,mObservers.get(fatherPath));
			else
				observer = new MyFileObserver(absolutePath,target,handler,globalMessageHandler,this,null);
			observer.start();
			mObservers.put(absolutePath, observer);
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
	
	/**�޸�observer��·�������������Լ��ƶ��ļ�ʱ����*/
	public void modifyObserverPath(String path,String newPath){
		MyFileObserver observer = mObservers.get(path);
		if(observer != null){
			String parentPath = path.substring(0, path.lastIndexOf("/"));
			String newParentPath = newPath.substring(0, newPath.lastIndexOf("/"));
			if(parentPath.equals(newParentPath)){				//��Ŀ¼��ͬ��Ϊ������
				observer.modifyPath(newPath);
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
			}
		}
		
	}
	
	private MyFileObserver getMyFileObserver(String path){
		return mObservers.get(path);
	}
	
	private void addFile(String path,MyFileObserver fatherObserver){
		MyFileObserver observer = new MyFileObserver(path,globalMessageHandler,this,fatherObserver);
		observer.start();
		mObservers.put(path, observer);
	}
	
	private void addDirectory(String path,MyFileObserver fatherObserver){
		MyFileObserver observer = new MyFileObserver(path,globalMessageHandler,this,fatherObserver);
		observer.start();
		mObservers.put(path, observer);
		File file = new File(path);
		if(file.isDirectory()){
			File []files = file.listFiles();
			for(File f:files){		//�����ļ��еݹ����ע��
				addDirectory(f.getAbsolutePath(),observer);
			}
		}
		
	}
	
	private void deleteFile(String path){
		deleteObserver(path);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		Looper.prepare();
		
		globalMessageHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
				Message m = Message.obtain(msg);
				
				
				assert m.obj != null: "FileObserverManager message.obj is null";
				
				String path = m.obj.toString();
				
				
				int result = eventTranslate.translate(path, m.what);
				switch(result){
				case IEventTranslate.FILEMODIFIED:				//�ļ����޸ģ�����Ϣ���͵�������ļ��Ķ���.
				case IEventTranslate.FILEMOVETO:			//�����ļ��ƶ������ܼ�ص��ļ����У���Ҫ�����ļ���Ϊ���ļ����observer
				case IEventTranslate.DIRCREATE:{				//�ļ��д���		
																									
					MyFileObserver ob = getMyFileObserver(getFatherPath(path));
					dispenseMessage(result,ob,path);
					if(getMyFileObserver(path) == null){ 		//�ļ������������ڼ���Ŀ¼��
						addFile(path,ob);
					}
				}break;
				
				case IEventTranslate.FILEMOVEFROM:			//�ļ��Ӽ��Ŀ¼�����ߣ���Ŀ���ļ��в��ڼ�ⷶΧ��
				case IEventTranslate.FILEDELETE:{							//�ļ�ɾ��				
					
					MyFileObserver ob = getMyFileObserver(path);
					dispenseMessage(result,ob,path);
					deleteFile(path);
						
				}break;
				
				case IEventTranslate.FILERENAMEORMOVE:		//�ļ������������ƶ�
				case IEventTranslate.DIRRENAMEORMOVE:{					//�ļ������������ƶ�
					
					String oldPath = eventTranslate.getOldPath();
					String newPath = eventTranslate.getNewPath();
					modifyObserverPath(oldPath,newPath);
					
					String oldFatherPath = getFatherPath(oldPath);
					String newFatherPath = getFatherPath(newPath);
					MyFileObserver ob1 = getMyFileObserver(oldFatherPath);
					MyFileObserver ob2 = getMyFileObserver(newFatherPath);
						
					dispenseMessage(result,ob1,ob2,oldPath,newPath);
	
					
				}break;
				
				case IEventTranslate.DIRDELETE:				//�ļ���ɾ��
				case IEventTranslate.DIRMOVEFROM:{			//�ļ����ƶ���δ���Ŀ¼�£�ͬ�ļ���ɾ��
					
					String oldPath = eventTranslate.getOldPath();
					MyFileObserver ob = getMyFileObserver(oldPath);
					dispenseMessage(result,ob,oldPath);
					deleteFile(oldPath);
					
				}break;
				
				case IEventTranslate.DIRMOVETO:{			//�ļ���������Ŀ¼
					
					MyFileObserver ob = getMyFileObserver(getFatherPath(path));
					dispenseMessage(result,ob,path);
					if(getMyFileObserver(path) == null){ 		//�ļ������������ڼ���Ŀ¼��
						addDirectory(path,ob);
					}    
				}break;
				
			}
			}
		};
		
		Looper.loop();
	}
	
	
	
	private String getFatherPath(String path){
		int index  = path.lastIndexOf("/");
		return path.substring(0, index);
	}
	
	private boolean dispenseMessage(int result, MyFileObserver o,String s){
		if(o != null){
			HashMap<String,Handler> targets = o.getTargetsAll();
			Iterator<Entry<String, Handler>> iter = targets.entrySet().iterator();
			while(iter.hasNext()){
				 Map.Entry<String,Handler> entry =(Map.Entry<String,Handler>)iter.next();
				 Handler handler = (Handler)entry.getValue();
				 handler.sendMessage(Message.obtain(handler, result, s));
			 }
			return true;
		}
		else return false;
	}
	
	private boolean dispenseMessage(int result,MyFileObserver o1,MyFileObserver o2,String s1,String s2){
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
	

	
	
	
	
}
