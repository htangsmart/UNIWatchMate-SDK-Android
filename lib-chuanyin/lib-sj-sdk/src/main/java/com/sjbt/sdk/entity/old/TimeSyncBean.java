package com.sjbt.sdk.entity.old;

import android.os.Parcel;
import android.os.Parcelable;

public class TimeSyncBean implements Parcelable {

    /**
     * timestamp : 1655350821
     * timeZoo : UTC+8
     * currDate : 20220616
     * currTime : 11:50:30
     * timeformat : 0
     */

    private long timestamp;
    private String timeZoo;
    private String currDate;
    private String currTime;
    private int timeformat;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimeZoo() {
        return timeZoo;
    }

    public void setTimeZoo(String timeZoo) {
        this.timeZoo = timeZoo;
    }

    public String getCurrDate() {
        return currDate;
    }

    public void setCurrDate(String currDate) {
        this.currDate = currDate;
    }

    public String getCurrTime() {
        return currTime;
    }

    public void setCurrTime(String currTime) {
        this.currTime = currTime;
    }

    public int getTimeformat() {
        return timeformat;
    }

    public void setTimeformat(int timeformat) {
        this.timeformat = timeformat;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.timestamp);
        dest.writeString(this.timeZoo);
        dest.writeString(this.currDate);
        dest.writeString(this.currTime);
        dest.writeInt(this.timeformat);
    }

    public void readFromParcel(Parcel source) {
        this.timestamp = source.readLong();
        this.timeZoo = source.readString();
        this.currDate = source.readString();
        this.currTime = source.readString();
        this.timeformat = source.readInt();
    }

    public TimeSyncBean() {
    }

    protected TimeSyncBean(Parcel in) {
        this.timestamp = in.readLong();
        this.timeZoo = in.readString();
        this.currDate = in.readString();
        this.currTime = in.readString();
        this.timeformat = in.readInt();
    }

    public static final Creator<TimeSyncBean> CREATOR = new Creator<TimeSyncBean>() {
        @Override
        public TimeSyncBean createFromParcel(Parcel source) {
            return new TimeSyncBean(source);
        }

        @Override
        public TimeSyncBean[] newArray(int size) {
            return new TimeSyncBean[size];
        }
    };

    @Override
    public String toString() {
        return "TimeSyncBean{" +
                "timestamp=" + timestamp +
                ", timeZoo='" + timeZoo + '\'' +
                ", currDate='" + currDate + '\'' +
                ", currTime='" + currTime + '\'' +
                ", timeformat=" + timeformat +
                '}';
    }
}
