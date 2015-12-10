package com.jinba.scheduled.dianping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONObject;
import com.jinba.core.BaseDetailClawer;
import com.jinba.core.DBHandle;
import com.jinba.pojo.XiaoQuEntity;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * @author leei
 *
 */
public class DianPingDetailClawer extends BaseDetailClawer<XiaoQuEntity>{

	private static final int TARGETID = 1;
	private String sourceKey;
	private static final String IDENTIDY = "dp_";
	
	public DianPingDetailClawer(XiaoQuEntity detailEntity, CountDownLatchUtils cdl) {
		super(TARGETID, detailEntity, cdl);
	}

	@Override
	protected ActionRes initParams() {
		return ActionRes.INITSUCC;
	}

	@Override
	protected String getDetailHtml() {
		sourceKey = detailEntity.getFromkey().replace(IDENTIDY, "");;
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
		String phone = doc.select("div.basic-info > p[class=expand-info tel] > span.item").text().trim();
		this.detailEntity.setPhone(phone);
		JSONObject infoObject = JSONObject.parseObject(info);
		JSONObject shopInfoObject = infoObject.getJSONObject("msg").getJSONObject("shopInfo");
		int cityCode = shopInfoObject.getIntValue("cityId");
		String cityName = DianPingCityCode.getCityName(cityCode);
		float glat = shopInfoObject.getFloat("glat");
		float glng = shopInfoObject.getFloatValue("glng");
		String address = shopInfoObject.getString("address");
		this.detailEntity.setLongItude(new BigDecimal(String.valueOf(glng)));
		this.detailEntity.setLatitude(new BigDecimal(String.valueOf(glat)));
		String areaName = doc.select("div[class=expand-info address] > a[rel=nofollow] > span").text().trim();
		areaName = areaName.replace("其他", "");
		String areaCode = DianPingCityMap.getAreaCodePro(areaName);
		boolean isQu = true;
		if (StringUtils.isBlank(areaCode)) {
			areaCode = DianPingCityMap.getAreaCode(cityName);
			isQu = false;
		}
		this.detailEntity.setAreacode(areaCode);
		if (isQu) {
			address = cityName + areaName + address;
		} else {
			address = cityName + address;
		}
		this.detailEntity.setAddress(address);
		String selectSql = "select xiaoquid from t_xiaoqu where fromkey='" + detailEntity.getFromkey() + "'";;
		List<Map<String, Object>> selectRes = dbHandle.select(selectSql);
		StringBuilder iubuilder = new StringBuilder();
		boolean iuRes = false;
		if (selectRes != null && selectRes.size() > 0) {
			iubuilder.append("update t_xiaoqu set ");
			iubuilder.append("areacode='" + detailEntity.getAreacode() + "',");
			iubuilder.append("xiaoquname='" + detailEntity.getXiaoquname() + "',");
			iubuilder.append("xiaoqutype='" + detailEntity.getXiaoquType() + "',");
			iubuilder.append("address='" + detailEntity.getAddress() + "',");
			iubuilder.append("longitude=" + detailEntity.getLongItude() + ",");
			iubuilder.append("latitude=" + detailEntity.getLatitude() + ",");
			iubuilder.append("phone='" + detailEntity.getPhone() + "',");
			iubuilder.append("headimg='" + detailEntity.getHeadimg() + "',");
			iubuilder.append("fromhost='" + detailEntity.getFromhost() + "',");
			iubuilder.append("fromurl='" + detailEntity.getFromurl() + "',");
			iubuilder.append("updatetime=now() ");
			iubuilder.append("where fromkey='" + detailEntity.getFromkey() + "'");
			String updateSql = this.checkUpdateSql(iubuilder.toString());
			iuRes = dbHandle.update(updateSql);
		} else {
			iubuilder.append("insert into t_xiaoqu areacode (areacode,xiaoquname,xiaoqutype,address,longitude,latitude,phone,headimg,fromhost,fromurl,fromkey,updatetime,createtime) values (");
			iubuilder.append("'" + detailEntity.getAreacode() + "',");
			iubuilder.append("'" + detailEntity.getXiaoquname() + "',");
			iubuilder.append("'" + detailEntity.getXiaoquType() + "',");
			iubuilder.append("'" + detailEntity.getAddress() + "',");
			iubuilder.append("" + detailEntity.getLongItude() + ",");
			iubuilder.append("" + detailEntity.getLatitude() + ",");
			iubuilder.append("'" + detailEntity.getPhone() + "',");
			iubuilder.append("'" + detailEntity.getHeadimg() + "',");
			iubuilder.append("'" + detailEntity.getFromhost() + "',");
			iubuilder.append("'" + detailEntity.getFromurl() + "',");
			iubuilder.append("'" + detailEntity.getFromkey()+ "',now(),now())");
			iuRes = dbHandle.insert(iubuilder.toString());
		}
		ActionRes res = null;
		if (iuRes) {
			res = ActionRes.ANALYSIS_SUCC;
		} else {
			res = ActionRes.DBHAND_FAIL;
		}
		return res;
	}


}
