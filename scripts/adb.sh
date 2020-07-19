#! /bin/bash

# Default flags
clearlogcat=0;
flags="";
filters="";
choice="";

# Parse filters and flags separately because default filters go in the middle.
# Both $flags and $filters will begin with a space, or will be empty.
while (( $# > 1 )); do
    if [ "$1" == "-c" ]; then
        clearlogcat=1
    else
        if [[ "$1" =~ ^[^\ ]+:[SFEWIDV]$ ]]; then
            filters="$filters $1"
        else
            flags="$flags $1"
        fi
    fi
    shift
done

if [ $# == 0 ]; then
    choice="--help"
else
    choice=$1
fi

case $choice in
    [FEWIDV])
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
        tags=`grep -R 'TAG = ".*"' ime/app/src/main/java/com/* ime/dictionaries/jnidictionaryv1/src/main/java/com/* ime/dictionaries/jnidictionaryv2/src/main/java/com/*`
        # We can go back to our original folder now:
        cd "$oldpath"
        tags="$(echo $tags | sed -E 's![a-z/A-Z12]*\.java: (protected |private )?(static )?(final )?String [A-Z_]* = "([^\"]*)";!\4!g')"
        if [ -z "$tags" ]; then
            echo -e "${error}Aborting.${nocolor} No tags found."
            exit 2
        fi
        tags="$tags dalvikvm System.err AndroidRuntime StrictMode DEBUG "
        if [ -z "$filters" ]; then
        comm="adb logcat$flags $(echo "$tags" | sed "s/ /:$1 /g")*:S"
        else
        comm="adb logcat$flags $(echo "$tags" | sed "s/ /:$1 /g")${filters:1}"
        fi
        echo -e "${color}Running: $nocolor$comm"
        # Run command:
        echo -e "${color}Logcat:$nocolor"
        $comm
        ;;
    --help|-h|-?)
        echo "Syntax: adb.sh […flags and filters…] {F|E|W|I|D|V}

        adb.sh outputs the logcat to standard output and filters out lines not related with this project.
        \`dalvikvm\`, \`System.err\`, \`AndroidRuntime\`, and \`DEBUG\` are included, as those are used
        to debug fatal crashes. \`StrictMode\` is also included for convenience.

        You can add extra filters; if you do not, a \`*:S\` filter is appended by default.

        The last argument is the initial of your log priority. (Fatal/Error/Warning/Info/Debug/Verbose)"
        ;;
    *)
        echo "Error: last argument must be the initial of your log priority. (F/E/W/I/D/V)

        Run this command with no arguments or with \`--help\` to get help."
        ;;
esac

