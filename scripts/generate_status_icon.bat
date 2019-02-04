@ECHO OFF

set TEXT=%1
set FILE_POSTFIX=%2
set SIZE=%3
set FONT_SIZE=%4
set TARGET_FOLDER=%5

mkdir %TARGET_FOLDER%
magick -background transparent -fill white ^
    -size %SIZE%x%SIZE% -pointsize %FONT_SIZE% -gravity center ^
    label:%TEXT% ^
    %TARGET_FOLDER%/ic_status_%FILE_POSTFIX%.png
