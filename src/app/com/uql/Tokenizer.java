package com.uql;

import java.util.Arrays;
import java.util.Stack;

import static com.uql.TokenType.COMPARISON_OPERATOR;
import static com.uql.TokenType.IDENTIFIER;
import static com.uql.TokenType.LITERAL;
import static com.uql.TokenType.LOGICAL_OPERATOR;
import static com.uql.TokenType.SEPARATOR;
import static java.lang.String.valueOf;


public class Tokenizer {

    int lCursor = 0;
    int rCursor = 0;
    char[] chars;
    Stack<Token> tokens = new Stack<>();

    public Tokenizer(String query) {
        chars = query.toCharArray();
        chars = Arrays.copyOf(chars, chars.length + 1);
        chars[chars.length - 1] = 0x00; // null byte
    }

    /**
     * This method starts from the current char and tries to match a token. In case of fail, it calls itself recursively
     * attempting for the next token type and so on. If no token is match, it checks for the presence of invalid
     * character and throw an error.
     *
     */
    public void tokenize() {
        if (isEndOfStream()) {
            return;
        }
        skipIgnorableChars();

        if (tokenizeLogicalOperator(currentChar())) {
            tokenize();
            return;
        }
        if (tokenizeCompOperator(currentChar())) {
            tokenize();
            return;
        }
        if (tokenizeCompOperatorIn(currentChar())) {
            tokenize();
            return;
        }
        if (tokenizeFilterKey(currentChar())) {
            tokenize();
            return;
        }
        if (tokenizeLiteral(currentChar(), currentChar(), 0)) {
            tokenize();
            return;
        }
        if (tokenizeOpParen(currentChar())) {
            tokenize();
            return;
        }
        if (tokenizeClParen(currentChar())) {
            tokenize();
            return;
        }
        if (tokenizeOpenBrackets(currentChar())) {
            tokenize();
            return;
        }
        if (tokenizeClosingBrackets(currentChar())) {
            tokenize();
            return;
        }
        if (tokenizeComma(currentChar())) {
            tokenize();
            return;
        }

        // If no token is matched, check for invalid character that the user
        // may have typed
        checkInvalidToken(currentChar());

        skipIgnorableChars();

        tokenize();
    }

    private void checkInvalidToken(char c) {
        var ex = new UQLLexicalException("Invalid character '" + c + "' " +
                "at column " + rCursor + 1);
        if (c == 0x40) { //@
            throw ex;
        }
        if (c == 0x3A) { //:
            throw ex;
        }
    }

    private boolean tokenizeCompOperatorIn(char c) {
        int pos = rCursor;
        int i = 0;
        //variable to store next two chars initialized with null bytes
        char[] opChars = new char[]{c, 0x00, 0x00};
        while (pos < chars.length && i < 3) {
            opChars[i] = chars[pos];
            i++;
            pos++;
        }
        char I = 0x49;
        char N = 0x4E;
        char comma = 0x3A;
        if (opChars[0] == I && opChars[1] == N && opChars[2] == comma) {
            tokens.add(new Token(valueOf(opChars), COMPARISON_OPERATOR));
            advanceRCur(3);
            mvLCursorToR();
            return true;
        }
        return false;
    }

    private boolean tokenizeLogicalOperator(char c) {
        if (c != 0x26 && c != 0x7C) {
            return false;
        }
        char[] opChars = new char[]{c, 0x00};
        int nextIndex = rCursor + 1;
        int lastPos = chars.length - 1;
        if (nextIndex < lastPos) {
            var next = chars[nextIndex];
            opChars[1] = next;
            if (opChars[0] == 0x26 && opChars[1] == 0x26) { // &&
                tokens.add(new Token(valueOf(opChars), LOGICAL_OPERATOR));
                advanceRCur(2);
                mvLCursorToR();
                return true;
            } else if (opChars[0] == 0x7C && opChars[1] == 0x7C) { // ||
                tokens.add(new Token(valueOf(opChars), LOGICAL_OPERATOR));
                advanceRCur(2);
                mvLCursorToR();
                return true;
            }
        }
        return false;
    }

    private void skipIgnorableChars() {
        while (isSkippable()) {
            advanceRCur();
            mvLCursorToR();
        }
    }

    private boolean isSkippable() {
        return currentChar() == 0x20 // Space
                || currentChar() == 0x0D // Carriage Return
                || currentChar() == 0x0A; // Line Feed
    }

    private boolean isBlankSpace() {
        return currentChar() == 0x20;
    }

    private boolean tokenizeComma(char c) {
        if (c == 0x2C) {
            tokens.add(new Token(getCursorValue(), SEPARATOR));
            advanceRCur();
            mvLCursorToR();
            return true;
        }
        return false;
    }

    private boolean tokenizeOpenBrackets(char c) {
        if (c == 0x5B) {
            tokens.add(new Token(getCursorValue(), SEPARATOR));
            advanceRCur();
            mvLCursorToR();
            return true;
        }
        return false;
    }

    private boolean tokenizeClosingBrackets(char c) {
        if (c == 0x5D) { // Closing Brackets
            tokens.add(new Token(getCursorValue(), SEPARATOR));
            advanceRCur();
            mvLCursorToR();
            return true;
        }
        return false;
    }

    private boolean tokenizeClParen(char c) {
        if (c == 0x29) { // Closing Parenthesis
            tokens.add(new Token(getCursorValue(), SEPARATOR));
            advanceRCur();
            mvLCursorToR();
            return true;
        }
        return false;
    }

