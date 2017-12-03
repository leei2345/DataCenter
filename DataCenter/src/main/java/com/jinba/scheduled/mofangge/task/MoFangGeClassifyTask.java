package com.jinba.scheduled.mofangge.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.jinba.dao.MysqlDao;
import com.jinba.pojo.QuestionClassifyEntity;
import com.jinba.pojo.QuestionSubjectEntity;
import com.jinba.scheduled.mofangge.MoFangGeClassifyListClawer;
import com.jinba.spider.core.Params;

/**
 * (1) 获取全部类目
 * @author zhangxiaolei
 *
 */
@Component
public class MoFangGeClassifyTask {

	@Autowired
	private MysqlDao dao;
	private static String insertSqlTemp = "insert into tb_question_classify (subject_id, classify, number) values (%d, '%s', %d)"; 
	private static String selectSqlTemp = "select * from tb_question_classify where subject=%d and number=%d";
	private static String updateSqlTemp = "update tb_question_classify set classify='%s' where id=%d";
	private Logger logger = LoggerFactory.getLogger(MoFangGeClassifyTask.class);
	
	public void run() {
		QuestionSubjectEntity[] subjects = QuestionSubjectEntity.values();
		for (QuestionSubjectEntity questionSubjectEntity : subjects) {
			Map<Params, String> paramsMap = new HashMap<Params, String>();
			paramsMap.put(Params.subject, questionSubjectEntity.toString());
			logger.info("[Start][subject " + questionSubjectEntity + "]");
			MoFangGeClassifyListClawer listClawer = new MoFangGeClassifyListClawer(paramsMap);
			List<QuestionClassifyEntity> listRes = listClawer.listAction();
			for (QuestionClassifyEntity questionClassifyEntity : listRes) {
				String selectSql = String.format(selectSqlTemp, questionClassifyEntity.getSubjectId(), questionClassifyEntity.getNumber());
				List<Map<String, Object>> selectRes = dao.select(selectSql);
				if (CollectionUtils.isEmpty(selectRes)) {
					String insertSql = String.format(insertSqlTemp, questionSubjectEntity.code, questionClassifyEntity.getClassify().replace("'", "\'"), questionClassifyEntity.getNumber());
					dao.insertAndGetId(insertSql);
					logger.info("[" + questionClassifyEntity.getClassify() + "][insert]");
				} else {
					Map<String, Object> dbMap = selectRes.get(0);
					int dbId = (int) dbMap.get("id");
					String dbClassify = (String) dbMap.get("classify");
					if (!StringUtils.equals(dbClassify, questionClassifyEntity.getClassify())) {
						String updateSql = String.format(updateSqlTemp, questionClassifyEntity.getClassify(), dbId);
						dao.execut(updateSql);
						logger.info("[" + questionClassifyEntity.getClassify() + "][update]");
					} else {
						logger.info("[" + questionClassifyEntity.getClassify() + "][nochange]");
						continue;
					}
				}
			}
		}
		logger.info("[" + this.getClass().getSimpleName() + "][Done]");
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		MoFangGeClassifyTask a = (MoFangGeClassifyTask) application.getBean("moFangGeClassifyTask");
		a.run();
	}
	

}

