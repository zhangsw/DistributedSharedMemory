package android_programe.Db;

import android.provider.BaseColumns;

public final class DBContract {
	
	public DBContract(){
		
	}
	
	public static abstract class MetaDataEntry implements BaseColumns {
		
		public static final String TABLE_NAME = "metadata";
		
		public static final String COLUMN_NAME_DIRECTORY = "directory";
		
		public static final String COLUMN_NAME_VERSION = "version";
		
		public static final String COLUMN_NAME_PRERVERSION = "preversion";
		
		public static final String COLUMN_NAME_AUTHOR = "author";
		
		public static final String COLUMN_NAME_SIZE = "size";
		
		
	}
	
	public static abstract class Version implements BaseColumns {
		
		public static final String COLUMN_NAME_PREVERSION = "preversion";
	}
	
}
