package com.sjbt.sdk.entity;

import android.os.Build;

import com.base.sdk.entity.apps.WmCameraFrameInfo;

import java.util.LinkedHashMap;

public class PayloadMap extends LinkedHashMap<Short, PayloadPackage> {
    private static final int MAX_SIZE = 100;
    private static final long serialVersionUID = 1L;
    private int maxSize = MAX_SIZE;

    public PayloadMap() {
        super(MAX_SIZE, 0.75F, true);
    }

    public PayloadMap(int maxSize) {
        super(maxSize, 0.75F, true);
        this.maxSize = maxSize;
    }

    protected boolean removeEldestEntry(Entry<Short, PayloadPackage> eldest) {
        synchronized (this) {
            return this.size() > this.maxSize;
        }
    }

    public PayloadPackage getFrame(Short payLoadId) {
        synchronized (this) {
            return (PayloadPackage) this.get(payLoadId);
        }
    }

    public void putFrame(PayloadPackage payloadPackage) {
        synchronized (this) {
            this.put(payloadPackage.get_id(), payloadPackage);
        }

    }

    public void removeOldFrames(long payLoadId) {
        synchronized (this) {
            // 删除小于frameId的帧
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.entrySet().removeIf(entry -> entry.getKey() < payLoadId);
            } else {
                for (Entry<Short, PayloadPackage> entry : this.entrySet()) {
                    if (entry.getKey() < payLoadId) {
                        this.remove(entry.getKey());
                    }
                }
            }
        }
    }

    public int getFrameCount() {
        synchronized (this) {
            return this.size();
        }
    }
}
