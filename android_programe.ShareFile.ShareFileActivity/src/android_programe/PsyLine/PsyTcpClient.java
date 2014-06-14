package android_programe.PsyLine;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android_programe.Util.*;


public class PsyTcpClient{

	private int timeout;
	private ExecutorService executorServiceSo = null;
	private final int POOL_SIZE = 8;				//�̳߳ش�С
	private PsyLine psyline;
	
	
	public PsyTcpClient(PsyLine p){
		timeout = 10000;
		int cpuCount = Runtime.getRuntime().availableProcessors();
		executorServiceSo = Executors.newFixedThreadPool(cpuCount*POOL_SIZE);
		psyline = p;
	}
	
	public boolean connect(String ip){
		if(psyline.getIndexByTargetID(ip) == -1){				//��δͬ���豸��������
			System.out.println("----PsyTcpClient----enter psytcpclient connect");
			Socket s = new Socket();
			try {
				s.connect(new InetSocketAddress(ip,FileConstant.TCPPORT), timeout);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			SocketIO socketIO = new SocketIO(ip,s,0,psyline);
			executorServiceSo.execute(socketIO);
			psyline.addSocket(socketIO);
			/*
			Responser res = new Responser(s,psyline);
			executorServiceRe.execute(res);*/
			return true;
		}
		return false;
	}
}
