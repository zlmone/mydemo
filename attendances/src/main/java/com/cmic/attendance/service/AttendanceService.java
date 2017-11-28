package com.cmic.attendance.service;

import com.cmic.attendance.dao.AttendanceDao;
import com.cmic.attendance.exception.AttendanceException;
import com.cmic.attendance.model.*;
import com.cmic.attendance.pojo.AttendancePojo;
import com.cmic.attendance.pojo.StatisticsPojo;
import com.cmic.attendance.utils.DateUtils;
import com.cmic.attendance.vo.*;
import com.cmic.saas.base.service.CrudService;
import com.cmic.saas.base.web.RestException;
import com.cmic.saas.utils.StringUtils;
import com.cmic.saas.utils.WebUtils;
import com.github.pagehelper.ISelect;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * 考勤表Service
 */
@Service
@Transactional(readOnly = true)
public class AttendanceService extends CrudService<AttendanceDao, Attendance> {

    private static Logger log = Logger.getLogger(AttendanceService.class);

    @Autowired
    private ClazzesService clazzesService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private GroupAddressService groupAddressService;
    @Autowired
    private GroupRuleService groupRuleService;


    public Attendance get(String id) {
        return super.get(id);
    }

    public List<Attendance> findList(Attendance attendance) {
        return super.findList(attendance);
    }

    public PageInfo<Attendance> findPage(PageInfo<Attendance> page, Attendance attendance) {
        return super.findPage(page, attendance);
    }

    @Transactional(readOnly = false)
    public void save(Attendance attendance) {
        super.save(attendance);
    }

    @Transactional(readOnly = false)
    public void dynamicUpdate(Attendance attendance) {
        super.dynamicUpdate(attendance);
    }

