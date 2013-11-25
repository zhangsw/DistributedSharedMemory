package android_programe.PsyLine;

import java.io.*;
import java.net.*;


public class SocketInf {
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private String ip;
	private int type;							//0为client连接，1为server连接
	private boolean tag;
	
	public SocketInf(String ip,Socket socket,int type){
		try {
			this.socket = socket;
			this.type = type;
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			this.ip = ip;
			tag = true;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Socket getSocket(){
		return socket;
	}
	
	public DataInputStream getDataInputStream(){
		return dis;
	}
	
	public DataOutputStream getDataOutputStream(){
		return dos;
	}
	
	public synchronized boolean getTag(){
		if(tag == true){
			tag = false;
			return true;
		}
		else return false;
	}
	
	public synchronized void releaseTag(){
		tag = true;
	}
	
	public String getIp(){
		return ip;
	}
	
	public int getType(){
		return type;
	}
	
	public void close() throws IOException{
		dis.close();
		dos.close();
		socket.close();
	}
	
}
