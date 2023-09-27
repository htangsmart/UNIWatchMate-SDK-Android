#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <fstream>

#include "up_parser.h"
#include "../utils/JArrayList.h"
#include "uparser-lib.h"
#include "dial_parser.h"

#define TAG    "UPARSER_JNI" // 这个是自定义的LOG的标识
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__) // 定义LOGD类型
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__) // 定义LOGI类型
#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,TAG,__VA_ARGS__) // 定义LOGW类型
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__) // 定义LOGE类型

#define FREE_JNI_VARIABLE
static jbyteArray copyDataToByte(JNIEnv *env, const uint8_t *p_data, int data_len) {
    jbyteArray jarr = env->NewByteArray(data_len);
    jbyte *arr = env->GetByteArrayElements(jarr, NULL);

    memcpy(arr, p_data, data_len);
    env->ReleaseByteArrayElements(jarr, arr, 0);

    return jarr;
}


extern "C" JNIEXPORT jlong JNICALL Java_com_sjbt_sdk_uparser_UparserJni_upInit
        (JNIEnv *env, jobject thiz, jstring upFilePath) {
    const char *file_path = env->GetStringUTFChars(upFilePath, 0);
    uint64_t p_context = reinterpret_cast<uint64_t>(up_init(file_path));

    return p_context;
}

extern "C" JNIEXPORT jlong JNICALL Java_com_sjbt_sdk_uparser_UparserJni_upInitByBytes
        (JNIEnv *env, jobject thiz, jbyteArray upBytes) {
    return -1;
}

extern "C" JNIEXPORT void JNICALL Java_com_sjbt_sdk_uparser_UparserJni_upDeinit
        (JNIEnv *env, jobject thiz, jlong upContext) {
    if (upContext == 0) return;

    UP_CONTEXT *p_context = reinterpret_cast<UP_CONTEXT *>(upContext);
    up_deinit(p_context);
}

extern "C" JNIEXPORT jobject JNICALL Java_com_sjbt_sdk_uparser_UparserJni_upPartitionGet
        (JNIEnv *env, jobject thiz, jlong upContext, jstring tag, jboolean needData) {
    uint32_t tmp_length = 0;
    uint32_t tmp_offset = 0;
    if (upContext==0)
        return NULL;

    UP_CONTEXT* p_context = reinterpret_cast<UP_CONTEXT *>(upContext);
    const char* p_tag = env->GetStringUTFChars(tag, 0);

    jclass myClass = env->FindClass("com/sjbt/sdk/uparser/model/UpPartitionInfo");
    // 获取类的构造函数，记住这里是调用无参的构造函数
    jmethodID id = env->GetMethodID(myClass, "<init>", "()V");
    // 创建一个新的对象
    jobject partitionInfoObj = env->NewObject(myClass, id);
    uint32_t index = 0;
    tmp_offset = up_partition_get(p_context, p_tag, &tmp_length, &index, 0);

    jfieldID j_tag = env->GetFieldID(myClass, "tag", "Ljava/lang/String;");
    jfieldID j_part_len = env->GetFieldID(myClass, "partitionLength", "I");
    jfieldID j_offset = env->GetFieldID(myClass, "offsetInFlash", "I");
    jfieldID j_data = env->GetFieldID(myClass, "partitionData", "[B");

    env->SetObjectField(partitionInfoObj, j_tag, env->NewStringUTF(p_tag));
    env->SetIntField(partitionInfoObj, j_part_len, (jint)tmp_length);
    env->SetIntField(partitionInfoObj, j_offset, (jint)tmp_offset);
    if (needData) {
        const uint8_t *p_data = p_context->flash_bytes + tmp_offset;
        jbyteArray jarr_data = copyDataToByte(env, p_data, tmp_length);
        env->SetObjectField(partitionInfoObj, j_data, jarr_data);
    }

#ifdef FREE_JNI_VARIABLE
    env->DeleteLocalRef(myClass);
#endif

    return partitionInfoObj;
}

