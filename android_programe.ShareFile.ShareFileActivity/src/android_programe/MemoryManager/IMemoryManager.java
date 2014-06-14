package android_programe.MemoryManager;

public interface IMemoryManager {
	
	public void fetchFile(String target,String relativePath);
	
	/**
	 * �����������ļ�
	 * @param fileID	�ļ���id
	 * @param oldRelativePath	�ļ��ɵ����·��
	 * @param newRelativePath	�ļ��µ����·��
	 */
	public void renameLocalFile(String oldRelativePath,String newRelativePath);
	
	/**
	 * �½��յ��ļ�����㣩
	 * @param path	·��
	 * @param fileID �ļ���id
	 */
	public void createEmptyFileNode(String path,String fileID);

}
