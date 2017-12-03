package com.jinba.scheduled.mofangge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jinba.core.BaseListClawer;
import com.jinba.pojo.QuestionClassifyEntity;
import com.jinba.pojo.QuestionSubjectEntity;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

public class MoFangGeClassifyListClawer extends BaseListClawer<QuestionClassifyEntity> {

	private static final int TARGETID = 0;
	private QuestionSubjectEntity subject;
	private static String urlTemp = "http://m.mofangge.com/Qlist/IndexSpar/%s/%d";
	private static Pattern pattern = Pattern.compile("http://m.mofangge.com/Qlist/.*/(\\d+)/");
	
	public MoFangGeClassifyListClawer (Map<Params, String> paramsMap) {
		super(TARGETID, new CountDownLatchUtils(1));
		this.paramsMap = paramsMap;
	}
	
	@Override
	protected ActionRes initParams() {
		String subjectStr = paramsMap.get(Params.subject);
		subject = QuestionSubjectEntity.valueOf(subjectStr);
		return ActionRes.INITSUCC;
	}

	@Override
	protected void analysisAction(List<QuestionClassifyEntity> box) {
		int pageIndex = 1;
		boolean hasNext = false;
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			hasNext = false;
			String url = String.format(urlTemp, subject.toString(), pageIndex);
			HttpMethod method = new HttpMethod(TARGETID);
//			HttpMethod method = new HttpMethod();
			String html = method.GetHtml(url, HttpResponseConfig.ResponseAsStream);
			Document doc = Jsoup.parse(html, url);
			Elements nodes = doc.select("ul.plist > li");
			if (nodes.size() > 0) {
				hasNext = true;
			}
			for (Element node : nodes) {
				QuestionClassifyEntity x = new QuestionClassifyEntity();
				x.setSubjectId(subject.code);
				String classifyUrl = node.select("a").attr("href").trim();
				if (StringUtils.isBlank(classifyUrl)) {
					continue;
				}
				Matcher matcher = pattern.matcher(classifyUrl);
				String number = StringUtils.EMPTY;
				if (matcher.find()) {
					number = matcher.group(1);
				} else {
					continue;
				}
				String classify = node.select("a").text().trim();
				if (StringUtils.isBlank(classify)) {
					continue;
				}
				x.setClassify(classify);
				x.setNumber(Integer.parseInt(number));
				box.add(x);
			}
			pageIndex++;
		} while (hasNext && pageIndex <100);
	}

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		Map<Params, String> paramsMap = new HashMap<Params, String>();
		paramsMap.put(Params.subject, "shuxue");
		try {
			new MoFangGeClassifyListClawer(paramsMap).listAction();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
