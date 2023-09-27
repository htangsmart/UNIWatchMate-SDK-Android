package com.sjbt.sdk.entity;

import android.text.TextUtils;

public class SportInitInfo {
    /**
     * height : 180
     * weight : 70
     * birthday : 2022-07-16
     * gender : 1
     * heat_goal : 12345
     * step_goal : 10000
     * dis_goal : 5
     */

    private int height = -1;
    private int weight = -1;
    private String birthday = "";
    private int gender = -1;

    private double heat_goal = -1;
    private double step_goal = -1;
    private double dis_goal = -1;

    public double getStep_len() {
        if (height <= 0) {
            return 0.6;
        }
        return height * 0.37;
    }

    public boolean isSportInit() {
        return (heat_goal != -1) && (step_goal != -1) && (dis_goal != -1);
    }

    public boolean isHealthInit() {
//        return (height != -1) && (weight != -1) && (gender != -1) && !TextUtils.isEmpty(birthday);
        return (height != -1) && (weight != -1) && !TextUtils.isEmpty(birthday);
    }

    public boolean isAllInit() {
        return isSportInit() && isHealthInit();
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public double getHeat_goal() {
        return heat_goal;
    }

    public int getHeat_goal_int() {
        return (int) heat_goal;
    }

    public void setHeat_goal(double heat_goal) {
        this.heat_goal = heat_goal;
    }

    public double getStep_goal() {
        return step_goal;
    }

    public int getStep_goal_int() {
        return (int) step_goal;
    }

    public void setStep_goal(double step_goal) {
        this.step_goal = step_goal;
    }

    public double getDis_goal() {
        return dis_goal;
    }

    public int getDis_goal_int() {
        return (int) dis_goal;
    }

    public void setDis_goal(double dis_goal) {
        this.dis_goal = dis_goal;
    }

    @Override
    public String toString() {
        return "SportInitInfo{" +
                "height=" + height +
                ", weight=" + weight +
                ", birthday='" + birthday + '\'' +
                ", gender=" + gender +
                ", heat_goal=" + heat_goal +
                ", step_goal=" + step_goal +
                ", dis_goal=" + dis_goal +
                '}';
    }
}
