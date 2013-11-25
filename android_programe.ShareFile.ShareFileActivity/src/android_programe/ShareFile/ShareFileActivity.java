package android_programe.ShareFile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.*;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android_programe.LogLine.LogLine;
import android_programe.ShareFile.SharedMem.MyBinder;

public class ShareFileActivity extends Activity {
    /** Called when the activity is first created. */
	
	private TextView tv;
	private Button bt1;
	private Button bt2;
	private EditText IPEt;
	private Button connectBt;
	private TextView IPTv;
	
	private SharedMem SharedMemService;
	private Intent serviceIntent;
	private SharedMem.MyBinder serviceBinder;
	
	private WifiManager wifiManager;
	private WifiInfo wifiInfo;
	private ServiceConnection serviceConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			SharedMemService = ((SharedMem.MyBinder) service).getService();
			serviceBinder = (MyBinder) service;
			Toast.makeText(ShareFileActivity.this, "Service Connected.", Toast.LENGTH_LONG)
					.show();

		}

		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			SharedMemService = null;
			Toast.makeText(ShareFileActivity.this, "Service failed.", Toast.LENGTH_LONG).show();
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
        tv = (TextView)findViewById(R.id.tv);
        bt1 = (Button)findViewById(R.id.button1);
        bt2 = (Button)findViewById(R.id.button2);
        connectBt = (Button)findViewById(R.id.button3);
        IPEt = (EditText)findViewById(R.id.editText1);
        IPTv = (TextView)findViewById(R.id.textView1);
        
        serviceIntent = new Intent(ShareFileActivity.this,SharedMem.class);
        System.out.println("oncreate ----------1");
        

       // logLine.connect("114.212.87.66");
        tv.setText("connected");     
        System.out.println("oncreate ----------2");
        
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        IPTv.setText(getLocalAddress());

        bt1.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//logLine.sendFile("114.212.87.66", "/sdcard/wallpaper/PicOdsfkq.jpg", "PicOdsfkq.jpg");
				//serviceIntent.putExtra("IP", "114.212.87.66");
				//serviceIntent.putExtra("path", "/sdcard/wallpaper/");
				bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
		        System.out.println("click ----------1");

				startService(serviceIntent);
				
		        System.out.println("click ----------2");

			}
		});
        
        bt2.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopService(serviceIntent);
				unbindService(serviceConnection);
			}
		});
        
        connectBt.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String ip = IPEt.getText().toString();
				System.out.println("input ip is "+ip);
				
					if(SharedMemService != null){
						try {
							if(serviceBinder == null) System.out.println("serviceBinder is null");
							if(serviceBinder.connect(ip))
								System.out.println("having connected to "+ip);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
						System.out.println("service hasn't been started");
				}
		});
        
        
        
    }
	
	private String getLocalAddress(){
		int ipAddress = wifiInfo.getIpAddress();    
        if(ipAddress==0)return null;  
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."  
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));  
	}
	
	
    
   

    
   
}