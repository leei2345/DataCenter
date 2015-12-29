package com.jinba.scheduled;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.jinba.core.BaseDetailClawer;
import com.jinba.pojo.XiaoQuEntity;
import com.jinba.scheduled.dianping.DianPingDetailClawer;
import com.jinba.utils.LoggerUtil;

@Component
@Scope("singleton")
public class DianPingWorker implements Runnable{
	
	private static ExecutorService detailThreadPool;
	private static LinkedBlockingQueue<XiaoQuEntity> queue = new LinkedBlockingQueue<XiaoQuEntity>(); 
	private static BlockingQueue<Runnable> threadQueue = new LinkedBlockingQueue<Runnable>(2000); 

	@Value("${dpclaw.thread.pool}")
	private int threadPoolSize = 30;
	private static DianPingWorker instance;
	
	public DianPingWorker () {
		LoggerUtil.TaskInfoLog("[DianPingWorker][Init Start]");
		detailThreadPool = new ThreadPoolExecutor(20, threadPoolSize, 60000, TimeUnit.MILLISECONDS, threadQueue);
		new Thread(this).start();
		instance = this;
	}
	
	public static DianPingWorker getInstance () {
		return instance;
	}
	
	public void offerWork (List<XiaoQuEntity> xiaoquList) {
		for (XiaoQuEntity xiaoQuEntity : xiaoquList) {
			try {
				queue.put(xiaoQuEntity);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void run() {
		while (true) {
			try {
				XiaoQuEntity x = queue.take();
				BaseDetailClawer<XiaoQuEntity> detailClawer = new DianPingDetailClawer(x);
				detailThreadPool.execute(detailClawer);
				LoggerUtil.TaskInfoLog("[DianPingWorker][Execut " + x.getFromkey() + "][Entity Queue Size Is " + queue.size() + "][Thread Queue Size Is " + threadQueue.size() + "]");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
