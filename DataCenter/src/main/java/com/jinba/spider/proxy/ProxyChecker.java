package com.jinba.spider.proxy;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.ProxyCheckResEntity;
import com.jinba.pojo.TargetEntity;
import com.jinba.utils.CountDownLatchUtils;
import com.jinba.utils.LoggerUtil;

public class ProxyChecker implements Runnable {

	private TargetEntity target;
	private ProxyCheckResEntity proxy;
	private CountDownLatchUtils cdl = new CountDownLatchUtils(1);
	private static final String IDENTIDY = "ProxyCheck";
	
	public ProxyChecker setCdl (CountDownLatchUtils cdl) {
		this.cdl = cdl;
		return this;
	}
	
	public ProxyChecker setTarget (TargetEntity target) {
		this.target = target;
		return this;
	}
	
	public ProxyChecker setProxy (ProxyCheckResEntity proxy) {
		this.proxy = proxy;
		return this;
	}
	
	public void run() {
		String url = target.getCheckUrl();
		String anchor = target.getAnchor();
		String charset = target.getCharset();
		try {
			HttpHandler handler = new HttpHandler();
			if (target.getTimeout() > 0) {
				handler.SetTimeOut(target.getTimeout());
			}
			handler.InstallProxy(proxy);
			handler.httpGet(url, charset);
			String html = proxy.getHtml();
			if (html.contains(anchor)) {
				proxy.setEnabled(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LoggerUtil.ProxyLog("[" + IDENTIDY + "][" + target.getId() + "][" + e.getMessage() + "]");
		} finally {
			MysqlDao.getInstance().updateProxyCheckRes(proxy);
			cdl.countDown();
		}
		LoggerUtil.ProxyLog("[" + IDENTIDY + "][" + target.getId() + "][" + cdl.getCount() + "/" + cdl.getAmount() + "][" + proxy.host + ":" + proxy.port + "][" + proxy.getEnabled() + "]");
	}
	
	public static void main(String[] args) {
		TargetEntity t = new TargetEntity();
		t.setCheckUrl("http://www.dianping.com/aboutus/zhishichanquan.html");
		t.setAnchor("大众点评网");
		t.setCharset("UTF-8");
		ProxyCheckResEntity p = new ProxyCheckResEntity().setHost("106.38.251.62").setPort(8088);
		new Thread(new ProxyChecker().setTarget(t).setProxy(p)).start();
	}

}
