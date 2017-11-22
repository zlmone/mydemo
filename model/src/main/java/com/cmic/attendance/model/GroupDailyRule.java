package com.cmic.attendance.model;

import com.cmic.saas.base.model.DataEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
/**
 * 实体
 */
@ApiModel(value = "GroupDailyRule", description = "")
public class GroupDailyRule extends DataEntity<GroupDailyRule> {

    @ApiModelProperty(value = "0/日报 1/周报", example = "0/日报 1/周报")
    protected String ruleDailyType;
    @ApiModelProperty(value = "(注：1-7表示提示日期)", example = "(注：1-7表示提示日期)")
    protected String ruleDailyWeek;
    @ApiModelProperty(value = "关联日报模板模块 ", example = "关联日报模板模块 ")
    protected String ruleTemplateId;
    @ApiModelProperty(value = "关联对应考勤组", example = "关联对应考勤组")
    protected String attendanceGroupId;
    @ApiModelProperty(value = "0/立即启用 1/停用", example = "0/立即启用 1/停用")
    protected String ruleDailyStatus;
    @ApiModelProperty(value = "用户预订的生效时间（待定是年月日）", example = "2017-09-09")
    protected Date ruleDailyReserve;
    @ApiModelProperty(value = "生效时间从-至 至的话默认2099年", example = "2017-09-09")
    protected Date ruleDeadline;
    @ApiModelProperty(value = "所属考勤组名", example = "所属考勤组名")
    protected String attendanceGroup;

    public GroupDailyRule(){

    }
    public GroupDailyRule(String id){
        super(id);
    }

    public String getRuleDailyType() {
        return ruleDailyType;
    }

    public void setRuleDailyType(String ruleDailyType) {
        this.ruleDailyType = ruleDailyType;
    }

    public String getRuleDailyWeek() {
        return ruleDailyWeek;
    }

    public void setRuleDailyWeek(String ruleDailyWeek) {
        this.ruleDailyWeek = ruleDailyWeek;
    }

    public String getRuleTemplateId() {
        return ruleTemplateId;
    }

    public void setRuleTemplateId(String ruleTemplateId) {
        this.ruleTemplateId = ruleTemplateId;
    }

    public String getAttendanceGroupId() {
        return attendanceGroupId;
    }

    public void setAttendanceGroupId(String attendanceGroupId) {
        this.attendanceGroupId = attendanceGroupId;
    }

    public String getRuleDailyStatus() {
        return ruleDailyStatus;
    }

    public void setRuleDailyStatus(String ruleDailyStatus) {
        this.ruleDailyStatus = ruleDailyStatus;
    }

    public Date getRuleDailyReserve() {
        return ruleDailyReserve;
    }

    public void setRuleDailyReserve(Date ruleDailyReserve) {
        this.ruleDailyReserve = ruleDailyReserve;
    }

    public Date getRuleDeadline() {
        return ruleDeadline;
    }

    public void setRuleDeadline(Date ruleDeadline) {
        this.ruleDeadline = ruleDeadline;
    }

    public String getAttendanceGroup() {
        return attendanceGroup;
    }

    public void setAttendanceGroup(String attendanceGroup) {
        this.attendanceGroup = attendanceGroup;
    }


}