    @Transactional(readOnly = false)
    public void delete(String id) {
        //判断是否存在
        Attendance attendance = get(id);
        if (attendance == null || StringUtils.isEmpty(attendance.getId())) {
            throw new RestException("删除失败，考勤表不存在");
        }
        super.delete(id);
        logger.info("删除考勤表：" + attendance.toJSONString());
    }
//#TODO 获取多地址数据
    public ArrayList<GroupAddressVo> getAllGroupAddress (){

        List<GroupAddress> addressList = groupAddressService.findAll();
        ArrayList<GroupAddressVo> listGroupAddress = new ArrayList<GroupAddressVo>();
        for (GroupAddress groupAddress:addressList) {
            GroupAddressVo groupAddressVo =new GroupAddressVo();
            groupAddressVo.setGroupAttendanceDimension(String.valueOf(groupAddress.getGroupAttendanceDimension()));
            groupAddressVo.setGroupAttendanceLongitude(String.valueOf(groupAddress.getGroupAttendanceLongitude()));
            groupAddressVo.setGroupAddress(groupAddress.getGroupAddress());
            groupAddressVo.setAttendanceGroupId(groupAddress.getAttendanceGroupId());
            listGroupAddress.add(groupAddressVo);
        }
        return listGroupAddress;
    }

//#TODO 判断当前用户是否存在
    /**
     * @param phone
     * @param createDate
     * @return Attendance
     */
    public Attendance checkAttendance(String phone,String createDate) {
        log.debug("检验唯一性考勤数据手机号："+phone +" 考勤日期："+createDate);
        Attendance attendance = dao.getAttendanceByCreatebyAndCreateTime(phone,createDate);
        if ( null ==attendance ) {
            return null;
        } else {
            return attendance;
        }
    }
//#TODO 早上打卡业务
    /**
     * @param attendanceVo 封装前台数据的bean
     * @return AttendanceVo 封装统一返回信息的bean
     */
    @Transactional(readOnly = false)
    public Attendance punchCard(AttendanceVo attendanceVo) throws AttendanceException {

        /*读取规则表
         *跟考勤组已经启用在状态来获取考勤组信息
         */
        GroupRule groupRule = groupRuleService.findGroupNameAndGroupStatus(attendanceVo.getAttendanceGroup(), 0);
        log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+groupRule.getGroupAttendanceWay()+"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        //服务器时间
        Date startDate = new Date();
        String compareTime = DateUtils.getDateToHourMinuteS(startDate);
        String[] compareTimeArry = compareTime.split(":");
        Integer compareHour = Integer.parseInt(compareTimeArry[0]);
        Integer compareMinute = Integer.parseInt(compareTimeArry[1]);

        //考勤的周 日期格式1-2-3-4-5-6-7
        String groupAttendanceWeek = groupRule.getGroupAttendanceWeek();
        String[] attendanceWeek = groupAttendanceWeek.split("-");
        String isForWeek = DateUtils.dayForWeek(startDate)+"";
        List<String> strings = Arrays.asList(attendanceWeek);
        boolean contains = strings.contains(isForWeek);
        //不在考勤期内
        log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>1"+contains+"<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        if (!contains){
            //不在考勤日期内直接返回预留业务
            //判断是否为工作日
            //工作日对应结果为0, 休息日对应结果为1, 节假日对应的结果为2
            String workDay = DateUtils.getWorkDays(startDate);
            if(!"0".equals(workDay)){
                throw new AttendanceException("当前考勤时间不是工作日!");
            }
        }
        //在考勤期内，但是当前日期是法定节假日
        if (contains){
            //判断是否为工作日
            //工作日对应结果为0, 休息日对应结果为1, 节假日对应的结果为2
            String workDay = DateUtils.getWorkDays(startDate);
            if(!"0".equals(workDay)){
                throw new AttendanceException("节假日不用考勤!");
            }
        }

        if (contains){
            //开始读取考勤组考勤的方式
            Integer groupAttendanceWay= groupRule.getGroupAttendanceWay();
            String groupAttendanceWays = groupAttendanceWay + "";
            //一、固定时长
            log.debug("进入打卡业务");
            if ("1".equals(groupAttendanceWays)) {
                //判断当前地点是否异常
                Attendance saveAttendance = new Attendance();
                String distance2 = attendanceVo.getDistance();
                if (distance2 ==null || "".equals(distance2)){
                    distance2="0.0";
                }
                String[] split = distance2.split("\\.");
                String distances=split[0];
                Integer groupAttendanceScope = groupRule.getGroupAttendanceScope();
                if (Integer.parseInt(distances) > groupAttendanceScope) {
                    saveAttendance.setAttendanceStatus("1");
                    saveAttendance.setAttendanceDesc("地点异常");

                } else {
                    //地址重合则会是null或小于都走这里的逻辑
                    saveAttendance.setAttendanceStatus("0");
                }
                //设计考勤时间
                /*String groupAttendanceStart = groupRule.getGroupAttendanceStart();*/
                String groupAttendanceStart = groupRule.getGroupAttendanceStart();
                String[] AttendanceStartArry = groupAttendanceStart.split(":");
                Integer groupAttendanceHour = Integer.parseInt(AttendanceStartArry[0]);
                Integer groupAttendanceMinute = Integer.parseInt(AttendanceStartArry[1]);

                //根据考勤时间不同插入状态码
                if (compareHour < groupAttendanceHour) {
                    //正常考勤
                    saveAttendance.setStartTimeStatus("0");
                } else if (compareHour == groupAttendanceHour) {
                    //正常考勤
                    if (compareMinute < groupAttendanceMinute) {
                        saveAttendance.setStartTimeStatus("0");
                    } else {
                        saveAttendance.setStartTimeStatus("1");
                    }
                } else if (compareHour > groupAttendanceHour) {
                    saveAttendance.setStartTimeStatus("1");
                }
                //插入数据
                saveAttendance.setAttendanceUser(attendanceVo.getUsername());
                Date startTime = DateUtils.getStringsToDates(DateUtils.getDateToStrings(startDate));
                saveAttendance.setStartTime(startTime);
                //年月日
                String dateToYearMonthDay = DateUtils.getDateToYearMonthDay(startDate);
                String[] dateToYearMonthDayArry = dateToYearMonthDay.split("-");
                saveAttendance.setAttendanceMonth(dateToYearMonthDayArry[0] + "-" +
                        dateToYearMonthDayArry[1]);
                saveAttendance.setAttendanceCardStatus("2");
                saveAttendance.setStartLocation(attendanceVo.getLocation());
                saveAttendance.setDailyStatus(0);
                /*saveAttendance.setAttendanceGroup(attendanceVo.getAttendanceGroup());*/
                this.save(saveAttendance);
                /*try {
                    //向统计表插入数据 String CreateBy,String createTime,String userName
                    insetStartStatic(attendanceVo.getPhone(), dateToYearMonthDay, attendanceVo.getUsername());
                } catch (Exception e) {
                    log.debug("插入统计表失败" + e.getMessage());
                }*/
                //返回数据页面
                return saveAttendance;
            } else {
                //2、预留自由打卡业务 插入数据
                Attendance saveAttendance = new Attendance();
                saveAttendance.setAttendanceUser(attendanceVo.getUsername());
                Date startTime = DateUtils.getStringsToDates(DateUtils.getDateToStrings(startDate));
                saveAttendance.setStartTime(startTime);
                String dateToYearMonthDay = DateUtils.getDateToYearMonthDay(startDate);
                String[] dateToYearMonthDayArry = dateToYearMonthDay.split("-");
                saveAttendance.setAttendanceMonth(dateToYearMonthDayArry[0] + "-" +
                        dateToYearMonthDayArry[1]);
                saveAttendance.setStartLocation(attendanceVo.getLocation());
                saveAttendance.setDailyStatus(0);
                saveAttendance.setAttendanceGroup(attendanceVo.getAttendanceGroup());
                saveAttendance.setStartTimeStatus("0");
                saveAttendance.setAttendanceCardStatus("2");
                saveAttendance.setAttendanceDimensionStart(attendanceVo.getAttendanceDimension());
                saveAttendance.setAttendanceLongitudeStart(attendanceVo.getAttendanceLongitude());
                this.save(saveAttendance);
                /*try {
                    //向统计表插入数据 String CreateBy,String createTime,String userName
                    insetStartStatic(attendanceVo.getPhone(), dateToYearMonthDay, attendanceVo.getUsername());
                } catch (Exception e) {
                    log.debug("插入统计表失败" + e.getMessage());
                }*/
                //返回数据页面
                return saveAttendance;
            }
        }
        //考勤异常
        return null;
    }

