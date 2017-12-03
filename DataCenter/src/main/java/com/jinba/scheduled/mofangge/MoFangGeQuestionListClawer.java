package com.jinba.scheduled.mofangge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.jinba.core.BaseListClawer;
import com.jinba.pojo.QuestionSourceEntity;
import com.jinba.pojo.QuestionSubjectEntity;
import com.jinba.spider.core.HttpMethod;
import com.jinba.spider.core.HttpResponseConfig;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

public class MoFangGeQuestionListClawer extends BaseListClawer<QuestionSourceEntity> implements Callable<List<QuestionSourceEntity>> {

	private static final int TARGETID = 0;
	private String eachPageUrl;
	private QuestionSubjectEntity qSubject;
	private int classifyId;
	private static Pattern pattern = Pattern.compile("http://m.mofangge.com/html/qDetail/(.*).html");
	
	public MoFangGeQuestionListClawer (Map<Params, String> paramsMap, CountDownLatchUtils cdl) {
		super(TARGETID, cdl);
		this.paramsMap = paramsMap;
	}
	
	@Override
	protected ActionRes initParams() {
		String subject = paramsMap.get(Params.subject);
		qSubject = QuestionSubjectEntity.valueOf(subject);
		classifyId = Integer.parseInt(paramsMap.get(Params.classify));
		eachPageUrl = "http://m.mofangge.com/Qlist/" + subject + "/" + classifyId + "/%d/";
		return ActionRes.INITSUCC;
	}

	@Override
	protected void analysisAction(List<QuestionSourceEntity> box) {
		int pageIndex = 1;
		boolean hasNext = false;
		do {
			hasNext = false;
			String url = String.format(eachPageUrl, pageIndex);
			HttpMethod method = new HttpMethod(TARGETID);
//			HttpMethod method = new HttpMethod();
			String html = method.GetHtml(url, HttpResponseConfig.ResponseAsStream);
			Document doc = Jsoup.parse(html, url);
			Elements nodes = doc.select("ul.qtlist > li.qtlistli");
			for (Element node : nodes) {
				QuestionSourceEntity x = new QuestionSourceEntity();
				x.setSubjectId(qSubject.code);
				String grade = node.select("div.qtlista>span").text().trim();
				if (StringUtils.isBlank(grade)) {
					continue;
				}
				x.setGrade(grade);
				String fromUrl = node.select("a").attr("href").trim();
				if (StringUtils.isBlank(fromUrl)) {
					continue;
				}
				x.setFromurl(fromUrl);
				Matcher matcher = pattern.matcher(fromUrl);
				String seq = StringUtils.EMPTY;
				if (matcher.find()) {
					seq = matcher.group(1);
				}
				x.setSeqCode(seq);
				box.add(x);
				hasNext = true;
			}
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			pageIndex++;
		} while (hasNext && pageIndex <200);
	}
	
	public List<QuestionSourceEntity> call() {
		List<QuestionSourceEntity> list = this.listAction();
		return list;
	}

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		Map<Params, String> paramsMap = new HashMap<Params, String>();
		paramsMap.put(Params.subject, "shuxue");
		paramsMap.put(Params.classify, "1");
		try {
			new MoFangGeQuestionListClawer(paramsMap, new CountDownLatchUtils(1)).call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
