/*
**
** Copyright 2009, The Android Open Source Project
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

#include <jni.h>
#include <string.h>
#include "dictionary.h"

// ----------------------------------------------------------------------------

using namespace nativeime;


static jmethodID sGetWordsCallbackMethodId;

//
// helper function to throw an exception
//
static void throwException(JNIEnv *env, const char *ex, const char *fmt, int data) {
    if (jclass cls = env->FindClass(ex)) {
        char msg[1000];
        sprintf(msg, fmt, data);
        env->ThrowNew(cls, msg);
        env->DeleteLocalRef(cls);
    }
}

static jlong nativeime_ResourceBinaryDictionary_open
        (JNIEnv *env, jobject object, jobject dictDirectBuffer,
         jint typedLetterMultiplier, jint fullWordMultiplier) {
    void *dict = env->GetDirectBufferAddress(dictDirectBuffer);
    if (dict == nullptr) {
        fprintf(stderr, "DICT: Dictionary buffer is null\n");
        return 0;
    }
    Dictionary *dictionary = new Dictionary(dict, typedLetterMultiplier, fullWordMultiplier);
    return jlong(dictionary);
}

static int nativeime_ResourceBinaryDictionary_getSuggestions(
        JNIEnv *env, jobject object, jlong dict, jintArray inputArray, jint arraySize,
        jcharArray outputArray, jintArray frequencyArray, jint maxWordLength, jint maxWords,
        jint maxAlternatives, jint skipPos, jintArray nextLettersArray, jint nextLettersSize) {
    auto *dictionary = reinterpret_cast<Dictionary *>(dict);
    if (dictionary == nullptr) return 0;

    int *frequencies = env->GetIntArrayElements(frequencyArray, nullptr);
    int *inputCodes = env->GetIntArrayElements(inputArray, nullptr);
    jchar *outputChars = env->GetCharArrayElements(outputArray, nullptr);
    int *nextLetters = nextLettersArray != nullptr ? env->GetIntArrayElements(nextLettersArray, nullptr)
                                                : nullptr;

    int count = dictionary->getSuggestions(inputCodes, arraySize, outputChars,
                                           frequencies, maxWordLength, maxWords, maxAlternatives,
                                           skipPos, nextLetters,
                                           nextLettersSize);

    env->ReleaseIntArrayElements(frequencyArray, frequencies, 0);
    env->ReleaseIntArrayElements(inputArray, inputCodes, JNI_ABORT);
    env->ReleaseCharArrayElements(outputArray, outputChars, 0);
    if (nextLetters) {
        env->ReleaseIntArrayElements(nextLettersArray, nextLetters, 0);
    }

    return count;
}

static jboolean nativeime_ResourceBinaryDictionary_isValidWord
        (JNIEnv *env, jobject object, jlong dict, jcharArray wordArray, jint wordLength) {
    Dictionary *dictionary = reinterpret_cast<Dictionary *>(dict);
    if (dictionary == NULL) return (jboolean) false;

    jchar *word = env->GetCharArrayElements(wordArray, NULL);
    jboolean result = static_cast<jboolean>(dictionary->isValidWord(word, wordLength));
    env->ReleaseCharArrayElements(wordArray, word, JNI_ABORT);

    return result;
}

static void nativeime_ResourceBinaryDictionary_getWords
        (JNIEnv *env, jobject object, jlong dict, jobject getWordsCallback) {
    Dictionary *dictionary = reinterpret_cast<Dictionary *>(dict);
    if (!dictionary) return;

    int wordCount = 0, wordsCharsCount = 0;
    dictionary->countWordsChars(wordCount, wordsCharsCount);
    unsigned short *words = new unsigned short[wordsCharsCount];
    jintArray frequencyArray = env->NewIntArray(wordCount);
    int *frequencies = env->GetIntArrayElements(frequencyArray, NULL);
    dictionary->getWords(words, frequencies);
    env->ReleaseIntArrayElements(frequencyArray, frequencies, 0);

    jobjectArray javaLandChars = env->NewObjectArray(wordCount, env->FindClass("[C"), NULL);

    unsigned short *pos = words;
    for (int i = 0; i < wordCount; ++i) {
        size_t count = 0;
        while (pos[count] != 0x00) ++count;

        jcharArray jchr = env->NewCharArray((jsize) count);
        jchar *chr = env->GetCharArrayElements(jchr, NULL);

        memcpy(chr, pos, count * sizeof(unsigned short));

        env->ReleaseCharArrayElements(jchr, chr, 0);
        env->SetObjectArrayElement(javaLandChars, i, jchr);
        pos += count + 1;
        env->DeleteLocalRef(jchr);
    }

    delete[] words;

    env->CallVoidMethod(getWordsCallback, sGetWordsCallbackMethodId, javaLandChars, frequencyArray);
}

static void nativeime_ResourceBinaryDictionary_close
        (JNIEnv *env, jobject object, jlong dict) {
    Dictionary *dictionary = reinterpret_cast<Dictionary *>(dict);
    delete dictionary;
}

// ----------------------------------------------------------------------------

static JNINativeMethod gMethods[] = {
        {"openNative",           "(Ljava/nio/ByteBuffer;II)J",                              (void *) nativeime_ResourceBinaryDictionary_open},
        {"closeNative",          "(J)V",                                                    (void *) nativeime_ResourceBinaryDictionary_close},
        {"getSuggestionsNative", "(J[II[C[IIIII[II)I",                                      (void *) nativeime_ResourceBinaryDictionary_getSuggestions},
        {"isValidWordNative",    "(J[CI)Z",                                                 (void *) nativeime_ResourceBinaryDictionary_isValidWord},
        {"getWordsNative",       "(JLcom/anysoftkeyboard/dictionaries/GetWordsCallback;)V", (void *) nativeime_ResourceBinaryDictionary_getWords}
};

static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods) {
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

static int registerNatives(JNIEnv *env) {
    const char *const kClassPathName = "com/anysoftkeyboard/dictionaries/jni/ResourceBinaryDictionary";
    return registerNativeMethods(env,
                                 kClassPathName, gMethods, sizeof(gMethods) / sizeof(gMethods[0]));
}

/*
 * Returns the JNI version on success, -1 on failure.
 */
jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = NULL;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        fprintf(stderr, "ERROR: GetEnv failed\n");
        return -1;
    }
    assert(env != NULL);

    if (!registerNatives(env)) {
        fprintf(stderr, "ERROR: BinaryDictionary native registration failed\n");
        return -1;
    }

    jclass getWordsCallbackClass = env->FindClass(
            "com/anysoftkeyboard/dictionaries/GetWordsCallback");
    //void (char[][] words, int[] frequencies);
    sGetWordsCallbackMethodId = env->GetMethodID(getWordsCallbackClass, "onGetWordsFinished",
                                                 "([[C[I)V");

    /* success -- return valid version number */
    return JNI_VERSION_1_6;
}
