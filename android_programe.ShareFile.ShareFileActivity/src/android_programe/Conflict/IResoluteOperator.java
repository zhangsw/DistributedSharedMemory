package android_programe.Conflict;

import android_programe.FileSystem.MyFileObserver;

public interface IResoluteOperator {
	
	public void addConflictFile(MyFileObserver ob,String path);
	
	public void addConflictNode(String path);
		
	public void removeConflictNode(String path);
		
	/**
	 * �½��ļ�
	 */
	public void createEmptyFileNode(String path,String fileID);
	
	public void deleteFile();
	
	public void mergeFile();
	
	/**
	 * �����ļ�
	 * @param target
	 * @param fileID
	 * @param relativePath
	 */
	public void fetchFile(String target,String fileID,String relativePath);
	
	/**
	 * �����������ļ�
	 * @param fileID	�ļ���id
	 * @param oldRelativePath	�ļ��ľɵ����·��
	 * @param newRelativePath	�ļ����µ����·��
	 */
	public void renameLocalFile(String oldRelativePath,String newRelativePath);
}
