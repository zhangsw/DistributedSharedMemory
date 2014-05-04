package android_programe.ShareFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android_programe.Conflict.ConflictDetect;
import android_programe.FileSystem.FileManager;
import android_programe.FileSystem.VersionMap;
import android_programe.MemoryManager.MemoryManager;
import android_programe.Util.FileConstant;

public class TestActivity extends Activity{

	private FileManager fom;
	private MemoryManager con;
	Button bt1;
	Button bt2;
	String path;
	private int num = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		//fom = new FileObserverManager();
		try {
			System.out.println("- 1 -");
			con = new MemoryManager("AA");
			System.out.println("- 2 -");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		path = Environment.getExternalStorageDirectory().getPath();
		bt1 = (Button)findViewById(R.id.testbutton1);
		bt2 = (Button)findViewById(R.id.testbutton2);
		bt1.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
					fom = new FileManager(FileConstant.DEFAULTSHAREPATH,"abc");
					//fom.registerObserver("01", path+"/wallpaper/13/e", null, null);
					//File file = new File(path+"/wallpaper/13/e");
					//System.out.println(file.getAbsolutePath());
					//System.out.println(file.getPath());
					//System.out.println(file.getName());
					//System.out.println(file.getParent());
					//fom.registerObserver("02", path+"/wallpaper/13/e", null, null);
					//fom.registerObserver("02", path+"/wallpaper/tyy", null, null);
					//fom.registerObserver("03", path+"/wallpaper/", null, null);
					//fom.registerObserver("01", path + "/wallpaper/ak1", null,null);
				
				//con.addShareDevice(path+"/wallpaper", "03", 0);
				//con.addShareDevice(path + "/wallpaper/ak1", "01", 0);
				/*
				ConflictDetect cd = new ConflictDetect();
				String local = "local";
				String remote = "remote";
				VersionMap localVersionMap = new VersionMap();
				VersionMap remoteVersionMap = new VersionMap();
				localVersionMap.put(local, 0);
				localVersionMap.put(remote, 0);
				remoteVersionMap.put(remote, 0);
				remoteVersionMap.put(local, 0);
				
				localVersionMap.put(local, 1);
				cd.detect(localVersionMap, local, remoteVersionMap, remote);
				
				remoteVersionMap.put(local, 1);
				cd.detect(localVersionMap, local, remoteVersionMap, remote);
				
				remoteVersionMap.put(remote, 1);
				cd.detect(localVersionMap, local, remoteVersionMap, remote);
					*/
			}
		});
		
		bt2.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//fom.withdrowObserver("01",path+"/wallpaper/13/e");
				//fom.withdrowObserver("03",path+"/wallpaper/13");
				//fom.withdrowObserver("02",path+"/wallpaper/tyy");
				/*
				try {
					DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(FileConstant.DEFAULTSHAREPATH+"/cc")));
					String line = dis.readUTF();
					while( line!= null){
						System.out.println(line);
						line = dis.readUTF();
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}catch (EOFException e){
					System.out.println("file complish reading");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				System.out.println("- 3 -");
				FileWriter fw;
				try {
					fw = new FileWriter(FileConstant.DEFAULTAPPPATH + "/文件d.txt");
					fw.write("abc");
					fw.flush();
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				File file = new File(FileConstant.DEFAULTSHAREPATH + "/文件d.txt");
				File file1 = new File(FileConstant.DEFAULTSHAREPATH + "/文件d.txt");
				file.renameTo(file1);
			}
		});
		
		
		
	}

}
