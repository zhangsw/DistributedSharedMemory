package android_programe.Util;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

import android_programe.Consistency.FileInf;

public class FileUtil {

	public static String getFileMD5(File file){
		 if (!file.isFile()){
		      return null;
		    }
		    MessageDigest digest = null;
		    FileInputStream in=null;
		    byte buffer[] = new byte[1024];
		    int len;
		    try {
		      digest = MessageDigest.getInstance("MD5");
		      in = new FileInputStream(file);
		      while ((len = in.read(buffer, 0, 1024)) != -1) {
		        digest.update(buffer, 0, len);
		      }
		      in.close();
		    } catch (Exception e) {
		      e.printStackTrace();
		      return null;
		    }
		    BigInteger bigInt = new BigInteger(1, digest.digest());
		    return bigInt.toString(16);
	}
	
	public static void getFileInfList(String path,List <FileInf>fileList){
		File filePath = new File(path);
		if(filePath.isDirectory()){
			File []files = filePath.listFiles();
			if(files == null) return;
			for(int i=0;i<files.length;i++)
				getFileInfList(files[i].getAbsolutePath(),fileList);
		}
		else if(filePath.isFile()){
			String filename = filePath.getName();
			String absolutepath = filePath.getAbsolutePath();
			String relativepath = absolutepath.substring(absolutepath.indexOf(path)+path.length());
			String MD5 = FileUtil.getFileMD5(filePath);
			FileInf fileInf = new FileInf(filename,absolutepath,MD5,relativepath);
			fileList.add(fileInf);
		}
	}
	
	public static String getFileNameFromPath(String path){
		if(path != null){
			int index = path.lastIndexOf('/');
			String name = path.substring(index+1, path.length());
			if(name != null) return name;
		}
		return null;
	}
}
