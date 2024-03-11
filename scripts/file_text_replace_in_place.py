#!/usr/bin/env python3


import sys
import re


if __name__ == "__main__":
    file_path = sys.argv[1]
    regex_input = sys.argv[2]
    text = sys.argv[3]

    print(f"Replace '{regex_input}' with '{text}' in file {file_path}")

    with open(file_path, "r") as input:
        lines = input.readlines()
    
    output_lines = map(lambda l: re.sub(regex_input, text, l), lines)

    with open(file_path, "w") as output:
        output.writelines(output_lines)
