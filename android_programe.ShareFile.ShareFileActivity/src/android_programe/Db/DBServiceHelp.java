package android_programe.Db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android_programe.Db.DBContract.MetaDataEntry;

public class DBServiceHelp extends SQLiteOpenHelper{
	
	private final static String DATABASE_NAME = "MetaData.db";
	private final static int DATABASE_VERSION = 1;
	private final static String COMMA_SEP = ",";
	

	public DBServiceHelp(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "CRETE TABLE" + MetaDataEntry.TABLE_NAME + "(" + MetaDataEntry.COLUMN_NAME_DIRECTORY + " TEXT,"
				+ MetaDataEntry.COLUMN_NAME_VERSION + " INTEGER," + MetaDataEntry.COLUMN_NAME_AUTHOR + " TEXT,"
				+ MetaDataEntry.COLUMN_NAME_SIZE + " INTEGER," + MetaDataEntry.COLUMN_NAME_PRERVERSION + " INTEGER" +")";
		db.execSQL(sql);		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
