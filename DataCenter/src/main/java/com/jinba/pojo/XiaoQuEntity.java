package com.jinba.pojo;

import java.math.BigDecimal;

public class XiaoQuEntity extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5059521541735702529L;
	/** 区域code */
	private String areacode;
	/** 小区名称 */
	private String xiaoquname;
	/** 小区类别 */
	private int xiaoquType;
	/** 小区地址 */
	private String address;
	/** 小区经度 */
	private BigDecimal longItude = new BigDecimal("0");
	/** 小区经度 */
	private BigDecimal latitude = new BigDecimal("0");
	/** 小区主页方图（近吧聊天群组头像） */
	private String heading;
	/** 小区介绍 */
	private String intro;
	/** 创建时间 */
	private String createtime = "1970-01-01";
	/** */
	private String phone;
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getAreacode() {
		return areacode;
	}
	public void setAreacode(String areacode) {
		this.areacode = areacode;
	}
	public String getXiaoquname() {
		return xiaoquname;
	}
	public void setXiaoquname(String xiaoquname) {
		this.xiaoquname = xiaoquname;
	}
	public int getXiaoquType() {
		return xiaoquType;
	}
	public void setXiaoquType(int xiaoquType) {
		this.xiaoquType = xiaoquType;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public BigDecimal getLongItude() {
		return longItude;
	}
	public void setLongItude(BigDecimal longItude) {
		this.longItude = longItude;
	}
	public BigDecimal getLatitude() {
		return latitude;
	}
	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}
	public String getHeading() {
		return heading;
	}
	public void setHeading(String heading) {
		this.heading = heading;
	}
	public String getIntro() {
		return intro;
	}
	public void setIntro(String intro) {
		this.intro = intro;
	}
	public String getCreatetime() {
		return createtime;
	}
	public void setCreatetime(String createtime) {
		this.createtime = createtime;
	}

}
