package com.jinba.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.jinba.pojo.ProxyCheckResEntity;
import com.jinba.pojo.SyntaxEntity;
import com.jinba.pojo.SyntaxEntity.SyntaxType;
import com.jinba.pojo.TargetEntity;

@Component
public class MysqlDao {
	
	@Autowired
	private DruidDataSource spiderSource;

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
			String sql = "select id,identidy,check_url,anchor,charset from tb_target where switch=1";
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
			String sql = "select host,port from tb_proxy_avail where target_id=" + targetId + " order by u_time limit 500";
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
	
	public LinkedBlockingDeque<ProxyCheckResEntity> getProxyQueue (int targetId) {
		DruidPooledConnection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		LinkedBlockingDeque<ProxyCheckResEntity> res = new LinkedBlockingDeque<ProxyCheckResEntity>();
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
	
	public List<String> getAreaList () {
		DruidPooledConnection conn = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		List<String> res = new ArrayList<String>();
		try {
			String sql = "select areacode,areaname,postcode from t_area where `level` IN (2,3) AND areaname NOT LIKE '%辖区'";
			conn = spiderSource.getConnection();
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
			while (rs.next()) {
				String areaCode = rs.getString("areacode");
				String areaName = rs.getString("areaname");
				String postCode = rs.getString("postcode");
				if (StringUtils.equals("N/A", postCode)) {
					postCode = "";
				}
				String line = areaName + "_" + areaCode + "_" + postCode;
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
	
	
}
