package com.sjbt.sdk.spp.bt;

import static com.sjbt.sdk.spp.cmd.CmdConfigKt.BT_MSG_BASE_LEN;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_8014;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.CMD_ID_8015;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.HEAD_COMMON;
import static com.sjbt.sdk.spp.cmd.CmdConfigKt.HEAD_DEVICE_ERROR;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.sjbt.sdk.SJUniWatch;
import com.sjbt.sdk.entity.MsgBean;
import com.sjbt.sdk.log.SJLog;
import com.sjbt.sdk.spp.cmd.CmdHelper;
import com.sjbt.sdk.utils.BtUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 客户端和服务端的基类，用于管理socket长连接
 */
@SuppressWarnings("MissingPermission")
public class BtEngine {

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static String TAG = "BtEngine";
    public static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static final int FLAG_MSG = 0;  //消息标记
    private static final int FLAG_FILE = 1; //文件标记

    private static BluetoothSocket mSocket;
    private static DataOutputStream mOut;
    private static Listener mListener;
    private static boolean isRunning;
    private static boolean isSending;
    private static boolean deviceBusing;
    private static Lock lock = new ReentrantLock();// 锁对象

    public static final int TRANSFER_END_TIMEOUT = 15000;
    private static int DEFAULT_MSG_TIMEOUT = 10 * 1000;
    private static int MIN_MSG_TIMEOUT = 5 * 1000;
    private static HashMap<String, Runnable> msgQueueMap = new HashMap<>();
    private static Handler mHandler = new Handler(Looper.myLooper());
    private static Handler mUIHandler = new Handler(Looper.getMainLooper());

    protected static BluetoothDevice mDevice;

    protected static final int TYPE_MSG = 0x11;
    protected static final int TYPE_CONNECT = 0x12;
    protected static MyHandlerThread myHandlerThread = new MyHandlerThread("bt_thread");

    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    private static final Map<String, Integer> mStateMap = new HashMap<>();

    public void putStateMap(String mac, int state) {
        mStateMap.put(mac, state);
    }

    public void clearStateMap() {
        mStateMap.clear();
    }

    public int getDeviceConnectState(String mac) {
        if (mStateMap.get(mac) == null) {
            return 0;
        }

        return mStateMap.get(mac);
    }

    public static void setDefaultMsgTimeout(int defaultMsgTimeout) {
        if (defaultMsgTimeout < MIN_MSG_TIMEOUT) {
            defaultMsgTimeout = MIN_MSG_TIMEOUT;
        }

        DEFAULT_MSG_TIMEOUT = defaultMsgTimeout;
    }

    private static SJUniWatch mSjUniWatch;

    private BtEngine() {
    }

    public BtEngine(SJUniWatch sjUniWatch) {
        mSjUniWatch = sjUniWatch;
        logD("BtEngine() 构建");
        myHandlerThread.startThread();
    }

    public Listener getListener() {
        return mListener;
    }

