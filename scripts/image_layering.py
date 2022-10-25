#!/usr/bin/env python3

import os
import shutil
import sys
from tempfile import mktemp
import tempfile


RES_FOLDER=sys.argv[1]
OUTPUT_RES_FOLDER=sys.argv[2]
IMAGE_FILE=sys.argv[3]

DIMENS=[ "mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"]
SIZE_X=22
SIZE_Y=22
DIMENS_FACTORS=[1, 1.5, 2, 3, 4]
OFFSET_X=12
OFFSET_Y=1

def _run_cmd(cmd):
    print(f"-- Running: {cmd}")
    output_stream = os.popen(cmd)
    print(f"  Output:\n{output_stream.read()}")

def _ask_script_border(input_image) -> str:
    tmp_file=tempfile.mktemp(suffix=".png")
    _run_cmd(f"scripts/add_border_to_png.sh {input_image} black 1 {tmp_file}")

    return tmp_file
    
def _canny_edge_image(input_image) -> str:
    tmp_file=tempfile.mktemp(suffix=".png")
    _run_cmd(f"convert {input_image} -canny 0x0+10%+10% {tmp_file}")
    tmp_file2=tempfile.mktemp(suffix=".png")
    _run_cmd(f"convert {tmp_file} -transparent black {tmp_file2}")
    tmp_file3=tempfile.mktemp(suffix=".png")
    _run_cmd(f"convert {tmp_file2} -fill black -opaque white {tmp_file3}")

    return tmp_file3

def _edge_border(input_image) -> str:
    tmp_file=tempfile.mktemp(suffix=".png")
    # https://legacy.imagemagick.org/discourse-server/viewtopic.php?p=59776#p59776
    _run_cmd(f"convert {input_image} -bordercolor none -border 0x0 -background transparent "
             f"-channel A -blur 2x2 -level 10%,10% "
             f"{tmp_file}")

    return tmp_file

def _edge_image(input_image) -> str:
    return _ask_script_border(input_image)

for i in range(0, len(DIMENS) - 1):
    result_file=""
    input=f"{RES_FOLDER}/drawable-{DIMENS[i]}/{IMAGE_FILE}.png"
    
    if os.path.exists(input):
        result_file=_edge_image(input)
    else:
        factor=DIMENS_FACTORS[i]
        output_size=f"{int(SIZE_X*factor)}x{int(SIZE_Y*factor)}"
        input_1=f"{RES_FOLDER}/drawable-{DIMENS[i]}/{IMAGE_FILE}_1.png"
        input_2=f"{RES_FOLDER}/drawable-{DIMENS[i]}/{IMAGE_FILE}_2.png"
        input_2_offset=f"+{int(OFFSET_X*factor)}+{int(OFFSET_Y*factor)}"

        input1_outline=_edge_image(input_1)
        input2_outline=_edge_image(input_2)

        result_file=tempfile.mktemp(suffix=".png")
        _run_cmd(f"convert -size {output_size} xc:transparent " \
                f"{input1_outline} -geometry +0+0 -composite " \
                f"{input_1} -geometry +0+0 -composite " \
                f"{input2_outline} -geometry {input_2_offset} -composite " \
                f"{input_2} -geometry {input_2_offset} -composite " \
                f"{result_file}")
    
    shutil.copyfile(result_file, f"{OUTPUT_RES_FOLDER}/drawable-{DIMENS[i]}/{IMAGE_FILE}.png")
