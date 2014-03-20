package android_programe.PsyLine;

import android_programe.FileSystem.VersionMap;

/**
 * �ļ�����ӿڣ��������ļ������еĸ�������
 * @author zhangsw
 *
 */
public interface FileTransfer {
	
	/**
	 * �����ļ���metaData
	 * @param fileMetaData	��Ҫ���͵��ļ���metaData
	 * @param target	�����Ķ���
	 */
	public void sendFileMetaData(Object fileMetaData,Object target); 
	
	/**
	 * ��ȡ�ļ���metaData
	 * @param fileID	��Ҫ��ȡ���ļ�id
	 * @param target	��Ϣ��Դ����
	 */
	public void getFileMetaData(String fileID,Object target);
	
	/**
	 * �����ļ�������
	 * @param fileData	�ļ�������
	 * @param target	�����Ķ���
	 */
	public void sendFileData(Object fileData,Object target);
	
	/**
	 * ��ȡ�ļ��ľ�������
	 * @param fileID	��Ҫ��ȡ���ļ�id
	 * @param target	��Ϣ��Դ����
	 */
	public void getFileData(Object fileID,Object target);
	
	/**
	 * �����ļ��İ汾map
	 * @param versionMap	��Ҫ���͵İ汾map
	 * @param target	�����Ķ���
	 */
	public void sendFileVersionMap(Object versionMap,Object target);
	
	//���Ͳ���ָ��
	public void sendCommand(String command);
	
	
}
