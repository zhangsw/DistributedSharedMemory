package android_programe.Conflict;

import java.util.ArrayList;
import java.util.List;

import android_programe.FileSystem.MyFileObserver;

/**
 * ���ڱ����໥���ͻ���ļ�
 * @author zhangsw
 *
 */
public class ConflictFileNode {

	private String path;
	private List<MyFileObserver> conflictFile;
	
	public ConflictFileNode(String path){
		this.path = path;
		conflictFile = new ArrayList<MyFileObserver>();
	}
	
	/**
	 * ���һ���ļ�
	 * @param ob
	 */
	public void add(MyFileObserver ob){
		if(!fileExist(ob))
			conflictFile.add(ob);
	}
	
	/**
	 * ɾ��һ���ļ�
	 * @param ob
	 */
	public void remove(MyFileObserver ob){
		int index = getIndex(ob);
		if(index >= 0) conflictFile.remove(index);
	}
	
	/**
	 * �Ƿ�Ϊ��
	 * @return
	 */
	public boolean isEmpty(){
		if(conflictFile.size() == 0) return true;
		else return false;
	}
	
	/**
	 * ��ͻ�ļ��ĸ���
	 * @return
	 */
	public int size(){
		return conflictFile.size();
	}
	
	public MyFileObserver getFileObserver(int index){
		if((conflictFile.size() >= index) &&(index >= 0)){
			return conflictFile.get(index);
		}
		else return null;
	}
	
	public MyFileObserver getLocalFileObserver(){
		return conflictFile.get(0);
	}
	
	private boolean fileExist(MyFileObserver ob){
		for(MyFileObserver fileObserver:conflictFile)
			if(fileObserver.getPath().equals(ob.getPath())) return true;
		return false;
	}
	
	/**
	 * ��ȡ�ļ��±꣬����ļ������ڣ�����-1
	 * @param ob
	 * @return
	 */
	private int getIndex(MyFileObserver ob){
		for(int i=0;i<conflictFile.size();i++){
			if(conflictFile.get(i).getPath().equals(ob.getPath()))
				return i;
		}
		return -1;
	}
}