    //TODO 下班打卡业务
    /*
      @param  attendanceEndVo 封装下班业务的bean
      @return AttendanceEndVo 返回Controller的bean
     */
    @Transactional(readOnly = false)
    public Attendance punchCardEnd(AttendanceEndVo attendanceEndVo ) throws AttendanceException {

        /*读取规则表
         *跟考勤组已经启用在状态来获取考勤组信息
         */
        GroupRule groupRule = groupRuleService.findGroupNameAndGroupStatus(attendanceEndVo.getAttendanceGroup(), 0);
        //服务器时间
        Date startDate = new Date();
        String compareTime = DateUtils.getDateToHourMinuteS(startDate);
        String[] compareTimeArry = compareTime.split(":");
        Integer compareHour = Integer.parseInt(compareTimeArry[0]);
        Integer compareMinute = Integer.parseInt(compareTimeArry[1]);
        String groupAttendanceWay = groupRule.getGroupAttendanceWay()+"";
        //一、固定时长
        if ("1".equals(groupAttendanceWay)){
            log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>进入固定打卡业务<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            Attendance saveAttendance=null;
            String dateToYearMonthDay2 = DateUtils.getDateToYearMonthDay(startDate);
            //查询当前用户数据是否存在
            Attendance attendance = checkAttendance(attendanceEndVo.getPhone(),dateToYearMonthDay2);
            if (null==attendance){
                saveAttendance= new Attendance();
            }else{
                saveAttendance=attendance;
                /*//插入数据 如果上班没打也算异常
                if (null ==attendance.getStartTime()){
                      attendance.setAttendanceStatus("1");
                      attendance.setAttendanceDesc("上班卡没打");
                }*/
                saveAttendance.setUpdateDate(startDate);
            }

            /*//判断当前地点是否异常
            String distance2 = attendanceEndVo.getDistance();
            if (distance2 ==null || "".equals(distance2)){
                distance2="0.0";
            }
            String[] split = distance2.split("\\.");
            String distances=split[0];
            Integer groupAttendanceScope = Integer.parseInt(clazzes.getNomalAddress());
            if (Integer.parseInt(distances) > groupAttendanceScope) {
                saveAttendance.setAttendanceStatus("1");
                saveAttendance.setAttendanceDesc("地点异常");

            } else {
                //地址重合则会是null或小于都走这里的逻辑
                saveAttendance.setAttendanceStatus("0");
            }*/

            //设计考勤时间
            /*String groupAttendanceStart = groupRule.getGroupAttendanceStart();*/
            String groupAttendanceEnd = groupRule.getGroupAttendanceEnd()+"";
            String[] AttendanceStartArry = groupAttendanceEnd.split(":");
            Integer groupAttendanceHour = Integer.parseInt(AttendanceStartArry[0]);
            Integer groupAttendanceMinute = Integer.parseInt(AttendanceStartArry[1]);

            /*大于或者等18点标准时间则为正常
             * 0为打卡正常
             * 1为迟到
             */
            if (compareHour>groupAttendanceHour){
                saveAttendance.setEndTimeStatus("0");
            }else if (compareHour==groupAttendanceHour){
                if (compareMinute>groupAttendanceMinute){
                    saveAttendance.setEndTimeStatus("0");
                }else {
                    saveAttendance.setEndTimeStatus("1");
                }
            }else {
                    saveAttendance.setEndTimeStatus("1");
            }

            //插入数据
            saveAttendance.setAttendanceUser(attendanceEndVo.getUsername());
            Date endTime = DateUtils.getStringsToDates(DateUtils.getDateToStrings(startDate));
            saveAttendance.setEndTime(endTime);
            //年月日
            String dateToYearMonthDay = DateUtils.getDateToYearMonthDay(startDate);
            String[] dateToYearMonthDayArry = dateToYearMonthDay.split("-");
            saveAttendance.setAttendanceMonth(dateToYearMonthDayArry[0]+"-"+
                    dateToYearMonthDayArry[1]);
            saveAttendance.setAttendanceCardStatus("0");
            saveAttendance.setEndLocation(attendanceEndVo.getLocation());
            saveAttendance.setAttendanceGroup(attendanceEndVo.getAttendanceGroup());
            saveAttendance.setAttendanceDimensionEnd(attendanceEndVo.getAttendanceDimension());
            saveAttendance.setAttendanceLongitudeEnd(attendanceEndVo.getAttendanceLongitude());
            //保存数据
            this.save(saveAttendance);
            /**
             * 统计考勤信息数据
             *1、是否早退
             *2、打下班卡时间
             */
            //返回数据
            String id = saveAttendance.getId();
            saveAttendance.setId(id);
            return saveAttendance;
        }else {
            //二、自由模式。预留业务
            log.debug(">>>>>>>>>>>>>>>>>>>>>>>>>>进入自由模式打卡<<<<<<<<<<<<<<<<<<<<<<<<<<<");


            return null;
        }
    }


