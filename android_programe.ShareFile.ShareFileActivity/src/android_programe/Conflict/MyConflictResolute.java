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
		//将冲突文件添加到conflictFile中
		iro.addConflictFile(fileManager.getMyFileObserver(FileConstant.DEFAULTSHAREPATH + relativePath), FileConstant.DEFAULTSHAREPATH + relativePath);
		
		//修改本地冲突文件名字，为"文件名 + 设备名 +修改日期"的形式
		String fileName = FileOperateHelper.getFileName(relativePath);
		long time = fileManager.getMyFileObserver(FileConstant.DEFAULTSHAREPATH + relativePath).getModifiedTime();
		String newFileName = fileRename(fileName,localDeviceId,FileUtil.getTimeFromLong(time));
		String newRelativePath = relativePath.substring(0, relativePath.length()-fileName.length()) + newFileName;
		//iro.renameLocalFile(fileID, relativePath,newRelativePath);
		//更新版本信息
		localVersionMap.put(remoteDeviceId, VersionManager.FILENOTEXIST);
		//创建文件名+remoteDeviceID的文件结点
		String path = FileConstant.DEFAULTSHAREPATH + relativePath + remoteDeviceId;
		iro.createEmptyFileNode(path, fileID);
		//更新remote设备的版本号
		fileManager.updateVersionMap(path, remoteDeviceId, remoteVersionMap.getVersionNumber(remoteDeviceId));
		//请求文件
		iro.fetchFile(remoteDeviceId, fileID, relativePath);
		
	}
	
	@Override
	public void resoluteConfliction(VersionMap localVersionMap,
			String localDeviceId, VersionMap remoteVersionMap,
			String remoteDeviceId, String relativePath,
			FileMetaData remoteMetaData, ConflictManager iro) {
		// TODO Auto-generated method stub
		iro.addConflictFile(fileManager.getMyFileObserver(FileConstant.DEFAULTSHAREPATH + relativePath), FileConstant.DEFAULTSHAREPATH + relativePath);
		
		//修改本地冲突文件名字，为"文件名 + 设备名 +修改日期"的形式
		String fileName = FileOperateHelper.getFileName(relativePath);
		long time = fileManager.getMyFileObserver(FileConstant.DEFAULTSHAREPATH + relativePath).getModifiedTime();
		String newFileName = fileRename(fileName,localDeviceId,FileUtil.getTimeFromLong(time));
		String newRelativePath = relativePath.substring(0, relativePath.length()-fileName.length()) + newFileName;
		iro.renameLocalFile(relativePath,newRelativePath);
		//更新版本信息
		localVersionMap.put(remoteDeviceId, VersionManager.FILENOTEXIST);
		//创建文件名+remoteDeviceID的文件结点
		String path = FileConstant.DEFAULTSHAREPATH + relativePath + remoteDeviceId;
		iro.createEmptyFileNode(path, remoteMetaData.getFileID());
		//更新remote设备的版本号
		fileManager.updateVersionMap(path, remoteDeviceId, remoteVersionMap.getVersionNumber(remoteDeviceId));
		//请求文件
		iro.fetchFile(remoteDeviceId, remoteMetaData.getFileID(), relativePath);
	}


	@Override
	public void receiveConflictFileData(String target,
			FileMetaData fileMetaData, File file,IResoluteOperator iro) {
		// TODO Auto-generated method stub
		String path = FileConstant.DEFAULTSHAREPATH + fileMetaData.getRelativePath();
		String suffix = getConflictFileSuffix(target,fileMetaData.getModifiedTime());
		String oldPath = path.substring(0, path.lastIndexOf(suffix)) + target;
		//更新metaData
		fileManager.updateMetaData(oldPath, fileMetaData);
		//更新本地的版本信息
		//fileManager.updateLocalVersion(oldPath, fileMetaData.getVersionID());
		//将文件移动到指定目录下
		FileOperateHelper.renameFile(file.getAbsolutePath(), path);
		//开始监听文件
		System.out.println("----MyConflictResolute----oldPath is:" + oldPath + ";newPath is:" + path);
		fileManager.modifyObserverPath(oldPath, path);
		fileManager.startObserverFile(path);
		//将该文件加入到冲突文件中
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
