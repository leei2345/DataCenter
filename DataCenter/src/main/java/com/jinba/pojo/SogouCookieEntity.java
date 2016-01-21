package com.jinba.pojo;

import java.io.Serializable;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.BasicCookieStore;

public class SogouCookieEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7958874958250719069L;

	private HttpHost proxy;
	private BasicCookieStore cookie;
	private long ctime;
	public HttpHost getProxy() {
		return proxy;
	}
	public void setProxy(HttpHost proxy) {
		this.proxy = proxy;
	}
	public BasicCookieStore getCookie() {
		return cookie;
	}
	public void setCookie(BasicCookieStore cookie) {
		this.cookie = cookie;
	}
	public long getCtime() {
		return ctime;
	}
	public void setCtime(long ctime) {
		this.ctime = ctime;
	}
	
}
