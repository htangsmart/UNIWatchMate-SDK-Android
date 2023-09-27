#include <jni.h>
#include <string>
#include "LSFR.h"


extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sjbt_sdk_utils_BtUtils_encryptData(JNIEnv *env, jclass thiz, jint init_key,
                                      jbyteArray l_data, jint length) {
    uint8_t *olddata = (uint8_t *) env->GetByteArrayElements(l_data, 0);
    SmartLinkCore::CLFSR clfsr{};
    clfsr.Encrypt_Data(init_key, olddata, length);

    jbyteArray jarray = env->NewByteArray(length);

    //将byte数组转换为java String,并输出
    env->SetByteArrayRegion(jarray, 0, length, reinterpret_cast<const jbyte *>(olddata));
    return jarray;
}

extern "C"
JNIEXPORT jchar JNICALL
Java_com_sjbt_sdk_utils_BtUtils_crc8Maxim(JNIEnv *env, jclass thiz,
                                    jbyteArray l_data, jint length) {

    uint8_t *olddata = (uint8_t *) env->GetByteArrayElements(l_data, 0);
    SmartLinkCore::CLFSR clfsr{};
//    clfsr.crc8_maxim(olddata, length);

//    jbyteArray jarray = env->NewByteArray(length);
//
//    //将byte数组转换为java String,并输出
//    env->SetByteArrayRegion(jarray, 0, length, reinterpret_cast<const jbyte *>(olddata));
    return clfsr.crc8_maxim(olddata, length);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_sjbt_sdk_utils_BtUtils_getCrc(JNIEnv *env, jclass thiz, jint val,
                                 jbyteArray l_data, jint length) {

    uint8_t *olddata = (uint8_t *) env->GetByteArrayElements(l_data, 0);
    SmartLinkCore::CLFSR clfsr{};

    return  clfsr.get_crc(val,olddata, length);
}