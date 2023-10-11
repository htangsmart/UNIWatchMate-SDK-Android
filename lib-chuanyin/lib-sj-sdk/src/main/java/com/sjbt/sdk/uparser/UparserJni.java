package com.sjbt.sdk.uparser;

import com.sjbt.sdk.uparser.model.JpgInfo;

public class UparserJni {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("btsdk-lib");
    }

    public native int peekJpgFromDial(String dialFilePath, JpgInfo jpgInfo);

}
