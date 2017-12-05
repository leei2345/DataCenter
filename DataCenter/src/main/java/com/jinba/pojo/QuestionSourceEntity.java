package com.jinba.pojo;

public class QuestionSourceEntity extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8744240954176967047L;

	private Long id;
	private Integer subjectId;
	private Long classifyId;
	private String sourceUrl;
	private String seqCode;
	private String grade;
	private String question;
	private String answer;
	private String explain;
	private String sourceHtml;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getGrade() {
		return grade;
	}
	public void setGrade(String grade) {
		this.grade = grade;
	}
	public String getSeqCode() {
		return seqCode;
	}
	public void setSeqCode(String seqCode) {
		this.seqCode = seqCode;
	}
	public Integer getSubjectId() {
		return subjectId;
	}
	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}
	public Long getClassifyId() {
		return classifyId;
	}
	public void setClassifyId(Long classifyId) {
		this.classifyId = classifyId;
	}
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public String getExplain() {
		return explain;
	}
	public void setExplain(String explain) {
		this.explain = explain;
	}
	public String getSourceUrl() {
		return sourceUrl;
	}
	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}
	public String getSourceHtml() {
		return sourceHtml;
	}
	public void setSourceHtml(String sourceHtml) {
		this.sourceHtml = sourceHtml;
	}
	
	
	
	
	
}
