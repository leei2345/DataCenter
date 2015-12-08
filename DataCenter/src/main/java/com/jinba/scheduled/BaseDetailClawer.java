package com.jinba.scheduled;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.stereotype.Component;

import com.jinba.pojo.BaseEntity;
import com.jinba.utils.CountDownLatchUtils;
import com.jinba.utils.LoggerUtil;

/**
 * 详情页数据抓取
 * 
 * @author leei
 *
 * @param <T>
 */
@Component
public abstract class BaseDetailClawer<T extends BaseEntity> extends BaseClawer implements Runnable {

	protected T detailEntity;
	protected CountDownLatchUtils cdl;
	
	public BaseDetailClawer(int targetId, T detailEntity, CountDownLatchUtils cdl) {
		super(targetId);
		this.detailEntity = detailEntity;
		this.cdl = cdl;
	}
	
	protected abstract String getDetailHtml ();
	
	protected abstract void analysistDetail (String html, DBHandle dbHandle);
	
	/**
	 * 整个list抓取解析的实现
	 * @return
	 */
	public void detailAction () {
		StopWatch watch = new StopWatch();
		watch.start();
		StringBuilder logBuilder = new StringBuilder();
		try {
			int initRes = initParams();
			watch.split();
			long initParamsTime = watch.getSplitTime();
			if (initRes == INITSUCC) {
				logBuilder.append("[InitParam Succ][" + initParamsTime + "]");
			} else {
				logBuilder.append("[InitParam Fail][" + initParamsTime + "]");
				return;
			}
			watch.reset();
			watch.start();
			String html = getDetailHtml();
			watch.split();
			long getHtmlTime = watch.getSplitTime();
			if (!StringUtils.isBlank(html)) {
				logBuilder.append("[GetHtml Done][" + getHtmlTime + "]");
			} else {
				logBuilder.append("[GetHtml Fail][" + getHtmlTime + "]");
				return;
			}
			watch.reset();
			watch.start();
			analysistDetail(html, new DBHandle() {
				 public  List<Map<String, Object>> select(String sql) {
					 return dao.select(sql);
				 }
				
				 public boolean insert(String sql) {
					 return dao.execut(sql);
				 }

				 public boolean update(String sql) {
					return dao.execut(sql);
				}
				
			});
			watch.split();
			long analysisTime = watch.getSplitTime();
			logBuilder.append("[Analysis Done][" + analysisTime + "]");
		} catch (Exception e) {
			e.printStackTrace();
			logBuilder.append("[Claw Error][" + e.getMessage() + "]");
		} finally {
			cdl.countDown();
			LoggerUtil.ClawerInfoLog("[Clawer][" + targetId + "][" + cdl.getCount() + "/" + cdl.getAmount() + "][Done]" + logBuilder.toString());
		}
	}

	public void run() {
		this.detailAction();
	}
	
	
}