    private boolean tokenizeOpParen(char c) {
        if (c == 0x28) { // Open Parenthesis
            tokens.add(new Token(getCursorValue(), SEPARATOR));
            advanceRCur();
            mvLCursorToR();
            return true;
        }
        return false;
    }

    private boolean tokenizeLiteral(char firstChar, char c, int pos) {
        if (isEndOfStream()) {
            return false;
        }
        if (isNumber(firstChar)) {
            if (isNumber(c)) {
                advanceRCur();
                boolean number = isNumber(currentChar());
                if (!number) {
                    backwardRCur();
                    tokens.add(new Token(getCursorValue(), LITERAL));
                    advanceRCur();
                    mvLCursorToR();
                    return true;
                }
                tokenizeLiteral(firstChar, currentChar(), ++pos);
            }
        } else {
            if (c != 0x22 && pos == 0) { // Opening Quotes
                return false;
            }
            if (c == 0x22 && pos > 0) { // Closing Quotes
                tokens.add(new Token(getCursorValue(), LITERAL));
                advanceRCur();
                mvLCursorToR();
                return true;
            }
            advanceRCur();
            tokenizeLiteral(firstChar, currentChar(), ++pos);
        }
        return false;
    }

    private boolean tokenizeFilterKey(char c) {
        if (isNumber(c)) { //filter key must not start with number
            return false;
        }
        boolean isKeyChar = isKeyChar(c);
        if (!isKeyChar) {
            return false;
        }
        while (isKeyChar && !isEndOfStream() && !isBlankSpace()) {
            advanceRCur();
            isKeyChar = isKeyChar(currentChar());
        }
        backwardRCur();
        String val = getCursorValue();
        if (!val.isEmpty()) {
            tokens.add(new Token(val, IDENTIFIER));
        }
        advanceRCur();
        mvLCursorToR();
        return true;
    }

    private boolean tokenizeCompOperator(char c) {
        int pos = rCursor;
        int i = 0;
        //variable to store next three chars initialized with null bytes
        char[] opChars = new char[]{c, 0x00, 0x00, 0x00};
        while (pos < chars.length && i < 4) {
            opChars[i] = chars[pos];
            i++;
            pos++;
        }
        char g = 0x67;
        char t = 0x74;
        char e = 0x65;
        char l = 0x6c;
        char n = 0x6e;
        char o = 0x6f;
        char q = 0x71;
        char comma = 0x3A;
        if (opChars[0] == g && opChars[1] == t && opChars[2] == e && opChars[3] == comma) {
            //gte:
            tokens.add(new Token(valueOf(opChars), COMPARISON_OPERATOR));
            advanceRCur(4);
            mvLCursorToR();
            return true;
        } else if (opChars[0] == l && opChars[1] == t && opChars[2] == e && opChars[3] == comma) {
            //lte:
            tokens.add(new Token(valueOf(opChars), COMPARISON_OPERATOR));
            advanceRCur(4);
            mvLCursorToR();
            return true;
        } else if (opChars[0] == n && opChars[1] == o && opChars[2] == t && opChars[3] == comma) {
            //not:
            tokens.add(new Token(valueOf(opChars), COMPARISON_OPERATOR));
            advanceRCur(4);
            mvLCursorToR();
            return true;
        } else if (opChars[0] == e && opChars[1] == q && opChars[2] == comma) {
            //eq:
            tokens.add(new Token(valueOf(new char[]{opChars[0], opChars[1], opChars[2]}),
                    COMPARISON_OPERATOR));
            advanceRCur(3);
            mvLCursorToR();
            return true;
        } else if (opChars[0] == g && opChars[1] == t && opChars[2] == comma) {
            //gt:
            tokens.add(new Token(valueOf(new char[]{opChars[0], opChars[1], opChars[2]}),
                    COMPARISON_OPERATOR));
            advanceRCur(3);
            mvLCursorToR();
            return true;
        } else if (opChars[0] == l && opChars[1] == t && opChars[2] == comma) {
            //lt:
            tokens.add(new Token(valueOf(new char[]{opChars[0], opChars[1], opChars[2]}),
                    COMPARISON_OPERATOR));
            advanceRCur(3);
            mvLCursorToR();
            return true;
        }

        return false;
    }

    private boolean isEndOfStream() {
        return currentChar() == 0x00;
    }

    private boolean isKeyChar(char c) {
        return (c == 0x2D) // Minus
                || (c == 0x2E) // Dot
                || (c == 0x5F) // Underscore
                || (c >= 0x30 && c <= 0x39) // 0 - 9
                || (c >= 0x41 && c <= 0x5A) // A - Z
                || (c >= 0x61 && c <= 0x7A); // a - z
    }

    private boolean isNumber(char c) {
        return (c >= 0x30 && c <= 0x39); // 0 - 9
    }

    private char currentChar() {
        return chars[rCursor];
    }

    private void advanceRCur() {
        rCursor++;
    }

    private void advanceRCur(int count) {
        rCursor = rCursor + count;
    }

    private void backwardRCur() {
        rCursor--;
    }

    private String getCursorValue() {
        return new String(Arrays.copyOfRange(chars, lCursor, rCursor + 1));
    }

    private void mvLCursorToR() {
        lCursor = rCursor;
    }
}
