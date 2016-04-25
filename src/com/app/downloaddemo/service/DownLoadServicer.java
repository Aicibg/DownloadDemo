package com.app.downloaddemo.service;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.HttpStatus;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.app.downloaddemo.entity.FileInfo;

public class DownLoadServicer extends Service{
	public static final String DOWNLOAD_PATH=Environment.getExternalStorageDirectory().getAbsolutePath()+"/downloads/";
	public static final String ACTION_START="ACTION_START";
	public static final String ACTION_STOP="ACTION_STOP";
	public static final String ACTION_UPDATE="ACTION_UPDATE";
	public static final int MSG_INIT=1;
	private DownLoadTask downloadtask=null;
   @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
	// TODO Auto-generated method stub
	   //���activity���ݹ����Ĳ���
	   if(ACTION_START.equals(intent.getAction())){
		   FileInfo fileinfo=(FileInfo) intent.getSerializableExtra("fileinfo");
		   Log.i("test","start"+fileinfo.toString());
		   //������ʼ���߳�
		   new InitThread(fileinfo).start();
	   }else if(ACTION_STOP.equals(intent.getAction())){
		   FileInfo fileinfo=(FileInfo) intent.getSerializableExtra("fileinfo");
		   Log.i("test","stop"+fileinfo.toString());
		   if(downloadtask!=null){
			   downloadtask.ispause=true;
		   }
	   }
	return super.onStartCommand(intent, flags, startId);
  }
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_INIT:
				FileInfo fileinfo=(FileInfo) msg.obj;
				Log.i("test","init"+fileinfo.toString());
				//������������
				downloadtask=new DownLoadTask(DownLoadServicer.this, fileinfo);
				downloadtask.download();
				break;

			default:
				break;
			}
		};
	};
    
	/**
	 * ��ʼ�����߳�
	 */
	class InitThread extends Thread{
		private FileInfo fileinfo=null;

		public InitThread(FileInfo fileinfo) {
			this.fileinfo = fileinfo;
		}
		@Override
		public void run() {
			HttpURLConnection conn=null;
			RandomAccessFile raf=null;
			try {
				//���������ļ�
				URL url=new URL(fileinfo.getUrl());
		        conn = (HttpURLConnection) url.openConnection();
		        //���ó�ʱ
		        conn.setConnectTimeout(10000);
		        //��������ʽ
		        conn.setRequestMethod("GET");
		        int length=-1;
		        if(conn.getResponseCode()==HttpStatus.SC_OK){
		           //����ļ�����
		        	length=conn.getContentLength();
		        }
		        if(length<=0){
		        	return;
		        }
		        File dir=new File(DOWNLOAD_PATH);
		        if(!dir.exists()){
		        	dir.mkdir();
		        }
		        File file=new File(dir, fileinfo.getFilename());
		        raf=new RandomAccessFile(file, "rwd");
		        //�����ļ�����
		        raf.setLength(length);
		        fileinfo.setLength(length);
		        handler.obtainMessage(MSG_INIT, fileinfo).sendToTarget();
				//
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					raf.close();
					conn.disconnect();
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
	}
}
