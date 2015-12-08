package com.jinba.scheduled;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.jinba.pojo.BaseEntity;
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
	
	/**
	 * 转载结果集的容器
	 */
	private List<T> box = new ArrayList<T>();
	
	/**
	 * list抓取实现过程
	 * @param box 结果集容器
	 * @return
	 */
	protected abstract void analysisAction (List<T> box);

	/**
	 * 整个list抓取解析的实现
	 * @return
	 */
	public List<T> listAction () {
		try {
			ActionRes initRes = initParams();
			if (initRes.equals(ActionRes.ANALYSIS_SUCC)) {
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