    /**
     * @author 何家来
     * @return
     * 考勤统计,按日统计早到榜
     */
    @Transactional(readOnly = false)
    public Map<String, Object> checkAttendanceByDay(QueryAttendanceVo queryAttendanceVo) {

        String date = queryAttendanceVo.getDate();
        PageInfo page = queryAttendanceVo.getPageInfo();
        if(page==null){
            Map<String, Object> map2 = new HashMap<>();
            map2.put("pageInfo",new ArrayList<HashMap>());
            map2.put("total",0);
            map2.put("pageCount",0);
            return map2;
        }

        if(page.getPageNum() <= 0) {
            page.setPageNum(1);
        }
        if(page.getPageSize() <= 0) {
            page.setPageSize(10);
        }
        PageHelper.startPage(page.getPageNum(), page.getPageSize(),"startTime");

        HttpServletRequest request = WebUtils.getRequest();
        AttendanceUserVo attendanceUserVo = (AttendanceUserVo)request.getSession().getAttribute("attendanceUserVo");
        String attendanceGroup = attendanceUserVo.getAttendanceGroup();


        AttendancePojo attendancePojo = new AttendancePojo();
        attendancePojo.setDate(date);//日期格式:2017-11-11
        attendancePojo.setAttendanceGroup(attendanceGroup);//所属考勤组名

        List<Map> pageInfo = (List<Map>)dao.checkAttendanceByDay(attendancePojo);
        Page pi = (Page)pageInfo;
        long total = pi.getTotal();
        Map<String, Object> map = new HashMap<>();
        map.put("pageInfo",pageInfo);
        map.put("total",total);
        map.put("pageCount",pi.getPages());
        return map;
    }

