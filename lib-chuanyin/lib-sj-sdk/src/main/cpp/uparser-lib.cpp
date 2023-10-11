#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <fstream>

#include "up_parser.h"
#include "utils/JArrayList.h"
#include "uparser-lib.h"
#include "dial_parser.h"

#define TAG    "UPARSER_JNI" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,TAG,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__) // 定义LOGE类型

extern "C" JNIEXPORT jint JNICALL Java_com_sjbt_sdk_uparser_UparserJni_peekJpgFromDial(JNIEnv *env, jobject thiz,
                                                                            jstring jstrDailPath, jobject jpgInfoObj) {
    jclass jpg_info_cls = env->FindClass("com/sjbt/sdk/uparser/model/JpgInfo");
    jfieldID jfid_jpgdata = env->GetFieldID(jpg_info_cls, "jpgdata", "[B");
    jfieldID jfid_resourceinfo = env->GetFieldID(jpg_info_cls, "resouceInfo", "[B");

    std::string dialFilePath = jstring_to_cpp_string(env, jstrDailPath);
    dial_thumbnail_info_t dial_thumbnail_info;
    std::vector<uint8_t> jpeg_data;

    int ret = peek_jpg_data(dialFilePath, dial_thumbnail_info, jpeg_data);

    if (ret == 0) {
        env->SetObjectField(jpgInfoObj, jfid_jpgdata, uint8_vector_to_jbytearray(env, jpeg_data));
        env->SetObjectField(jpgInfoObj,
                            jfid_resourceinfo, cpp_array_to_jbytearray(env,reinterpret_cast<const int8_t *>(&(dial_thumbnail_info.thumbnails[0].thumbnail_info)),
                                                                       (int64_t)sizeof(dial_thumbnail_info.thumbnails[0].thumbnail_info)));
    } else {
        LOGE("Cannot open dial file or parse error!!! ret=%d", ret);
    }
    env->DeleteLocalRef(jpg_info_cls);
    return ret;

}