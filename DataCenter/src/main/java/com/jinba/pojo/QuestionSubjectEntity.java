package com.jinba.pojo;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

public enum QuestionSubjectEntity {

	shuxue(1, "数学"),
	yuwen(2, "语文"),
	yingyu(3, "语文"),
	wuli(4, "物理"),
	huaxue(5, "化学"),
	shengwu(6, "生物"),
	lishi(7, "历史"),
	zhengzhi(8, "政治"),
	dili(9, "地理")
	
	;
	
	public int code;
	public String name;
	public static Map<Integer, QuestionSubjectEntity> map = new HashMap<>();
	
	private QuestionSubjectEntity(int code, String name) {
		this.code = code;
		this.name = name;
	}
	
	public static QuestionSubjectEntity getSubjectByCode (int code) {
		if (MapUtils.isEmpty(map)) {
			for (QuestionSubjectEntity entry : QuestionSubjectEntity.values()) {
				map.put(entry.code, entry);
			}
		}
		return map.get(code);
	}
	
	
	
}
