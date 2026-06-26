// rle_decompress.cs
using System;
using System.IO;
using System.Text;
using System.Collections.Generic;

class RLEDecompress
{
    static string Colorize(string text, string color)
    {
        string col = color switch
        {
            "green" => "\x1b[92m",
            "red" => "\x1b[91m",
            "yellow" => "\x1b[93m",
            _ => "\x1b[0m"
        };
        return col + text + "\x1b[0m";
    }

    const char ESCAPE = '\\';

    static string Decompress(string text)
    {
        if (string.IsNullOrEmpty(text)) return "";
        var result = new StringBuilder();
        int i = 0, n = text.Length;
        while (i < n)
        {
            char ch = text[i];
            if (ch == ESCAPE)
            {
                if (i + 1 < n && text[i + 1] == ESCAPE)
                {
                    result.Append(ESCAPE);
                    i += 2;
                    continue;
                }
                if (i + 1 >= n) throw new Exception("Unexpected end after escape");
                char repeatChar = text[i + 1];
                i += 2;
                string numStr = "";
                while (i < n && char.IsDigit(text[i]))
                {
                    numStr += text[i];
                    i++;
                }
                if (string.IsNullOrEmpty(numStr)) throw new Exception("Missing number after escape");
                int count = int.Parse(numStr);
                result.Append(repeatChar, count);
            }
            else
            {
                result.Append(ch);
                i++;
            }
        }
        return result.ToString();
    }

    static string ReadInput(string filename)
    {
        if (string.IsNullOrEmpty(filename) || filename == "-")
        {
            return Console.In.ReadToEnd();
        }
        return File.ReadAllText(filename, Encoding.UTF8);
    }

    static void WriteOutput(string filename, string content)
    {
        if (string.IsNullOrEmpty(filename) || filename == "-")
        {
            Console.Write(content);
        }
        else
        {
            File.WriteAllText(filename, content, Encoding.UTF8);
        }
    }

    static void Main(string[] args)
    {
        string inputFile = null, outputFile = null;
        bool verbose = false;
        for (int i = 0; i < args.Length; i++)
        {
            if (args[i] == "-v" || args[i] == "--verbose")
            {
                verbose = true;
            }
            else if (inputFile == null)
            {
                inputFile = args[i];
            }
            else
            {
                outputFile = args[i];
            }
        }
        if (inputFile == null)
        {
            Console.WriteLine(Colorize("Usage: rle_decompress <input> [output] [-v]", "yellow"));
            return;
        }

        string data;
        try
        {
            data = ReadInput(inputFile);
        }
        catch (Exception e)
        {
            Console.WriteLine(Colorize("Error reading input: " + e.Message, "red"));
            return;
        }
        int inputSize = Encoding.UTF8.GetByteCount(data);

        string result;
        try
        {
            result = Decompress(data);
        }
        catch (Exception e)
        {
            Console.WriteLine(Colorize("Decompression error: " + e.Message, "red"));
            return;
        }
        int outputSize = Encoding.UTF8.GetByteCount(result);

        if (verbose)
        {
            double ratio = inputSize > 0 ? (double)outputSize / inputSize : 1.0;
            Console.WriteLine(Colorize($"Compressed size: {inputSize} bytes", "yellow"));
            Console.WriteLine(Colorize($"Decompressed size: {outputSize} bytes", "yellow"));
            Console.WriteLine(Colorize($"Expansion ratio: {ratio:F2}x", "green"));
        }

        try
        {
            WriteOutput(outputFile, result);
            if (!string.IsNullOrEmpty(outputFile) && outputFile != "-")
            {
                Console.WriteLine(Colorize("Result written to " + outputFile, "green"));
            }
        }
        catch (Exception e)
        {
            Console.WriteLine(Colorize("Error writing output: " + e.Message, "red"));
        }
    }
}
