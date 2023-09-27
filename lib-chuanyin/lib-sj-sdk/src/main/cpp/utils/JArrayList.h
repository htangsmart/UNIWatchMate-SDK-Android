#include <climits>
//
// Created by 王文锋 on 2/25/22.
//
#pragma once

#include <jni.h>
#include <string>

class JArrayList {
private:
    JNIEnv *m_env;
public:
    static jclass    m_list_jcls;
    static jmethodID m_list_get;
    static jmethodID m_list_size;
    static jmethodID m_list_add;

    JArrayList(JNIEnv *env);
    ~JArrayList();

    void add(jobject listObj, ...);
    int  size(jobject listObj);
    jobject get(jobject listObj, int index);

};


