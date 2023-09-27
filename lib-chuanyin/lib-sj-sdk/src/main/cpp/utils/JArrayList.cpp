//
// Created by 王文锋 on 2/25/22.
//

#include "JArrayList.h"

jclass JArrayList::m_list_jcls = nullptr;
jmethodID JArrayList::m_list_get = nullptr;
jmethodID JArrayList::m_list_size = nullptr;
jmethodID JArrayList::m_list_add = nullptr;

JArrayList::JArrayList(JNIEnv *env) {
    m_env = env;
    if (m_list_jcls == nullptr) {
        m_list_jcls = env->FindClass("java/util/ArrayList");
        m_list_get = env->GetMethodID(m_list_jcls, "get","(I)Ljava/lang/Object;");
        m_list_size = env->GetMethodID(m_list_jcls, "size", "()I");
        m_list_add = env->GetMethodID(m_list_jcls, "add", "(Ljava/lang/Object;)Z");
    }

}

JArrayList::~JArrayList() {

}

void JArrayList::add(jobject listObj, ...) {
    va_list args;
    va_start(args, m_list_add);
    m_env->CallBooleanMethodV(listObj, m_list_add, args);
    va_end(args);
}

int JArrayList::size(jobject listObj) {
    return m_env->CallIntMethod(listObj, m_list_size);
}

jobject JArrayList::get(jobject listObj, int index) {
    return m_env->CallObjectMethod(listObj, m_list_get, index);
}


