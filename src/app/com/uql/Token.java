package com.uql;

import java.util.Objects;

public class Token {
    String value;
    TokenType type;

    public Token(String value, TokenType type) {
        this.value = value;
        this.type = type;
    }

    public static Token closingParen() {
        return new Token(")", TokenType.SEPARATOR);
    }

    public static Token openingParen() {
        return new Token("(", TokenType.SEPARATOR);
    }

    public static Token closingBracket() {
        return new Token("]", TokenType.SEPARATOR);
    }

    public static Token openingBracket() {
        return new Token("[", TokenType.SEPARATOR);
    }

    public static Token comparisonOpIn() {
        return new Token("IN:", TokenType.COMPARISON_OPERATOR);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Objects.equals(value, token.value) && type == token.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    @Override
    public String toString() {
        return type.name() + "(\"" + value + "\")";
    }
}
