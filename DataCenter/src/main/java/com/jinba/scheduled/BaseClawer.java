package com.jinba.scheduled;

import java.util.HashMap;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jinba.dao.MysqlDao;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpRequestConfig;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.Params;

@Component
public abstract class BaseClawer {

	protected int targetId;
	protected static MysqlDao dao;
	protected Map<Params, String> paramsMap = new HashMap<Params, String>();
	private static final String DEFAULTCHARSET = "UTF-8";
	protected HttpMethod http = null;
	protected static final int INITSUCC = 0;
	protected static final int INITFAIL = 1;
	
	public BaseClawer (int targetId) {
		this.targetId = targetId;
		http =  new HttpMethod(this.targetId);
	}
	
	/**
	 * 初始化传入参数，list抓取传入城市名称，和模板url
	 * @param paramsMap
	 * @return
	 */
	protected abstract int initParams ();

	@Autowired
	public void setDao(MysqlDao dao) {
		BaseClawer.dao = dao;
	}
	
	public void setParamsMap (Map<Params, String> paramsMap) {
		this.paramsMap = paramsMap;
	}
	
	protected String httpGet (String url) {
		return httpGet(url, DEFAULTCHARSET);
	}
	
	protected String httpGet (String url, String charset) {
		String html = http.GetHtml(url, HttpResponseConfig.ResponseAsString);
		return html;
	}
	
	protected String httpPost (String url, String body) {
		return httpGet(url, DEFAULTCHARSET);
	}
	
	protected String httpPost (String url, String body, String charset) {
		String html = http.GetHtml(url, body, HttpRequestConfig.RequestBodyAsString, HttpResponseConfig.ResponseAsString);
		return html;
	}
	
	
}
