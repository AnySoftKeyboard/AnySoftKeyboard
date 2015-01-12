/*
**
** Copyright 2009, The Android Open Source Project
** Copyright 2011 AnySoftKeyboard
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

#define LOG_TAG "BinaryDictionary"

#include <stdio.h>
#include <assert.h>
#include <unistd.h>
#include <fcntl.h>

#include <jni.h>

#include "dictionary.h"

// ----------------------------------------------------------------------------

using namespace nativeime;



static jfieldID sDescriptorField;

//
// helper function to throw an exception
//
static void throwException(JNIEnv *env, const char* ex, const char* fmt, int data)
{
    if (jclass cls = env->FindClass(ex)) {
        char msg[1000];
        sprintf(msg, fmt, data);
        env->ThrowNew(cls, msg);
        env->DeleteLocalRef(cls);
    }
}

static jlong nativeime_BinaryDictionary_open
        (JNIEnv *env, jobject object, jobject fileDescriptor, jlong offset, jlong length,
         jint typedLetterMultiplier, jint fullWordMultiplier)
{
  	jint fd = env->GetIntField(fileDescriptor, sDescriptorField);

    unsigned char *dict = new unsigned char[length];
    if (dict == NULL) {
        fprintf(stderr, "DICT: Failed to allocate dictionary buffer\n");
        return 0;
    }

    lseek(fd, offset, SEEK_SET);
    size_t bytesLeft = length;
    unsigned char *p = dict;
    while (bytesLeft > 0) {
        size_t bytesRead = read(fd, p, bytesLeft);
        p += bytesRead;
        bytesLeft -= bytesRead;
    }
    // FIXME check: need to close fd?
    Dictionary *dictionary = new Dictionary(dict, typedLetterMultiplier, fullWordMultiplier);

    return (jlong) dictionary;
}

static int nativeime_BinaryDictionary_getSuggestions(
        JNIEnv *env, jobject object, jlong dict, jintArray inputArray, jint arraySize,
        jcharArray outputArray, jintArray frequencyArray, jint maxWordLength, jint maxWords,
        jint maxAlternatives, jint skipPos)
{
    Dictionary *dictionary = (Dictionary*) dict;
    if (dictionary == NULL)
        return 0;

    int *frequencies = env->GetIntArrayElements(frequencyArray, NULL);
    int *inputCodes = env->GetIntArrayElements(inputArray, NULL);
    jchar *outputChars = env->GetCharArrayElements(outputArray, NULL);

    int count = dictionary->getSuggestions(inputCodes, arraySize, (unsigned short*) outputChars, frequencies,
            maxWordLength, maxWords, maxAlternatives, skipPos);

    env->ReleaseIntArrayElements(frequencyArray, frequencies, 0);
    env->ReleaseIntArrayElements(inputArray, inputCodes, JNI_ABORT);
    env->ReleaseCharArrayElements(outputArray, outputChars, 0);

    return count;
}

static jboolean nativeime_BinaryDictionary_isValidWord
        (JNIEnv *env, jobject object, jlong dict, jcharArray wordArray, jint wordLength)
{
    Dictionary *dictionary = (Dictionary*) dict;
    if (dictionary == NULL) return (jboolean) false;

    jchar *word = env->GetCharArrayElements(wordArray, NULL);
    jboolean result = dictionary->isValidWord((unsigned short*) word, wordLength);
    env->ReleaseCharArrayElements(wordArray, word, JNI_ABORT);

    return result;
}

static void nativeime_BinaryDictionary_close(JNIEnv *env, jobject object, jlong dict)
{
    Dictionary *dictionary = (Dictionary*) dict;
    delete dictionary->getDictBuffer();
    delete (Dictionary*) dict;
}

// ----------------------------------------------------------------------------

static JNINativeMethod gMethods[] = {
    {"openNative",           "(Ljava/io/FileDescriptor;JJII)J", (void*)nativeime_BinaryDictionary_open},
    {"closeNative",          "(J)V",            (void*)nativeime_BinaryDictionary_close},
    {"getSuggestionsNative", "(J[II[C[IIIII)I",  (void*)nativeime_BinaryDictionary_getSuggestions},
    {"isValidWordNative",    "(J[CI)Z",         (void*)nativeime_BinaryDictionary_isValidWord}
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
    const char* const kClassPathName = "com/anysoftkeyboard/dictionaries/jni/BinaryDictionary";
    jclass clazz;

    clazz = env->FindClass("java/io/FileDescriptor");
    if (clazz == NULL) {
        //LOGE("Can't find %s", "java/io/FileDescriptor");
		fprintf(stderr, "Can't find %s", "java/io/FileDescriptor");
        return -1;
    }
    sDescriptorField = env->GetFieldID(clazz, "descriptor", "I");


    return registerNativeMethods(env,
            kClassPathName, gMethods, sizeof(gMethods) / sizeof(gMethods[0]));
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
