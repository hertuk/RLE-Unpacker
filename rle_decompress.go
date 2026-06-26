// rle_decompress.go
package main

import (
	"bufio"
	"errors"
	"flag"
	"fmt"
	"io"
	"os"
	"strconv"
	"strings"
)

const (
	reset  = "\033[0m"
	green  = "\033[92m"
	red    = "\033[91m"
	yellow = "\033[93m"
)

func colorize(text, color string) string {
	return color + text + reset
}

const ESCAPE = '\\'

func decompress(text string) (string, error) {
	if text == "" {
		return "", nil
	}
	var result strings.Builder
	n := len(text)
	i := 0
	for i < n {
		ch := text[i]
		if ch == ESCAPE {
			if i+1 < n && text[i+1] == ESCAPE {
				result.WriteByte(ESCAPE)
				i += 2
				continue
			}
			if i+1 >= n {
				return "", errors.New("unexpected end after escape")
			}
			repeatChar := text[i+1]
			i += 2
			numStr := ""
			for i < n && text[i] >= '0' && text[i] <= '9' {
				numStr += string(text[i])
				i++
			}
			if numStr == "" {
				return "", errors.New("missing number after escape")
			}
			count, err := strconv.Atoi(numStr)
			if err != nil {
				return "", err
			}
			for k := 0; k < count; k++ {
				result.WriteByte(repeatChar)
			}
		} else {
			result.WriteByte(ch)
			i++
		}
	}
	return result.String(), nil
}

func readInput(filename string) (string, error) {
	if filename == "-" || filename == "" {
		reader := bufio.NewReader(os.Stdin)
		var builder strings.Builder
		for {
			line, err := reader.ReadString('\n')
			if err != nil && err != io.EOF {
				return "", err
			}
			builder.WriteString(line)
			if err == io.EOF {
				break
			}
		}
		return builder.String(), nil
	}
	data, err := os.ReadFile(filename)
	if err != nil {
		return "", err
	}
	return string(data), nil
}

func writeOutput(filename string, content string) error {
	if filename == "-" || filename == "" {
		fmt.Print(content)
		return nil
	}
	return os.WriteFile(filename, []byte(content), 0644)
}

func main() {
	verbose := flag.Bool("v", false, "show statistics")
	flag.Usage = func() {
		fmt.Println(colorize("Usage: rle_decompress <input> [output] [-v]", yellow))
	}
	flag.Parse()
	args := flag.Args()
	if len(args) < 1 {
		flag.Usage()
		os.Exit(1)
	}
	inputFile := args[0]
	outputFile := ""
	if len(args) >= 2 {
		outputFile = args[1]
	}

	data, err := readInput(inputFile)
	if err != nil {
		fmt.Println(colorize("Error reading input: "+err.Error(), red))
		os.Exit(1)
	}
	inputSize := len(data)

	result, err := decompress(data)
	if err != nil {
		fmt.Println(colorize("Decompression error: "+err.Error(), red))
		os.Exit(1)
	}
	outputSize := len(result)

	if *verbose {
		ratio := float64(outputSize) / float64(inputSize)
		if inputSize == 0 {
			ratio = 1
		}
		fmt.Println(colorize(fmt.Sprintf("Compressed size: %d bytes", inputSize), yellow))
		fmt.Println(colorize(fmt.Sprintf("Decompressed size: %d bytes", outputSize), yellow))
		fmt.Println(colorize(fmt.Sprintf("Expansion ratio: %.2fx", ratio), green))
	}

	if err := writeOutput(outputFile, result); err != nil {
		fmt.Println(colorize("Error writing output: "+err.Error(), red))
		os.Exit(1)
	}
	if outputFile != "" && outputFile != "-" {
		fmt.Println(colorize("Result written to "+outputFile, green))
	}
}
