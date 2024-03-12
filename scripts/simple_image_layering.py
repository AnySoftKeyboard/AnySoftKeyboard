#!/usr/bin/env python3

import os
import shutil
from tempfile import mktemp
import tempfile


# ARGS
INPUT_1="ime/app/src/main/res/drawable-{{DIMEN}}/ic_inline_suggestions_reply.png"
INPUT_2="ime/app/src/main/res/drawable-{{DIMEN}}/ic_inline_suggestions_ai.png"
OUTPUT="ime/app/src/main/res/drawable-{{DIMEN}}/ic_inline_suggestions_ai_reply.png"
OFFSET_X=5
OFFSET_Y=3

SIZE_X=22
SIZE_Y=22

SIZE_2_X=12
SIZE_2_Y=12

# CONSTS
DIMENS=[ "mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"]
DIMENS_FACTORS=[1, 1.5, 2, 3, 4]

def _run_cmd(cmd):
    print(f"-- Running: {cmd}")
    output_stream = os.popen(cmd)
    print(f"  Output:\n{output_stream.read()}")


for i in range(0, len(DIMENS)):
    output_file=OUTPUT.replace("{{DIMEN}}", DIMENS[i])
    input_1=INPUT_1.replace("{{DIMEN}}", DIMENS[i])
    if not os.path.isfile(input_1):
        raise Exception(f"Could not find input file {input_1}.")
    input_2=INPUT_2.replace("{{DIMEN}}", DIMENS[i])
    if not os.path.isfile(input_2):
        raise Exception(f"Could not find input file {input_2}.")
    factor=DIMENS_FACTORS[i]
    input_2_offset=f"+{int(OFFSET_X*factor)}+{int(OFFSET_Y*factor)}"
    input_2_size=f"{int(SIZE_2_X*factor)}x{int(SIZE_2_Y*factor)}"

    output_size=f"{int(SIZE_X*factor)}x{int(SIZE_Y*factor)}"

    result_file=tempfile.mktemp(suffix=".png")
    temp_input_2=tempfile.mktemp(suffix=".png")

    _run_cmd(f"convert {input_2} -resize {input_2_size} {temp_input_2}")
    
    _run_cmd(f"convert -size {output_size} xc:transparent " \
            f"{input_1} -geometry +0+0 -composite " \
            f"{temp_input_2} -geometry {input_2_offset} -composite " \
            f"{result_file}")
    
    shutil.copyfile(result_file, output_file)
