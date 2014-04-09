package android_programe.Conflict;

import android_programe.FileSystem.VersionMap;

/**
 * ��ͻ���
 * @author zhangsw
 *
 */
public class ConflictDetect {
	
	public ConflictDetect(){
		
	}
	
	/**
	 * ������versionMap���бȽ�
	 * @param localVersionMap
	 * @param localDeviceId
	 * @param remoteVersionMap
	 * @param remoteDeviceId
	 * @return
	 */
	public int detect(VersionMap localVersionMap,String localDeviceId,VersionMap remoteVersionMap,String remoteDeviceId){
		//��ȡ����versionMap�б���ı��ذ汾��
		int localNumberInLocalVersion = localVersionMap.getVersionNumber(localDeviceId);
		if(localVersionMap.getVersionNumber(remoteDeviceId) == null) System.out.println("version numbers is null");
		//��ȡ����versinMap�б����Զ�˰汾��
		int remoteNumberInLocalVersion = localVersionMap.getVersionNumber(remoteDeviceId);
		
		//��ȡԶ��versionMap�б���ı��ذ汾��
		int localNumberInRemoteVersion = remoteVersionMap.getVersionNumber(localDeviceId);
		
		//��ȡԶ��versionMap�б����Զ�˰汾��
		int remoteNumberInRemoteVersion = remoteVersionMap.getVersionNumber(remoteDeviceId);
		
		System.out.println("localNumberInLocalVersion is:" + localNumberInLocalVersion);
		System.out.println("remoteNumberInLocalVersion is:" + remoteNumberInLocalVersion);
		System.out.println("localNumberInRemoteVersion is:" + localNumberInRemoteVersion);
		System.out.println("remoteNumberInRemoteVersion is:" + remoteNumberInRemoteVersion);
		if(localNumberInLocalVersion > localNumberInRemoteVersion){		//���ذ汾�Ŵ�����Զ�˱���ı��ذ汾�ţ���Զ�˻�δ��ñ��صİ汾����
			if(remoteNumberInLocalVersion < remoteNumberInRemoteVersion){	//���ر����Զ�˰汾��С��Զ�˱����Զ�˰汾�ţ������ػ�δ���Զ�˵İ汾����
				//������ͻ,˫���ڶ�δ��öԷ��ĸ��µ�����£��ڸ��Ե��豸�Ϸ������޸�
				//TODO
				System.out.println("---conflict occurs---");
				return ConflictManager.CONFLICT;
				
			}
			else if(remoteNumberInLocalVersion == remoteNumberInRemoteVersion){ 	//�����Ѿ����Զ�˵ĸ���
				//Զ����Ҫ�õ�����
				System.out.println("---remote needs update---");
				return ConflictManager.REMOTENEEDUPDATE;
				
			}
			else{		//���ر����Զ�˰汾�Ŵ���Զ�˱����Զ�˰汾�ţ��������δ֪
				System.out.println("---unknowing situation---");
			}
		}
		else if(localNumberInLocalVersion == localNumberInRemoteVersion){		//Զ���Ѿ���Ϥ�˱��ص��޸�
			if(remoteNumberInLocalVersion < remoteNumberInRemoteVersion){
				//���ػ�δ��ϤԶ�˵ĸ��£�������Ҫ����
				System.out.println("---local needs update---");
				localVersionMap.put(remoteDeviceId, remoteNumberInRemoteVersion);	//���±��ص�versionMap��
				return ConflictManager.LOCALNEEDUPDATE;
			}
			else if(remoteNumberInLocalVersion == remoteNumberInRemoteVersion){
				//˫������Ϥ�˶Է��İ汾
				System.out.println("---both know the versionMap");
				return ConflictManager.BOTHKNOW;
			}
			else{	//�������δ֪
				System.out.println("---unknowing situation---");
			}
		}
		
		return -1;
		
	}
}
