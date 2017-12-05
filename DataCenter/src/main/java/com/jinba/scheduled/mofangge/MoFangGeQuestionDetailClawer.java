package com.jinba.scheduled.mofangge;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.jinba.core.BaseDetailClawer;
import com.jinba.core.DBHandle;
import com.jinba.pojo.QuestionSourceEntity;
import com.jinba.utils.CountDownLatchUtils;

/**
 * 
 * 2017-12-02T10:39:57.781114Z 1 [Note] A temporary password is generated for root@localhost: 6rNtA02Ihw<w
 * If you lose this password, please consult the section How to Reset the Root Password in the MySQL reference manual.
 * 
 * @author leei
 *
 */
public class MoFangGeQuestionDetailClawer extends BaseDetailClawer<QuestionSourceEntity> {

	private static final int TARGETID = 0;
	private static String updateSqlTemp = "update tb_question_source set source_html='%s' where id=%d";
	
	public MoFangGeQuestionDetailClawer(QuestionSourceEntity detailEntity, CountDownLatchUtils cdl) {
		super(TARGETID, detailEntity, cdl);
	}

	@Override
	protected ActionRes initParams() {
		return ActionRes.INITSUCC;
	}

	@Override
	protected String getDetailHtml() {
		String url = detailEntity.getSourceUrl();
		if (StringUtils.isBlank(url)) {
			return null;
		}
		String html = httpGet(url);
		return html;
	}

	@Override
	protected ActionRes analysistDetail(String html, DBHandle dbHandle) {
		if (StringUtils.isBlank(html)) {
			return ActionRes.ANALYSIS_HTML_NULL;
		}
		long id = detailEntity.getId();
		html = html.replace("'", "\'");
		String updateSql = String.format(updateSqlTemp, html, id);
		boolean iuRes = dbHandle.update(updateSql);
		ActionRes res = null;
		if (iuRes) {
			res = ActionRes.ANALYSIS_SUCC;
		} else {
			res = ActionRes.DBHAND_FAIL;
		}
		return res;
	}
	
	@Override
	public void run() {
		super.run();
	}

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		/** 非酒店 */
		String json = "{\"areacode\":\"1201\",\"content\":\"\",\"fromhost\":\"m.baidu.com\",\"fromkey\":\"\",\"fromurl\":\"http://cpc.people.com.cn/n1/2017/0325/c117005-29168539.html\",\"headimg\":\"\",\"newstime\":\"2017-03-25 10:52:00\",\"options\":128,\"posttime\":\"2017-03-25 17:20:00\",\"source\":\"中国共产党新闻网\",\"title\":\"天津市领导干部学习贯彻党的十八届六中全会精神专题研讨班开班 ...\",\"xiaoquid\":0}";
		QuestionSourceEntity x = JSON.parseObject(json, QuestionSourceEntity.class);
		BaseDetailClawer<QuestionSourceEntity> b = new MoFangGeQuestionDetailClawer(x, new CountDownLatchUtils(1));
		b.detailAction();
	}


}
