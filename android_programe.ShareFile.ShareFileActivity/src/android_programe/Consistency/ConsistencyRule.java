package android_programe.Consistency;

public abstract class ConsistencyRule {

	/*ɾ���ļ�*/
	public abstract boolean deleteFile(String path);
	
	/*�����ļ�����*/
	public abstract boolean sendFile(String path);
	
	/*�������ļ�*/
	public abstract boolean renameFile(String oldPath, String newPath);
	
	/*�ƶ��ļ�*/
	public abstract boolean moveFile(String oldPath, String newPath);
	
	/*ɾ���ļ���*/
	public abstract boolean deleteDirectory(String path);
	
	/*�½��ļ���*/
	public abstract boolean createDirectory(String path);
	
	/*�������ļ���*/
	public abstract boolean renameDirectory(String oldPath, String newPath);
	
	/*�ƶ��ļ���*/
	public abstract boolean moveDirectory(String oldPath,String newPath);
	
	/*�����ļ���*/
	public abstract boolean sendDirectory(String path);
	
}
