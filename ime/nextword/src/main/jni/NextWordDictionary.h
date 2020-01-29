#ifndef ANYSOFTKEYBOARD_NEXTWORDDICTIONARY_H
#define ANYSOFTKEYBOARD_NEXTWORDDICTIONARY_H


class NextWordDictionary {
private:
    const char * mDictFilename;
public:
    NextWordDictionary(const char * dictFilename);
    /**
     * loads the data from the file into memory
     */
    void load();
    /**
     * writes the data in memory to file
     */
    void close();

    /**
     * clears the next-word memory.
     */
    void clear();

    ~NextWordDictionary();
};


#endif //ANYSOFTKEYBOARD_NEXTWORDDICTIONARY_H
