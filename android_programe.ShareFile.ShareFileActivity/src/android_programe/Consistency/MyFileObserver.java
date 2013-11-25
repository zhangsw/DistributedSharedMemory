package android_programe.Consistency;

import java.io.File;
import java.util.*;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android_programe.Util.FileUtil;

public class MyFileObserver extends Thread{
	private SDFileObserver observer;
	private String path;
	private HashMap <String,Handler> mTargets;
	private List<MyFileObserver> lChildObserver;
	private MyFileObserver fatherObserver;
	private IFileObserverManager iFOManager;
	private Handler handler;
	private Handler globalMessageHandler;
	
	
	private VersionHistoryNode mLatestVersion;	//最新的版本
	
	private VersionHistoryNode mLocalVersion;	//本地存储版本
	
	
	public MyFileObserver(String path,String target,Handler handler,Handler globalMessageHandler,IFileObserverManager i,MyFileObserver fatherObserver){
		this.path = path;
		mTargets = new HashMap<String,Handler>();
		lChildObserver = new ArrayList<MyFileObserver>();
		mTargets.put(target, handler);
		iFOManager = i;
		this.fatherObserver = fatherObserver;
		this.globalMessageHandler = globalMessageHandler;
		//observer = new SDFileObserver(path,handler);
	}
	
	public MyFileObserver(String path,Handler globalMessageHandler,IFileObserverManager i,MyFileObserver fatherObserver){
		this.path = path;
		this.globalMessageHandler = globalMessageHandler;
		this.fatherObserver = fatherObserver;
		lChildObserver = new ArrayList<MyFileObserver>();
		mTargets = new HashMap<String,Handler>(fatherObserver.getTargetsAll());
		iFOManager = i;
		
	}
	
	/*
	public void startWatching(){
		observer.startWatching();
	}*/
	
	public void stopWatching(){
		Iterator<MyFileObserver> iter = lChildObserver.iterator();
		while(iter.hasNext()){
			MyFileObserver o = iter.next();
			o.stopWatching();
		}
		observer.stopWatching();
	}
	
	public void modifyPath(String path){
		iFOManager.updateObserverMap(this.path, path);
		this.path = path;
		observer.updatePath(path);
		Iterator<MyFileObserver> iter = lChildObserver.iterator();
		while(iter.hasNext()){
			System.out.println("has child");
			MyFileObserver o = iter.next();
			String name = FileUtil.getFileNameFromPath(o.getPath());
			o.modifyPath(path + "/" + name);
			System.out.println(path + "/" + name);
		}
		
	}
	
	public String getPath(){
		return path;
	}
	
	public boolean hasFather(){
		if(fatherObserver != null) return true;
		else return false;
	}
	
	public void setFather(MyFileObserver observer){
		fatherObserver = observer;
	}
	
	public MyFileObserver getFather(){
		return fatherObserver;
	}
	
	public boolean hasChild(){
		if(lChildObserver.isEmpty()) return false;
		else return true;
	}
	
	public List<MyFileObserver> getChildAll(){
		return lChildObserver;
	}
	
	public HashMap<String,Handler> getTargetsAll(){
		return mTargets;
	}
	
	public boolean hasTarget(){
		if(mTargets.isEmpty()) return false;
		else return true;
	}
	
	
	public void addChildObserver(MyFileObserver childObserver){
		for(MyFileObserver fileObserver:lChildObserver)
			if(fileObserver.getPath().equals(childObserver.getPath())) return;
		lChildObserver.add(childObserver);					
	}
	
	public void deleteChildObserver(String path){
		
	}
	
	public void deleteChildObserver(MyFileObserver childObserver){
		lChildObserver.remove(childObserver);
	}
	
	//public List<MyFileObserver>
	
	public boolean addTarget(String target,Handler handler){
		if(mTargets.containsKey(target))
			return false;
		else{
			mTargets.put(target, handler);
			Iterator<MyFileObserver> iter = lChildObserver.iterator();
			while(iter.hasNext()){
				MyFileObserver o = iter.next();
				o.addTarget(target, handler);
			}
			return true;
		}	
	}
	
	public void deleteTarget(String target){
		mTargets.remove(target);
	}

	/*
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(o == this) return true;
		if(o == null) return false;
		else if(path.equals(((MyFileObserver)o).getPath())) return true;
		else return false;
	}
	*/

	public void run() {
		// TODO Auto-generated method stub
		Looper.prepare();
		
		handler = new MyHandler();
		observer = new SDFileObserver(path,globalMessageHandler);
		observer.startWatching();
		Looper.loop();
	}
	
	private class MyHandler extends Handler{
		String absolutePath;
		public MyHandler(){
			absolutePath = null;
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(msg == null) System.out.println("Message is null");
			else System.out.println("Message is not null");
			super.handleMessage(msg);
			 
			 Message m = Message.obtain(msg);
			 
			 if(m.what == FileObserver.MOVE_SELF || m.what == FileObserver.DELETE_SELF){
				 absolutePath = path;
			 }
			 else{
				 absolutePath = path + "/" + m.obj.toString();
			 }
			 
			 System.out.println("MyFileObserver message type is " + m.what);
			 /*
			 int what = m.what;
			 Iterator iter = mTargets.entrySet().iterator();
			 while(iter.hasNext()){
				 Map.Entry entry =(Map.Entry)iter.next();
				 Handler handler = (Handler)entry.getValue();
				 handler.sendMessage(Message.obtain(handler, what, absolutePath));
			 }
			 */
			 
		}
		
		
	}
	
	
	
}
