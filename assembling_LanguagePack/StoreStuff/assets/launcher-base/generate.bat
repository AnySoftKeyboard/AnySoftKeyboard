@ECHO OFF

set PROJECT_DIR=%1
set BUILD_DIR=%2
set DIMEN=%3
set HEIGHT=%4
set OFFSET=%5
set ASSETS_DIR=%6

set SOURCE_FLAG_FILE=%PROJECT_DIR%\flag\flag.png
if exist %SOURCE_FLAG_FILE% (
    goto convert
)

set SOURCE_FLAG_FILE=%PROJECT_DIR%\flag\flag.svg
if exist %SOURCE_FLAG_FILE% (
    goto convert
)

echo Flag file does not exist.
echo Please provide a flag image (svg or png format) and store it at %PROJECT_DIR%\flag\ as flag.png or flag.svg
exit 1

:convert

mkdir %BUILD_DIR%\image_temp
mkdir %PROJECT_DIR%\src\main\res\mipmap-%DIMEN%

echo Using %SOURCE_FLAG_FILE%

magick %SOURCE_FLAG_FILE% ^
    -adaptive-resize x%HEIGHT% ^
    %BUILD_DIR%\image_temp\flag-%DIMEN%.png
magick %ASSETS_DIR%\%DIMEN%.png %BUILD_DIR%\image_temp\flag-%DIMEN%.png ^
    -gravity south -geometry +0+%OFFSET% -composite ^
    %PROJECT_DIR%\src\main\res\mipmap-%DIMEN%\ic_launcher.png
