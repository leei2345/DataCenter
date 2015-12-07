package com.jinba.scheduled;

import com.jinba.pojo.BaseEntity;

public interface DBHandle {
	
	/**
	 * 操作数据库
	 * @param entity 要存储的实体
	 * @param fileds 要存储的字段
	 * @param keys 检验是否更新要更新索要参考的主键
	 * @return
	 */
	public boolean execut (BaseEntity entity, String[] fileds, String[] keys);

}
