package com.jinba.pojo;



public class ProxyCheckResEntity {
	
	public String host;
	public int port;
	private String html = "";
	private int statusCode = -1;
	private int enabled = 0;
	private long usetime = 9999;
	private String message = "normal";
	public int targetId;

	public ProxyCheckResEntity setHost (String host) {
		this.host = host;
		return this;
	}
	
	public ProxyCheckResEntity setPort (int port) {
		this.port = port;
		return this;
	}
	
	public ProxyCheckResEntity setTargetId (int targetId) {
		this.targetId = targetId;
		return this;
	}
	
	public int getEnabled() {
		return enabled;
	}

	public void setEnabled(int enabled) {
		this.enabled = enabled;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getHtml() {
		return html;
	}
	public void setHtml(String html) {
		this.html = html;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public long getUsetime() {
		return usetime;
	}
	public void setUsetime(long usetime) {
		this.usetime = usetime;
	}
	
	
}
