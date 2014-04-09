package android_programe.Storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Handler;

public class StorageOperator {
	
	private static String VERSIONNUMBER = "  $VersionNumber:";
	
	public static void writeVersionNumber(BufferedWriter bw,String filePath,int versionNumber) throws IOException{
		bw.write(versionFormat(filePath,versionNumber));
		bw.flush();
	}
	
	public static void writeVersionNumber(BufferedWriter bw,LinkedHashMap<String,Integer> versionMap) throws IOException{
		Iterator<Entry<String, Integer>> iter = versionMap.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String,Integer> entry =(Map.Entry<String,Integer>)iter.next();
			bw.write(versionFormat(entry.getKey(),entry.getValue()));
		}
		bw.flush();
		
	}
	
	public static LinkedHashMap<String,Integer> readVersionNumber(BufferedReader br) throws IOException{
		LinkedHashMap<String,Integer> map = new LinkedHashMap<String,Integer>();
		String line;
		while((line = br.readLine()) != null){
			//TODO readUTF»áÅ×³öeofexception
			int splitIndex = line.indexOf(VERSIONNUMBER);
			String path = line.substring(0, splitIndex);
			String version = line.substring(splitIndex + VERSIONNUMBER.length(),line.length());
			int versionNumber = Integer.parseInt(version);
			map.put(path, versionNumber);
			System.out.println("-----StorageOperator-----path :" + path + ",versionNumber:" + versionNumber);
		}
		return map;
	}
	
	private static String versionFormat(String filePath,int versionNumber){
		return filePath + VERSIONNUMBER + versionNumber + "\n";
	}
}
