package com.jinba.core;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
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
	private static Pattern sqlp = Pattern.compile("update.*? set\\s+(.*)\\s+where.*");
	
	public enum ActionRes {
		
		INITSUCC,
		INITFAIL,
		ANALYSIS_SUCC,
		ANALYSIS_FAIL,
		DBHAND_FAIL,
		;
	}
	
	public BaseClawer (int targetId) {
		this.targetId = targetId;
		http =  new HttpMethod(this.targetId);
	}
	
	/**
	 * 初始化传入参数，list抓取传入城市名称，和模板url
	 * @param paramsMap
	 * @return
	 */
	protected abstract ActionRes initParams ();

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
	
	protected String checkUpdateSql (String usql) {
		try {
			Matcher matcher = sqlp.matcher(usql);
			if (matcher.find()) {
				String params = matcher.group(1);
				String[] kvs = params.split(",");
				for (int index = 0; index < kvs.length; index++) {
					String kv = kvs[index];
					String[] kvArr = kv.trim().split("=");
					if (StringUtils.isBlank(kvArr[1])) {
						if (index != (kvs.length - 1)) {
							usql = usql.replaceAll(kv + "\\s?,", "");
						} else {
							usql = usql.replace(kv, "");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return usql;
	}
	
	
}
