package com.jinba.pojo;

import java.math.BigDecimal;

public class XiaoQuEntity extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5059521541735702529L;
	
	private String areacode;
	private String xiaoquname;
	private int xiaoquType;
	private String address;
	private String postCode;
	private BigDecimal longItude = new BigDecimal("0");
	private BigDecimal latitude = new BigDecimal("0");
	private String heading;
	private String intro;
	private String createtime = "1970-01-01";
	private String fromhost;
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
	public String getPostCode() {
		return postCode;
	}
	public void setPostCode(String postCode) {
		this.postCode = postCode;
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
	public String getFromhost() {
		return fromhost;
	}
	public void setFromhost(String fromhost) {
		this.fromhost = fromhost;
	}

}
