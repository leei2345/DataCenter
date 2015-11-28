package com.jinba.utils;

import java.util.concurrent.CountDownLatch;

/**
 * 计数工具，统计以identidy为维度的运行进度
 * @author leei
 *
 */
public class CountDownLatchUtils {
	
	private int amount;
	private CountDownLatch cdl;

	public CountDownLatchUtils(int count) {
		this.amount = count;
		this.cdl = new CountDownLatch(count);
	}

	public void countDown() {
		this.cdl.countDown();
	}

	public void await() throws InterruptedException {
		this.cdl.await();
	}

	public long getCount() {
		return this.cdl.getCount();
	}

	public int getAmount() {
		return this.amount;
	}
	
}