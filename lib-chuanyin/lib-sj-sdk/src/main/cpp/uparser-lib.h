//
// Created by wangwenfeng on 2023/3/29.
//

#ifndef FUN_UPARSER_LIB_H
#define FUN_UPARSER_LIB_H

#include <jni.h>
#include <string>
#include <vector>

inline std::string jstring_to_cpp_string(JNIEnv *env, jstring jstr) {
    // In java, a unicode char will be encoded using 2 bytes (utf16).
    // so jstring will contain characters utf16. std::string in c++ is
    // essentially a string of bytes, not characters, so if we want to
    // pass jstring from JNI to c++, we have convert utf16 to bytes.
    if (!jstr) {
        return "";
    }
    const jclass stringClass = env->GetObjectClass(jstr);
    const jmethodID getBytes =
            env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    const jbyteArray stringJbytes = (jbyteArray)env->CallObjectMethod(
            jstr, getBytes, env->NewStringUTF("UTF-8"));

    size_t length = (size_t)env->GetArrayLength(stringJbytes);
    jbyte *pBytes = env->GetByteArrayElements(stringJbytes, NULL);

    std::string ret = std::string(reinterpret_cast<char *>(pBytes), length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

inline jbyteArray cpp_array_to_jbytearray(JNIEnv *env, const int8_t *buf,
                                          int64_t len) {
    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, buf);
    return result;
}

inline jlongArray int64_vector_to_jlongarray(JNIEnv *env,
                                             const std::vector<int64_t> &vec) {
    jlongArray result = env->NewLongArray(vec.size());
    jlong *buf = new jlong[vec.size()];
    for (size_t i = 0; i < vec.size(); ++i) {
        buf[i] = (jlong)vec[i];
    }
    env->SetLongArrayRegion(result, 0, vec.size(), buf);
    delete[] buf;
    return result;
}

inline jbyteArray uint8_vector_to_jbytearray(JNIEnv *env,
                                             const std::vector<uint8_t> &vec) {
    jbyteArray result = env->NewByteArray(vec.size());
    jbyte *buf = new jbyte[vec.size()];
    for (size_t i = 0; i < vec.size(); ++i) {
        buf[i] = (jbyte)vec[i];
    }
    env->SetByteArrayRegion(result, 0, vec.size(), buf);
    delete[] buf;
    return result;
}

inline jfloatArray float_vector_to_jfloatarray(JNIEnv *env,
                                             const std::vector<float> &vec) {
    jfloatArray result = env->NewFloatArray(vec.size());
    jfloat *buf = new jfloat[vec.size()];
    for (size_t i = 0; i < vec.size(); ++i) {
        buf[i] = (jfloat)vec[i];
    }
    env->SetFloatArrayRegion(result, 0, vec.size(), buf);
    delete[] buf;
    return result;
}

#endif //FUN_UPARSER_LIB_H
