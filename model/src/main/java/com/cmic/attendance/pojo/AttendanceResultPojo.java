package com.cmic.attendance.pojo;/**
 * Created by pc on 2017/10/30.
 */

import java.io.Serializable;

/**
 * @author 何家来
 * @create 2017-10-30 19:04
 **/
public class AttendanceResultPojo  implements Serializable,Comparable<Object> {


    private String workStartTime;
    private String workEndTime;
    private float workHour;
    private String username;

    public float getWorkHour() {
        return workHour;
    }

    public String getWorkStartTime() {
        return workStartTime;
    }

    public void setWorkStartTime(String workStartTime) {
        this.workStartTime = workStartTime;
    }

    public String getWorkEndTime() {
        return workEndTime;
    }

    public void setWorkEndTime(String workEndTime) {
        this.workEndTime = workEndTime;
    }

    public void setWorkHour(float workHour) {
        this.workHour = workHour;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public int compareTo(Object o) {
        if(this ==o){
            return 0;
        }
        else if (o!=null && o instanceof AttendanceResultPojo) {
            AttendanceResultPojo u = (AttendanceResultPojo) o;
            if(workHour>=u.workHour){
                return -1;
            }else{
                return 1;
            }
        }else{
            return -1;
        }
    }
}
