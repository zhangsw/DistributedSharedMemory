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
	private Handler globalMessageHandler;		//全局消息handler
	private IEventTranslate eventTranslate;
	
	public FileObserverManager(){
		mObservers = new HashMap<String,MyFileObserver>();
		eventTranslate = new AndEventTranslate();
	}
	
	public MyFileObserver registerObserver(String target,String absolutePath,Handler handler){
		return registerObserver(target,absolutePath,handler,null);
	}
	
	/**	
	 * 注册observer
	 * @param path: 
	 * @param target: 
	 * */
	public MyFileObserver registerObserver(String target,String absolutePath,Handler handler,String fatherPath){
		MyFileObserver observer= mObservers.get(absolutePath);
		if(observer != null){		//已经存在监视该路径的observer
			observer.addTarget(target,handler);
			if((fatherPath != null) && (!observer.hasFather()))
					observer.setFather(mObservers.get(fatherPath));
		}
		else{									//不存在监视该路径的observer
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
			for(File f:files){		//对子文件递归进行注册
				if(f.isDirectory())
					System.out.println(f.getAbsolutePath()+"------"+f.getParent());
				observer.addChildObserver(registerObserver(target,f.getAbsolutePath(),handler,f.getParent()));
				
			}
		}
		return observer;
	}
	
	/**
	 * 注销observer
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
	
	/**修改observer的路径，在重命名以及移动文件时调用*/
	public void modifyObserverPath(String path,String newPath){
		MyFileObserver observer = mObservers.get(path);
		if(observer != null){
			String parentPath = path.substring(0, path.lastIndexOf("/"));
			String newParentPath = newPath.substring(0, newPath.lastIndexOf("/"));
			if(parentPath.equals(newParentPath)){				//父目录相同，为重命名
				observer.modifyPath(newPath);
			}
			else{								//父目录不同，树结构发生变化
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
	
	private void moveObserver(String path){			//对路径为path的observer，将其从监控中移除
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
			for(File f:files){		//对子文件夹递归进行注册
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
				case IEventTranslate.FILEMODIFIED:				//文件被修改，将消息发送到共享该文件的对象.
				case IEventTranslate.FILEMOVETO:			//有新文件移动到了受监控的文件夹中，需要发送文件并为该文件添加observer
				case IEventTranslate.DIRCREATE:{				//文件夹创建		
																									
					MyFileObserver ob = getMyFileObserver(getFatherPath(path));
					dispenseMessage(result,ob,path);
					if(getMyFileObserver(path) == null){ 		//文件本来不存在于监视目录下
						addFile(path,ob);
					}
				}break;
				
				case IEventTranslate.FILEMOVEFROM:			//文件从监测目录中移走，且目标文件夹不在监测范围内
				case IEventTranslate.FILEDELETE:{							//文件删除				
					
					MyFileObserver ob = getMyFileObserver(path);
					dispenseMessage(result,ob,path);
					deleteFile(path);
						
				}break;
				
				case IEventTranslate.FILERENAMEORMOVE:		//文件重命名或者移动
				case IEventTranslate.DIRRENAMEORMOVE:{					//文件夹重命名或移动
					
					String oldPath = eventTranslate.getOldPath();
					String newPath = eventTranslate.getNewPath();
					modifyObserverPath(oldPath,newPath);
					
					String oldFatherPath = getFatherPath(oldPath);
					String newFatherPath = getFatherPath(newPath);
					MyFileObserver ob1 = getMyFileObserver(oldFatherPath);
					MyFileObserver ob2 = getMyFileObserver(newFatherPath);
						
					dispenseMessage(result,ob1,ob2,oldPath,newPath);
	
					
				}break;
				
				case IEventTranslate.DIRDELETE:				//文件夹删除
				case IEventTranslate.DIRMOVEFROM:{			//文件夹移动至未监控目录下，同文件夹删除
					
					String oldPath = eventTranslate.getOldPath();
					MyFileObserver ob = getMyFileObserver(oldPath);
					dispenseMessage(result,ob,oldPath);
					deleteFile(oldPath);
					
				}break;
				
				case IEventTranslate.DIRMOVETO:{			//文件夹移入监控目录
					
					MyFileObserver ob = getMyFileObserver(getFatherPath(path));
					dispenseMessage(result,ob,path);
					if(getMyFileObserver(path) == null){ 		//文件本来不存在于监视目录下
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
						withdrowObserver(target,s2);				//注销target对该文件的监控
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