extern "C" JNIEXPORT jbyteArray JNICALL Java_com_sjbt_sdk_uparser_UparserJni_upPartitionGetData
        (JNIEnv *env, jobject thiz, jlong upContext, jstring tag) {
    uint32_t tmp_length = 0;
    uint32_t tmp_offset = 0;
    UP_CONTEXT* p_context = reinterpret_cast<UP_CONTEXT *>(upContext);
    const char* p_tag = env->GetStringUTFChars(tag, 0);

    if (upContext==0) return NULL;

    uint32_t index;
    tmp_offset = up_partition_get(p_context, p_tag, &tmp_length, &index, 0);
    const uint8_t* p_data = p_context->flash_bytes + tmp_offset;

    return copyDataToByte(env, p_data, tmp_length);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_sjbt_sdk_uparser_UparserJni_upPartitionKeep(JNIEnv *env, jobject thiz, jlong upContext,
                                                   jobject partionInfoObj) {
    // TODO: implement upPartitionKeep()
    UP_CONTEXT* p_context = reinterpret_cast<UP_CONTEXT *>(upContext);
    if (upContext==0) return -1;

    LOGE("upPartitionKeep 1");

    jclass myClass = env->FindClass("com/sjbt/sdk/uparser/model/UpPartitionInfo");

    jfieldID j_tag = env->GetFieldID(myClass, "tag", "Ljava/lang/String;");
    jfieldID j_part_len = env->GetFieldID(myClass, "partitionLength", "I");
    jfieldID j_offset = env->GetFieldID(myClass, "offsetInFlash", "I");
    jfieldID j_data = env->GetFieldID(myClass, "partitionData", "[B");

    jstring tag = static_cast<jstring>(env->GetObjectField(partionInfoObj, j_tag));
    jint data_len = env->GetIntField(partionInfoObj, j_part_len);
    jbyteArray data_arr = static_cast<jbyteArray>(env->GetObjectField(partionInfoObj, j_data));
    uint8_t* data = reinterpret_cast<uint8_t *>(env->GetByteArrayElements(data_arr, NULL));

    const char* p_tag = env->GetStringUTFChars(tag, 0);
    LOGE("upPartitionKeep 2");
    int ret = up_partition_keep(p_context, p_tag, data);
    LOGE("upPartitionKeep 3");

    env->DeleteLocalRef(myClass);
    env->ReleaseByteArrayElements(data_arr, reinterpret_cast<jbyte *>(data), 0);
    env->DeleteLocalRef(data_arr);
    LOGE("upPartitionKeep 4");

    return ret;
}

extern "C" JNIEXPORT jint JNICALL Java_com_sjbt_sdk_uparser_UparserJni_upPartitionSet
        (JNIEnv *env, jobject thiz, jlong upContext, jobject partionInfoObj) {
    UP_CONTEXT* p_context = reinterpret_cast<UP_CONTEXT *>(upContext);
    if (upContext==0) return -1;

    LOGE("upPartitionSet 1");

    jclass myClass = env->FindClass("com/sjbt/sdk/uparser/model/UpPartitionInfo");

    jfieldID j_tag = env->GetFieldID(myClass, "tag", "Ljava/lang/String;");
    jfieldID j_part_len = env->GetFieldID(myClass, "partitionLength", "I");
    jfieldID j_offset = env->GetFieldID(myClass, "offsetInFlash", "I");
    jfieldID j_data = env->GetFieldID(myClass, "partitionData", "[B");

    jstring tag = static_cast<jstring>(env->GetObjectField(partionInfoObj, j_tag));
    jint data_len = env->GetIntField(partionInfoObj, j_part_len);
    jbyteArray data_arr = static_cast<jbyteArray>(env->GetObjectField(partionInfoObj, j_data));
    uint8_t* data = reinterpret_cast<uint8_t *>(env->GetByteArrayElements(data_arr, NULL));

    const char* p_tag = env->GetStringUTFChars(tag, 0);
    LOGE("upPartitionSet 2");
    int ret = up_partition_set(p_context, p_tag, data, data_len);
    LOGE("upPartitionSet 3");

    env->DeleteLocalRef(myClass);
    env->ReleaseByteArrayElements(data_arr, reinterpret_cast<jbyte *>(data), 0);
    env->DeleteLocalRef(data_arr);
    LOGE("upPartitionSet 4");

    return ret;
}

extern "C" JNIEXPORT jint JNICALL Java_com_sjbt_sdk_uparser_UparserJni_upSave
        (JNIEnv *env, jobject thiz, jlong upContext, jstring filePath) {

    if (upContext==0) return -1;

    UP_CONTEXT* p_context = reinterpret_cast<UP_CONTEXT *>(upContext);
    const char* up_file_path = env->GetStringUTFChars(filePath, 0);

    return up_save(p_context, up_file_path);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_sjbt_sdk_uparser_UparserJni_upGetAllBytes(JNIEnv *env, jobject thiz, jlong up_context) {
    // TODO: implement upGetAllBytes()
    return NULL;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_sjbt_sdk_uparser_UparserJni_peekJpgFromDial(JNIEnv *env, jobject thiz,
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