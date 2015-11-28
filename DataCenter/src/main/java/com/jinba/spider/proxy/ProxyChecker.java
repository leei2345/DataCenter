package com.jinba.spider.proxy;


import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.ProxyCheckResEntity;
import com.jinba.pojo.TargetEntity;
import com.jinba.utils.CountDownLatchUtils;
import com.jinba.utils.LoggerUtil;

@Component
public class ProxyChecker implements Runnable {

	private TargetEntity target;
	private ProxyCheckResEntity proxy;
	private CountDownLatchUtils cdl = new CountDownLatchUtils(1);
	private static final String IDENTIDY = "ProxyCheck";
	@Resource
	private MysqlDao dao;
	
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
			dao.updateProxyCheckRes(proxy);
			cdl.countDown();
		}
		LoggerUtil.ProxyLog("[" + IDENTIDY + "][" + target.getId() + "][" + cdl.getCount() + "/" + cdl.getAmount() + "][" + proxy.host + ":" + proxy.port + "][" + proxy.getEnabled() + "]");
	}
	
	public static void main(String[] args) {
		TargetEntity t = new TargetEntity();
		t.setCheckUrl("http://weixin.sogou.com/docs/terms.htm?v=1");
		t.setAnchor("免责声明");
		t.setCharset("GBK");
		ProxyCheckResEntity p = new ProxyCheckResEntity().setHost("101.66.253.22").setPort(8080);
		new Thread(new ProxyChecker().setTarget(t).setProxy(p)).start();
	}

}