    /**
     * @author 何家来
     * @return
     * 考勤统计,按日统计迟到榜
     */
    public Map<String, Object> checkAttendanceLatterByDay(QueryAttendanceVo queryAttendanceVo) {

        String date = queryAttendanceVo.getDate();
        PageInfo page = queryAttendanceVo.getPageInfo();

        HttpServletRequest request = WebUtils.getRequest();
        AttendanceUserVo attendanceUserVo = (AttendanceUserVo)request.getSession().getAttribute("attendanceUserVo");
        String attendanceGroup = attendanceUserVo.getAttendanceGroup();

        if(page.getPageNum() <= 0) {
            page.setPageNum(1);
        }
        if(page.getPageSize() <= 0) {
            page.setPageSize(10);
        }

        AttendancePojo attendancePojo = new AttendancePojo();
        attendancePojo.setDate(date);
        attendancePojo.setAttendanceGroup(attendanceGroup);
        PageHelper.startPage(page.getPageNum(), page.getPageSize(),"startTime DESC");

        List<Map> pageInfo = (List<Map>)this.dao.checkAttendanceLatterByDay(attendancePojo);
        Page pi = (Page)pageInfo;
        long total = pi.getTotal();
        Map<String, Object> map = new HashMap<>();
        map.put("pageInfo",pageInfo);
        map.put("total",total);
        map.put("pageCount",pi.getPages());
        return map;
    }


    /**
     * @author 何家来
     * @return
     * 考勤统计,按日统计出勤率
     */
    public Map<String,Object> checkAttendanceData(QueryAttendanceVo queryAttendanceVo) {

        String date = queryAttendanceVo.getDate();
        HttpServletRequest request = WebUtils.getRequest();
        AttendanceUserVo attendanceUserVo = (AttendanceUserVo)request.getSession().getAttribute("attendanceUserVo");
        String attendanceGroup = attendanceUserVo.getAttendanceGroup();

        Map<String, Object> map = new HashMap<>();
//        获取班次信息
        Clazzes clazzes = clazzesService.getByGroupName(attendanceGroup);
        if(clazzes==null){
            return null;
        }

        int startWork = clazzesService.startWork(attendanceGroup);
        int endWork = clazzesService.endWork(attendanceGroup);
        if(startWork>0){
            map.put("startWorkFlag","1");
        }else{
            map.put("startWorkFlag","0");
        }
        if(endWork>0){
            map.put("endWorkFlag","1");
            map.put("startWorkFlag","0");
        }else{
            map.put("endWorkFlag","0");
        }

        int total = clazzes.getTotal();
        map.put("total",total);

        AttendancePojo attendancePojo = new AttendancePojo();
        attendancePojo.setDate(date);
        attendancePojo.setAttendanceGroup(attendanceGroup);
//       获取当天打卡人数
        int workCount = this.dao.getWorkCount(attendancePojo);
        map.put("workCount",workCount);
        map.put("noWorkCount",total-workCount);

//        获取外勤人数
        int outworkCount = this.dao.getOutworkCount(attendancePojo);
        map.put("outworkCount",outworkCount);

//       当天迟到人数
        StatisticsPojo statisticsPojo = new StatisticsPojo();
        statisticsPojo.setDate(date);
        statisticsPojo.setAttendanceGroup(attendanceGroup);
        int latterCount = this.dao.getLatterCount(statisticsPojo);
        map.put("latterCount",latterCount);

        return map;
    }

