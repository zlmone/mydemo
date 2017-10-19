package com.key.dwsurvey.answersurvey.dao;

import com.key.common.dao.BaseDao;
import com.key.dwsurvey.savesurvey.entity.Question;
import com.key.dwsurvey.answersurvey.entity.AnEnumqu;

/**
 * 枚举题 interface
 * @author KeYuan(keyuan258@gmail.com)
 *
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 *
 */
public interface AnEnumquDao extends BaseDao<AnEnumqu, String>{

	public void findGroupStats(Question question);

}
