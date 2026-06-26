// rle_decompress.cpp
#include <iostream>
#include <fstream>
#include <string>
#include <sstream>
#include <cctype>
#include <stdexcept>
#include <vector>

using namespace std;

const string RESET = "\033[0m";
const string GREEN = "\033[92m";
const string RED = "\033[91m";
const string YELLOW = "\033[93m";

string colorize(const string& text, const string& color) {
    return color + text + RESET;
}

const char ESCAPE = '\\';

string decompress(const string& text) {
    if (text.empty()) return "";
    ostringstream result;
    size_t i = 0, n = text.size();
    while (i < n) {
        char ch = text[i];
        if (ch == ESCAPE) {
            if (i + 1 < n && text[i + 1] == ESCAPE) {
                result << ESCAPE;
                i += 2;
                continue;
            }
            if (i + 1 >= n) throw runtime_error("Unexpected end after escape");
            char repeat_char = text[i + 1];
            i += 2;
            string num_str;
            while (i < n && isdigit(text[i])) {
                num_str += text[i];
                ++i;
            }
            if (num_str.empty()) throw runtime_error("Missing number after escape");
            int count = stoi(num_str);
            result << string(count, repeat_char);
        } else {
            result << ch;
            ++i;
        }
    }
    return result.str();
}

string readFile(const string& filename) {
    if (filename == "-" || filename.empty()) {
        stringstream buffer;
        buffer << cin.rdbuf();
        return buffer.str();
    }
    ifstream f(filename);
    if (!f) throw runtime_error("Cannot open file: " + filename);
    stringstream buffer;
    buffer << f.rdbuf();
    return buffer.str();
}

void writeFile(const string& filename, const string& content) {
    if (filename == "-" || filename.empty()) {
        cout << content;
    } else {
        ofstream f(filename);
        if (!f) throw runtime_error("Cannot write file: " + filename);
        f << content;
    }
}

int main(int argc, char* argv[]) {
    vector<string> args;
    for (int i = 1; i < argc; ++i) args.push_back(argv[i]);
    if (args.empty()) {
        cout << colorize("Usage: rle_decompress <input> [output] [-v]", YELLOW) << endl;
        return 1;
    }
    string inputFile, outputFile;
    bool verbose = false;
    for (const string& arg : args) {
        if (arg == "-v" || arg == "--verbose") {
            verbose = true;
        } else if (inputFile.empty()) {
            inputFile = arg;
        } else {
            outputFile = arg;
        }
    }
    if (inputFile.empty()) {
        cout << colorize("Error: input file not specified", RED) << endl;
        return 1;
    }

    string data;
    try {
        data = readFile(inputFile);
    } catch (const exception& e) {
        cout << colorize("Error reading input: " + string(e.what()), RED) << endl;
        return 1;
    }
    size_t inputSize = data.size();

    string result;
    try {
        result = decompress(data);
    } catch (const exception& e) {
        cout << colorize("Decompression error: " + string(e.what()), RED) << endl;
        return 1;
    }
    size_t outputSize = result.size();

    if (verbose) {
        double ratio = (inputSize > 0) ? (double)outputSize / inputSize : 1.0;
        cout << colorize("Compressed size: " + to_string(inputSize) + " bytes", YELLOW) << endl;
        cout << colorize("Decompressed size: " + to_string(outputSize) + " bytes", YELLOW) << endl;
        cout << colorize("Expansion ratio: " + to_string(ratio) + "x", GREEN) << endl;
    }

    try {
        writeFile(outputFile, result);
        if (!outputFile.empty() && outputFile != "-") {
            cout << colorize("Result written to " + outputFile, GREEN) << endl;
        }
    } catch (const exception& e) {
        cout << colorize("Error writing output: " + string(e.what()), RED) << endl;
        return 1;
    }
    return 0;
}
