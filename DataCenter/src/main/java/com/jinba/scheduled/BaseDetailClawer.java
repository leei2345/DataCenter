package com.jinba.scheduled;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.jinba.pojo.BaseEntity;
import com.jinba.utils.LoggerUtil;

/**
 * 详情页数据抓取
 * 
 * @author leei
 *
 * @param <T>
 */
public abstract class BaseDetailClawer<T extends BaseEntity> extends BaseClawer {

	protected T detailEntity;

	public BaseDetailClawer(int targetId, T detailEntity) {
		super(targetId);
		this.detailEntity = detailEntity;
	}
	
	
	protected abstract String getDetailHtml ();
	
	protected abstract T analysistDetail (String html, DBHandle dbHandle);
	
	/**
	 * 整个list抓取解析的实现
	 * @return
	 */
	public void detailAction () {
		try {
			int initRes = initParams();
			if (initRes == INITSUCC) {
				LoggerUtil.ClawerInfoLog("[Clawer][" + targetId + "][InitParams][Done][" + JSON.toJSONString(paramsMap) + "]");
			} else {
				LoggerUtil.ClawerInfoLog("[Clawer][" + targetId + "][InitParams][Fail][" + JSON.toJSONString(paramsMap) + "]");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			LoggerUtil.ClawerInfoLog("[Clawer][" + targetId + "][InitParams][Error][" + JSON.toJSONString(paramsMap) + "][" + e.getMessage() + "]");
		}
		try {
			 T t = analysistDetail(getDetailHtml(), new DBHandle() {
				public boolean execut(BaseEntity entity, String[] fileds, String[] keys) {
					return false;
				}
			});
			LoggerUtil.ClawerInfoLog("[Clawer][" + targetId + "][Claw][Done][" + JSON.toJSONString(paramsMap) + "][" + box.size() + "]");
		} catch (Exception e) {
			e.printStackTrace();
			LoggerUtil.ClawerInfoLog("[Clawer][" + targetId + "][Claw][Error][" + JSON.toJSONString(paramsMap) + "][" + e.getMessage() + "]");
		}
		return box;
	}
	
	
}
