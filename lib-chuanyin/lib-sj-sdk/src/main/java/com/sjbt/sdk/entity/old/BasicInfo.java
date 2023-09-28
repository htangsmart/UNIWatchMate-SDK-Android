package com.sjbt.sdk.entity.old;

import android.os.Parcel;
import android.os.Parcelable;

public class BasicInfo implements Parcelable {

    /**
     * chip_mode : 6801
     * prod_mode : pen133
     * soft_ver : 2205220800
     * mac_addr : AB:CD:EF:01:00:00
     * dev_id : 2205220800
     * dev_name : 云P3
     * prod_category : 01
     * prod_subcate : 01
     * battery_main : 90
     */

    public String chip_mode;
    public String prod_mode;
    public String soft_ver;
    public String hard_ver;//
    public long remain_memory;//
    public long total_memory;//
    public String prod_date;//
    public String mac_addr;
    public String dev_id;
    public String dev_name;
    public String prod_category;
    public String prod_subcate;
    public String dial_ability;
    public String screen;
    public String alipay;
    public String spo2;
    public int battery_main;
    public int is_charging;
    public int lang;
    public int cw;
    public int ch;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.chip_mode);
        dest.writeString(this.prod_mode);
        dest.writeString(this.soft_ver);
        dest.writeString(this.hard_ver);
        dest.writeLong(this.remain_memory);
        dest.writeLong(this.total_memory);
        dest.writeString(this.prod_date);
        dest.writeString(this.mac_addr);
        dest.writeString(this.dev_id);
        dest.writeString(this.dev_name);
        dest.writeString(this.prod_category);
        dest.writeString(this.prod_subcate);
        dest.writeString(this.dial_ability);
        dest.writeInt(this.battery_main);
        dest.writeInt(this.is_charging);
        dest.writeInt(this.lang);
        dest.writeString(this.screen);
        dest.writeString(this.alipay);
        dest.writeString(this.spo2);
        dest.writeInt(this.cw);
        dest.writeInt(this.ch);
    }

    public void readFromParcel(Parcel source) {
        this.chip_mode = source.readString();
        this.prod_mode = source.readString();
        this.soft_ver = source.readString();
        this.hard_ver = source.readString();
        this.remain_memory = source.readLong();
        this.total_memory = source.readLong();
        this.prod_date = source.readString();
        this.mac_addr = source.readString();
        this.dev_id = source.readString();
        this.dev_name = source.readString();
        this.prod_category = source.readString();
        this.prod_subcate = source.readString();
        this.dial_ability = source.readString();
        this.battery_main = source.readInt();
        this.is_charging = source.readInt();
        this.lang = source.readInt();
        this.alipay = source.readString();
        this.screen = source.readString();
        this.spo2 = source.readString();
        this.ch = source.readInt();
        this.cw = source.readInt();
    }

    public BasicInfo() {
    }

    protected BasicInfo(Parcel in) {
        this.chip_mode = in.readString();
        this.prod_mode = in.readString();
        this.soft_ver = in.readString();
        this.hard_ver = in.readString();
        this.remain_memory = in.readLong();
        this.total_memory = in.readLong();
        this.prod_date = in.readString();
        this.mac_addr = in.readString();
        this.dev_id = in.readString();
        this.dev_name = in.readString();
        this.prod_category = in.readString();
        this.prod_subcate = in.readString();
        this.dial_ability = in.readString();
        this.battery_main = in.readInt();
        this.is_charging = in.readInt();
        this.alipay = in.readString();
        this.screen = in.readString();
        this.spo2 = in.readString();
        this.lang = in.readInt();
        this.ch = in.readInt();
        this.cw = in.readInt();
    }

    public static final Creator<BasicInfo> CREATOR = new Creator<BasicInfo>() {
        @Override
        public BasicInfo createFromParcel(Parcel source) {
            return new BasicInfo(source);
        }

        @Override
        public BasicInfo[] newArray(int size) {
            return new BasicInfo[size];
        }
    };

    @Override
    public String toString() {
        return "BiuDeviceInfo{" +
                "chip_mode='" + chip_mode + '\'' +
                ", prod_mode='" + prod_mode + '\'' +
                ", soft_ver='" + soft_ver + '\'' +
                ", hard_ver='" + hard_ver + '\'' +
                ", remain_memory='" + remain_memory + '\'' +
                ", total_memory='" + total_memory + '\'' +
                ", made_date='" + prod_date + '\'' +
                ", mac_addr='" + mac_addr + '\'' +
                ", dev_id='" + dev_id + '\'' +
                ", dev_name='" + dev_name + '\'' +
                ", prod_category='" + prod_category + '\'' +
                ", prod_subcate='" + prod_subcate + '\'' +
                ", dial_ability='" + dial_ability + '\'' +
                ", battery_main=" + battery_main +
                ", lang=" + lang +
                ", cw=" + cw +
                ", ch=" + ch +
                '}';
    }
}
