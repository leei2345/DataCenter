package com.jinba.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.jinba.pojo.BaseEntity;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * @author leei
 *
 * @param <T>
 */
public abstract class BaseListClawer<T extends BaseEntity> extends BaseClawer {

	private CountDownLatchUtils cdl;
	protected Logger logger;
	
	public BaseListClawer (int targetId, CountDownLatchUtils cdl) {
		super(targetId);
		this.cdl = cdl;
		this.logger = LoggerFactory.getLogger(this.getClass());
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
		StopWatch watch = new StopWatch();
		watch.start();
		StringBuilder logBuilder = new StringBuilder("[ListClaw][" + targetId + "][" + JSON.toJSONString(paramsMap) + "]");
		try {
			ActionRes initRes = initParams();
			watch.split();
			long initParamsTime = watch.getSplitTime();
			if (initRes.equals(ActionRes.INITSUCC)) {
				logBuilder.append("[InitParam Succ][" + initParamsTime + "]");
			} else {
				logBuilder.append("[InitParam Fail][" + initParamsTime + "]");
				return box;
			}
			watch.reset();
			watch.start();
			analysisAction(box);
			watch.split();
			long analysisTime = watch.getSplitTime();
			logBuilder.append("[Analysis Done][" + analysisTime + "][Get Entity " + box.size() + "]");
		} catch (Exception e) {
			e.printStackTrace();
			logBuilder.append("[List Error][" + e.getMessage() + "]");
		} finally {
			cdl.countDown();
			logger.info(logBuilder.toString() + "[" + cdl.getCount() + "/" + cdl.getAmount() + "]");
		}
		return box;
	}
	
	
}
