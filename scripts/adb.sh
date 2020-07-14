#! /bin/bash

# Default flags
clearlogcat=0;
if [ "$1" == "-c" ]; then
    clearlogcat=1
    shift
fi
if [ $# != 1 ]; then
    echo "Syntax: adb.sh [-c] {E|W|I|D|V}

    adb.sh outputs the logcat to standard output and filters out lines not related with this project.
    \`dalvikvm\`, \`System.err\` and \`AndroidRuntime\` are included, as those are used to debug fatal crashes.

    The only argument is the initial of your log priority. (Error/Warning/Info/Debug/Verbose)

    You can optionally add the \`-c\` flag to clear the logcat prior to printing new logcat lines."
else
    # These variables are just to higlight text:
    color="\033[0;36m"
    nocolor="\033[0m"
    error="\033[0;31m"
    # Retrieving current working path
    oldpath="$(pwd)"
    if [ $clearlogcat == 1 ]; then
        echo -e "${color}Clearing${nocolor} last entries…"
        adb logcat -c
    fi
    echo -e "${color}Searching${nocolor} for tags in source code…"
    # cd project_root_folder
    # This will traverse from current folder to its parent folderwork until it finds a .git folder:
    cd "$(dirname "$0")"
    while [ ! -d ".git" ]
    do
        cd ..
        if [[ "$(pwd)" == "/" ]]; then
            echo -e "${error}Aborting.${nocolor} Not inside a Git project."
            exit 1
        fi
    done
    tags=`grep -R 'TAG = ".*"' ime/app/src/main/java/com/* ime/jnidictionaryv1/src/main/java/com/* ime/jnidictionaryv2/src/main/java/com/*`
    # We can go back to our original folder now:
    cd "$oldpath"
    tags="$(echo $tags | sed -E 's![a-z/A-Z12]*\.java: (protected |private )?(static )?(final )?String [A-Z_]* = "([^\"]*)";!\4!g')"
    if [ -z $tags ]; then
        echo -e "${error}Aborting.${nocolor} No tags found."
        exit 2
    fi
    tags="$tags dalvikvm System.err AndroidRuntime "
    comm="adb logcat $(echo "$tags" | sed "s/ /:$1 /g")*:S"
    echo -e "${color}Running: $nocolor$comm"
    # Run command:
    echo -e "${color}Logcat:$nocolor"
    $comm
fi

