package net.onest.timestoryprj.constant;

import net.onest.timestoryprj.entity.HistoryDay;
import net.onest.timestoryprj.entity.User;

import java.util.ArrayList;
import java.util.List;

/**
 * 安卓用到的常量 全局对象等等
 */
public class Constant {
    //分隔符
    public static String DELIMITER = "|||";
    //当前登录的用户
    public static User User;




    //历史上的今天事件集合
    public static List<HistoryDay> historyDays = new ArrayList<>();
}