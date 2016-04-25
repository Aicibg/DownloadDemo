package com.app.downloaddemo.db;

import java.util.List;

import com.app.downloaddemo.entity.ThreadInfo;

public interface ThreadDao {
  public void insertThread(ThreadInfo threadinfo);
  
  public void deleteThread(String url,int thread_id);
  
  public void updateThread(String url,int thread_id,int finished);
  
  public List<ThreadInfo> getThreads(String url);
  //线程信息是否存在
  public boolean isExists(String url,int thread_id);
}
