package com.jinba.core;

import java.util.List;
import java.util.Map;


public interface DBHandle {
	
	/**
	 * 首先查询
	 * @param sql
	 * @return true 存在 false 不存在
	 */
	public List<Map<String, Object>> select (String sql);

	/**
	 * 不存在insert
	 * @param sql
	 * @return true 成功 false 失败
	 */
	public boolean insert (String sql);
	
	/**
	 * 存在就update
	 * @param sql
	 * @return true 成功 false 失败
	 */
	public boolean update (String sql);
	
}
