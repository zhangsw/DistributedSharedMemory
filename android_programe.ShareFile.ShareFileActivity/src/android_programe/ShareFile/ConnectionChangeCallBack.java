package android_programe.ShareFile;

public interface ConnectionChangeCallBack {
	
	public void wifiDisabled();
	
	/**
	 * �����豸��������wifi
	 */
	public void wifiConnected();
	
	/**
	 * �����豸�Ͽ���wifi
	 */
	public void wifiDisconnected();
	
}
