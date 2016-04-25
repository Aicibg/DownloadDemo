package com.app.downloaddemo;

import com.app.downloaddemo.db.ThreadDao;
import com.app.downloaddemo.db.ThreadDaoImpl;
import com.app.downloaddemo.entity.FileInfo;
import com.app.downloaddemo.entity.ThreadInfo;
import com.app.downloaddemo.service.DownLoadServicer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {
  private TextView tvfilename;
  private Button btStart,btStop,btAdd,btDelete;
  private ProgressBar progressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//初始化控件
		initView();
		//
		final FileInfo fileinfo=new FileInfo(0, 
				"http://www.imooc.com/mobile/imooc.apk",
				"imooc.apk", 0, 0);
		tvfilename.setText(fileinfo.getFilename());
		btStart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,DownLoadServicer.class);
				intent.setAction(DownLoadServicer.ACTION_START);
				intent.putExtra("fileinfo", fileinfo);
				startService(intent);
			}
		});
		
		btStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this,DownLoadServicer.class);
				intent.setAction(DownLoadServicer.ACTION_STOP);
				intent.putExtra("fileinfo", fileinfo);
				startService(intent);
			}
		});
		//注册广播
		IntentFilter filter=new IntentFilter();
		filter.addAction(DownLoadServicer.ACTION_UPDATE);
		registerReceiver(receiver, filter);
		
		btAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ThreadDao dbdao=ThreadDaoImpl.getInstance(MainActivity.this);
				dbdao.insertThread(new ThreadInfo(0, "www.baidu.com", 1, 5, 3));
			}
		});
		
		btDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ThreadDao dbdao=ThreadDaoImpl.getInstance(MainActivity.this);
				dbdao.updateThread("www.baidu.com", 0, 10);
			}
		});
	}
	
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	};
	
	BroadcastReceiver receiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			if(DownLoadServicer.ACTION_UPDATE.equals(intent.getAction())){
				int finished=intent.getIntExtra("finished", 0);
				progressBar.setProgress(finished);
			}
		}
		
	};
	
	private void initView() {
		tvfilename=(TextView) findViewById(R.id.tv_filename);
		btStart=(Button) findViewById(R.id.bt_start);
		btStop=(Button) findViewById(R.id.bt_stop);
		btAdd=(Button) findViewById(R.id.bt_add);
		btDelete=(Button) findViewById(R.id.bt_delete);
		progressBar=(ProgressBar) findViewById(R.id.progressBar1);
		progressBar.setMax(100);
	}

}
