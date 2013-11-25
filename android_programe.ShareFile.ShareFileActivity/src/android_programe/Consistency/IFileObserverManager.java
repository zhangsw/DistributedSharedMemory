package android_programe.Consistency;

import android.os.Handler;

public interface IFileObserverManager{
	public void deleteObserver(String path);
	public MyFileObserver registerObserver(String target,String absolutePath,Handler handler,String fatherPath);
	public void updateObserverMap(String path,String newPath);
	public void modifyObserverPath(String path,String newPath);
	public void withdrowObserver(String target,String path);
	
}
