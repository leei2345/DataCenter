package com.jinba.scheduled;

import com.jinba.pojo.BaseEntity;

/**
 * 详情页数据抓取
 * 
 * @author leei
 *
 * @param <T>
 */
public abstract class BaseDetailClawer<T extends BaseEntity> extends BaseClawer {

	public BaseDetailClawer(int targetId) {
		super(targetId);
	}

	
	
	
	
	
}
