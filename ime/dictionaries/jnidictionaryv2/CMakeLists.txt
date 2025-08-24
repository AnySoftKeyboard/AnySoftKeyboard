cmake_minimum_required(VERSION 3.4.1)

add_library(anysoftkey2_jni SHARED
    src/main/jni/source/char_utils.cpp
    src/main/jni/source/com_anysoftkeyboard_dictionaries_ResourceBinaryDictionary.cpp
    src/main/jni/source/dictionary.cpp)

target_include_directories(anysoftkey2_jni PRIVATE
    src/main/jni/include)

target_link_libraries(anysoftkey2_jni
    log)
