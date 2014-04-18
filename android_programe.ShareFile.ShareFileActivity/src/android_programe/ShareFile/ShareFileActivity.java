package android_programe.ShareFile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.ActivityManager;
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
	private EditText disconnectEt;
	private Button disconnectBt;
	
	
	private SharedMem SharedMemService;
	private Intent serviceIntent;
	private SharedMem.MyBinder serviceBinder;
	
	private WifiManager wifiManager;
	private WifiInfo wifiInfo;
	private String serviceName = "android_programe.ShareFile.SharedMem";
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
        disconnectBt = (Button)findViewById(R.id.button4);
        disconnectEt = (EditText)findViewById(R.id.editText2);
        
        serviceIntent = new Intent(ShareFileActivity.this,SharedMem.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        System.out.println("oncreate ----------1");
        

       // logLine.connect("114.212.87.66");
        tv.setText("connected");     
        
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        IPTv.setText(getLocalAddress());
        

        bt1.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
		        System.out.println("click ----------1");
		        //if(!isServiceRunning(serviceName))
		        	startService(serviceIntent);
				
		        System.out.println("click ----------2");

			}
		});
        
        bt2.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopService(serviceIntent);
			}
		});
        
        connectBt.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String ip = IPEt.getText().toString();
				System.out.println("input ip is-----"+ip + "-----");
				
					if(SharedMemService != null){
						try {
							if(serviceBinder == null) System.out.println("serviceBinder is null");
							if(serviceBinder.connect(ip))
								{
								System.out.println("----ShareFileActivity----have connected to "+ip);
								Toast connectToast = Toast.makeText(ShareFileActivity.this, "Connected to " + ip, Toast.LENGTH_LONG);
								connectToast.show();
								}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else
						System.out.println("service hasn't been started");
				}
		});
        
        disconnectBt.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String ip = disconnectEt.getText().toString();
				if(SharedMemService != null){
					if(serviceBinder != null){
						serviceBinder.disconnect(ip);
						Toast disconnectToast = Toast.makeText(ShareFileActivity.this, "disconnected  " + ip, Toast.LENGTH_LONG);
						disconnectToast.show();
					}
				}
			}
		});
    }
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unbindService(serviceConnection);
	}
	
	



	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}



	private String getLocalAddress(){
		int ipAddress = wifiInfo.getIpAddress();    
        if(ipAddress==0)return null;  
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."  
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));  
	}
	
	public boolean isServiceRunning(String className) {

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
        getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList 
                   = activityManager.getRunningServices(40);

        if (!(serviceList.size()>0)) {
            return false;
        }

        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
	
	
    
   

    
   
}