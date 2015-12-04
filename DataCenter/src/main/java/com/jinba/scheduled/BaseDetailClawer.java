package com.jinba.scheduled;



import com.jinba.pojo.BaseEntity;

public abstract class BaseDetailClawer<T extends BaseEntity> extends BaseClawer {

	public BaseDetailClawer(int targetId) {
		super(targetId);
	}

	
	
	
}
