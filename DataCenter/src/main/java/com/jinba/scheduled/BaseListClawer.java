package com.jinba.scheduled;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.jinba.pojo.BaseEntity;
import com.jinba.spider.core.Params;
import com.jinba.utils.LoggerUtil;

/**
 * 
 * @author leei
 *
 * @param <T>
 */
public abstract class BaseListClawer<T extends BaseEntity> extends BaseClawer {

	public BaseListClawer (int targetId) {
		super(targetId);
	}
	
	protected static final int INITSUCC = 0;
	protected static final int INITFAIL = 1;
	
	protected abstract int initParams (Map<Params, String> paramsMap);
	
	private List<T> box = new ArrayList<T>();
	
	protected abstract List<T> analysisAction (List<T> box);

	public List<T> claw () {
		try {
			int initRes = initParams(paramsMap);
			if (initRes == INITSUCC) {
				LoggerUtil.ClawerInfoLog("[Clawer][" + targetId + "][InitParams][Done][" + JSON.toJSONString(paramsMap) + "]");
			} else {
				LoggerUtil.ClawerInfoLog("[Clawer][" + targetId + "][InitParams][Fail][" + JSON.toJSONString(paramsMap) + "]");
				return box;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LoggerUtil.ClawerInfoLog("[Clawer][" + targetId + "][InitParams][Error][" + JSON.toJSONString(paramsMap) + "][" + e.getMessage() + "]");
		}
		try {
			analysisAction(box);
			LoggerUtil.ClawerInfoLog("[Clawer][" + targetId + "][Claw][Done][" + JSON.toJSONString(paramsMap) + "][" + box.size() + "]");
		} catch (Exception e) {
			e.printStackTrace();
			LoggerUtil.ClawerInfoLog("[Clawer][" + targetId + "][Claw][Error][" + JSON.toJSONString(paramsMap) + "][" + e.getMessage() + "]");
		}
		return box;
	}
	
}
