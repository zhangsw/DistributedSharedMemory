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
		//将冲突文件添加到conflictFile中
		iro.addConflictFile(fileManager.getMyFileObserver(FileConstant.DEFAULTROOTPATH + relativePath), FileConstant.DEFAULTROOTPATH + relativePath);
		
		//修改本地冲突文件名字，为"修改日期 + 文件名 "的形式
		String fileName = FileOperateHelper.getFileName(relativePath);
		long time = fileManager.getMyFileObserver(FileConstant.DEFAULTROOTPATH + relativePath).getModifiedTime();
		String newFileName = "("+FileUtil.getTimeFromLong(time)+")"+fileName;
		String newRelativePath = relativePath.substring(0, relativePath.length()-fileName.length()) + newFileName;
		iro.renameLocalFile(fileID, relativePath,newRelativePath);
		//更新版本信息
		localVersionMap.put(remoteDeviceId, VersionManager.FILENOTEXIST);
		//创建文件名+remoteDeviceID的文件结点
		String path = FileConstant.DEFAULTROOTPATH + relativePath + remoteDeviceId;
		iro.createEmptyFileNode(path, fileID);
		//请求文件
		iro.fetchFile(remoteDeviceId, fileID, relativePath);
		
	}


	@Override
	public void receiveConflictFileData(String target,
			FileMetaData fileMetaData, File file,String orginalName,IResoluteOperator iro) {
		// TODO Auto-generated method stub
		int index = fileMetaData.getRelativePath().indexOf(file.getName());
		String relativePath = fileMetaData.getRelativePath().substring(0, index)+orginalName ;
		String path = FileConstant.DEFAULTROOTPATH + relativePath + target;
		//更新metaData
		fileManager.updateMetaData(path, fileMetaData);
		//更新本地的版本信息
		fileManager.updateLocalVersion(path, fileMetaData.getVersionID());
		//将文件移动到指定目录下
		FileOperateHelper.renameFile(file.getAbsolutePath(), FileConstant.DEFAULTROOTPATH + fileMetaData.getRelativePath());
		//开始监听文件
		fileManager.startObserverFile(FileConstant.DEFAULTROOTPATH + fileMetaData.getRelativePath());
		//将该文件加入到冲突文件中
		iro.addConflictFile(fileManager.getMyFileObserver(FileConstant.DEFAULTROOTPATH + fileMetaData.getRelativePath()), path);
		
	}

}
