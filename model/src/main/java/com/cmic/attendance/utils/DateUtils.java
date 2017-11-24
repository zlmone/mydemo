package com.cmic.attendance.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DateUtils {
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
    //SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取年月日  yyyy-MM-dd HH:mm:ss 的字符串
     * @param date
     * @return string
     */
    public  static String getDateToStrings(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString=formatter.format(date);
        return dateString;
    }
    /**
     * 获取年月日的字符串
     * @param date
     * @return  String
     */
    public  static String getDateToYearMonthDay(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString=formatter.format(date);
        String[] split = dateString.split(" ");
        return split[0];
    }

    /**
     * 获取HH:mm:ss的字符串
     * @param date
     * @return  String
     */
    public  static String getDateToHourMinuteS(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString=formatter.format(date);
        String[] split = dateString.split(" ");
        return split[1];
    }


    /**
     * 将时间格式转换成为  yyyy-MM-dd的日期格式
     * @param  stingTodate
     * @return date
     */
    public  static Date getStringsToDate(String stingTodate){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date dateString = null;
        try {
            dateString = formatter.parse(stingTodate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }

    /**
     * 将时间格式转换成为  yyyy-MM-dd的日期格式
     * @param date
     * @return date
     */
    public  static Date getStringsToDates(String date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateString = null;
        try {
            dateString = formatter.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }

    /**
     * HH:mm:ss
     * @param date
     * @return date
     */
    public  static Integer DatesToHour(String date){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String dateString=formatter.format(date);
        int dates=Integer.parseInt(dateString.split(":")[1]);
        return dates;
    }

    /**
     * 获取当前  年月 格式 yyyy-MM
     * @return
     */
    public static String getCurrMonth(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        return formatter.format(new Date());
    }

    /**
     * 判断当前日期是星期几
     * @param date
     * @return
     */
    public static int dayForWeek(String date){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date tmpDate = format.parse(date);
            Calendar cal = new GregorianCalendar();
            cal.set(tmpDate.getYear(), tmpDate.getMonth(), tmpDate.getDay());
            return cal.get(Calendar.DAY_OF_WEEK);
        }catch (Throwable e){
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * @param httpUrl :请求接口
     * @param httpArg :参数
     * @return 返回结果
     */
    public static String request(String httpUrl, String httpArg) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        httpUrl = httpUrl + "?" + httpArg;
        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey", "abfa5282a89706affd2e4ad6651c9648");
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 节假日查询
     * date格式为yyyy-MM-dd
     * @return 工作日对应结果为0, 休息日对应结果为1, 节假日对应的结果为2
     */
    public static String getWorkDays(String date){
        String httpUrl = "http://api.goseek.cn/Tools/holiday";
        String fdate = "date=" + date.replace("-","");
        String jsonResult = request(httpUrl, fdate);
        JSONObject jsonObject = JSON.parseObject(jsonResult);
        String value = jsonObject.get("data").toString();
        return value;
    }

}
