package com.menny.android.anysoftkeyboard.backup;

import java.lang.reflect.InvocationTargetException;

import com.menny.android.anysoftkeyboard.Workarounds;

public class CloudBackupRequester {

	
	protected static final String TAG = "ASK BKUP";
	private static CloudBackupRequester msRequester = null;
	
	public synchronized static void createRequesterInstance(String packageName)
	{
		if (Workarounds.getApiLevel() < 8)
			msRequester = new CloudBackupRequester(packageName);
		else
		{
			try {
				
				Class<?> theClass = Class.forName("com.menny.android.anysoftkeyboard.backup.CloudBackupRequesterApi8");
				msRequester = (CloudBackupRequester)theClass.getConstructor(String.class).newInstance(packageName);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				msRequester = null;
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				msRequester = null;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				msRequester = null;
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				msRequester = null;
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				msRequester = null;
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				msRequester = null;
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				msRequester = null;
			}
			
			if (msRequester == null)
				msRequester = new CloudBackupRequester(packageName);
		}
	}
	
	public synchronized static void requestPrefsBackupToTheCloud()
	{
		if (msRequester != null)
			msRequester.notifyBackupManager();
	}
	
	private final String mPackageName;

	public CloudBackupRequester(String packageName)
	{
		mPackageName = packageName;
	}
	
	void notifyBackupManager()
	{
		//this is an empty implementation. This is available from API8 or higher
	}
	
	String getPackageName() {
		return mPackageName;
	}
}
