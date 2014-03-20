package android_programe.FileSystem;

import java.util.ArrayList;
import java.util.List;

import android_programe.MemoryManager.FileMetaData;

public class VersionHistoryNode {

	private int mVersionId;
	private int mNumOfFathers;
	private List <VersionHistoryNode> mFatherNodes;
	private FileMetaData mMetaData;
	
	public VersionHistoryNode(int id,List<VersionHistoryNode> fathers){
		mVersionId = id;
		mFatherNodes = new ArrayList<VersionHistoryNode>();
		mFatherNodes = fathers;
		mNumOfFathers = fathers.size();
	}
	
	public VersionHistoryNode(int id){
		mVersionId = id;
		mNumOfFathers = 0;
		mFatherNodes = new ArrayList<VersionHistoryNode>();
	}
	
	public int getVersionId(){
		return mVersionId;
	}
	
	public int getNumOfFathers(){
		return mNumOfFathers;
	}
	
	public List getFathers(){
		return mFatherNodes;
	}
}
