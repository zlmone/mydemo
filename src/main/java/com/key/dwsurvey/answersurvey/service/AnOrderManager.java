package com.key.dwsurvey.answersurvey.service;

import java.util.List;

import com.key.common.service.BaseService;
import com.key.dwsurvey.savesurvey.entity.Question;
import com.key.dwsurvey.answersurvey.entity.AnOrder;

/**
 * 排序题
 * @author keyuan(keyuan258@gmail.com)
 *
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 */
public interface AnOrderManager extends BaseService<AnOrder, String>{
	public List<AnOrder>  findAnswer(String belongAnswerId,String quId);

	public void findGroupStats(Question question);
}