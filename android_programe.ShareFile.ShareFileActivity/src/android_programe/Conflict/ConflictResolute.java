package android_programe.Conflict;

import java.io.File;

import android_programe.FileSystem.FileMetaData;
import android_programe.FileSystem.VectorClock;

/**
 * 冲突消解
 * @author zhangsw
 *
 */
public abstract class ConflictResolute {
	//两个文件发生冲突
	
	public abstract void resoluteConfliction(String fildID,VectorClock localVersionMap,String localDeviceId,VectorClock remoteVersionMap,String remoteDeviceId,String relativePath,IResoluteOperator iro);
	
	public abstract boolean isConflictFile(String target,FileMetaData fileMetaData,File file);
	
	public abstract void receiveConflictFileData(String target, FileMetaData fileMetaData,File file,IResoluteOperator iro);

	public abstract void resoluteConfliction(VectorClock localVersionMap,
			String localDeviceId, VectorClock remoteVersionMap,
			String remoteDeviceId, String relativePath,
			FileMetaData remoteMetaData, ConflictManager iro);
}
