package android_programe.Conflict;

import java.io.File;

import android_programe.FileSystem.VersionMap;
import android_programe.MemoryManager.FileMetaData;

/**
 * ��ͻ����
 * @author zhangsw
 *
 */
public abstract class ConflictResolute {
	//�����ļ�������ͻ
	public abstract void resoluteConfliction(String fildID,VersionMap localVersionMap,String localDeviceId,VersionMap remoteVersionMap,String remoteDeviceId,String relativePath,IResoluteOperator iro);
	
	public abstract void receiveConflictFileData(String target, FileMetaData fileMetaData,File file,String orginalName,IResoluteOperator iro);
}
