package com.jinba.pojo;

/**
 * 检测目标实体
 * @author leei
 *
 */
public class TargetEntity {
	
	private int id;
	private String identidy;
	private String checkUrl;
	private String anchor;
	private String charset = "UTF-8";
	
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIdentidy() {
		return identidy;
	}
	public void setIdentidy(String identidy) {
		this.identidy = identidy;
	}
	public String getCheckUrl() {
		return checkUrl;
	}
	public void setCheckUrl(String checkUrl) {
		this.checkUrl = checkUrl;
	}
	public String getAnchor() {
		return anchor;
	}
	public void setAnchor(String anchor) {
		this.anchor = anchor;
	}
	
}
