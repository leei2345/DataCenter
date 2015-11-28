package com.jinba.spider.core;

/**
 * 标记response处理为字节流还是字符串
 * @author leei
 *
 */
public enum HttpResponseConfig {
	ResponseAsString("ResponseAsString", false), ResponseAsStream("ResponseAsStream", true);

	private String Config;
	private boolean YesOrNo;

	private HttpResponseConfig(String config, boolean yesorno) {
		this.Config = config;
		this.YesOrNo = yesorno;
	}

	public String getConfig() {
		return this.Config;
	}

	public void setConfig(String config) {
		this.Config = config;
	}

	public boolean isYesOrNo() {
		return this.YesOrNo;
	}

	public void setYesOrNo(boolean yesOrNo) {
		this.YesOrNo = yesOrNo;
	}
}