#include "NextWordDictionary.h"

NextWordDictionary::NextWordDictionary(const char * dictFilename)
        :mDictFilename(dictFilename) {
}

NextWordDictionary::~NextWordDictionary() {
    delete mDictFilename;
}

void NextWordDictionary::close() {

}

void NextWordDictionary::load() {

}

void NextWordDictionary::clear() {

}
