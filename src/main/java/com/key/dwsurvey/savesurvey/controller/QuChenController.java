package com.key.dwsurvey.savesurvey.controller;

import com.key.common.QuType;
import com.key.common.utils.web.SpringUtils;
import com.key.dwsurvey.savesurvey.entity.QuChenColumn;
import com.key.dwsurvey.savesurvey.entity.QuChenRow;
import com.key.dwsurvey.savesurvey.entity.Question;
import com.key.dwsurvey.savesurvey.entity.QuestionLogic;
import com.key.dwsurvey.savesurvey.service.QuChenColumnManager;
import com.key.dwsurvey.savesurvey.service.QuChenRowManager;
import com.key.dwsurvey.savesurvey.service.QuestionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 矩阵单选，矩阵多选，矩阵填空
 * @author KeYuan(keyuan258@gmail.com)
 * @date 2014下午10:11:53
 *
 * https://github.com/wkeyuan/DWSurvey
 * http://dwsurvey.net
 *
 */
@Controller
@RequestMapping("/qu-chen")
public class QuChenController {

	@Autowired
	private QuestionManager questionManager;
	@Autowired
	private QuChenRowManager quChenRowManager;
	@Autowired
	private QuChenColumnManager quChenColumnManager;

	/**
	 **新保存矩阵单选题
	 * @return
	 * @throws Exception
	 **/
	@RequestMapping(value = "/ajaxSave")
	@ResponseBody
	public String ajaxSave() throws Exception {
		HttpServletRequest request= SpringUtils.getRequest();
		HttpServletResponse response= SpringUtils.getResponse();
		try{
			Question entity=ajaxBuildSaveOption(request);
			questionManager.save(entity);
			String resultJson=buildResultJson(entity);
			/*response.getWriter().write(resultJson);*/
			return  resultJson;
		}catch (Exception e) {
			e.printStackTrace();
			response.getWriter().write("error");
			return "error";
		}
	}
	
	private Question ajaxBuildSaveOption(HttpServletRequest request) throws UnsupportedEncodingException {
		String quId=request.getParameter("quId");
		String belongId=request.getParameter("belongId");
		String quTitle=request.getParameter("quTitle");
		String orderById=request.getParameter("orderById");
		String tag=request.getParameter("tag");
		String quType=request.getParameter("quType");
		//isRequired 是否必选
		String isRequired=request.getParameter("isRequired");
		//hv 1水平显示 2垂直显示
		String hv=request.getParameter("hv");
		//randOrder 选项随机排列
		String randOrder=request.getParameter("randOrder");
		String cellCount=request.getParameter("cellCount");
		
		if("".equals(quId)){
			quId=null;
		}
		Question entity=questionManager.getModel(quId);
		entity.setBelongId(belongId);
		if(quTitle!=null){
			quTitle=URLDecoder.decode(quTitle,"utf-8");
			entity.setQuTitle(quTitle);
		}
		entity.setOrderById(Integer.parseInt(orderById));
		entity.setTag(Integer.parseInt(tag));
		//quType
		entity.setQuType(QuType.valueOf(quType));
		//参数
		isRequired=(isRequired==null || "".equals(isRequired))?"0":isRequired;
		hv=(hv==null || "".equals(hv))?"0":hv;
		randOrder=(randOrder==null || "".equals(randOrder))?"0":randOrder;
		cellCount=(cellCount==null || "".equals(cellCount))?"0":cellCount;
		
		entity.setIsRequired(Integer.parseInt(isRequired));
		entity.setHv(Integer.parseInt(hv));
		entity.setRandOrder(Integer.parseInt(randOrder));
		entity.setCellCount(Integer.parseInt(cellCount));
		//quChenColumn列选项
		Map<String, Object> columnOptionNameMaps=WebUtils.getParametersStartingWith(request, "columnValue_");
		List<QuChenColumn> quChenColumns=new ArrayList<QuChenColumn>();
		for (String key : columnOptionNameMaps.keySet()) {
			String optionId=request.getParameter("columnId_"+key);
			Object optionName=columnOptionNameMaps.get(key);
			String optionNameValue=(optionName!=null)?optionName.toString():"";
			QuChenColumn quChenColumn=new QuChenColumn();
			if("".equals(optionId)){
				optionId=null;
			}
			quChenColumn.setId(optionId);
			optionNameValue=URLDecoder.decode(optionNameValue,"utf-8");
			quChenColumn.setOptionName(optionNameValue);
			quChenColumn.setOrderById(Integer.parseInt(key));
			quChenColumns.add(quChenColumn);
		}
		entity.setColumns(quChenColumns);
		
		//quChenRow行选项
		Map<String, Object> rowOptionNameMaps=WebUtils.getParametersStartingWith(request, "rowValue_");
		List<QuChenRow> quChenRows=new ArrayList<QuChenRow>();
		for (String key : rowOptionNameMaps.keySet()) {
			String optionId=request.getParameter("rowId_"+key);
			Object optionName=rowOptionNameMaps.get(key);
			String optionNameValue=(optionName!=null)?optionName.toString():"";
			QuChenRow quChenRow=new QuChenRow();
			if("".equals(optionId)){
				optionId=null;
			}
			quChenRow.setId(optionId);
			optionNameValue=URLDecoder.decode(optionNameValue,"utf-8");
			quChenRow.setOptionName(optionNameValue);
			quChenRow.setOrderById(Integer.parseInt(key));
			quChenRows.add(quChenRow);
		}
		entity.setRows(quChenRows);
		
		//逻辑选项设置
		Map<String, Object> quLogicIdMap=WebUtils.getParametersStartingWith(request, "quLogicId_");
		List<QuestionLogic> quLogics=new ArrayList<QuestionLogic>();
		for (String key : quLogicIdMap.keySet()) {
			String cgQuItemId=request.getParameter("cgQuItemId_"+key);
			String skQuId=request.getParameter("skQuId_"+key);
			String visibility=request.getParameter("visibility_"+key);
			String logicType=request.getParameter("logicType_"+key);
			Object quLogicId=quLogicIdMap.get(key);
			String quLogicIdValue=(quLogicId!=null)?quLogicId.toString():"";
			
			QuestionLogic quLogic=new QuestionLogic();
			if("".equals(quLogic)){
				quLogic=null;
			}
			quLogic.setId(quLogicIdValue);
			quLogic.setCgQuItemId(cgQuItemId);
			quLogic.setSkQuId(skQuId);
			quLogic.setVisibility(Integer.parseInt(visibility));
			quLogic.setTitle(key);
			quLogic.setLogicType(logicType);
			quLogics.add(quLogic);
		}
		entity.setQuestionLogics(quLogics);
		
		return entity;
	}
	