    //记录上班时间数据
    public void  insetStartStatic(String CreateBy,String createTime,String userName){
        Statistics DBstatistics =
                statisticsService.checkAttendanceByCreateByAndCreateTime(CreateBy, createTime);
        if (null==DBstatistics){
            Statistics saveStatistics = new Statistics();
            saveStatistics.setLateTime(1);
            saveStatistics.setUsername(userName);
            statisticsService.save(saveStatistics);
        }else {
            if (0==DBstatistics.getLateTime()){
                DBstatistics.setLateTime(1);
            }else {
                int lateTime = DBstatistics.getLateTime();
                lateTime=lateTime+1;
                statisticsService.save(DBstatistics);
            }
        }
    }



 /**
     * 查询考勤列表数据分页
     * @param pageNum 当前页
     * @param pageSize 显示条数
     * @param attendance  人名 状态  开始时间 结束时间
     * @return map集合
     */
    public Map<String, Object> selectAttendances(Integer pageNum , Integer pageSize, Attendance attendance){
        PageInfo<Map> pageInfo = PageHelper.startPage(pageNum, pageSize,"a.create_time DESC").doSelectPageInfo(new ISelect() {
            @Override
            public void doSelect() {
                dao.selectAttendances(attendance);
            }
        });
        //创建封装数据
        Map<String, Object> dateMap = new HashMap<>();
        //考勤数据
        dateMap.put("attendances",pageInfo.getList());
        //当前页
        dateMap.put("pageNum",pageNum);
        //显示条数
        dateMap.put("pageSize",pageSize);
        //总页数
        dateMap.put("pageCount",pageInfo.getPages());
        //总记录数
        dateMap.put("pageTotal",pageInfo.getTotal());
        return  dateMap;
    }


