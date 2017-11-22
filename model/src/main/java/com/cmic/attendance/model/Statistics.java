package com.cmic.attendance.model;

import com.cmic.saas.base.model.DataEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
/**
 * 实体
 */
@ApiModel(value = "Statistics", description = "")
public class Statistics extends DataEntity<Statistics> {

    @ApiModelProperty(value = "迟到次数", example = "迟到次数")
    protected Integer lateTime;
    @ApiModelProperty(value = "早退次数", example = "早退次数")
    protected Integer earlyTime;
    @ApiModelProperty(value = "上班时长", example = "上班时长")
    protected Integer officeTime;
    @ApiModelProperty(value = "补填日报次数", example = "补填日报次数")
    protected String dailyTime;
    @ApiModelProperty(value = "用户名", example = "用户名")
    protected String username;
    @ApiModelProperty(value = "所属考勤组名", example = "所属考勤组名")
    protected String attendanceGroup;

    public Statistics(){

    }
    public Statistics(String id){
        super(id);
    }

    public Integer getLateTime() {
        return lateTime;
    }

    public void setLateTime(Integer lateTime) {
        this.lateTime = lateTime;
    }

    public Integer getEarlyTime() {
        return earlyTime;
    }

    public void setEarlyTime(Integer earlyTime) {
        this.earlyTime = earlyTime;
    }

    public Integer getOfficeTime() {
        return officeTime;
    }

    public void setOfficeTime(Integer officeTime) {
        this.officeTime = officeTime;
    }

    public String getDailyTime() {
        return dailyTime;
    }

    public void setDailyTime(String dailyTime) {
        this.dailyTime = dailyTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAttendanceGroup() {
        return attendanceGroup;
    }

    public void setAttendanceGroup(String attendanceGroup) {
        this.attendanceGroup = attendanceGroup;
    }


}