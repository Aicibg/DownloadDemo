package com.app.downloaddemo.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.content.Intent;

import com.app.downloaddemo.db.ThreadDao;
import com.app.downloaddemo.db.ThreadDaoImpl;
import com.app.downloaddemo.entity.FileInfo;
import com.app.downloaddemo.entity.ThreadInfo;

public class DownLoadTask {
   private Context context;
   private FileInfo fileinfo;
   private int mfinished=0;
   public boolean ispause=false;
   private ThreadDao threadDao=null;
   
public DownLoadTask(Context context, FileInfo fileinfo) {
	this.context = context;
	this.fileinfo = fileinfo;
	this.threadDao=ThreadDaoImpl.getInstance(context);
}

  public void download(){
	  try {
		//读取数据库线程信息
		  List<ThreadInfo> threadinfos=threadDao.getThreads(fileinfo.getUrl());
		  ThreadInfo threadinfo=null;
		  if(threadinfos.size()==0){
			  //初始化线程信息对象
			  threadinfo=new ThreadInfo(0, fileinfo.getUrl(), 0,fileinfo.getLength(),0);
		  }else{
			  threadinfo=threadinfos.get(0);
		  }
		  //创建子线程进行下载
		  new DownLoadThread(threadinfo).start();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }
  
   class DownLoadThread extends Thread{
	   private ThreadInfo threadinfo=null;

	public DownLoadThread(ThreadInfo threadinfo) {
		this.threadinfo = threadinfo;
	}
	   @Override
	public void run() {
		//向数据库插入线程数据
		   if(!threadDao.isExists(threadinfo.getUrl(), threadinfo.getId())){
			  threadDao.insertThread(threadinfo);   
		   }
		   HttpURLConnection conn=null;
		   RandomAccessFile raf=null;
		   InputStream input=null;
		   try {
			   URL url=new URL(threadinfo.getUrl());
			  conn=(HttpURLConnection) url.openConnection();
			  conn.setConnectTimeout(3000);
			  conn.setRequestMethod("GET");
			  //设置下载位置
			  int start=threadinfo.getStart()+threadinfo.getFinished();
			  conn.setRequestProperty("Range", "bytes="+start+"-"+threadinfo.getEnd());
			  //设置文件写入位置
			  File file=new File(DownLoadServicer.DOWNLOAD_PATH,fileinfo.getFilename());
			   raf=new RandomAccessFile(file, "rwd");
			  raf.seek(start);
			  Intent intent=new Intent(DownLoadServicer.ACTION_UPDATE);
			  mfinished+=threadinfo.getFinished();
			   //开始下载
			  if(conn.getResponseCode()==HttpStatus.SC_PARTIAL_CONTENT){
				  //读取数据
				  input=conn.getInputStream();
				  byte[] buffer=new byte[1024*4];
				  int len=-1;
				  long time=System.currentTimeMillis();
				  while((len=input.read(buffer))!=-1){
					  //写入文件
					  raf.write(buffer,0,len);
					  //把下载进度发送广播给activity
					  mfinished+=len;
					  if(System.currentTimeMillis()-time>500){
						  time=System.currentTimeMillis();
						  intent.putExtra("finished",mfinished*100/fileinfo.getLength());
						  context.sendBroadcast(intent);
					  }
					  //在下载暂停时，保存下载进度
					  if(ispause){
						  threadDao.updateThread(threadinfo.getUrl(), threadinfo.getId(), mfinished);
						  return;
					  }
				  }
				  //删除线程信息
				 threadDao.deleteThread(threadinfo.getUrl(),threadinfo.getId());
			  }
			 
			 
			 
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
		   try {
			conn.disconnect();
			   input.close();
			   raf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
		   
		
	}
   }
}
