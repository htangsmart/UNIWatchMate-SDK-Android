package com.sjbt.sdk.utils;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.permissionx.guolindev.PermissionX;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteOrder;

public class BtUtils {

    static {
        System.loadLibrary("btsdk-lib");
    }

    /**
     * 字符串转换为16进制字符串
     *
     * @param s
     * @return
     */
    public static String stringToHexString(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    public static short byte2short(byte[] b) {
        short l = 0;
        for (int i = 0; i < 2; i++) {
            l <<= 8; //<<=和我们的 +=是一样的，意思就是 l = l << 8
            l |= (b[i] & 0xff); //和上面也是一样的  l = l | (b[i]&0xff)
        }
        return l;
    }

    public static String intToHex(int n) {

        String num = Integer.toHexString(n);

        if (num.length() == 1 && num.matches("[0-9A-Za-z]+")) {
            num = "0" + num;
        }

        return num;
    }

    public static int hexToInt(String s) {
        int i = 0, l = s.length(), n = 0;
        while (i < l) {
            int x = s.codePointAt(i);
            n = n << 4 | (x > '9' ? x - ('A' - 10) : x - '0');
            i += 1;
        }
        return n;
    }

    /**
     * 16进制字符串转换为字符串
     *
     * @param s
     * @return
     */
    public static String hexStringToString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        s = s.replace(" ", "");
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "utf-8");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    /**
     * 16进制表示的字符串转换为字节数组
     *
     * @param s 16进制表示的字符串
     * @return byte[] 字节数组
     */
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] b = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            b[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return b;
    }

    /**
     * byte数组转16进制字符串
     *
     * @param bArray
     * @return
     */
    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp);
        }
        return sb.toString().toUpperCase();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public static native byte[] encryptData(int key, byte[] data, int length);

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public static native char crc8Maxim(byte[] data, int length);

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public static native int getCrc(int val, byte[] data, int length);

    /**
     * 将字符串数据补为n字节长度,并以小端的顺序显示
     *
     * @param n   目标字节长度
     * @param src
     * @return
     */
    public static String getSplitReverseHexStringCmd(String src, int n) {

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < (n * 2 - src.length()); i++) {
            sb.append("0");
        }

        sb.append(src);

        String[] reverseArray = new String[sb.length() / 2];

        for (int i = 0; i < reverseArray.length; i++) {
            reverseArray[i] = sb.substring(2 * i, 2 * i + 2);
        }

        reverseArray = ArrayUtils.reverse(reverseArray);

        StringBuilder stringBuilder = new StringBuilder();
        for (String str : reverseArray) {
            stringBuilder.append(str);
        }

        return stringBuilder.toString();
    }

    /**
     * 计算出key2
     *
     * @param data
     */
    public static String getTheAccumulatedValueAnd(String data) {
        if (TextUtils.isEmpty(data)) {
            return "";
        }
        int total = 0;
        int len = data.length();
        int num = 0;
        while (num < len) {
            String s = data.substring(num, num + 2);
            total += Integer.parseInt(s, 16);
            num = num + 2;
        }
        /**
         * 用256求余最大是255，即16进制的FF
         */
        String hex = Integer.toHexString(total);
        return hex;
    }

    /**
     * 解密并验证数据
     *
     * @param data
     * @param key2
     * @param oldData
     * @return
     */
    public static boolean verificationCmd(String data, String key2, String oldData, String old2Data) {
        String substring = data.substring(32, 96);
        //        Log.i("TAG", "verificationCmd: 30-3F： " + oldData);
        //        Log.i("TAG", "verificationCmd: 10-2F： " + old2Data);
        //        Log.i("TAG", "verificationCmd: new 10-2F： " + substring);
        byte[] bytes = hexStringToByteArray(substring);
        int i = Integer.parseInt(key2, 16);
        //解密数据
        String s2 = bytesToHexString(encryptData(i, bytes, bytes.length));
        //        Log.i("TAG", "verificationCmd:解密后的10-2F： " + s2);
        //异或
        String substring1 = s2.substring(0, 32);
        String substring2 = s2.substring(32);
        byte[] bytes1 = hexStringToByteArray(substring1);
        byte[] bytes2 = hexStringToByteArray(substring2);
        byte[] bytes3 = hexStringToByteArray(oldData);
        byte[] bytes4 = new byte[bytes1.length + bytes2.length];
        for (int j = 0; j < bytes1.length; j++) {
            bytes4[j] = (byte) (bytes1[j] ^ bytes3[j]);
        }
        for (int j = 0; j < bytes2.length; j++) {
            bytes4[j + bytes2.length] = (byte) (bytes2[j] ^ bytes3[j]);
        }
        //        String s3 = bytesToHexString(encryptData(i, bytes4, bytes4.length));
        //        Log.i("TAG", "verificationCmd:异或后的数据： " + bytesToHexString(bytes4));
        return bytesToHexString(bytes4).equalsIgnoreCase(old2Data);
    }

    /**
     * 蓝牙是否打开
     *
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean isBlueEnable(Context context) {
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void enableBt(Context context) {
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothAdapter.enable();
    }

    /**
     * 获取手机电量
     *
     * @return
     */
    public static int getPhoneBattery(Context context) {
        Intent batteryInfoIntent = context.getApplicationContext()
                .registerReceiver(null,
                        new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        return batteryInfoIntent.getIntExtra("level", 0);//电量（0-100）;
    }

    /**
     * 四字节 小端 byte[]转为 int ，直接转会出现负数
     *
     * @param bytes
     * @return
     */
    public static long fourBytes2Long(byte[] bytes, ByteOrder byteOrder) {

        return ByteOrder.LITTLE_ENDIAN == byteOrder ? (bytes[0] & 255 | (bytes[1] & 255) << 8 | (bytes[2] & 255) << 16 | (bytes[3] & 255) << 24) & 0xFFFFFFFFL : (bytes[3] & 255 | (bytes[2] & 255) << 8 | (bytes[1] & 255) << 16 | (bytes[0] & 255) << 24) & 0xFFFFFFFFL;
    }

    public static String getBtMac(Context context) {
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = mBluetoothManager.getAdapter();
        Class<? extends BluetoothAdapter> btAdapterClass = adapter.getClass();
        try {
            Class<?> btClass = Class.forName("android.bluetooth.IBluetooth");
            Field bluetooth = btAdapterClass.getDeclaredField("mService");
            bluetooth.setAccessible(true);
            Method btAddress = btClass.getMethod("getAddress");
            btAddress.setAccessible(true);
            return (String) btAddress.invoke(bluetooth.get(adapter));
        } catch (Exception e) {
            if (PermissionX.isGranted(context, Manifest.permission.BLUETOOTH_CONNECT)) {
                return "";
            }
            return adapter.getAddress();
        }
    }

    public static boolean isValidMacAddress(String input) {
        String pattern = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
        return input.matches(pattern);
    }

}
