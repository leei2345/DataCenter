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

import com.alibaba.fastjson.JSON;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.jinba.dao.MysqlDao;
import com.jinba.pojo.QuestionSourceEntity;
import com.jinba.pojo.QuestionSubjectEntity;
import com.jinba.scheduled.mofangge.MoFangGeQuestionListClawer;
import com.jinba.spider.core.Params;
import com.jinba.utils.CountDownLatchUtils;

/**
 * (1) 获取全部类目
 * @author zhangxiaolei
 *
 */
@Component
public class MoFangGeQuestionListTask {

	@Autowired
	private MysqlDao dao;
	private static String insertSqlTemp = "insert into tb_question_source (subject_id, classify_id, seq_code, source_url, grade) values (%d, %d, '%s', '%s', '%s')"; 
	private static String selectSqlTemp = "select * from tb_question_source where subject_id=%d and classify_id=%d and seq_code='%s'";
	private static String updateSqlTemp = "update tb_question_source set grade='%s',source_url='%s' where id=%d";
	private static String selectAllClassifySql = "select * from tb_question_classify";
	private static String filterSql = "select CONCAT_WS('|', subject_id, classify_id) as sandc from `tb_question_source` GROUP BY subject_id,`classify_id`";
	private Logger logger = LoggerFactory.getLogger(MoFangGeQuestionListTask.class);
	
	public void run() {
		List<Map<String, Object>> selectAllClassifyRes = dao.select(selectAllClassifySql);
		int size = selectAllClassifyRes.size();
		List<Map<String, Object>> filterSqlRes = dao.select(filterSql);
		List<String> filterList = Lists.transform(filterSqlRes, new Function<Map<String, Object>, String>() {
			@Override
			public String apply(Map<String, Object> arg0) {
				return (String) arg0.get("sandc");
			}
		});
		for (int index = 0; index < size; index++) {
			Map<String, Object> map = selectAllClassifyRes.get(index);
			long classifyId = (long) map.get("id");
			int subjectId = (int) map.get("subject_id");
			String filterStr = subjectId + "|" + classifyId;
			int number = (int) map.get("number");
			QuestionSubjectEntity subjectEntity = QuestionSubjectEntity.getSubjectByCode(subjectId);
			String subjectValue = subjectEntity.toString();
			Map<Params, String> paramsMap = new HashMap<Params, String>();
			paramsMap.put(Params.subject, subjectValue);
			paramsMap.put(Params.classify, String.valueOf(number));
			if (filterList.contains(filterStr)) {
				logger.info("[Filter][paramsMap=" + JSON.toJSONString(paramsMap) + "]");
				continue;
			}
			logger.info("[Start][paramsMap=" + JSON.toJSONString(paramsMap) + "]");
			MoFangGeQuestionListClawer listClawer = new MoFangGeQuestionListClawer(paramsMap, new CountDownLatchUtils(1));
			List<QuestionSourceEntity> questionSourceList = listClawer.listAction();
			for (QuestionSourceEntity questionSourceEntity : questionSourceList) {
				questionSourceEntity.setClassifyId(classifyId);
				String selectSql = String.format(selectSqlTemp, questionSourceEntity.getSubjectId(), questionSourceEntity.getClassifyId(), questionSourceEntity.getSeqCode());
				List<Map<String, Object>> selectRes = dao.select(selectSql);
				if (CollectionUtils.isEmpty(selectRes)) {
					String insertSql = String.format(insertSqlTemp, questionSourceEntity.getSubjectId(), questionSourceEntity.getClassifyId(), questionSourceEntity.getSeqCode(), questionSourceEntity.getFromurl(), questionSourceEntity.getGrade());
					dao.insertAndGetId(insertSql);
					logger.info("[" + questionSourceEntity.getSubjectId() + ", " + questionSourceEntity.getClassifyId() + ", " + questionSourceEntity.getSeqCode() + "][insert]");
				} else {
					Map<String, Object> dbMap = selectRes.get(0);
					long dbId = (long) dbMap.get("id");
					String grade = (String) dbMap.get("grade");
					String sourceUrl = (String) dbMap.get("source_url");
					if (!StringUtils.equals(grade, questionSourceEntity.getGrade()) && !StringUtils.equals(sourceUrl, questionSourceEntity.getFromurl())) {
						String updateSql = String.format(updateSqlTemp, questionSourceEntity.getGrade(), questionSourceEntity.getFromurl(), dbId);
						dao.execut(updateSql);
						logger.info("[" + questionSourceEntity.getSubjectId() + ", " + questionSourceEntity.getClassifyId() + ", " + questionSourceEntity.getSeqCode() + "][update]");
					} else {
						logger.info("[" + questionSourceEntity.getSubjectId() + ", " + questionSourceEntity.getClassifyId() + ", " + questionSourceEntity.getSeqCode() + "][nochange]");
						continue;
					}
				}
				
			}
			logger.info("[paramsMap=" + JSON.toJSONString(paramsMap) + "][Done]");
		}
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext application = new ClassPathXmlApplicationContext(new String[]{"database.xml"});
		application.start();
		MoFangGeQuestionListTask a = (MoFangGeQuestionListTask) application.getBean("moFangGeQuestionListTask");
		a.run();
	}
	

}

