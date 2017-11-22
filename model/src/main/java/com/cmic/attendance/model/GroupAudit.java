package com.cmic.attendance.model;

import com.cmic.saas.base.model.DataEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
/**
 * 实体
 */
@ApiModel(value = "GroupAudit", description = "")
public class GroupAudit extends DataEntity<GroupAudit> {

    @ApiModelProperty(value = "日报审核人手机号", example = "日报审核人手机号")
    protected String auditPhone;
    @ApiModelProperty(value = "日报审核人名字", example = "日报审核人名字")
    protected String auditName;
    @ApiModelProperty(value = "企业id", example = "企业id")
    protected String enterpriseId;
    @ApiModelProperty(value = "企业名称（保留字段）", example = "企业名称（保留字段）")
    protected String enterpriseName;
    @ApiModelProperty(value = "关联对应的日报表", example = "关联对应的日报表")
    protected String dailyRuleId;
    @ApiModelProperty(value = "所属考勤组名", example = "所属考勤组名")
    protected String attendanceGroup;

    public GroupAudit(){

    }
    public GroupAudit(String id){
        super(id);
    }

    public String getAuditPhone() {
        return auditPhone;
    }

    public void setAuditPhone(String auditPhone) {
        this.auditPhone = auditPhone;
    }

    public String getAuditName() {
        return auditName;
    }

    public void setAuditName(String auditName) {
        this.auditName = auditName;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getDailyRuleId() {
        return dailyRuleId;
    }

    public void setDailyRuleId(String dailyRuleId) {
        this.dailyRuleId = dailyRuleId;
    }

    public String getAttendanceGroup() {
        return attendanceGroup;
    }

    public void setAttendanceGroup(String attendanceGroup) {
        this.attendanceGroup = attendanceGroup;
    }


}