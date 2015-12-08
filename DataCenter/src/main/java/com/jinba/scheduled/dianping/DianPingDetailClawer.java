package com.jinba.scheduled.dianping;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONObject;
import com.jinba.pojo.XiaoQuEntity;
import com.jinba.scheduled.BaseDetailClawer;
import com.jinba.scheduled.DBHandle;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * @author leei
 *
 */
public class DianPingDetailClawer extends BaseDetailClawer<XiaoQuEntity>{

	private static final int TARGETID = 1;
	private String sourceKey;
	
	public DianPingDetailClawer(XiaoQuEntity detailEntity, CountDownLatchUtils cdl) {
		super(TARGETID, detailEntity, cdl);
	}

	@Override
	protected ActionRes initParams() {
		return ActionRes.INITSUCC;
	}

	@Override
	protected String getDetailHtml() {
		sourceKey = detailEntity.getFormkey();
		String url = "http://www.dianping.com/shop/" + sourceKey;
		String html = httpGet(url);
		return html;
	}

	@Override
	protected ActionRes analysistDetail(String html, DBHandle dbHandle) {
		long timeStemp = System.currentTimeMillis();
		String url = "http://www.dianping.com/ajax/json/shop/wizard/BasicHideInfoAjaxFP?_nr_force=" + timeStemp + "&shopId=" + sourceKey;
		String info = httpGet(url);
		if (StringUtils.isBlank(info)) {
			return ActionRes.ANALYSIS_FAIL;
		}
		Document doc = Jsoup.parse(html);
		JSONObject infoObject = JSONObject.parseObject(info);
		JSONObject shopInfoObject = infoObject.getJSONObject("msg").getJSONObject("shopInfo");
		int cityCode = shopInfoObject.getIntValue("cityId");
		String cityName = DianPingCityCode.getCityName(cityCode);
		float glat = shopInfoObject.getFloat("glat");
		float glng = shopInfoObject.getFloatValue("glng");
		this.detailEntity.setLongItude(new BigDecimal(String.valueOf(glng)));
		this.detailEntity.setLatitude(new BigDecimal(String.valueOf(glat)));
		String areaName = doc.select("div[class=expand-info address] > a[rel=nofollow] > span").text().trim();
		areaName = areaName.replace("其他", "");
		String areaCode = DianPingCityMap.getAreaCodePro(areaName);
		if (StringUtils.isBlank(areaCode)) {
			
		}
		
		
	}


}
