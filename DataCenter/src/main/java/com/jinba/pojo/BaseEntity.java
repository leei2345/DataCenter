package com.jinba.pojo;

import java.io.Serializable;

import com.alibaba.fastjson.serializer.SerializerFeature;


public class BaseEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3607603058575907237L;
	/** 信息来源URL */
	private String fromurl;
	/** 信息来源主键 */
	private String formkey;
	/** 信息来源主机 */
	private String fromhost;
	
	public String getFromhost() {
		return fromhost;
	}

	public void setFromhost(String fromhost) {
		this.fromhost = fromhost;
	}

	public String getFormkey() {
		return formkey;
	}

	public void setFormkey(String formkey) {
		this.formkey = formkey;
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
