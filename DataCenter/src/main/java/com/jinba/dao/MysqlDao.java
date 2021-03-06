package com.jinba.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.jinba.pojo.AreaType;
import com.jinba.pojo.ProxyCheckResEntity;
import com.jinba.pojo.SyntaxEntity;
import com.jinba.pojo.SyntaxEntity.SyntaxType;
import com.jinba.pojo.TargetEntity;

@Component
@Scope("singleton")
public class MysqlDao  {
	
	@Autowired
	private DruidDataSource spiderSource;
	
	private static MysqlDao instance;
	
	@PostConstruct
	public void staticMysqlDao () {
		instance = this;
	}
	
	public static MysqlDao getInstance () {
		return instance;
	}
	
	/**
	 * 更新代理检测结果
	 * @return true  成功 false 失败
	 */
	public boolean updateProxyCheckRes (ProxyCheckResEntity proxy) {
		DruidPooledConnection conn = null;
		Statement sm = null;
		boolean count = false;
		try {
			int enabled = proxy.getEnabled();
			String sql = "update tb_proxy_avail set enable=" + enabled + ",res_time=" + proxy.getUsetime() + ",avail=(avail+1),u_time=now() where host='" + proxy.host + "' and port=" + proxy.port + " and target_id=" + proxy.targetId;
			if (enabled == 0) {
				sql = "update tb_proxy_avail set enable=" + enabled + ",res_time=" + proxy.getUsetime() + ",unavail=(unavail+1),u_time=now() where host='" + proxy.host + "' and port=" + proxy.port + " and target_id=" + proxy.targetId;
			}
			conn = spiderSource.getConnection();
			sm = conn.createStatement();
			boolean res = sm.execute(sql);
			if (!res) {
				count = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (sm != null) {
				try {
					sm.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return count;
	}
	
	public Map<Integer, TargetEntity> getTargetMap () {
		DruidPooledConnection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		Map<Integer, TargetEntity> res = new HashMap<Integer, TargetEntity>();
		try {
			String sql = "select id,identidy,check_url,anchor,charset,timeout from tb_target where switch=1";
			conn = spiderSource.getConnection();
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				TargetEntity t = new TargetEntity();
				int id = rs.getInt("id");
				t.setId(id);
				String identidy = rs.getString("identidy");
				t.setIdentidy(identidy);
				String checkUrl = rs.getString("check_url");
				t.setCheckUrl(checkUrl);
				String anchor = rs.getString("anchor");
				t.setAnchor(anchor);
				String charset = rs.getString("charset");
				t.setCharset(charset);
				int timeout = rs.getInt("timeout");
				t.setTimeout(timeout);
				res.put(id, t);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}
	
	/**
	 * 查看目标代理已有数量
	 * @param targetId为-1 时 查看代理源的总数
	 * @return
	 */
	public Multiset<String> getTargetProxyCount (int targetId) {
		DruidPooledConnection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		Multiset<String> res = HashMultiset.create();
		try {
			String sql = "select concat(host,':',port) str from tb_proxy_source";
			if (targetId != -1) {
				sql = "select concat(host,':',port) str from tb_proxy_avail where target_id=" + targetId;
			}
			conn = spiderSource.getConnection();
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				String str = rs.getString("str");
				if (!StringUtils.isBlank(str)) {
					res.add(str);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}
	
	/**
	 * 查看目标代理已有数量
	 * @param targetId为-1 时 查看代理源的总数
	 * @return
	 */
	public void removeProxy (String proxy, int targetId) {
		DruidPooledConnection conn = null;
		Statement st = null;
		try {
			String[] proxyInfo = proxy.split(":");
			String sql = "delete from tb_proxy_avail where target_id=" + targetId + " and `host`='" + proxyInfo[0] + "' and `port`=" + proxyInfo[1] + " and enable=0";
			conn = spiderSource.getConnection();
			st = conn.createStatement();
			st.execute(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	public boolean insertProxyToAvail (String host, String port, int targetId) {
		DruidPooledConnection conn = null;
		Statement st = null;
		Boolean res = false;
		try {
			String sql = "insert into tb_proxy_avail (host,port,target_id,c_time,u_time) values ('" + host + "'," + port + "," + targetId + ",now(),now())";
			conn = spiderSource.getConnection();
			st = conn.createStatement();
			boolean insertRes = st.execute(sql);
			if (!insertRes) {
				res = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}
	
	public List<ProxyCheckResEntity> getNeedCheckProxy (int targetId) {
		DruidPooledConnection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		List<ProxyCheckResEntity> res = new ArrayList<ProxyCheckResEntity>();
		try {
			String sql = "select host,port from tb_proxy_avail where target_id=" + targetId + " order by u_time limit 1000";
			conn = spiderSource.getConnection();
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				String host = rs.getString("host");
				int port = rs.getInt("port");
				ProxyCheckResEntity proxy = new ProxyCheckResEntity().setHost(host).setPort(port).setTargetId(targetId);
				res.add(proxy);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}
	
	public ConcurrentLinkedQueue<ProxyCheckResEntity> getProxyQueue (int targetId) {
		DruidPooledConnection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		ConcurrentLinkedQueue<ProxyCheckResEntity> res = new ConcurrentLinkedQueue<ProxyCheckResEntity>();
		try {
			String sql = "select host,port from tb_proxy_avail where target_id=" + targetId + " and enable=1 order by u_time desc limit 1000";
			conn = spiderSource.getConnection();
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				String host = rs.getString("host");
				int port = rs.getInt("port");
				ProxyCheckResEntity proxy = new ProxyCheckResEntity().setHost(host).setPort(port).setTargetId(targetId);
				res.add(proxy);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}
	
	public Map<Integer, Map<String, List<SyntaxEntity>>> initAnalysisSyntax () {
		DruidPooledConnection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		Map<Integer, Map<String, List<SyntaxEntity>>> res = new HashMap<Integer, Map<String,List<SyntaxEntity>>>();
		try {
			String sql = "select target_id,`type`,step,param_name,`syntax` from tb_analysis_syntax where target_id in (select id from tb_target where switch=1)";
			conn = spiderSource.getConnection();
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				int targetId = rs.getInt("target_id");
				Map<String, List<SyntaxEntity>> syntaxMap = res.get(targetId);
				if (syntaxMap == null) {
					syntaxMap = new HashMap<String, List<SyntaxEntity>>();
				}
				String param_name= rs.getString("param_name");
				List<SyntaxEntity> syntaxList = syntaxMap.get(param_name);
				if (syntaxList == null) {
					syntaxList = new ArrayList<SyntaxEntity>();
				}
				int step = rs.getInt("step");
				String type = rs.getString("type");
				SyntaxType syntaxType = Enum.valueOf(SyntaxType.class, type);
				String syntax = rs.getString("syntax");
				SyntaxEntity syntaxEntity = new SyntaxEntity();
				syntaxEntity.setType(syntaxType);
				syntaxEntity.setSyntax(syntax);
				syntaxList.add(step, syntaxEntity);
				syntaxMap.put(param_name, syntaxList);
				res.put(targetId, syntaxMap);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}
	
	public List<String> getAreaList (int... areaLevel) {
		DruidPooledConnection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		List<String> res = new ArrayList<String>();
		try {
			String areaLevelArr = Arrays.toString(areaLevel).replace("[", "").replace("]", "");
			String sql = "select areacode,keywords,areaname,postcode from t_area where level in (" + areaLevelArr + ")";
			conn = spiderSource.getConnection();
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				String areaCode = rs.getString("areacode");
				String areaName = rs.getString("areaname");
				String keyWords = rs.getString("keywords");
				String postCode = rs.getString("postcode");
				if (StringUtils.equals("N/A", postCode)) {
					postCode = "";
				}
				String line = areaName + "_" + areaCode + "_" + postCode + "_" + keyWords;
				res.add(line);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}
	
	public List<String[]> getGongzhonghaoList () {
		DruidPooledConnection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		List<String[]> res = new ArrayList<String[]>();
		try {
			String sql = "SELECT gzhname,areacode,xiaoquid FROM t_wxgzh";
			conn = spiderSource.getConnection();
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				String areaCode = rs.getString("areacode");
				String gongzhongName = rs.getString("gzhname");
				int xiaoquid = rs.getInt("xiaoquid");
				String[] line = new String[]{areaCode,gongzhongName,String.valueOf(xiaoquid)};
				res.add(line);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}
	
	@SuppressWarnings("resource")
	public Map<AreaType, Map<String, String>> getAreaMap (String cityCode) {
		DruidPooledConnection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		Map<AreaType, Map<String, String>> res = new HashMap<AreaType, Map<String, String>>();
		try {
			/** 商圈 */
			Map<String, String> districtMap = new HashMap<String, String>();
			String sql = "select areaname,areacode from t_area where level=4 and areacode like '" + cityCode + "%'";
			conn = spiderSource.getConnection();
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				String areaCode = rs.getString("areacode");
				String areaName = rs.getString("areaname");
				districtMap.put(areaName, areaCode);
			}
			res.put(AreaType.District, districtMap);
			/** 区县 */
			Map<String, String> districtCountyMap = new HashMap<String, String>();
			sql = "select areaname,areacode from t_area where level=3 and areacode like '" + cityCode + "%'";
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				String areaCode = rs.getString("areacode");
				String areaName = rs.getString("areaname");
				districtCountyMap.put(areaName, areaCode);
			}
			res.put(AreaType.DistrictCounty, districtCountyMap);
			/** 区县 */
			Map<String, String> nomalMap = new HashMap<String, String>();
			sql = "select areaname,areacode from t_area where level!=1 and level!=3 and level!=4 and areacode like '" + cityCode + "%'";;
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				String areaCode = rs.getString("areacode");
				String areaName = rs.getString("areaname");
				nomalMap.put(areaName, areaCode);
			}
			res.put(AreaType.Nomal, nomalMap);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}
	
	public Map<AreaType, Map<String, Map<String, String>>> getMuiltAreaMap () {
		DruidPooledConnection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		Map<AreaType, Map<String, Map<String, String>>> res = new HashMap<AreaType, Map<String, Map<String, String>>>();
		try {
			String selectMuiltAreaSql = "select b.areacode,b.fullname,b.areaname from t_area b, (SELECT areaname,COUNT(1) count FROM t_area GROUP BY areaname HAVING count>1) a where a.areaname=b.areaname order by b.areaname";
			conn = spiderSource.getConnection();
			st = conn.prepareStatement(selectMuiltAreaSql);
			rs = st.executeQuery();
			Map<String, Map<String, String>> map =  new HashMap<String, Map<String,String>>();
			while (rs.next()) {
				String areaName = rs.getString("areaname");
				Map<String, String> innerMap = map.get(areaName);
				if (innerMap == null) {
					innerMap = new HashMap<String, String>();
				}
				String fullName = rs.getString("fullname");
				String[] fullNameArr = fullName.split("\\s+");
				if (fullNameArr.length < 1) {
					continue;
				}
				String city = fullNameArr[0];
				String areaCode = rs.getString("areacode");
				innerMap.put(city, areaCode);
				map.put(areaName, innerMap);
			}
			res.put(AreaType.FirstStemp, map);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}
	
	/*public Map<String, String> getChengquCode () {
		DruidPooledConnection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		Map<String, String> res = new HashMap<String, String>();
		try {
			String sql = "select areaname,areacode from t_area where level=3";
			conn = spiderSource.getConnection();
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				String areaCode = rs.getString("areacode");
				String areaName = rs.getString("areaname");
				res.put(areaName, areaCode);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return res;
	}*/
	
	public List<Map<String, Object>> select (String sql) {
		List<Map<String, Object>> resList = new ArrayList<Map<String, Object>>();
		DruidPooledConnection conn = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			conn = spiderSource.getConnection();
			statement = conn.createStatement();
			rs = statement.executeQuery(sql);
			while (rs.next()) {
				ResultSetMetaData rsmd = rs.getMetaData();
				int count = rsmd.getColumnCount();
				Map<String, Object> map = new HashMap<String, Object>();
				for (int index = 1; index <= count; index++) {
					String key = rsmd.getColumnLabel(index);
					Object value = rs.getObject(index);
					map.put(key, value);
				}
				resList.add(map);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) 
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
	        if (statement != null)
	        	try {
	        		statement.close();
	        	} catch (SQLException e) {
	        		e.printStackTrace();
	        	}
	        if (conn != null)
	        	try {
	        		conn.close();
	        	} catch (SQLException e) {
	        		e.printStackTrace();
	        	}
		}
		return resList;
	}
	
	public boolean execut (String sql) {
		boolean res = false;
		DruidPooledConnection conn = null;
		Statement sm = null;
		try {
			conn = spiderSource.getConnection();
			sm = conn.createStatement();
			int count = sm.executeUpdate(sql);
			if (count > 0) {
				res = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (sm != null)
				try {
					sm.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (conn != null)
	        	try {
	        		conn.close();
	        	} catch (SQLException e) {
	        		e.printStackTrace();
	        	}
		}
		return res;
	}
	
	public int insertAndGetId (String sql) {
		int id = 0;
		DruidPooledConnection conn = null;
		PreparedStatement  sm = null;
		ResultSet rs = null;
		try {
			conn = spiderSource.getConnection();
			sm = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			sm.executeUpdate();
			rs = sm.getGeneratedKeys();
			if (rs.next()) {
				id = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (sm != null)
				try {
					sm.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (conn != null)
	        	try {
	        		conn.close();
	        	} catch (SQLException e) {
	        		e.printStackTrace();
	        	}
		}
		return id;
	}
	
	
	
}
