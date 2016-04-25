package com.app.downloaddemo.db;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.app.downloaddemo.entity.ThreadInfo;

public class ThreadDaoImpl implements ThreadDao{
    private DBHelper mhelper=null;
    private static ThreadDaoImpl threadDaoimpl;
    
	private ThreadDaoImpl(Context context) {
		this.mhelper =new DBHelper(context);
	}
	
	public synchronized static ThreadDaoImpl getInstance(Context context){
		if(threadDaoimpl==null){
			threadDaoimpl=new ThreadDaoImpl(context);
		}
		return threadDaoimpl;
	}

	@Override
	public void insertThread(ThreadInfo threadinfo) {
		SQLiteDatabase db=mhelper.getWritableDatabase();
		db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
				new Object[]{threadinfo.getId(),threadinfo.getUrl(),threadinfo.getStart(),threadinfo.getEnd(),threadinfo.getFinished()});
		db.close();
	}

	@Override
	public void deleteThread(String url, int thread_id) {
		SQLiteDatabase db=mhelper.getWritableDatabase();
		db.execSQL("delete from thread_info where url = ? and thread_id =?",
				new Object[]{url,thread_id});
		db.close();
	}

	@Override
	public void updateThread(String url, int thread_id, int finished) {
		SQLiteDatabase db=mhelper.getWritableDatabase();
		db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?",
				new Object[]{finished,url,thread_id});
		db.close();
	}

	@Override
	public List<ThreadInfo> getThreads(String url) {
		SQLiteDatabase db=mhelper.getWritableDatabase();
		List<ThreadInfo> list=new ArrayList<ThreadInfo>();
		Cursor cursor=db.rawQuery("select * from thread_info where url = ?", new String[]{url});
		while(cursor.moveToNext()){
			ThreadInfo thread=new ThreadInfo();
			thread.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
			thread.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
			thread.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
			thread.setStart(cursor.getInt(cursor.getColumnIndex("start")));
			thread.setUrl(cursor.getString(cursor.getColumnIndex("url")));
			list.add(thread);
		}
		cursor.close();
		db.close();
		return list;
	}

	@Override
	public boolean isExists(String url, int thread_id) {
		SQLiteDatabase db=mhelper.getWritableDatabase();
		List<ThreadInfo> list=new ArrayList<ThreadInfo>();
		Cursor cursor=db.rawQuery("select * from thread_info where url = ? and thread_id = ?", new String[]{url,thread_id+""});
		boolean exists=cursor.moveToNext();
		cursor.close();
		db.close();
		return exists;
	}

}
