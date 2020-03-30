#include <stdio.h>
#include <regex.h>
#include <string.h>
#include <ctype.h>

#include <jni.h>

#define REGEX "^(ngh|d\\^|ch|gh|kh|nh|ng|ph|th|tr|qu|[bcdghklmnpqrstvx]?)([aeiouy\\^)`\\/>~.]*)(ch|nh|ng|[cmnpt]?)$"

enum comp {
    HAT,
    TAIL,
    UNDO
};

int char_comp(char char1, char char2, char char3, enum comp flag) {
    int comp = 0;
    char1 = tolower(char1);
    char2 = tolower(char2);
    char3 = tolower(char3);
    switch(flag) {
        case HAT:
            switch(char1) {
                case 'a':
                case 'e':
                case 'o':
                case 'd':
                    if(char1 == char2) comp = 1;
                    break;
            }
            break;
        case TAIL:
            switch(char2) {
                case 'a':
                case 'o':
                case 'u':
                    if(char1 == 'w') comp = 1;
                    break;
            }
            break;
        case UNDO:
            if(char1 == char3 && char2 == '^') comp = 1;
            else if(char1 == 'w' && char2 == ')') comp = 1;
            break;
    }
    return comp;
}

void rem_char(char *str1, int pos) {
    int len = strlen(str1);
    int i;
    char buff[20];
    for(i = 0; i < len; i++) {
        if(i > pos) buff[i - 1] = str1[i];
        else if(i < pos) buff[i] = str1[i];
    }
    buff[i - 1] = 0;
    strcpy(str1, buff);
}

void ins_char(char *str1, char c, int pos) {
    int len = strlen(str1);
    int i;
    char buff[20];
    for(i = 0; i < len + 1; ++i) {
        if(i == pos) buff[i] = c;
        else if(i > pos) buff[i] = str1[i - 1];
        else buff[i] = str1[i];
    }
    buff[i] = 0;
    strcpy(str1, buff);
}

void cat_char(char *str1, char c) {
    int len;
    for(len = 0; str1[len] != 0; len++);
    str1[len] = c;
    str1[len + 1] = 0;
}

void cpy_range(char *str1, char *str2, int x1, int x2) {
    int i, j = 0;
    for(i = x1; i < x2; i++, ++j) {
        str1[j] = str2[i];
    }
    str1[j] = 0;
}

int is_vowel(char c) {
    int vowel = 0;
    switch(tolower(c)) {
        case 'a':
        case 'e':
        case 'i':
        case 'o':
        case 'u':
        case 'y':
            vowel = 1;
            break;
    }
    return vowel;
}

char get_tone(char c) {
    char tone;
    switch(tolower(c)) {
        case 'z':
            tone = 0;
            break;
        case 'f':
            tone = '`';
            break;
        case 's':
            tone = '/';
            break;
        case 'r':
            tone = '>';
            break;
        case 'x':
            tone = '~';
            break;
        case 'j':
            tone = '.';
            break;
    }
    return tone;
}

int is_tone(char c) {
    int tone = 0;
    switch(tolower(c)) {
        case '`':
        case '/':
        case '>':
        case '~':
        case '.':
        case 'z':
        case 'f':
        case 's':
        case 'r':
        case 'x':
        case 'j':
            tone = 1;
            break;
    }
    return tone;
}

int is_end_cons(char c) {
    int end_cons = 0;
    switch(tolower(c)) {
        case 'c':
        case 'm':
        case 'n':
        case 'p':
        case 't':
            end_cons = 1;
            break;
    }
    return end_cons;
}

