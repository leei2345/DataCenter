package com.jinba.pojo;

import java.io.Serializable;

import com.alibaba.fastjson.serializer.SerializerFeature;


public class BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3607603058575907237L;
	
	private String fromurl;
	private String formkey;
	private int status = 0;
	private String comments;
	
	public String getFormkey() {
		return formkey;
	}

	public void setFormkey(String formkey) {
		this.formkey = formkey;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getFromurl() {
		return fromurl;
	}

	public void setFromurl(String fromurl) {
		this.fromurl = fromurl;
	}

	/**
	 * 转换json字符串
	 */
	public String toString () {
		String r = com.alibaba.fastjson.JSON.toJSONString(this, SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteMapNullValue);
		return r;
	}
	
	public static void main(String[] args) {
		
	}
	
}
