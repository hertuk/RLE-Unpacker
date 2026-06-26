// rle_decompress.js
#!/usr/bin/env node
'use strict';

const fs = require('fs');
const process = require('process');

const COLORS = {
    green: '\x1b[92m',
    red: '\x1b[91m',
    yellow: '\x1b[93m',
    reset: '\x1b[0m'
};

function colorize(text, color) {
    return COLORS[color] + text + COLORS.reset;
}

const ESCAPE = '\\';

function decompress(text) {
    if (!text) return '';
    const result = [];
    let i = 0, n = text.length;
    while (i < n) {
        const ch = text[i];
        if (ch === ESCAPE) {
            if (i + 1 < n && text[i + 1] === ESCAPE) {
                result.push(ESCAPE);
                i += 2;
                continue;
            }
            if (i + 1 >= n) throw new Error('Unexpected end after escape');
            const repeatChar = text[i + 1];
            i += 2;
            let numStr = '';
            while (i < n && text[i] >= '0' && text[i] <= '9') {
                numStr += text[i];
                i++;
            }
            if (!numStr) throw new Error('Missing number after escape');
            const count = parseInt(numStr, 10);
            for (let k = 0; k < count; k++) result.push(repeatChar);
        } else {
            result.push(ch);
            i++;
        }
    }
    return result.join('');
}

function readInput(filename) {
    if (!filename || filename === '-') {
        return fs.readFileSync(0, 'utf-8');
    }
    return fs.readFileSync(filename, 'utf-8');
}

function writeOutput(filename, content) {
    if (!filename || filename === '-') {
        process.stdout.write(content);
    } else {
        fs.writeFileSync(filename, content, 'utf-8');
    }
}

function main() {
    const args = process.argv.slice(2);
    let inputFile = null, outputFile = null, verbose = false;
    for (let i = 0; i < args.length; i++) {
        if (args[i] === '-v' || args[i] === '--verbose') {
            verbose = true;
        } else if (!inputFile) {
            inputFile = args[i];
        } else {
            outputFile = args[i];
        }
    }
    if (!inputFile) {
        console.log(colorize('Usage: node rle_decompress.js <input> [output] [-v]', 'yellow'));
        process.exit(1);
    }

    let data;
    try {
        data = readInput(inputFile);
    } catch (err) {
        console.log(colorize('Error reading input: ' + err.message, 'red'));
        process.exit(1);
    }
    const inputSize = Buffer.byteLength(data, 'utf8');

    let result;
    try {
        result = decompress(data);
    } catch (err) {
        console.log(colorize('Decompression error: ' + err.message, 'red'));
        process.exit(1);
    }
    const outputSize = Buffer.byteLength(result, 'utf8');

    if (verbose) {
        const ratio = inputSize ? outputSize / inputSize : 1;
        console.log(colorize(`Compressed size: ${inputSize} bytes`, 'yellow'));
        console.log(colorize(`Decompressed size: ${outputSize} bytes`, 'yellow'));
        console.log(colorize(`Expansion ratio: ${ratio.toFixed(2)}x`, 'green'));
    }

    try {
        writeOutput(outputFile, result);
        if (outputFile && outputFile !== '-') {
            console.log(colorize('Result written to ' + outputFile, 'green'));
        }
    } catch (err) {
        console.log(colorize('Error writing output: ' + err.message, 'red'));
        process.exit(1);
    }
}

main();
