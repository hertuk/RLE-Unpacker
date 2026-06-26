# rle_decompress.py
#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import argparse
import os

# ANSI colors
COLORS = {
    'green': '\033[92m',
    'red': '\033[91m',
    'yellow': '\033[93m',
    'reset': '\033[0m'
}

def colorize(text, color):
    return f"{COLORS.get(color, '')}{text}{COLORS['reset']}"

ESCAPE = '\\'

def decompress(text):
    """Распаковка RLE."""
    if not text:
        return ''
    result = []
    i = 0
    n = len(text)
    while i < n:
        ch = text[i]
        if ch == ESCAPE:
            if i + 1 < n and text[i + 1] == ESCAPE:
                result.append(ESCAPE)
                i += 2
                continue
            if i + 1 >= n:
                raise ValueError("Unexpected end after escape")
            char = text[i + 1]
            i += 2
            num_str = ''
            while i < n and text[i].isdigit():
                num_str += text[i]
                i += 1
            if not num_str:
                raise ValueError("Missing number after escape")
            count = int(num_str)
            result.append(char * count)
        else:
            result.append(ch)
            i += 1
    return ''.join(result)

def read_input(filename):
    if not filename or filename == '-':
        return sys.stdin.read()
    with open(filename, 'r', encoding='utf-8') as f:
        return f.read()

def write_output(filename, content):
    if not filename or filename == '-':
        sys.stdout.write(content)
    else:
        with open(filename, 'w', encoding='utf-8') as f:
            f.write(content)

def main():
    parser = argparse.ArgumentParser(description="RLE Decompressor")
    parser.add_argument('input', help='Входной файл (или - для stdin)')
    parser.add_argument('output', nargs='?', help='Выходной файл (или - для stdout)')
    parser.add_argument('-v', '--verbose', action='store_true', help='Показать статистику')
    args = parser.parse_args()

    try:
        data = read_input(args.input)
    except Exception as e:
        sys.exit(colorize(f"Error reading input: {e}", 'red'))

    input_size = len(data.encode('utf-8'))

    try:
        result = decompress(data)
    except Exception as e:
        sys.exit(colorize(f"Decompression error: {e}", 'red'))

    output_size = len(result.encode('utf-8'))

    if args.verbose:
        ratio = output_size / input_size if input_size > 0 else 1
        print(colorize(f"Compressed size: {input_size} bytes", 'yellow'))
        print(colorize(f"Decompressed size: {output_size} bytes", 'yellow'))
        print(colorize(f"Expansion ratio: {ratio:.2f}x", 'green'))

    try:
        write_output(args.output, result)
        if args.output and args.output != '-':
            print(colorize(f"Result written to {args.output}", 'green'))
    except Exception as e:
        sys.exit(colorize(f"Error writing output: {e}", 'red'))

if __name__ == '__main__':
    main()
