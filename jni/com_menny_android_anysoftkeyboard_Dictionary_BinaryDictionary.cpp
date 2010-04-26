/*
**
** Copyright 2009, The Android Open Source Project
** Copyright 2009, Spiros Papadimitriou <spapadim@cs.cmu.edu>
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

#include <jni.h>

#include <stdio.h>
#include <assert.h>
#include <unistd.h>
#include <fcntl.h>

#include "dictionary.h"

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

static jint nativeime_BinaryDictionary_open
        (JNIEnv *env, jobject object, jobject fileDescriptor,
         jlong offset, jlong length,
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

    return (jint) dictionary;
}

static int nativeime_BinaryDictionary_getSuggestions(
        JNIEnv *env, jobject object, jint dict, jintArray inputArray, jint arraySize,
        jcharArray outputArray, jintArray frequencyArray, jint maxWordLength, jint maxWords,
        jint maxAlternatives)
{
    Dictionary *dictionary = (Dictionary*) dict;
    if (dictionary == NULL)
        return 0;

    int *frequencies = env->GetIntArrayElements(frequencyArray, NULL);
    int *inputCodes = env->GetIntArrayElements(inputArray, NULL);
    jchar *outputChars = env->GetCharArrayElements(outputArray, NULL);

    int count = dictionary->getSuggestions(inputCodes, arraySize, (unsigned short*) outputChars, frequencies,
            maxWordLength, maxWords, maxAlternatives);

    env->ReleaseIntArrayElements(frequencyArray, frequencies, JNI_COMMIT);
    env->ReleaseIntArrayElements(inputArray, inputCodes, JNI_ABORT);
    env->ReleaseCharArrayElements(outputArray, outputChars, JNI_COMMIT);

    return count;
}

static jboolean nativeime_BinaryDictionary_isValidWord
        (JNIEnv *env, jobject object, jint dict, jcharArray wordArray, jint wordLength)
{
    Dictionary *dictionary = (Dictionary*) dict;
    if (dictionary == NULL) return (jboolean) false;

    jchar *word = env->GetCharArrayElements(wordArray, NULL);
    jboolean result = dictionary->isValidWord((unsigned short*) word, wordLength);
    env->ReleaseCharArrayElements(wordArray, word, JNI_ABORT);

    return result;
}

static void nativeime_BinaryDictionary_close
        (JNIEnv *env, jobject object, jint dict)
{
    Dictionary *dictionary = (Dictionary*) dict;
    delete dictionary->getDictBuffer();
    delete (Dictionary*) dict;
}

// ----------------------------------------------------------------------------

static JNINativeMethod gMethods[] = {
    {"openNative",           "(Ljava/io/FileDescriptor;JJII)I",
                                                (void*)nativeime_BinaryDictionary_open},
    {"closeNative",          "(I)V",            (void*)nativeime_BinaryDictionary_close},
    {"getSuggestionsNative", "(I[II[C[IIII)I",  (void*)nativeime_BinaryDictionary_getSuggestions},
    {"isValidWordNative",    "(I[CI)Z",         (void*)nativeime_BinaryDictionary_isValidWord}
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

/*
 * Returns the JNI version on success, -1 on failure.
 */
jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    const char* const kClassPathName = "com/menny/android/anysoftkeyboard/dictionary/BinaryDictionary";

    JNIEnv* env = NULL;
    jclass clazz;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        fprintf(stderr, "ERROR: GetEnv failed\n");
        return -1;
    }
    assert(env != NULL);

    clazz = env->FindClass("java/io/FileDescriptor");
    if (clazz == NULL) {
        fprintf(stderr, "Can't find %s", "java/io/FileDescriptor");
        return -2;
    }
    sDescriptorField = env->GetFieldID(clazz, "descriptor", "I");

    if (!registerNativeMethods(env,
            kClassPathName, gMethods, sizeof(gMethods) / sizeof(gMethods[0]))) {
        fprintf(stderr, "ERROR: BinaryDictionary native registration failed\n");
        return -3;
    }

    /* success -- return valid version number */
    return JNI_VERSION_1_4;
}
