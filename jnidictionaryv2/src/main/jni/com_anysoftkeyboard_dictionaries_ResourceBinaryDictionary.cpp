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
#include "dictionary.h"

// ----------------------------------------------------------------------------

using namespace nativeime;

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

static jlong nativeime_ResourceBinaryDictionary_open
        (JNIEnv *env, jobject object, jobject dictDirectBuffer,
         jint typedLetterMultiplier, jint fullWordMultiplier)
{
    void *dict = env->GetDirectBufferAddress(dictDirectBuffer);
    if (dict == NULL) {
        fprintf(stderr, "DICT: Dictionary buffer is null\n");
        return 0;
    }
    Dictionary *dictionary = new Dictionary(dict, typedLetterMultiplier, fullWordMultiplier);
    return (jlong) dictionary;
}

static int nativeime_ResourceBinaryDictionary_getSuggestions(
        JNIEnv *env, jobject object, jlong dict, jintArray inputArray, jint arraySize,
        jcharArray outputArray, jintArray frequencyArray, jint maxWordLength, jint maxWords,
        jint maxAlternatives, jint skipPos, jintArray nextLettersArray, jint nextLettersSize)
{
    Dictionary *dictionary = (Dictionary*) dict;
    if (dictionary == NULL) return 0;

    int *frequencies = env->GetIntArrayElements(frequencyArray, NULL);
    int *inputCodes = env->GetIntArrayElements(inputArray, NULL);
    jchar *outputChars = env->GetCharArrayElements(outputArray, NULL);
    int *nextLetters = nextLettersArray != NULL ? env->GetIntArrayElements(nextLettersArray, NULL)
            : NULL;

    int count = dictionary->getSuggestions(inputCodes, arraySize, (unsigned short*) outputChars,
            frequencies, maxWordLength, maxWords, maxAlternatives, skipPos, nextLetters,
            nextLettersSize);

    env->ReleaseIntArrayElements(frequencyArray, frequencies, 0);
    env->ReleaseIntArrayElements(inputArray, inputCodes, JNI_ABORT);
    env->ReleaseCharArrayElements(outputArray, outputChars, 0);
    if (nextLetters) {
        env->ReleaseIntArrayElements(nextLettersArray, nextLetters, 0);
    }

    return count;
}

static int nativeime_ResourceBinaryDictionary_getWordsForPath(
        JNIEnv *env, jobject object, jlong dict, jintArray inputArray, jint arraySize,
        jcharArray outputArray, jintArray frequencyArray, jint maxWordLength, jint maxWords)
{
    Dictionary *dictionary = (Dictionary*) dict;
    if (dictionary == NULL) return 0;

    int *frequencies = env->GetIntArrayElements(frequencyArray, NULL);
    int *inputCodes = env->GetIntArrayElements(inputArray, NULL);
    jchar *outputChars = env->GetCharArrayElements(outputArray, NULL);

    int count = dictionary->getWordsForPath(inputCodes, arraySize, (unsigned short*) outputChars,
                                           frequencies, maxWordLength, maxWords);

    env->ReleaseIntArrayElements(frequencyArray, frequencies, 0);
    env->ReleaseIntArrayElements(inputArray, inputCodes, JNI_ABORT);
    env->ReleaseCharArrayElements(outputArray, outputChars, 0);

    return count;
}

static int nativeime_ResourceBinaryDictionary_getBigrams
        (JNIEnv *env, jobject object, jlong dict, jcharArray prevWordArray, jint prevWordLength,
         jintArray inputArray, jint inputArraySize, jcharArray outputArray,
         jintArray frequencyArray, jint maxWordLength, jint maxBigrams, jint maxAlternatives)
{
    Dictionary *dictionary = (Dictionary*) dict;
    if (dictionary == NULL) return 0;

    jchar *prevWord = env->GetCharArrayElements(prevWordArray, NULL);
    int *inputCodes = env->GetIntArrayElements(inputArray, NULL);
    jchar *outputChars = env->GetCharArrayElements(outputArray, NULL);
    int *frequencies = env->GetIntArrayElements(frequencyArray, NULL);

    int count = dictionary->getBigrams((unsigned short*) prevWord, prevWordLength, inputCodes,
            inputArraySize, (unsigned short*) outputChars, frequencies, maxWordLength, maxBigrams,
            maxAlternatives);

    env->ReleaseCharArrayElements(prevWordArray, prevWord, JNI_ABORT);
    env->ReleaseIntArrayElements(inputArray, inputCodes, JNI_ABORT);
    env->ReleaseCharArrayElements(outputArray, outputChars, 0);
    env->ReleaseIntArrayElements(frequencyArray, frequencies, 0);

    return count;
}


static jboolean nativeime_ResourceBinaryDictionary_isValidWord
        (JNIEnv *env, jobject object, jlong dict, jcharArray wordArray, jint wordLength)
{
    Dictionary *dictionary = (Dictionary*) dict;
    if (dictionary == NULL) return (jboolean) false;

    jchar *word = env->GetCharArrayElements(wordArray, NULL);
    jboolean result = dictionary->isValidWord((unsigned short*) word, wordLength);
    env->ReleaseCharArrayElements(wordArray, word, JNI_ABORT);

    return result;
}

static void nativeime_ResourceBinaryDictionary_close
        (JNIEnv *env, jobject object, jlong dict)
{
    Dictionary *dictionary = (Dictionary*) dict;
    delete (Dictionary*) dict;
}

// ----------------------------------------------------------------------------

static JNINativeMethod gMethods[] = {
    {"openNative",           "(Ljava/nio/ByteBuffer;II)J",(void*)nativeime_ResourceBinaryDictionary_open},
    {"closeNative",          "(J)V",            (void*)nativeime_ResourceBinaryDictionary_close},
    {"getSuggestionsNative", "(J[II[C[IIIII[II)I",  (void*)nativeime_ResourceBinaryDictionary_getSuggestions},
    {"getWordsForPathNative", "(J[II[C[III)I",  (void*)nativeime_ResourceBinaryDictionary_getWordsForPath},
    {"isValidWordNative",    "(J[CI)Z",         (void*)nativeime_ResourceBinaryDictionary_isValidWord}/*,
    {"getBigramsNative",    "(I[CI[II[C[IIII)I",         (void*)nativeime_ResourceBinaryDictionary_getBigrams}*/
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
    const char* const kClassPathName = "com/anysoftkeyboard/dictionaries/jni/ResourceBinaryDictionary";
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
