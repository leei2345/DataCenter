package com.jinba.scheduled.dianping;

public class DianPing {

	//curl 'http://www.dianping.com/ajax/json/shop/wizard/BasicHideInfoAjaxFP?_nr_force=1449131352000&shopId=24470535' -H 'Cookie: _hc.v=86bd9a03-245b-ab79-cdfe-26508f0cc9b2.1448075468; ua=%E6%B4%9B%E6%BA%90_8408; __utma=1.2056014761.1448854992.1448854992.1449036927.2; __utmc=1; __utmz=1.1448854992.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); PHOENIX_ID=0a0302bc-15166c79ed5-2ec83b; s_ViewType=10; JSESSIONID=C64655804438F77B417E252188241D90; aburl=1; cy=2; cye=beijing' -H 'Accept-Encoding: gzip, deflate, sdch' -H 'X-Request: JSON' -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/45.0.2454.101 Chrome/45.0.2454.101 Safari/537.36' -H 'Accept-Language: zh-CN,zh;q=0.8' -H 'Accept: application/json, text/javascript' -H 'Cache-Control: max-age=0' -H 'X-Requested-With: XMLHttpRequest' -H 'Connection: keep-alive' -H 'Referer: http://www.dianping.com/shop/24470535' --compressed
	public static Location decodePOI(String poi) {
		// settings
		int digi = 16;
		int add = 10;
		int plus = 7;
		int cha = 36;

		int I = -1;
		int H = 0;
		String B = "";
		int J = poi.length();
		char G = poi.charAt(J - 1);
		poi = poi.substring(0, J - 1);
		J--;
		for (int E = 0; E < J; E++) {
			int D = Integer.parseInt(String.valueOf(poi.charAt(E)), cha) - add;
			if (D >= add) {
				D = D - plus;
			}
			B += Integer.toString(D, cha);
			if (D > H) {
				I = E;
				H = D;
			}
		}
		int A = Integer.parseInt(B.substring(0, I), digi);
		int F = Integer.parseInt(B.substring(I + 1), digi);
		float L = (A + F - (0xFF & G)) / 2;
		float K = (F - L) / 100000;
		L /= 100000;
		return new Location("", K, L);
	}
}

class Location {
	String address;
	double latitude;
	double longitude;

	public Location(String address, double latitude, double longitude) {
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