    /**
     * 查询考勤数据导出
     * @param attendance   人名 状态  开始时间 结束时间
     *          filePath   保存的路径
     * @return 状态码 0
     */
    public String exportExcel( Attendance attendance){
        //获取数据
        List<Attendance> attendanceList = dao.selectExportAttendanceData(attendance);
        Configuration configuration = null;

        StringWriter stringWriter = null;
        BufferedWriter bufferedWriter = null ;
        try {

            /** 创建Configuration配置信息对象,需要指定版本号 */
            configuration = new Configuration(Configuration.VERSION_2_3_25);
            /** 通过Configuration设置模版文件的基础路径 */
            configuration.setClassForTemplateLoading(this.getClass(), "/templates");
            /** 通过Configuration获取指定模版文件对应的模版对象 */
            Template template = configuration.getTemplate("excel.ftl");
            /** 定义模版中需要的数据模型 */
            Map<String, Object> dataModel = new HashMap<>();
            /** 设置数据 */
            dataModel.put("attendanceList", attendanceList);
            dataModel.put("currMonth",attendance.getAttendanceMonth());

            stringWriter = new StringWriter();
            bufferedWriter = new BufferedWriter(stringWriter);
            template.process(dataModel,bufferedWriter);

            return stringWriter.toString() ;
        }catch (Exception e){
            e.printStackTrace();
            throw new RestException("导出失败!");
        }finally {
            try {
                bufferedWriter.flush();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    stringWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 通过考勤月份查询当月的考勤数据
     * @param paramMap 封装 attaendanceMonth考勤月份,pageSize分页显示条数,pageNum第几页 三个参数
     * @param phone 用户电话号码
     * @return 考勤数据集合
     */
    public Map<String,Object> getMonthAttendanceData(Map<String,Object> paramMap ,String phone){
        //设置查询参数和排序条件
        int pageSize = (int)paramMap.get("pageSize");
        int pageNum = (int)paramMap.get("pageNum");
        PageHelper.startPage(pageNum,pageSize,"a.create_time ASC");
        paramMap.put("createBy",phone);

        String attaendanceMonth = (String)paramMap.get("attaendanceMonth");
        paramMap.put("attaendanceMonth",attaendanceMonth.replace("/","-"));
        //分页查询并获取分页信息
        List<Map> attendanceList = dao.findAttendanceList(paramMap);
        PageInfo<Attendance> pageInfo =  new PageInfo(attendanceList);

        //创建对象对相应数据进行封装
        Map<String,Object> responseMap = new HashMap<String,Object>();
        List< Map<String,Object>> attendList = new ArrayList<>();

        //获取总页数和总记录数
        responseMap.put("totalPages",pageInfo.getPages());
        responseMap.put("totalCount",pageInfo.getTotal());
        responseMap.put("attendanceList",pageInfo.getList());
        return responseMap;

    }


   /* //统计正常下班数据 只更新数据不做迟到计算
    public void  insetEndMStatic(InsetEndStaticBo insetEndStaticBo) {
        Statistics DBstatistics =
                statisticsService.checkAttendanceByCreateByAndCreateTime(insetEndStaticBo.getCreateBy()
                        , insetEndStaticBo.getCreateTime());
        if (null ==DBstatistics){
            //产生统计数据
            Statistics saveStatistics = new Statistics();
            //如果没打上班卡，使用默认时间来统计时长 14点开始上班 早于 12点打下班卡
            Date date=new Date();
            String dates = DateUtils.getDateToStrings(date);
            String Hour = dates.split(" ")[1].split(":")[0];
            int Hours=Integer.parseInt(Hour);
            String startTime=null;

            if (null==insetEndStaticBo.getStartTime() && Hours>=14){
                startTime = "14:00:00";
            }else if (null==insetEndStaticBo.getStartTime() && Hours<=14){
                startTime=insetEndStaticBo.getStartTime();
            }else {
                startTime=insetEndStaticBo.getStartTime();
            }
            String[] startTimeArry = startTime.split(":");
            int starHour=Integer.parseInt(startTimeArry[0]);
            int starMinute=Integer.parseInt(startTimeArry[1]);
            int starTime=starHour*60+starMinute;
            String offtime = insetEndStaticBo.getOfftime();
            String[]offtimeArry = offtime.split(":");
            int offHour=Integer.parseInt(offtimeArry[0]);
            int offMinute=Integer.parseInt(offtimeArry[1]);
            int offTime=offHour*60+offMinute;
            int saveTime=offTime-starTime;
            saveStatistics.setOfficeTime(saveTime);
            saveStatistics.setUsername(insetEndStaticBo.getUserName());
            statisticsService.save(saveStatistics);
        }else {
            //如果没打上班卡，使用默认时间来统计时长 14点开始上班 早于
            Date date=new Date();
            String dates = DateUtils.getDateToStrings(date);
            String Hour = dates.split(" ")[1].split(":")[0];
            int Hours=Integer.parseInt(Hour);
            String startTime3=null;
            if (null==insetEndStaticBo.getStartTime() && Hours>=14){
                startTime3 = "14:00:00";
            }else if (null==insetEndStaticBo.getStartTime() && Hours<14){
                startTime3=insetEndStaticBo.getStartTime();
            }else {
                startTime3=insetEndStaticBo.getStartTime();
            }
            String[] startTimeArry = startTime3.split(":");
            int starHour=Integer.parseInt(startTimeArry[0]);
            int starMinute=Integer.parseInt(startTimeArry[1]);
            int starTime=starHour*60+starMinute;
            //如果是负数则为0
            String offtime = insetEndStaticBo.getOfftime();
            String[] offtimeArry = offtime.split(":");
            int offHour=Integer.parseInt(offtimeArry[0]);
            int offMinute=Integer.parseInt(offtimeArry[1]);
            *//*int offSecond=Integer.parseInt(offtimeArry[2]);
            int offCreated=offHour*3600*1000+offMinute*60*1000+offSecond*1000;*//*
            int offTime=offHour*60+ offMinute;
            int saveTime=offTime-starTime;
            DBstatistics.setOfficeTime(saveTime);
            DBstatistics.setUsername(insetEndStaticBo.getUserName());
            statisticsService.save(DBstatistics);
        }
    }*/
}