int telex(char *word, char c) {
    char regex_word[150] = REGEX;
    regex_t regex_compiled;
    regmatch_t group_array[4];
    regcomp(&regex_compiled, regex_word, REG_EXTENDED|REG_ICASE);
    if(regexec(&regex_compiled, word, 4, group_array, 0)) return 1;
    regfree(&regex_compiled);
    char beg_cons[4] = {};
    char vowels[10] = {};
    char end_cons[3] = {};
    int i;
    for(i = 0; i < 4; i++) {
        // Divide the word into sections with magic regex.
        char temp[strlen(word) + 1];
        cpy_range(temp, word, group_array[i].rm_so, group_array[i].rm_eo);
        if(i == 1) strcpy(beg_cons, temp);
        else if(i == 2) strcpy(vowels, temp);
        else if(i == 3) strcpy(end_cons, temp);
        temp[0] = 0;
    }
    char word_test[strlen(word) + 1];
    strcat(word_test, beg_cons);
    strcat(word_test, vowels);
    strcat(word_test, end_cons);
    // Make sure the sections added together are the same as the original word.
    if(strcmp(word_test, word)) return 1;
    int b = strlen(beg_cons);
    int v = strlen(vowels);
    int e = strlen(end_cons);
    int vowel_count = 0, spec_pos = 0, tone_pos = 0;
    for(i = 0; i < v; i++) {
        if(is_vowel(vowels[i])) vowel_count++;
        else if(vowels[i] == ')' || vowels[i] == '^') spec_pos = i;
        else if(is_tone(vowels[i])) tone_pos = i;
    }
    char tone = get_tone(c);
    if(b && !v && !e) {
        if(b == 1 && char_comp(c, beg_cons[0], 0, HAT)) {
            // Bar'd D only occurs at the beginning of a word.
            cat_char(beg_cons, '^');
        } else if(char_comp(c, beg_cons[b - 1], beg_cons[b - 2], UNDO)) {
            beg_cons[1] = c;
        } else return 1;
    } else if(v) {
        if(char_comp(c, vowels[v - 1], 0, HAT)) {
            // Two of either 'a', 'e', or 'o' puts a hat on em.
            cat_char(vowels, '^');
        } else if(char_comp(c, vowels[v - 1], 0, TAIL)) {
            // 'a', 'o' or 'u' with a w after gives it a tail.
            cat_char(vowels, ')');
        } else if(char_comp(c, vowels[v - 1], vowels[v - 2], UNDO) ||
                  char_comp(c, vowels[v - 1], 0, UNDO)) {
            // If mod char is pressed twice, you get just the mod char.
            vowels[v - 1] = c;
        } else if(vowels[tone_pos] == get_tone(c)) {
            // If tone char is pressed twice, you get just the char.
            rem_char(vowels, tone_pos);
            if(e) cat_char(end_cons, c);
            else cat_char(vowels, c);
        } else if(e && is_tone(c)) {
            if(tone_pos) {
                // If a tone character is found.
                if(!tone && tone_pos) {
                    // If tone is 'z', we delete the tone marking.
                    rem_char(vowels, tone_pos);
                } else if(tone && tone_pos) {
                    // Else, we change the tone marking at the position found.
                    vowels[tone_pos] = tone;
                } else return 1;
            } else if(tone && !tone_pos && !spec_pos) {
                // If a tone character isn't found, put it right
                // after the vowels.
                if(vowel_count < 3) cat_char(vowels, tone);
                else if(vowel_count == 3) ins_char(vowels, tone, 2);
                else return 1;
            } else if(tone && spec_pos) {
                // If there is a ')' or '^' tone always goes there.
                ins_char(vowels, tone, spec_pos + 1);
            } else return 1;
        } else if(!e) {
            if(tone_pos != strlen(vowels) - 1 && is_end_cons(c)) {
                // If a tone char is found and there are 2 vowels,
                // move it to after the last vowel.
                if (vowel_count == 2) {
                    char tone_char = vowels[tone_pos];
                    rem_char(vowels, tone_pos);
                    cat_char(vowels, tone_char);
                    cat_char(vowels, c);
                } else return 1;
            } else if(is_tone(c)) {
                // If no ending, move tone to right after the vowels
                // or after the first vowel depending on number of.
                if(spec_pos && vowel_count < 4) {
                    ins_char(vowels, tone, spec_pos + 1);
                } else if(tone_pos && vowel_count < 4) {
                    if(tone) vowels[tone_pos] = tone;
                    else rem_char(vowels, tone_pos);
                } else if(vowel_count == 3) {
                    ins_char(vowels, tone, 2);
                } else if(vowel_count < 3) {
                    ins_char(vowels, tone, 1);
                } else return 1;
            } else return 1;
        } else return 1;
    } else return 1;
    // Cat all the goodness together.
    char temp[strlen(word) + 1];
    strcpy(temp, beg_cons);
    strcat(temp, vowels);
    strcat(temp, end_cons);
    strcpy(word, temp);
    return 0;
}

JNIEXPORT jstring JNICALL
Java_com_anysoftkeyboard_ext_jni_telex_getTelex(JNIEnv* env, jobject thiz, jstring j_word, jstring j_c) {
    jboolean isCopy;
    const char *word_const = (*env)->GetStringUTFChars(env, j_word, &isCopy);
    char word[strlen(word_const)];
    strcpy(word, word_const);
    (*env)->ReleaseStringUTFChars(env, j_word, word_const);

    const char *c_string = (*env)->GetStringUTFChars(env, j_c, &isCopy);
    char c = c_string[0];
    (*env)->ReleaseStringUTFChars(env, j_c, c_string);

    if (telex(word, c)) {
        return (*env)->NewStringUTF(env, "");
    } else {
        return (*env)->NewStringUTF(env, word);
    }
}
