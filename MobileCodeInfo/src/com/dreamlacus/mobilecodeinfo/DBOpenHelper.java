package com.dreamlacus.mobilecodeinfo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

public class DBOpenHelper extends SQLiteOpenHelper {
	private SQLiteDatabase mDB=null;

	public DBOpenHelper(Context context,String dbname,CursorFactory factory,int version) {
		super(context, dbname, factory, version);
	}
	public DBOpenHelper(Context context) {
		this(context,Environment.getExternalStorageDirectory()+"/dreamlacus/mobilecodeinfo.db", null, 1);
		//this(context,"/data/data/com.dreamlacus.mobilecodeinfo/databases/mobilecodeinfo.db", null, 1);
		mDB=this.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//db.execSQL("CREATE TABLE codeinfo(id integer primary key,num text,code text,city text,cardtype text)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
		

	public CodeInfo queryCodeInfo(String num) {
		 
		Cursor cur=mDB.rawQuery("SELECT * FROM codeinfo where num=?", new String[]{num});
		if(cur.moveToFirst()){
			CodeInfo ci=new CodeInfo();
			ci.setId(cur.getInt(0));
			ci.setNum(cur.getString(1));
			ci.setCode(cur.getString(2));
			ci.setCity(cur.getString(3));
			ci.setCardtype(cur.getString(4));
			cur.close();
			cur=null;
			return ci;
		}else{
			cur.close();
			cur=null;
			return null;
		}

	}

	public int insertCodeInfo(CodeInfo ci) {
		int  result=0;
		String num=ci.getNum();
		String code=ci.getCode();
		String city=ci.getCity();
		String cardtype=ci.getCardtype();
		try{
		mDB.execSQL("insert into codeinfo(num,code,city,cardtype)values(?,?,?,?)",
				new Object[]{num,code,city,cardtype});}
		catch(Exception e){
			e.printStackTrace();
			Log.i("errrr", e.toString());
		}
		return result;
	}

}
