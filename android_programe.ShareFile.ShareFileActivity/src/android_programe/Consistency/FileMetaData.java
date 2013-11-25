package android_programe.Consistency;

import java.util.HashMap;

public class FileMetaData {
	
	private int  mVersionId;
	
	private String mContentLocation;
	
	private String mFileName;
	
	private HashMap<String,String> mMetaMap;
	
	
	public FileMetaData(int id, String location, String name){
		
		mMetaMap = new HashMap<String,String>();
		mVersionId = id;
		mContentLocation = location;
		mFileName = name;
	}
	
	public void addAttribute(String key,String value){
		mMetaMap.put(key, value);
	}
	
	public String getValue(String key){
		return mMetaMap.get(key);
	}
	
	public int getVersionId(){
		return mVersionId;
	}
	
	public String getContentLocation(){
		return mContentLocation;
	}
	
	
}
