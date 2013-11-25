package android_programe.ShareFile;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android_programe.Consistency.Consistency;
import android_programe.Consistency.FileObserverManager;

public class TestActivity extends Activity{

	private FileObserverManager fom;
	private Consistency con;
	Button bt1;
	Button bt2;
	String path;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		//fom = new FileObserverManager();
		try {
			System.out.println("- 1 -");
			con = new Consistency();
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
				
					//fom = new FileObserverManager();
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
				
				con.addShareDevice(path+"/wallpaper", "03", 0);
				con.addShareDevice(path + "/wallpaper/ak1", "01", 0);
					
			}
		});
		
		bt2.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//fom.withdrowObserver("01",path+"/wallpaper/13/e");
				//fom.withdrowObserver("03",path+"/wallpaper/13");
				//fom.withdrowObserver("02",path+"/wallpaper/tyy");
			}
		});
		
		
		
	}

}
