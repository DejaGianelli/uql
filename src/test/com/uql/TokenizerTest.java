package com.uql;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Stack;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenizerTest {

    Stack<Token> expectedTokens;

    @ParameterizedTest
    @MethodSource("queries_source")
    void tokenize(String qsln, Stack<Token> expectedTokens) {

        this.expectedTokens = expectedTokens;

        Tokenizer tokenizer = new Tokenizer(qsln);
        tokenizer.tokenize();
        Stack<Token> generatedTokens = tokenizer.tokens;

        assertEquals(generatedTokens.size(), expectedTokens.size());
        assertTrue(hasSameTokens(generatedTokens));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "( city := :eq: \"são joão\" )", // the colon before eq is an invalid token
            "( city := @ eq: \"são joão\" )", // the @ before eq is an invalid token
    })
    void throw_error_on_invalid_token(String qsln) {
        assertThrows(UQLLexicalException.class, () -> {
            Tokenizer tokenizer = new Tokenizer(qsln);
            tokenizer.tokenize();
        });
    }

    boolean hasSameTokens(Stack<Token> generatedTokens) {
        String generated = String.join("", generatedTokens.stream()
                .map(Token::toString).toList());
        String expected = String.join("", expectedTokens.stream()
                .map(Token::toString).toList());
        return generated.equals(expected);
    }

    static Stream<Arguments> queries_source() {
        return Stream.of(
                Arguments.of(query_scenario_1()),
                Arguments.of(query_scenario_2()),
                Arguments.of(query_scenario_2_no_white_spaces()),
                Arguments.of(query_scenario_3()),
                Arguments.of(query_scenario_4()),
                Arguments.of(query_scenario_5()),
                Arguments.of(query_scenario_6()),
                Arguments.of(query_scenario_7()));
    }

    static Object[] query_scenario_7() {
        var query = "( number := gt: \"345\" )";
        var tokens = new Stack<>() {{
            add(new Token("(", TokenType.SEPARATOR));
            add(new Token("number", TokenType.IDENTIFIER));
            add(new Token(":=", TokenType.ASSIGN_OPERATOR));
            add(new Token("gt:", TokenType.COMPARISON_OPERATOR));
            add(new Token("\"345\"", TokenType.LITERAL));
            add(new Token(")", TokenType.SEPARATOR));
        }};
        return new Object[]{query, tokens};
    }

    static Object[] query_scenario_6() {
        var query = "( city := eq: \"São João\" ) || ( neighborhood := not: \"Centro\" )";
        var tokens = new Stack<>() {{
            add(new Token("(", TokenType.SEPARATOR));
            add(new Token("city", TokenType.IDENTIFIER));
            add(new Token(":=", TokenType.ASSIGN_OPERATOR));
            add(new Token("eq:", TokenType.COMPARISON_OPERATOR));
            add(new Token("\"São João\"", TokenType.LITERAL));
            add(new Token(")", TokenType.SEPARATOR));
            add(new Token("||", TokenType.LOGICAL_OPERATOR));
            add(new Token("(", TokenType.SEPARATOR));
            add(new Token("neighborhood", TokenType.IDENTIFIER));
            add(new Token(":=", TokenType.ASSIGN_OPERATOR));
            add(new Token("not:", TokenType.COMPARISON_OPERATOR));
            add(new Token("\"Centro\"", TokenType.LITERAL));
            add(new Token(")", TokenType.SEPARATOR));
        }};
        return new Object[]{query, tokens};
    }

    static Object[] query_scenario_5() {
        var query = "( city := eq: \"São João\" ) && ( neighborhood := not: \"Centro\" )";
        var tokens = new Stack<>() {{
            add(new Token("(", TokenType.SEPARATOR));
            add(new Token("city", TokenType.IDENTIFIER));
            add(new Token(":=", TokenType.ASSIGN_OPERATOR));
            add(new Token("eq:", TokenType.COMPARISON_OPERATOR));
            add(new Token("\"São João\"", TokenType.LITERAL));
            add(new Token(")", TokenType.SEPARATOR));
            add(new Token("&&", TokenType.LOGICAL_OPERATOR));
            add(new Token("(", TokenType.SEPARATOR));
            add(new Token("neighborhood", TokenType.IDENTIFIER));
            add(new Token(":=", TokenType.ASSIGN_OPERATOR));
            add(new Token("not:", TokenType.COMPARISON_OPERATOR));
            add(new Token("\"Centro\"", TokenType.LITERAL));
            add(new Token(")", TokenType.SEPARATOR));
        }};
        return new Object[]{query, tokens};
    }

    static Object[] query_scenario_4() {
        var query = "( city := eq: \"São João\" )";
        var tokens = new Stack<>() {{
            add(new Token("(", TokenType.SEPARATOR));
            add(new Token("city", TokenType.IDENTIFIER));
            add(new Token(":=", TokenType.ASSIGN_OPERATOR));
            add(new Token("eq:", TokenType.COMPARISON_OPERATOR));
            add(new Token("\"São João\"", TokenType.LITERAL));
            add(new Token(")", TokenType.SEPARATOR));
        }};
        return new Object[]{query, tokens};
    }

    static Object[] query_scenario_3() {
        var query = ":=";
        var tokens = new Stack<>() {{
            add(new Token(":=", TokenType.ASSIGN_OPERATOR));
        }};
        return new Object[]{query, tokens};
    }

    static Object[] query_scenario_2_no_white_spaces() {
        var query = "city:=IN:[\"São João\",234]";
        return new Object[]{query, query_scenario_2_tokens()};
    }

    static Object[] query_scenario_2() {
        var query = "city := IN: [ \"São João\", 234 ]";
        return new Object[]{query, query_scenario_2_tokens()};
    }

    static Stack<Token> query_scenario_2_tokens() {
        return new Stack<>() {{
            add(new Token("city", TokenType.IDENTIFIER));
            add(new Token(":=", TokenType.ASSIGN_OPERATOR));
            add(new Token("IN:", TokenType.COMPARISON_OPERATOR));
            add(new Token("[", TokenType.SEPARATOR));
            add(new Token("\"São João\"", TokenType.LITERAL));
            add(new Token(",", TokenType.SEPARATOR));
            add(new Token("234", TokenType.LITERAL));
            add(new Token("]", TokenType.SEPARATOR));
        }};
    }

    static Object[] query_scenario_1() {
        var query = "city-district := eq:";
        var tokens = new Stack<Token>() {{
            add(new Token("city-district", TokenType.IDENTIFIER));
            add(new Token(":=", TokenType.ASSIGN_OPERATOR));
            add(new Token("eq:", TokenType.COMPARISON_OPERATOR));
        }};
        return new Object[]{query, tokens};
    }
}
