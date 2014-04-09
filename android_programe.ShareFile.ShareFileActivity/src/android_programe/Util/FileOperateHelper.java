package android_programe.Util;

import java.io.File;

public class FileOperateHelper {

	
	
	//文件是否存在
	public static boolean fileExist(String path){
        File file = new File(path);
        if(file.exists()){
            return true;
        }
        return false;
    }
	
	//判断是否是文件夹
	public static boolean isDirectory(String path){
		File file = new File(path);
		if(file.isDirectory())
			return true;
		else return false;
	}
	
	//获取子文件
	public static File[] subFiles(String path){
		File file = new File(path);
		if(file.isDirectory()){
			File []files = file.listFiles();
			return files;
		}
		else return null;
	}
	
	//创建文件夹
	public static File makeDir(String path){
        File file = new File(path);
        if(!file.exists())
            file.mkdirs();
        return file;
    }
	
	public static boolean renameFile(String oldPath,String newPath){
		File file = new File(oldPath);
		File newFile = new File(newPath);
		return file.renameTo(newFile);
	}
	
	public static boolean deleteFile(String path){
		File file = new File(path);
		return file.delete();
	}
	
	public static String getFileName(String path){
		if(path != null){
			int index = path.lastIndexOf('/');
			String name = path.substring(index+1, path.length());
			if(name != null) return name;
		}
		return null;
	}
	
	public static long getFileLength(String path){
		File file = new File(path);
		return file.length();
	}
	
	public static long getFileModifiedTime(String path){
		File file = new File(path);
		return file.lastModified();
	}
}
