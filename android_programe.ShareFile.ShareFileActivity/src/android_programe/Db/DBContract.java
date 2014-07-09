package android_programe.Db;

import android.provider.BaseColumns;

/**
 * Contract class for database,keep global constants used for other classes
 * @author zhangsw
 *
 */

public final class DBContract {
	
	public DBContract(){
		
	}
	
	public static abstract class MetaDataEntry implements BaseColumns {
		
		public static final String TABLE_NAME = "metadata";
		
		public static final String COLUMN_NAME_DIRECTORY = "directory";
		
		public static final String COLUMN_NAME_VERSION = "version";
		
		//public static final String COLUMN_NAME_PREVERSION = "preversion";
		
		public static final String COLUMN_NAME_AUTHOR = "author";
		
		public static final String COLUMN_NAME_SIZE = "size";
		
		public static final String COLUMN_NAME_MODIFIEDTIME = "modified_time";
		
		
	}
	
	public static abstract class Version implements BaseColumns {
		
		public static final String COLUMN_NAME_PREVERSION = "preversion";
	}
	
}