    private static void logD(String msg) {
        ((SJLog) mSjUniWatch.getWmLog()).logD(TAG, msg);
    }

    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    private static void socketConnectRead() throws IOException {

        if (!mSocket.isConnected()) {
            logD("开始连接-->:" + mDevice.getAddress());

            mStateMap.put(mDevice.getAddress(), STATE_CONNECTING);
            mSocket.connect();
            deviceBusing = false;
        }

        if (mSocket.isConnected()) {
            mStateMap.put(mDevice.getAddress(), STATE_CONNECTED);
            BluetoothDevice device = mSocket.getRemoteDevice();
            clearMessageQueue();
            logD("连接成功:" + device.getAddress());

            notifyUI(Listener.CONNECTED, device);

            EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        mOut = new DataOutputStream(mSocket.getOutputStream());
                        InputStream inputStream = mSocket.getInputStream();
                        lock.lock();
                        isRunning = true;
                        lock.unlock();

                        byte[] result = new byte[0];

                        while (isRunning) {
                            byte[] buffer = new byte[256];
                            // 等待有数据
                            while (inputStream.available() == 0 && isRunning) {
                                if (System.currentTimeMillis() < 0)
                                    break;
                            }
                            while (isRunning) {//循环读取
                                try {
                                    int num = inputStream.read(buffer);
//                            mSjUniWatch.getWmLog().logD(TAG,"容许最大长度Transmit:" + socket.getMaxTransmitPacketSize());
                                    byte[] temp = new byte[result.length + num];
                                    System.arraycopy(result, 0, temp, 0, result.length);
                                    System.arraycopy(buffer, 0, temp, result.length, num);
                                    result = temp;
                                    if (inputStream.available() == 0)
                                        break;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    notifyErrorOnUI("1-" + e.getMessage());
                                    break;
                                }
                            }

                            try {

                                mSjUniWatch.getWmLog().logD(TAG,"receiveMsg:"+byte2Hex(result));
                                if (result.length == 0) {
                                    return;
                                }

//                        mSjUniWatch.getWmLog().logD(TAG,"BIU APP 正常 消息队列：" + msgQueue.keySet());
                                parseMsg(result);
                                // 清空
                                result = new byte[0];
                            } catch (Exception e) {
                                e.printStackTrace();
                                notifyErrorOnUI("2-" + e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        notifyUI(Listener.ON_SOCKET_CLOSE, mDevice);
                    }
                }
            });
        }
    }

    public BluetoothSocket getmSocket() {
        return mSocket;
    }

    public static class MyHandlerThread extends HandlerThread {
        Handler mBzyHandler;

        public MyHandlerThread(String name) {
            super(name);
        }

        public void startThread() {
            logD("startThread:启动线程");
            start();
            mBzyHandler = new Handler(getLooper()) {
                @Override
                public void handleMessage(Message msg) {

//                    mSjUniWatch.getWmLog().logD(TAG,"handleMessage:" + msg.what);
                    // 在这里处理消息
                    switch (msg.what) {
                        case TYPE_MSG:
                            sendMsg((byte[]) msg.obj);
                            break;

                        case TYPE_CONNECT:
                            if (mDevice != null) {
                                logD("创建BluetoothSocket：" + mDevice.getAddress());
                                try {
                                    mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                                    logD("开启子线程读取" + mDevice.getAddress());
                                    socketConnectRead();
                                } catch (IOException e) {
//                                    closeSocket("loopRead异常 " + e, true);
                                    e.printStackTrace();
                                    notifyErrorOnUI("3-" + e.getMessage());
                                }
                            } else {
                                logD("设备异常Device=null");
                            }
                            break;
                    }
                }
            };
        }

        public void sendMessage(int what, Object obj) {
//            mSjUniWatch.getWmLog().logD(TAG,"sendMessage:" + what);
            if (mBzyHandler != null) {
                Message message = mBzyHandler.obtainMessage();
                message.what = what;
                message.obj = obj;
                boolean success = mBzyHandler.sendMessage(message);
//                mSjUniWatch.getWmLog().logD(TAG,"sendMessage结果:" + success);
                if (!success) {
                    startThread();
                }
            }
        }
    }

    /**
     * 与远端设备建立长连接
     *
     * @param dev 远端设备
     */
    public void connect(BluetoothDevice dev) {
        try {
            closeSocket("BtClient connect ：" + dev.getAddress(), false);

            mDevice = dev;
            sendHandleMessage(TYPE_CONNECT, dev);
        } catch (Throwable e) {
            closeSocket("BtClient Exception ", true);
            e.printStackTrace();
            notifyErrorOnUI(e.getMessage());
        }
    }

    private void sendHandleMessage(int what, Object obj) {
        myHandlerThread.sendMessage(what, obj);
    }

    /**
     * 发送短消息
     */
    public void sendMsgOnWorkThread(byte[] bytes) {
        if (deviceBusing) {
            MsgBean msgBean = CmdHelper.getPayLoadJson(false, bytes);
            notifyUI(Listener.BUSY, msgBean);
            return;
        }

        sendHandleMessage(TYPE_MSG, bytes);
    }

    public static void sendMsg(byte[] bytes) {
        lock.lock();
        isSending = true;
        lock.unlock();
        try {
            MsgBean msgBean = CmdHelper.getPayLoadJson(false, bytes);

            if (!msgBean.isNotTimeOut()) {
                msgQueueMap.put(msgBean.getTimeOutCode(), new Runnable() {
                    @Override
                    public void run() {
                        logD("message timeout：" + msgBean.getTimeOutCode());

                        notifyUI(Listener.TIME_OUT, msgBean);

                        mHandler.removeCallbacks(msgQueueMap.get(msgBean.getTimeOutCode()));
                        msgQueueMap.remove(msgBean.getTimeOutCode());
                    }
                });

                mHandler.postDelayed(msgQueueMap.get(msgBean.getTimeOutCode()), DEFAULT_MSG_TIMEOUT);
            }

            mSocket.getOutputStream().write(bytes);
            mSocket.getOutputStream().flush();
            logD("sended Msg：" + BtUtils.bytesToHexString(bytes));

        } catch (Throwable e) {
//            closeSocket("发送过程 " + e.getMessage(), true);
            e.printStackTrace();
            notifyErrorOnUI("4-" + e.getMessage());
        }
        lock.lock();
        isSending = false;
        lock.unlock();
    }

    /**
     * BTSocket Spp连接状态
     *
     * @return
     */
    public boolean isSocketConnected() {

        try {
            if (mSocket != null) {
                return mSocket.isConnected();
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static Runnable busyRun = new Runnable() {
        @Override
        public void run() {
            deviceBusing = false;
        }
    };

    /**
     * 发送短消息
     */
    public void sendStringMsg(String msg) {
        if (checkSend()) return;
        isSending = true;
        try {
            mOut.writeInt(FLAG_MSG); //消息标记
            mOut.writeUTF(msg);
            mOut.flush();
        } catch (Throwable e) {
            closeSocket("发送文本 ", true);
        }
        isSending = false;
    }

    /**
     * 发送文件
     */
    public void sendFile(final String filePath) {
        if (checkSend()) return;
        isSending = true;
        EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream in = new FileInputStream(filePath);
                    File file = new File(filePath);
                    mOut.writeInt(FLAG_FILE); //文件标记
                    mOut.writeUTF(file.getName()); //文件名
                    mOut.writeLong(file.length()); //文件长度
                    int r;
                    byte[] b = new byte[4 * 1024];
                    while ((r = in.read(b)) != -1)
                        mOut.write(b, 0, r);
                    mOut.flush();
                } catch (Throwable e) {
                    closeSocket("发送文件过程 ", true);
                }
                isSending = false;
            }
        });
    }

    /**
     * 释放监听引用(例如释放对Activity引用，避免内存泄漏)
     */
    public void unListener() {
        mListener = null;
    }

    /**
     * 设置监听功能
     *
     * @param mListener
     */
    public void setListener(Listener mListener) {
        this.mListener = mListener;
    }

    /**
     * 关闭Socket连接
     */
    public void closeSocket(String name, boolean isNotify) {
        try {
            mStateMap.clear();

            deviceBusing = false;
            lock.lock();
            isRunning = false;
            lock.unlock();

            if (mSocket != null && mSocket.isConnected()) {
                mSocket.close();
                logD(name + " 关闭Socket");

                if (isNotify) {
                    notifyUI(Listener.ON_SOCKET_CLOSE, null);
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // ============================================通知UI===========================================================
    private boolean checkSend() {
        if (isSending) {
//            BaseApplication.getInstance().toast("正在发送其它数据,请稍后再发...");
            return true;
        }
        return false;
    }

    private static void notifyUI(final int state, final Object obj) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mListener != null) {
                        if (state == Listener.MSG) {

                            MsgBean msgBean = (MsgBean) obj;

                            if (msgBean.head == HEAD_DEVICE_ERROR) {
                                mListener.socketNotifyError(msgBean);
                            } else {
                                try {
                                    if (msgBean.cmdId == CMD_ID_8015 && msgBean.head == HEAD_COMMON) {

                                        deviceBusing = msgBean.payload[16] == 1;

                                        logD("msg busy：" + deviceBusing);

                                        if (deviceBusing) {
                                            mHandler.postDelayed(busyRun, 15000);
                                        } else {
                                            mHandler.removeCallbacks(busyRun);
                                        }

                                    } else {
                                        mListener.socketNotify(state, msgBean);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                        } else {
                            mListener.socketNotify(state, obj);
                        }
                    } else {
                        logD("Listener is null，cannot dispatch");
                    }

                } catch (Throwable e) {
                    logD("333." + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isDeviceBusy() {
        return deviceBusing;
    }

    private static void parseMsg(byte[] msg) {
        byte[] lenArray = new byte[4];
        System.arraycopy(msg, 4, lenArray, 0, 4);
        int payloadLen = ((lenArray[2]) & 0XFF) | ((lenArray[3] & 0XFF) << 8);

        if (payloadLen == msg.length - BT_MSG_BASE_LEN) {
            MsgBean msgBean = CmdHelper.getPayLoadJson(true, msg);

            String msgTimeCode = msgBean.getTimeOutCode();

//                        mSjUniWatch.getWmLog().logD(TAG,"返回MSGCode：" + msgTimeCode);
            Runnable runnable = msgQueueMap.get(msgTimeCode);

            if (runnable != null) {
                mHandler.removeCallbacks(runnable);
                msgQueueMap.remove(msgTimeCode);
            }

            notifyUI(Listener.MSG, msgBean);
        } else {
            int tempPosition = 0;
            while (tempPosition != msg.length) {

                byte[] tempLenArray = new byte[4];
                System.arraycopy(msg, tempPosition + 4, tempLenArray, 0, 4);
                payloadLen = ((tempLenArray[2]) & 0XFF) | ((tempLenArray[3] & 0XFF) << 8);

//                mSjUniWatch.getWmLog().logD(TAG,"payLoad2长度 hex:" + BtUtils.bytesToHexString(tempLenArray));
//                mSjUniWatch.getWmLog().logD(TAG,"payLoad2长度：" + payloadLen);

                byte[] singleMsg = new byte[payloadLen + BT_MSG_BASE_LEN];
                System.arraycopy(msg, tempPosition, singleMsg, 0, singleMsg.length);
                tempPosition = tempPosition + singleMsg.length;

//                mSjUniWatch.getWmLog().logD(TAG,"摘开消息：" + BtUtils.bytesToHexString(singleMsg));

                MsgBean msgBean = CmdHelper.getPayLoadJson(true, singleMsg);
                String msgTimeCode = msgBean.getTimeOutCode();

//                        mSjUniWatch.getWmLog().logD(TAG,"返回MSGCode：" + msgTimeCode);
                Runnable runnable = msgQueueMap.get(msgTimeCode);

                if (runnable != null) {
                    mHandler.removeCallbacks(runnable);
                    msgQueueMap.remove(msgTimeCode);
                }

                notifyUI(Listener.MSG, msgBean);

//                mSjUniWatch.getWmLog().logD(TAG,"tempPosition：" + tempPosition);
            }
        }
    }

    protected static void notifyErrorOnUI(String msg) {
        if (mDevice != null) {
            mStateMap.put(mDevice.getAddress(), 0);
            mUIHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mListener != null) {
                            mListener.onConnectFailed(mDevice, msg);
                        } else {
                            logD("BtEngine listener was destroyed");
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public interface Listener {
        int ON_SOCKET_CLOSE = 0;
        int CONNECTED = 1;
        int MSG = 2;
        int TIME_OUT = 3;
        int BUSY = 4;

        void socketNotify(int state, Object obj);

        void socketNotifyError(MsgBean msgBean);

        void onConnectFailed(BluetoothDevice device, String msg);
    }

    public void clearMsgQueue() {
        if (msgQueueMap.size() > 0) {
            for (String str : msgQueueMap.keySet()) {
                mHandler.removeCallbacks(msgQueueMap.get(str));
            }
            msgQueueMap.clear();
        }
    }

    private static void clearMessageQueue() {
        if (msgQueueMap.size() > 0) {
            for (String str : msgQueueMap.keySet()) {
                mHandler.removeCallbacks(msgQueueMap.get(str));
            }
            msgQueueMap.clear();
        }
    }

    /**
     * 字节数组转换为 16 进制字符串
     *
     * @param bytes 字节数组
     * @return Hex 字符串
     */
    private static String byte2Hex(byte[] bytes) {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String hash = formatter.toString();
        formatter.close();
        return hash;
    }

}
