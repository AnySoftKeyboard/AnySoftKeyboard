#!/bin/bash
#
# Copyright (C) 2013 Reece H. Dunn
# License: GPLv3+
#
# Helper utility for removing a string/string-list item in all string resources.

ls app/src/main/res/values*/strings.xml | while read STRINGS ; do
    xmlstarlet ed -P -d "/resources/*[@name='${1}']" ${STRINGS} > tmp_strings.xml
    mv tmp_strings.xml ${STRINGS}
done
