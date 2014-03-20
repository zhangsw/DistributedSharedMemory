package android_programe.Conflict;

import java.io.File;

import android_programe.FileSystem.FileManager;
import android_programe.FileSystem.MyFileObserver;
import android_programe.FileSystem.VersionManager;
import android_programe.FileSystem.VersionMap;
import android_programe.MemoryManager.FileMetaData;
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
		iro.addConflictFile(fileManager.getMyFileObserver(FileConstant.DEFAULTROOTPATH + relativePath), FileConstant.DEFAULTROOTPATH + relativePath);
		
		//�޸ı��س�ͻ�ļ����֣�Ϊ"�޸����� + �ļ��� "����ʽ
		String fileName = FileOperateHelper.getFileName(relativePath);
		long time = fileManager.getMyFileObserver(FileConstant.DEFAULTROOTPATH + relativePath).getModifiedTime();
		String newFileName = "("+FileUtil.getTimeFromLong(time)+")"+fileName;
		String newRelativePath = relativePath.substring(0, relativePath.length()-fileName.length()) + newFileName;
		iro.renameLocalFile(fileID, relativePath,newRelativePath);
		//���°汾��Ϣ
		localVersionMap.put(remoteDeviceId, VersionManager.FILENOTEXIST);
		//�����ļ���+remoteDeviceID���ļ����
		String path = FileConstant.DEFAULTROOTPATH + relativePath + remoteDeviceId;
		iro.createEmptyFileNode(path, fileID);
		//�����ļ�
		iro.fetchFile(remoteDeviceId, fileID, relativePath);
		
	}


	@Override
	public void receiveConflictFileData(String target,
			FileMetaData fileMetaData, File file,String orginalName,IResoluteOperator iro) {
		// TODO Auto-generated method stub
		int index = fileMetaData.getRelativePath().indexOf(file.getName());
		String relativePath = fileMetaData.getRelativePath().substring(0, index)+orginalName ;
		String path = FileConstant.DEFAULTROOTPATH + relativePath + target;
		//����metaData
		fileManager.updateMetaData(path, fileMetaData);
		//���±��صİ汾��Ϣ
		fileManager.updateLocalVersion(path, fileMetaData.getVersionID());
		//���ļ��ƶ���ָ��Ŀ¼��
		FileOperateHelper.renameFile(file.getAbsolutePath(), FileConstant.DEFAULTROOTPATH + fileMetaData.getRelativePath());
		//��ʼ�����ļ�
		fileManager.startObserverFile(FileConstant.DEFAULTROOTPATH + fileMetaData.getRelativePath());
		//�����ļ����뵽��ͻ�ļ���
		iro.addConflictFile(fileManager.getMyFileObserver(FileConstant.DEFAULTROOTPATH + fileMetaData.getRelativePath()), path);
		
	}

}
