#include <stdio.h>
#include <regex.h>
#include <string.h>
#include <ctype.h>

#include <jni.h>

#define REGEX "^(ngh|d\\^|ch|gh|kh|nh|ng|ph|th|tr|qu|[bcdghklmnpqrstvx]?)([aeiouy\\^)`\\/>~.]*)(ch|nh|ng|[cmnpt]?)$"

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
        // Divide the word into sections.
        char temp[strlen(word) + 1];
        cpy_range(temp, word, group_array[i].rm_so, group_array[i].rm_eo);
        if(i == 1) strcpy(beg_cons, temp);
        else if(i == 2) strcpy(vowels, temp);
        else if(i == 3) strcpy(end_cons, temp);
        memset(temp, 0, sizeof(temp));
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
    char lower_c = tolower(c);
    if(b && !v && !e && b == 1 && tolower(beg_cons[0]) == lower_c && lower_c == 'd') {
        // Bar'd D only occurs at the beginning of a word.
        cat_char(beg_cons, '^');
    } else if(v && lower_c == tolower(vowels[v - 1]) && (lower_c == 'a' || lower_c == 'e' || lower_c == 'o')) {
        // Two of either 'a', 'e', or 'o' puts a hat on em.
        cat_char(vowels, '^');
    } else if(v && lower_c == 'w' && (tolower(vowels[v - 1]) == 'a' || tolower(vowels[v - 1]) == 'o' || tolower(vowels[v - 1]) == 'u')) {
        // Any of these vowels with a 'w' after puts a tail thing on em.
        cat_char(vowels, ')');
    } else if((!e && ((lower_c == tolower(vowels[v - 2]) && vowels[v - 1] == '^') || (lower_c == 'w' && vowels[v - 1] == ')'))) || (!v && (!strcmp(beg_cons, "d^") || !strcmp(beg_cons, "D^")))) {
        // If you hit 'w', 'd' or 'a', 'e' or 'o' thrice, no hat, just the letter
        if(v) vowels[v - 1] = c;
        else if(b) beg_cons[b - 1] = c;
        else return 1;
    } else if(v && e && is_tone(c)) {
        if(tone_pos) {
            // If a tone character is found.
            if(!tone && is_tone(vowels[tone_pos])) {
                // If tone is 'z', we delete the tone marking.
                memmove(&vowels[tone_pos], &vowels[tone_pos + 1], strlen(vowels) - tone_pos);
            } else if(tone && is_tone(vowels[tone_pos])) {
                // Else, we change the tone marking at the position found.
                vowels[tone_pos] = tone;
            } else return 1;
        } else if(tone && !tone_pos && !spec_pos) {
            // If a tone character isn't found, put it right after the vowels.
            if(vowel_count < 3) {
                cat_char(vowels, tone);
            } else if(vowel_count == 3) {
                ins_char(vowels, tone, 2);
            } else return 1;
        } else if(tone && spec_pos) {
            // If there is a ')' or '^' tone always goes there.
            ins_char(vowels, tone, spec_pos + 1);
        } else return 1;
    } else if(v && !e && tone_pos != strlen(vowels) - 1 && is_end_cons(c)) {
        // If a tone char is found and there are 2 vowels, move it to after the last vowel.
        if (vowel_count == 2) {
            char tone_char = vowels[tone_pos];
            memmove(&vowels[tone_pos], &vowels[tone_pos + 1], strlen(vowels) - tone_pos);
            cat_char(vowels, tone_char);
            cat_char(vowels, c);
        } else return 1;
    } else if(v && !e && is_tone(c)) {
        // If no ending, move tone to right after or after the first vowel depending on number of.
        if(spec_pos && vowel_count < 4) {
            ins_char(vowels, tone, spec_pos + 1);
        } else if(tone_pos && vowel_count < 4) {
            if(tone) vowels[tone_pos] = tone;
            else memmove(&vowels[tone_pos], &vowels[tone_pos + 1], strlen(vowels) - tone_pos);
        } else if(vowel_count == 3) {
            ins_char(vowels, tone, 2);
        } else if(vowel_count < 3) {
            ins_char(vowels, tone, 1);
        } else return 1;
    } else return 1;
    // Cat all the goodness together.
    char temp2[b + v + e];
    strcpy(temp2, beg_cons);
    strcat(temp2, vowels);
    strcat(temp2, end_cons);
    strcpy(word, temp2);
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
