// rle_decompress.java
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class rle_decompress {
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[92m";
    private static final String RED = "\u001B[91m";
    private static final String YELLOW = "\u001B[93m";

    private static String colorize(String text, String color) {
        return color + text + RESET;
    }

    private static final char ESCAPE = '\\';

    private static String decompress(String text) throws Exception {
        if (text == null || text.isEmpty()) return "";
        StringBuilder result = new StringBuilder();
        int i = 0, n = text.length();
        while (i < n) {
            char ch = text.charAt(i);
            if (ch == ESCAPE) {
                if (i + 1 < n && text.charAt(i + 1) == ESCAPE) {
                    result.append(ESCAPE);
                    i += 2;
                    continue;
                }
                if (i + 1 >= n) throw new Exception("Unexpected end after escape");
                char repeatChar = text.charAt(i + 1);
                i += 2;
                StringBuilder numStr = new StringBuilder();
                while (i < n && Character.isDigit(text.charAt(i))) {
                    numStr.append(text.charAt(i));
                    i++;
                }
                if (numStr.length() == 0) throw new Exception("Missing number after escape");
                int count = Integer.parseInt(numStr.toString());
                for (int k = 0; k < count; k++) result.append(repeatChar);
            } else {
                result.append(ch);
                i++;
            }
        }
        return result.toString();
    }

    private static String readInput(String filename) throws IOException {
        if (filename == null || filename.equals("-")) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        }
        return new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
    }

    private static void writeOutput(String filename, String content) throws IOException {
        if (filename == null || filename.equals("-")) {
            System.out.print(content);
        } else {
            Files.write(Paths.get(filename), content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void main(String[] args) {
        List<String> argList = Arrays.asList(args);
        String inputFile = null, outputFile = null;
        boolean verbose = false;
        for (String arg : argList) {
            if (arg.equals("-v") || arg.equals("--verbose")) {
                verbose = true;
            } else if (inputFile == null) {
                inputFile = arg;
            } else {
                outputFile = arg;
            }
        }
        if (inputFile == null) {
            System.out.println(colorize("Usage: java rle_decompress <input> [output] [-v]", YELLOW));
            return;
        }

        String data;
        try {
            data = readInput(inputFile);
        } catch (Exception e) {
            System.err.println(colorize("Error reading input: " + e.getMessage(), RED));
            return;
        }
        int inputSize = data.getBytes(StandardCharsets.UTF_8).length;

        String result;
        try {
            result = decompress(data);
        } catch (Exception e) {
            System.err.println(colorize("Decompression error: " + e.getMessage(), RED));
            return;
        }
        int outputSize = result.getBytes(StandardCharsets.UTF_8).length;

        if (verbose) {
            double ratio = inputSize > 0 ? (double) outputSize / inputSize : 1.0;
            System.out.println(colorize("Compressed size: " + inputSize + " bytes", YELLOW));
            System.out.println(colorize("Decompressed size: " + outputSize + " bytes", YELLOW));
            System.out.println(colorize(String.format("Expansion ratio: %.2fx", ratio), GREEN));
        }

        try {
            writeOutput(outputFile, result);
            if (outputFile != null && !outputFile.equals("-")) {
                System.out.println(colorize("Result written to " + outputFile, GREEN));
            }
        } catch (Exception e) {
            System.err.println(colorize("Error writing output: " + e.getMessage(), RED));
        }
    }
}
