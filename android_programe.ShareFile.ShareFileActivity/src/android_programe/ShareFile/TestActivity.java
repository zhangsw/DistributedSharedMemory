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
			}
		});
		
		bt2.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
