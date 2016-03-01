package com.jinba.pojo;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

public class PartyEntity extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8789236085073077371L;
	
	private String parttype = "";
	private String title = "";
	private String intro = "";
	private String headimg = "";
	private String posttime = "";
	private String deadline = "";
	private String begintime = "";
	private String endtime;
	private String partytime = "";
	private String areacode;
	private BigDecimal latitude = new BigDecimal("0");
	private BigDecimal longitude = new BigDecimal("0");
	private String place = "";
	private String attendee = "";
	private int userlimit;
	private BigDecimal fee = new BigDecimal("0");
	private String feedesc = "";
	private String organizer = "";
	private String contact = "";
	private String partystatus = "";
	
	
	public String getHeadimg() {
		return headimg;
	}
	public void setHeadimg(String headimg) {
		this.headimg = headimg;
	}
	public BigDecimal getLatitude() {
		return latitude;
	}
	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}
	public BigDecimal getLongitude() {
		return longitude;
	}
	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}
	public String getParttype() {
		return parttype;
	}
	public void setParttype(String parttype) {
		this.parttype = parttype;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = StringUtils.isBlank(title)?"":title.replace("'", "\\'");
	}
	public String getIntro() {
		return intro;
	}
	public void setIntro(String intro) {
		this.intro = StringUtils.isBlank(intro)?"":intro.replace("'", "\\'");
	}
	public String getPosttime() {
		return posttime;
	}
	public void setPosttime(String posttime) {
		this.posttime = posttime;
	}
	public String getDeadline() {
		return deadline;
	}
	public void setDeadline(String deadline) {
		this.deadline = deadline;
	}
	public String getBegintime() {
		return begintime;
	}
	public void setBegintime(String begintime) {
		this.begintime = begintime;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public String getPartytime() {
		return partytime;
	}
	public void setPartytime(String partytime) {
		this.partytime = StringUtils.isBlank(partytime)?"":partytime.replace("'", "\\'");
	}
	public String getAreacode() {
		return areacode;
	}
	public void setAreacode(String areacode) {
		this.areacode = areacode;
	}
	public String getPlace() {
		return place;
	}
	public void setPlace(String place) {
		this.place = StringUtils.isBlank(place)?"":place.replace("'", "\\'");
	}
	public String getAttendee() {
		return attendee;
	}
	public void setAttendee(String attendee) {
		this.attendee = attendee;
	}
	public int getUserlimit() {
		return userlimit;
	}
	public void setUserlimit(int userlimit) {
		this.userlimit = userlimit;
	}
	public BigDecimal getFee() {
		return fee;
	}
	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}
	public String getFeedesc() {
		return feedesc;
	}
	public void setFeedesc(String feedesc) {
		this.feedesc = StringUtils.isBlank(feedesc)?"":feedesc.replace("'", "\\'");
	}
	public String getOrganizer() {
		return organizer;
	}
	public void setOrganizer(String organizer) {
		this.organizer = StringUtils.isBlank(organizer)?"":organizer.replace("'", "\\'");
	}
	public String getContact() {
		return contact;
	}
	public void setContact(String contact) {
		this.contact = contact;
	}
	public String getPartystatus() {
		return partystatus;
	}
	public void setPartystatus(String partystatus) {
		this.partystatus = partystatus;
	}
	
	public static void main(String[] args) {
		String a = "sdsdnjk'dfdf";
		System.out.println(a.replace("'", "\\'"));
	}
	
}