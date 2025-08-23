cmake_minimum_required(VERSION 3.4.1)

add_library(anysoftkey_jni SHARED
    src/main/jni/source/com_anysoftkeyboard_dictionaries_BinaryDictionary.cpp
    src/main/jni/source/dictionary.cpp)

target_include_directories(anysoftkey_jni PRIVATE
    src/main/jni/include)
