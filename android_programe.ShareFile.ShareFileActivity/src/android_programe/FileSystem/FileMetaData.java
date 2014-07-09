package android_programe.FileSystem;

import java.io.Serializable;

public class FileMetaData implements Serializable{
	
	private String mFileID;	//�ļ���id
	
	private int  mVersionID;	//�汾��
	
	private String mRelativePath;	//�ļ������·��
	
	//private String mAbsolutePath;	//�ļ��ľ���·��
	
	private long mFileSize;	//�ļ���С
	
	//private String mFileCreator;	//�ļ�����
	
	private long mModifiedTime;	//�ļ����޸�����
	
	//private boolean mLabel = true;	//����ļ����°汾���Ǿɰ汾���°汾��True,�ɰ汾��false
	
	public FileMetaData(){
		
	}
	
	/**
	 * ����һ���ļ���meta data
	 * @param fileID	�ļ���id
	 * @param versionID	�ļ��İ汾��
	 * @param relativePath	���·��
	 * @param absolutePath ����·��
	 * @param fileSize	�ļ��Ĵ�С
	 * @param creator	�ļ�������
	 */
	public FileMetaData(String fileID, int versionID, String relativePath,String absolutePath, long fileSize,String creator,long modifiedTime){
		mFileID = fileID;
		mVersionID = versionID;
		mRelativePath = relativePath;
		//mAbsolutePath = absolutePath;
		mFileSize = fileSize;
		//mFileCreator = creator;
		mModifiedTime = modifiedTime;
	}
	
	public void setFileID(String fileID){
		mFileID = fileID;
	}

	public String getFileID(){
		return mFileID;
	}
	
	public void setVersionID(int versionID){
		mVersionID = versionID;
	}
	
	public int getVersionID(){
		return mVersionID;
	}
	
	public void setRelativePath(String relativePath){
		mRelativePath = relativePath;
	}
	
	public String getRelativePath(){
		return mRelativePath;
	}
	
	/*
	public void setAbsolutePath(String absolutePath){
		mAbsolutePath = absolutePath;
	}
	
	public String getAbsolutePath(){
		return mAbsolutePath;
	}*/
	
	public void setFileSize(long size){
		mFileSize = size;
	}
	
	public long getFileSize(){
		return mFileSize;
	}
	
	/*
	public void setFileCreator(String creator){
		mFileCreator = creator;
	}
	
	public String getFileCreator(){
		return mFileCreator;
	}*/
	
	public void setModifiedTime(long time){
		mModifiedTime = time;
	}
	
	public long getModifiedTime(){
		return mModifiedTime;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(this == o) return true;
		if(!(o instanceof FileMetaData)) return false;
		FileMetaData fmd = (FileMetaData) o;
		return (mVersionID == fmd.mVersionID) && (mFileSize == fmd.mFileSize) && (mModifiedTime == fmd.mModifiedTime) &&
				(mFileID == null ? fmd.mFileID == null:mFileID.equals(fmd.mFileID)) &&
				(mRelativePath == null ?fmd.mRelativePath == null:mRelativePath.equals(fmd.mRelativePath));
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		int result = 17;
		result = 31*result + (mFileID == null ? 0:mFileID.hashCode());
		result = 31*result + (mRelativePath == null ? 0:mRelativePath.hashCode());
		result = 31*result + mVersionID;
		result = 31*result + (int)(mFileSize^(mFileSize >>> 32));
		result = 31*result + (int)(mModifiedTime^(mModifiedTime >>> 32));
		return result;
	}
	
	
	
	/**
	 * ����ļ�Ϊ�°汾
	 */
	/*
	public void setVersionNew(){
		mLabel = true;
	}
	
	/**
	 * ����ļ�Ϊ�ɰ汾
	 */
	/*
	public void setVersionOld(){
		mLabel = false;
	}
	
	public boolean getLabel(){
		return mLabel;
	}	*/
}
