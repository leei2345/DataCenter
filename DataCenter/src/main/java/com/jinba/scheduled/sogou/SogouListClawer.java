package com.jinba.scheduled.sogou;

import java.util.List;

import com.jinba.core.BaseListClawer;
import com.jinba.pojo.NewsEntity;

public class SogouListClawer extends BaseListClawer<NewsEntity> {

	private static final int TARGETID = 2;
	
	public SogouListClawer() {
		super(TARGETID);
	}

	@Override
	protected void analysisAction(List<NewsEntity> box) {
		
	}

	@Override
	protected ActionRes initParams() {
		
		return null;
	}

}
