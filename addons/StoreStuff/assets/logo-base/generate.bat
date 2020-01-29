@ECHO OFF

set PROJECT_DIR=%1
set BUILD_DIR=%2
set ASSETS_DIR=%3

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

mkdir %BUILD_DIR%\image_web_temp

echo Using %SOURCE_FLAG_FILE%

magick %SOURCE_FLAG_FILE% ^
    -adaptive-resize x128 ^
    %BUILD_DIR%\image_web_temp\flag.png
magick %ASSETS_DIR%\web.png %BUILD_DIR%\image_web_temp\flag.png ^
    -gravity south -geometry +0+64 -composite ^
    %BUILD_DIR%\image_web_temp\flag_final.png

mkdir %PROJECT_DIR%\src\main\play\listings\en-US\graphics\icon
move %BUILD_DIR%\image_web_temp\flag_final.png %PROJECT_DIR%\src\main\play\listings\en-US\graphics\icon\pack_store_icon.png

magick %SOURCE_FLAG_FILE% ^
    -adaptive-resize x256 ^
    %BUILD_DIR%\image_web_temp\flag.png
magick %ASSETS_DIR%\feature_graphics.png %BUILD_DIR%\image_web_temp\flag.png ^
    -gravity southeast -geometry +16+16 -composite ^
    %BUILD_DIR%\image_web_temp\pack_store_feature_graphics.png

mkdir %PROJECT_DIR%\src\main\play\listings\en-US\graphics\feature-graphic
move %BUILD_DIR%\image_web_temp\pack_store_feature_graphics.png %PROJECT_DIR%\src\main\play\listings\en-US\graphics\feature-graphic\pack_store_feature_graphics.png
