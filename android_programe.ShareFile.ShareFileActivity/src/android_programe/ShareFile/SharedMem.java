package android_programe.ShareFile;


import java.io.*;
import java.util.*;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android_programe.Consistency.Consistency;
import android_programe.Consistency.FileInf;
import android_programe.Util.FileUtil;

public class SharedMem extends Service{

	private Consistency conManager;
	private List fileList;
	private String path;
	
	private MyBinder myBinder = new MyBinder();
	
	/*public SharedMem(){
		fileList =new ArrayList<FileInf>();
		getFileInfList(path,fileList);
		conManager = new Consistency(path,fileList);
	}*/
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		
		return myBinder;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		System.out.println("enter service oncreate");
		/*path = "/sdcard/wallpaper/";
		fileList =new ArrayList<FileInf>();
		getFileInfList(path,fileList);
		try {
			conManager = new Consistency(path,fileList);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conManager.SimpleCM();*/
		
		try {
			conManager = new Consistency();
			String target = "";
			System.out.println("before connect");
			//conManager.connect("114.212.87.66");
			//conManager.addShareDevice("/sdcard/wallpaper/", "114.212.87.66", 0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	public void readFile(String filename,String filePath){
		File file = new File(filePath);
	}
	
	public void writeFile(String filename){
		
	}
	/*
	private void getFileInfList(String path,List <FileInf>fileList){
		File filePath = new File(path);
		if(filePath.isDirectory()){
			File []files = filePath.listFiles();
			if(files == null) return;
			for(int i=0;i<files.length;i++)
				getFileInfList(files[i].getAbsolutePath(),fileList);
		}
		else if(filePath.isFile()){
			String filename = filePath.getName();
			String filepath = filePath.getAbsolutePath();
			String MD5 = FileUtil.getFileMD5(filePath);
			FileInf fileInf = new FileInf(filename,filepath,MD5);
			fileList.add(fileInf);
		}
	}
	
	/*
	private boolean handleDataFromActivity(int code, Parcel data, Parcel reply, int flags){
		switch(code){
		case 1:{
			String IP = data.readString();
		}break;
		
		}
	}
	*/

	
	public class MyBinder extends Binder{
		public SharedMem getService(){
			return SharedMem.this;
		}

		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) throws RemoteException {
			// TODO Auto-generated method stub
			return super.onTransact(code, data, reply, flags);
		}
		
		public boolean connect(String ip) throws IOException{
			if(conManager == null) conManager = new Consistency();
			if(conManager.connect(ip)){
				//conManager.addShareDevice("/sdcard/wallpaper/", ip, 0);
				return true;
			}
			else return false;
		}
		
		
	}
	
	
	
	
}

