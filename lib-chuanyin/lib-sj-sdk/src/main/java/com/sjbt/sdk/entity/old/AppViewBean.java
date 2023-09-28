package com.sjbt.sdk.entity.old;

import java.util.ArrayList;

public class AppViewBean {

    /**
     * total : 3
     * list : [{"id":1,"name":"列表","sort":0,"using":"1"},{"id":2,"name":"九宫格","sort":1,"using":"0"}]
     */
    private int total;
    private ArrayList<AppViewListBean> list;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public ArrayList<AppViewListBean> getList() {
        return list;
    }

    public void setList(ArrayList<AppViewListBean> list) {
        this.list = list;
    }

    public AppViewBean() {
    }

    @Override
    public String toString() {
        return "AppViewBean{" +
                "total=" + total +
                ", list=" + list +
                '}';
    }
}
