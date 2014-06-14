package android_programe.Conflict;

import java.io.File;

import android_programe.FileSystem.FileManager;
import android_programe.FileSystem.FileMetaData;
import android_programe.FileSystem.MyFileObserver;
import android_programe.FileSystem.VersionManager;
import android_programe.FileSystem.VersionMap;
import android_programe.Util.FileConstant;
import android_programe.Util.FileOperateHelper;
import android_programe.Util.FileUtil;

public class MyConflictResolute extends ConflictResolute{
	
	private FileManager fileManager;
	
	public MyConflictResolute(FileManager fileManager){
		this.fileManager = fileManager;
	} 
	
	
	@Override
	public void resoluteConfliction(String fileID,VersionMap localVersionMap,
			String localDeviceId, VersionMap remoteVersionMap,
			String remoteDeviceId, String relativePath, IResoluteOperator iro) {
		// TODO Auto-generated method stub
		//����ͻ�ļ���ӵ�conflictFile��
		iro.addConflictFile(fileManager.getMyFileObserver(FileConstant.DEFAULTSHAREPATH + relativePath), FileConstant.DEFAULTSHAREPATH + relativePath);
		
		//�޸ı��س�ͻ�ļ����֣�Ϊ"�ļ��� + �豸�� +�޸�����"����ʽ
		String fileName = FileOperateHelper.getFileName(relativePath);
		long time = fileManager.getMyFileObserver(FileConstant.DEFAULTSHAREPATH + relativePath).getModifiedTime();
		String newFileName = fileRename(fileName,localDeviceId,FileUtil.getTimeFromLong(time));
		String newRelativePath = relativePath.substring(0, relativePath.length()-fileName.length()) + newFileName;
		//iro.renameLocalFile(fileID, relativePath,newRelativePath);
		//���°汾��Ϣ
		localVersionMap.put(remoteDeviceId, VersionManager.FILENOTEXIST);
		//�����ļ���+remoteDeviceID���ļ����
		String path = FileConstant.DEFAULTSHAREPATH + relativePath + remoteDeviceId;
		iro.createEmptyFileNode(path, fileID);
		//����remote�豸�İ汾��
		fileManager.updateVersionMap(path, remoteDeviceId, remoteVersionMap.getVersionNumber(remoteDeviceId));
		//�����ļ�
		iro.fetchFile(remoteDeviceId, fileID, relativePath);
		
	}
	
	@Override
	public void resoluteConfliction(VersionMap localVersionMap,
			String localDeviceId, VersionMap remoteVersionMap,
			String remoteDeviceId, String relativePath,
			FileMetaData remoteMetaData, ConflictManager iro) {
		// TODO Auto-generated method stub
		iro.addConflictFile(fileManager.getMyFileObserver(FileConstant.DEFAULTSHAREPATH + relativePath), FileConstant.DEFAULTSHAREPATH + relativePath);
		
		//�޸ı��س�ͻ�ļ����֣�Ϊ"�ļ��� + �豸�� +�޸�����"����ʽ
		String fileName = FileOperateHelper.getFileName(relativePath);
		long time = fileManager.getMyFileObserver(FileConstant.DEFAULTSHAREPATH + relativePath).getModifiedTime();
		String newFileName = fileRename(fileName,localDeviceId,FileUtil.getTimeFromLong(time));
		String newRelativePath = relativePath.substring(0, relativePath.length()-fileName.length()) + newFileName;
		iro.renameLocalFile(relativePath,newRelativePath);
		//���°汾��Ϣ
		localVersionMap.put(remoteDeviceId, VersionManager.FILENOTEXIST);
		//�����ļ���+remoteDeviceID���ļ����
		String path = FileConstant.DEFAULTSHAREPATH + relativePath + remoteDeviceId;
		iro.createEmptyFileNode(path, remoteMetaData.getFileID());
		//����remote�豸�İ汾��
		fileManager.updateVersionMap(path, remoteDeviceId, remoteVersionMap.getVersionNumber(remoteDeviceId));
		//�����ļ�
		iro.fetchFile(remoteDeviceId, remoteMetaData.getFileID(), relativePath);
	}


	@Override
	public void receiveConflictFileData(String target,
			FileMetaData fileMetaData, File file,IResoluteOperator iro) {
		// TODO Auto-generated method stub
		String path = FileConstant.DEFAULTSHAREPATH + fileMetaData.getRelativePath();
		String suffix = getConflictFileSuffix(target,fileMetaData.getModifiedTime());
		String oldPath = path.substring(0, path.lastIndexOf(suffix)) + target;
		//����metaData
		fileManager.updateMetaData(oldPath, fileMetaData);
		//���±��صİ汾��Ϣ
		//fileManager.updateLocalVersion(oldPath, fileMetaData.getVersionID());
		//���ļ��ƶ���ָ��Ŀ¼��
		FileOperateHelper.renameFile(file.getAbsolutePath(), path);
		//��ʼ�����ļ�
		System.out.println("----MyConflictResolute----oldPath is:" + oldPath + ";newPath is:" + path);
		fileManager.modifyObserverPath(oldPath, path);
		fileManager.startObserverFile(path);
		//�����ļ����뵽��ͻ�ļ���
		iro.addConflictFile(fileManager.getMyFileObserver(path), oldPath);
		
	}
	
	@Override
	public boolean isConflictFile(String id, FileMetaData fileMetaData,
			File file) {
		// TODO Auto-generated method stub
		long time = fileMetaData.getModifiedTime();
		String suffix = "(" + id + "-" + FileUtil.getTimeFromLong(time) + ")";
		String path = file.getAbsolutePath();
		if(path.endsWith(suffix)) return true;
		else return false;
	}

	private String getConflictFileSuffix(String id,long time){
		return "(" + id + "-" + FileUtil.getTimeFromLong(time) + ")";
	}
	
	private String fileRename(String oldname,String id,String time){
		return oldname + "(" + id + "-" + time + ")";
	}


	


	
}
