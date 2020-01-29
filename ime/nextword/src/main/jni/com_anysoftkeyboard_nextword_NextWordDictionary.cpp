/*
**
** Copyright 2015, Menny Even-Danan
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

#include <stdio.h>
#include <assert.h>

#include <cstdlib>
#include <cstdio> // for fopen() etc.

#include <jni.h>

#include "NextWordDictionary.h"

// ----------------------------------------------------------------------------

static jlong nativeime_NextWordDictionary_open(JNIEnv *env, jclass clazz, jstring filename)
{
    NextWordDictionary *dictionary = new NextWordDictionary(env->GetStringUTFChars(filename, 0));
    return (jlong) dictionary;
}

static void nativeime_NextWordDictionary_close(JNIEnv *env, jclass clazz, jlong dict)
{
    NextWordDictionary *dictionary = (NextWordDictionary*) dict;
    dictionary->close();
    delete dictionary;
}

static void nativeime_NextWordDictionary_load(JNIEnv *env, jclass clazz, jlong dict)
{
    NextWordDictionary *dictionary = (NextWordDictionary*) dict;
    dictionary->load();
}

static void nativeime_NextWordDictionary_clear(JNIEnv *env, jclass clazz, jlong dict)
{
    NextWordDictionary *dictionary = (NextWordDictionary*) dict;
    dictionary->clear();
}

// ----------------------------------------------------------------------------

static JNINativeMethod gMethods[] = {
    {"openNative",           "(Ljava/lang/String;)J",(void*)nativeime_NextWordDictionary_open},
    {"closeNative",          "(J)V",                 (void*)nativeime_NextWordDictionary_close},
    {"loadNative",           "(J)V",                 (void*)nativeime_NextWordDictionary_load},
    {"clearNative",          "(J)V",                 (void*)nativeime_NextWordDictionary_clear}
};

static int registerNativeMethods(JNIEnv* env, const char* className,
    JNINativeMethod* gMethods, int numMethods)
{
    jclass clazz;

    clazz = env->FindClass(className);
    if (clazz == NULL) {
        fprintf(stderr,
            "Native registration unable to find class '%s'\n", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        fprintf(stderr, "RegisterNatives failed for '%s'\n", className);
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

static int registerNatives(JNIEnv *env)
{
    const char* const kClassPathName = "com/anysoftkeyboard/nextword/NextWordDictionary";
    return registerNativeMethods(env, kClassPathName, gMethods, sizeof(gMethods) / sizeof(gMethods[0]));
}

/*
 * Returns the JNI version on success, -1 on failure.
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        fprintf(stderr, "ERROR: GetEnv failed\n");
        goto bail;
    }
    assert(env != NULL);

    if (!registerNatives(env)) {
        fprintf(stderr, "ERROR: BinaryDictionary native registration failed\n");
        goto bail;
    }

    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

bail:
    return result;
}
