package com.jinba.pojo;

public class QuestionClassifyEntity extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8744240954176967047L;

	private int subjectId;
	private String classify;
	private int number;
	
	public int getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}
	public String getClassify() {
		return classify;
	}
	public void setClassify(String classify) {
		this.classify = classify;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	
	
	
}
