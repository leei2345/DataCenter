package com.jinba.scheduled.mofangge.task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.QuestionSourceEntity;
import com.jinba.pojo.QuestionSubjectEntity;
import com.jinba.scheduled.mofangge.MoFangGeQuestionDetailClawer;
import com.jinba.utils.CountDownLatchUtils;

/**
 * (1) 获取全部类目
 * @author zhangxiaolei
 *
 */
@Component
public class MoFangGeQuestionDetailTask {

	@Autowired
	private MysqlDao dao;
	private int threadPoolSize = 8;
	private ExecutorService detailThreadpool;
	private Logger logger = LoggerFactory.getLogger(MoFangGeQuestionDetailTask.class);
	private static final String selectSqlBySubjectId = "select id,source_url from tb_question_source where subject_id=%d and source_html is null limit %d, 200";
	
	public void run() {
		detailThreadpool = Executors.newFixedThreadPool(threadPoolSize);
		QuestionSubjectEntity[] subjects = QuestionSubjectEntity.values();
		for (QuestionSubjectEntity questionSubjectEntity : subjects) {
			int subjectId = questionSubjectEntity.code;
			logger.info("[Start][subject " + subjectId + "]");
			String sql = String.format(selectSqlBySubjectId, subjectId);
			List<Map<String, Object>> selectRes = dao.select(sql);
			int size = selectRes.size();
			for (int index = 0; index < size; index ++) {
				Map<String, Object> map = selectRes.get(index);
				long id = (long) map.get("id");
				String sourceUrl = (String) map.get("source_url");
				QuestionSourceEntity entity = new QuestionSourceEntity();
				entity.setSourceUrl(sourceUrl);
				entity.setId(id);
				MoFangGeQuestionDetailClawer clawer = new MoFangGeQuestionDetailClawer(entity, new CountDownLatchUtils(size - index));
				detailThreadpool.execute(clawer);
			}
			logger.info("subject " + subjectId + "][Done]");
		}
		logger.info("[" + this.getClass().getSimpleName() + "][Done]");
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		MoFangGeQuestionDetailTask a = (MoFangGeQuestionDetailTask) application.getBean("moFangGeQuestionDetailTask");
		a.run();
	}
	

}