	public static String buildResultJson(Question entity){
		//{id:'null',quItems:[{id:'null',title:'null'},{id:'null',title:'null'}]}
		StringBuffer strBuf=new StringBuffer();
		strBuf.append("{id:'").append(entity.getId());
		strBuf.append("',orderById:");
		strBuf.append(entity.getOrderById());
		//列选项
		strBuf.append(",quColumnItems:[");
		List<QuChenColumn> quChenColumns=entity.getColumns();
		for (QuChenColumn column : quChenColumns) {
			strBuf.append("{id:'").append(column.getId());
			strBuf.append("',title:'").append(column.getOrderById()).append("'},");
		}
		int strLen=strBuf.length();
		if(strBuf.lastIndexOf(",")==(strLen-1)){
			strBuf.replace(strLen-1, strLen, "");
		}
		strBuf.append("]");
		
		//行选项
		strBuf.append(",quRowItems:[");
		List<QuChenRow> rows=entity.getRows();
		for (QuChenRow quChenRow : rows) {
			strBuf.append("{id:'").append(quChenRow.getId());
			strBuf.append("',title:'").append(quChenRow.getOrderById()).append("'},");
		}
		strLen=strBuf.length();
		if(strBuf.lastIndexOf(",")==(strLen-1)){
			strBuf.replace(strLen-1, strLen, "");
		}
		strBuf.append("]");
		
		//逻辑选项
		strBuf.append(",quLogics:[");
		List<QuestionLogic> questionLogics=entity.getQuestionLogics();
		if(questionLogics!=null){
			for (QuestionLogic questionLogic : questionLogics) {
				strBuf.append("{id:'").append(questionLogic.getId());
				strBuf.append("',title:'").append(questionLogic.getTitle()).append("'},");
			}
		}
		strLen=strBuf.length();
		if(strBuf.lastIndexOf(",")==(strLen-1)){
			strBuf.replace(strLen-1, strLen, "");
		}
		strBuf.append("]}");
		return strBuf.toString();
	}
	
	/**
	 * 删除矩阵单选题列选项
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/ajaxDeleteColumn")
	@ResponseBody
	public String ajaxDeleteColumn() throws Exception {
		HttpServletRequest request= SpringUtils.getRequest();
		HttpServletResponse response= SpringUtils.getResponse();
		try{
			String quItemId=request.getParameter("quItemId");
			quChenColumnManager.ajaxDelete(quItemId);
			response.getWriter().write("true");
			return "true";
		}catch(Exception e){
			e.printStackTrace();
			response.getWriter().write("error");
			return "error";
		}
	}

	/**
	 * 删除列选项选项
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/ajaxDeleteRow")
	@ResponseBody
	public String ajaxDeleteRow() throws Exception {
		HttpServletRequest request= SpringUtils.getRequest();
		HttpServletResponse response= SpringUtils.getResponse();
		try{
			String quItemId=request.getParameter("quItemId");
			quChenRowManager.ajaxDelete(quItemId);
			return "true";
		}catch(Exception e){
			e.printStackTrace();
			return "error";
		}
	}
}
