package android_programe.PsyLine;

import android_programe.Util.FileConstant;

public  class FileTransferHeader {
	
	/**
	 * �����ļ�����ǰ����Ϣ
	 * @param fileLength	�ļ�����
	 * @param fileID	�ļ�id
	 * @param relativePath	�ļ����·��
	 * @return
	 */
	public static String sendFileDataHeader(long fileLength){
		return FileConstant.FILEDATA + "$SIZE$" + fileLength + "\n";
	}
	
	/**
	 * �����ļ�versionǰ����Ϣ
	 * @param fileID	�ļ�id
	 * @param relativePath	�ļ����·��
	 * @return
	 */
	public static String sendFileVersionHeader(String relativePath,String tag){
		return FileConstant.FILEVERSION +"$TAG$" + tag + "$PATH$" + relativePath + "\n";
	}
	
	public static String sendFileUpdateHeader(){
		return FileConstant.FILEUPDATE + "\n";
	}
	
	public static String disconnect(){
		return FileConstant.DISCONNECT + "\n";
	}
	
	/**
	 * �����ѳ�ʼ��version��ϣ����Խ���ͬ��
	 * @return
	 */
	public static String synReady(){
		return FileConstant.SYNREADY + "\n";
	}
	
	public static String heartBeat(){
		return FileConstant.HEARTBEAT + "\n";
	}

	/**
	 * ɾ���ļ�����
	 * @param relativePath
	 * @return
	 */
	public static String deleteFileCmd(String relativePath){
		return FileConstant.DELETEFILE + "$PATH$" + relativePath+"\n";
	}
	
	public static String fetchFileCmd(String relativePath){
		return FileConstant.ASKFILE + "$PATH$" + relativePath + "\n";
	}
	
	public static String makeDirCmd(String relativePath){
		return FileConstant.MAKEDIR + "$PATH$" + relativePath + "\n";
	}
	
	public static String renameFileCmd(String oldRelativePath,String newRelativePath){
		return FileConstant.RENAMEFILE + "$OLDPATH$" + oldRelativePath + "$NEWPATH$" + newRelativePath + "\n";
	}
}
