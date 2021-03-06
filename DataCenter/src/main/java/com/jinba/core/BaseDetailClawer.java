package com.jinba.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.markdown4j.Markdown4jProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.BaseEntity;
import com.jinba.spider.core.ImageParser;
import com.jinba.utils.CountDownLatchUtils;
import com.overzealous.remark.Remark;


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
	private Remark remark = new Remark();
	private Markdown4jProcessor processor = new Markdown4jProcessor();
	protected Logger logger;
	
	public BaseDetailClawer(int targetId, T detailEntity, CountDownLatchUtils cdl) {
		super(targetId);
		this.detailEntity = detailEntity;
		this.cdl = cdl;
		this.logger = LoggerFactory.getLogger(this.getClass());
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
		StringBuilder logBuilder = new StringBuilder("[DetailClaw][" + this.getClass().getSimpleName() + "]");
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
				 
				public int insertAndGetId (String sql) {
					return MysqlDao.getInstance().insertAndGetId(sql);
				}
				
			});
			watch.split();
			long analysisTime = watch.getSplitTime();
			if (ActionRes.ANALYSIS_SUCC.equals(analysisRes)) {
				logBuilder.append("[" + detailEntity.getFromkey() + "][Analysis Succ][" + analysisTime + "]");
			} else {
				logBuilder.append("[" + detailEntity.getFromkey() + "][Analysis " + analysisRes + "][" + analysisTime + "]");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logBuilder.append("[Detail Error][" + e.getMessage() + "]");
		} finally {
			cdl.countDown();
			this.detailEntity = null;
			this.http = null;
			logger.info(logBuilder.toString() + "[" + cdl.getCount() + "/" + cdl.getAmount() + "][Done]");
		}
	}

	public void run() {
		this.detailAction();
	}
	
	public String markdownImage(String imageUrl, String imageName) {
		String fromhost = this.detailEntity.getFromhost();
		String fromkey = this.detailEntity.getFromkey();
		processor.addHtmlAttribute("style", "text-indent:2em","p");
		String replaceImagedArticle = ImageParser.parseImagesByUrl(imageUrl, fromhost, fromkey, imageName, targetId);
		replaceImagedArticle = remark.convertFragment(replaceImagedArticle, "");
		try {
			replaceImagedArticle = processor.process(replaceImagedArticle);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return replaceImagedArticle;
	}
	
	public String markdownText(String text) {
		processor.addHtmlAttribute("style", "text-indent:2em","p");
		String replaceImagedArticle =null;
		try {
			replaceImagedArticle = processor.process(text);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return replaceImagedArticle;
	}
	
	public String markdownContent(String text, String path, String baseUrl) {
		processor.addHtmlAttribute("style", "text-indent:2em","p");
		String imgName = detailEntity.getFromkey() + "_" + UUID.randomUUID().toString();
		String replaceImagedArticle = ImageParser.parseImages(text, baseUrl, path, imgName, targetId);
		replaceImagedArticle = remark.convertFragment(replaceImagedArticle, baseUrl);
		try {
			replaceImagedArticle = processor.process(replaceImagedArticle);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return replaceImagedArticle;
	}
	
	public String markdownImage(String imgName, String path, String text, String baseUrl) {
		processor.addHtmlAttribute("style", "text-indent:2em","p");
		String replaceImagedArticle = ImageParser.parseImages(text, baseUrl, path, imgName, targetId);
		replaceImagedArticle = remark.convertFragment(replaceImagedArticle, baseUrl);
		try {
			replaceImagedArticle = processor.process(replaceImagedArticle);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return replaceImagedArticle;
	}
	
}
