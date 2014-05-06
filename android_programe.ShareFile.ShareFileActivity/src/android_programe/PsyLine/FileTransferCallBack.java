package android_programe.PsyLine;

import java.io.File;

import android_programe.FileSystem.VersionMap;
import android_programe.MemoryManager.FileMetaData;

public interface FileTransferCallBack {
	public void connectionFailure(String id);

	public void receiveFileData(String ip, FileMetaData fileMetaData, File file);

	public boolean receiveFileInf(String ip, String relativePath, String string,
			String mD5);

	public boolean receiveAskFile(String ip, String relativePath, String string);

	public boolean receiveDeleteFile(String ip, String string);

	public boolean receiveRenameFile(String ip, String string, String string2);

	public boolean receiveMakeDir(String ip, String string);

	public boolean receiveVersionMap(String ip, VersionMap versionMap,
			String fileID, String relativePath, String tag);

	public void receiveFileUpdate(String ip, FileMetaData fileMetaData);

	public void receiveDisconnect(String ip);

	public void receiveSynReady(String ip);

	public void hasDisconnected(String targetID);
}
