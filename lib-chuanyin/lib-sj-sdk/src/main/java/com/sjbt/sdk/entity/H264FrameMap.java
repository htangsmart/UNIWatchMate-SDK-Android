package com.sjbt.sdk.entity;

import android.os.Build;

import com.base.sdk.entity.apps.WmCameraFrameInfo;

import java.util.LinkedHashMap;

public class H264FrameMap extends LinkedHashMap<Long, WmCameraFrameInfo> {
    private static final int MAX_SIZE = 100;
    private static final long serialVersionUID = 1L;
    private int maxSize = MAX_SIZE;

    public H264FrameMap() {
        super(MAX_SIZE, 0.75F, true);
    }

    public H264FrameMap(int maxSize) {
        super(maxSize, 0.75F, true);
        this.maxSize = maxSize;
    }

    protected boolean removeEldestEntry(Entry<Long, WmCameraFrameInfo> eldest) {
        synchronized (this) {
            return this.size() > this.maxSize;
        }
    }

    public WmCameraFrameInfo getFrame(long frameId) {
        synchronized (this) {
            return (WmCameraFrameInfo) this.get(frameId);
        }
    }

    public void putFrame(WmCameraFrameInfo frameInfo) {
        synchronized (this) {
            this.put(frameInfo.getFrameId(), frameInfo);
        }

    }

    public void removeOldFrames(long frameId) {
        synchronized (this) {
            // 删除小于frameId的帧
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.entrySet().removeIf(entry -> entry.getKey() < frameId);
            } else {
                for (Entry<Long, WmCameraFrameInfo> entry : this.entrySet()) {
                    if (entry.getKey() < frameId) {
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
