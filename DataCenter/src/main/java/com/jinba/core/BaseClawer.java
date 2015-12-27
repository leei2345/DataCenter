package com.jinba.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.BasicCookieStore;

import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpRequestConfig;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.Method;
import com.jinba.spider.core.Params;

public abstract class BaseClawer {

	protected int targetId;
	protected Map<Params, String> paramsMap = new HashMap<Params, String>();
	protected HttpMethod http = null;
	private static Pattern usqlp = Pattern.compile("update.*? set\\s+(.*)\\s+where.*");
	private static Pattern isqlp = Pattern.compile("insert into\\s+(.*?)\\s+\\((.*?)\\)\\s+values\\s+\\((.*)\\)");
	
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
	
	public BaseClawer () {
		super();
	}

	/**
	 * 初始化传入参数，list抓取传入城市名称，和模板url
	 * @param paramsMap
	 * @return
	 */
	protected abstract ActionRes initParams ();

	public void setParamsMap (Map<Params, String> paramsMap) {
		this.paramsMap = paramsMap;
	}
	
	public void addGetHeader (String key, String value) {
		http.AddHeader(Method.Get, key, value);
	}
	
	public void addPostHeader (String key, String value) {
		http.AddHeader(Method.Post, key, value);
	}
	
	protected String httpGet (String url) {
		return httpGet(url, null);
	}
	
	protected String httpGet (String url, BasicCookieStore cookie) {
		if (cookie != null) {
			http.setCookieStore(cookie);
		}
		String html = http.GetHtml(url, HttpResponseConfig.ResponseAsStream);
		return html;
	}
	
	protected String httpGet (String url, BasicCookieStore cookie, HttpHost proxy) {
		if (cookie != null) {
			http.setCookieStore(cookie);
		}
		if (proxy != null) {
			http.setProxy(proxy);
		}
		String html = http.GetHtml(url, HttpResponseConfig.ResponseAsStream);
		return html;
	}
	
	protected BasicCookieStore httpGetCookie (String url) {
		http.GetHtml(url, HttpResponseConfig.ResponseAsString);
		return http.getCookieStore();
	}
	
	protected String httpGetLocalUrl (String url) {
		String localUrl = http.GetLocationUrl(url);
		return localUrl;
	}
	
	protected String httpPost (String url, String body) {
		return http.GetHtml(url, body, HttpRequestConfig.RequestBodyAsString, HttpResponseConfig.ResponseAsString);
	}
	
	protected String httpPostLocalUrl (String url, String body) {
		String localUrl = http.GetLocationUrl(url, body, HttpRequestConfig.RequestBodyAsString);
		return localUrl;
	}
	
	protected String checkUpdateSql (String usql) {
		try {
			Matcher matcher = usqlp.matcher(usql);
			if (matcher.find()) {
				String params = matcher.group(1);
				String[] kvs = params.split(",");
				for (int index = 0; index < kvs.length; index++) {
					String kv = kvs[index];
					String[] kvArr = kv.trim().split("=");
					if (StringUtils.isBlank(kvArr[1].replace("'", ""))) {
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
	
	protected String checkInsertSql (String isql) {
		List<String> keysList = new ArrayList<String>();
		List<String> valuesList = new ArrayList<String>();
		String tableName = "";
		try {
			Matcher matcher = isqlp.matcher(isql);
			if (matcher.find()) {
				tableName = matcher.group(1);
				String params = matcher.group(2);
				String values = matcher.group(3);
				String[] paramsArr = params.split(",");
				String[] valuesArr = values.split(",");
				for (int i = 0; i < valuesArr.length; i++) {
					String value = valuesArr[i];
					String v = value.replace("'", "");
					if (!StringUtils.isBlank(v) && !StringUtils.equals("null", v.toLowerCase())) {
						keysList.add(paramsArr[i]);
						valuesList.add(value);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (keysList.size() == valuesList.size()) {
			String keysStr = "";
			String valuesStr = "";
			for (int i = 0; i < keysList.size(); i++) {
				keysStr += ("," + keysList.get(i));
				valuesStr += ("," + valuesList.get(i));
			}
			isql = "insert into " + tableName + " (" + keysStr.replaceFirst(",", "") + ") values (" + valuesStr.replaceFirst(",", "") + ")";
		}
		return isql;
	}
	
}
