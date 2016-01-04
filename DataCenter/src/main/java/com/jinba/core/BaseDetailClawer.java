package com.jinba.core;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import com.jinba.dao.MysqlDao;
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
public abstract class BaseDetailClawer<T extends BaseEntity> extends BaseClawer implements Runnable {

	protected T detailEntity; 
	protected CountDownLatchUtils cdl;
	
	public BaseDetailClawer(int targetId, T detailEntity, CountDownLatchUtils cdl) {
		super(targetId);
		this.detailEntity = detailEntity;
		this.cdl = cdl;
	}
	
	protected abstract String getDetailHtml ();
	
	protected abstract ActionRes analysistDetail (String html, DBHandle dbHandle);
	
	/**
	 * 整个list抓取解析的实现
	 * @return
	 */
	public void detailAction () {
		StopWatch watch = new StopWatch();
		watch.start();
		StringBuilder logBuilder = new StringBuilder("[DetailClaw][" + targetId + "][" + detailEntity.getFromkey() + "]");
		try {
			/**
			 * 初始化传入参数
			 */
			ActionRes initRes = initParams();
			watch.split();
			long initParamsTime = watch.getSplitTime();
			if (initRes.equals(ActionRes.INITSUCC)) {
				logBuilder.append("[InitParam Succ][" + initParamsTime + "]");
			} else if (initRes.equals(ActionRes.INITEXIST)) {
				logBuilder.append("[InitParam Exist][" + initParamsTime + "]");
				return;
			} else {
				logBuilder.append("[InitParam Fail][" + initParamsTime + "]");
				return;
			}
			watch.reset();
			watch.start();
			/**
			 * 详情页数据抓取
			 */
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
			/**
			 * 解析详情页数据
			 */
			ActionRes analysisRes = analysistDetail(html, new DBHandle() {
				 public  List<Map<String, Object>> select(String sql) {
					 return MysqlDao.getInstance().select(sql);
				 }
				
				 public boolean insert(String sql) {
					 return MysqlDao.getInstance().execut(sql);
				 }

				 public boolean update(String sql) {
					return MysqlDao.getInstance().execut(sql);
				}
				
			});
			watch.split();
			long analysisTime = watch.getSplitTime();
			if (ActionRes.ANALYSIS_SUCC.equals(analysisRes)) {
				logBuilder.append("[Analysis Succ][" + analysisTime + "]");
			} else {
				logBuilder.append("[Analysis " + analysisRes + "][" + analysisTime + "]");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logBuilder.append("[Detail Error][" + e.getMessage() + "]");
		} finally {
			cdl.countDown();
			this.detailEntity = null;
			this.http = null;
			LoggerUtil.ClawerInfoLog(logBuilder.toString() + "[" + cdl.getCount() + "/" + cdl.getAmount() + "][Done]");
		}
	}

	public void run() {
		this.detailAction();
	}
	
	
}
