package android_programe.Util;

import android.os.Environment;

public class FileConstant {
	/** TCP Э��Ķ˿�*/
	public static final int TCPPORT = 7000;
	
	/**ɾ���ļ�*/
	public static final int DELETEFILE = 25;
	
	/**�ļ���Ϣ��Ϣ*/
	public static final int FILEINF = 26;
	
	/**�����ļ���Ϣ*/
	public static final int ASKFILE = 27;
	
	/**�ļ�����*/
	public static final int FILEDATA = 28;
	
	/**�������ļ�*/
	public static final int RENAMEFILE = 29;
	
	public static final int MAKEDIR = 30;
	
	/**�򵥴ֱ�һ����*/
	public static final int SIMPLECM = 0;
	
	/**�����ļ���������*/
	public static final int SENDFILEMESSAGE = 10;
	
	/**ɾ���ļ�����*/
	public static final int DELETEFILEMESSAGE = 11;
	
	/**�������ļ�����*/
	public static final int RENAMEFILEMESSAGE = 12;
	
	/**�ƶ��ļ�����*/
	public static final int MOVEFILEMESSAGE = 13;
	
	/**�����ļ�������*/
	public static final int CREATEDIRMESSAGE = 14;
	
	/**ɾ���ļ�������*/
	public static final int DELETEDIRMESSAGE = 15;
	
	public static final int MOVEDIRMESSAGE = 16;
	
	public static final int RENAMEDIRMESSAGE = 17;
	
	
	/***/
	public static final int ISDIR = 0x40000000;
	
	public static String ROOTPATH = Environment.getExternalStorageDirectory().getPath();
}
