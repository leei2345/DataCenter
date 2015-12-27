package com.jinba.scheduled;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jinba.core.BaseDetailClawer;
import com.jinba.pojo.XiaoQuEntity;
import com.jinba.scheduled.dianping.DianPingDetailClawer;
import com.jinba.utils.LoggerUtil;

@Component
public class DianPingWorker implements Runnable{
	
	private static ExecutorService detailThreadPool;
	private static LinkedBlockingQueue<XiaoQuEntity> queue = new LinkedBlockingQueue<XiaoQuEntity>(); 
	@Value("${dpclaw.thread.pool}")
	private int threadPoolSize = 40;
	
	public DianPingWorker () {
		LoggerUtil.TaskInfoLog("[DianPingWorker][Init Start]");
		detailThreadPool = Executors.newFixedThreadPool(threadPoolSize);
		new Thread(this).start();
	}
	
	public static void offerWork (List<XiaoQuEntity> xiaoquList) {
		for (XiaoQuEntity xiaoQuEntity : xiaoquList) {
			queue.offer(xiaoQuEntity);
		}
	}

	public void run() {
		while (true) {
			try {
				XiaoQuEntity x = queue.take();
				BaseDetailClawer<XiaoQuEntity> detailClawer = new DianPingDetailClawer(x);
				detailThreadPool.execute(detailClawer);
				LoggerUtil.TaskInfoLog("[DianPingWorker][Queue Size Is " + queue.size() + "]");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
