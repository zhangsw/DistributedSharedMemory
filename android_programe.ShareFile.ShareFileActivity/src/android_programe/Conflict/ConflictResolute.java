package android_programe.Conflict;

import java.io.File;

import android_programe.FileSystem.VersionMap;
import android_programe.MemoryManager.FileMetaData;

/**
 * 冲突消解
 * @author zhangsw
 *
 */
public abstract class ConflictResolute {
	//两个文件发生冲突
	public abstract void resoluteConfliction(String fildID,VersionMap localVersionMap,String localDeviceId,VersionMap remoteVersionMap,String remoteDeviceId,String relativePath,IResoluteOperator iro);
	
	public abstract void receiveConflictFileData(String target, FileMetaData fileMetaData,File file,String orginalName,IResoluteOperator iro);
}
