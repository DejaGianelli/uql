package com.uql;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.uql.TokenType.COMPARISON_OPERATOR;
import static com.uql.TokenType.EOF;
import static com.uql.TokenType.IDENTIFIER;
import static com.uql.TokenType.LITERAL;
import static com.uql.TokenType.LOGICAL_OPERATOR;

public class Parser {

    int pos;
    List<Token> tokens;
    Token currentToken;

    public Parser(List<Token> tokens) {
        if (tokens.isEmpty()) {
            throw new RuntimeException("You must provide a list of tokens");
        }
        this.tokens = tokens;
        this.currentToken = tokens.getFirst();
        this.pos = 0;
    }

    public Node parse() {
        return uqlExpr();
    }

    void advance() {
        pos++;
        if (pos < tokens.size()) {
            currentToken = tokens.get(pos);
        } else {
            currentToken = new Token("NULL", EOF);
        }
    }

    Token peekNext() {
        return tokens.get(pos + 1);
    }

    Node uqlExpr() {
        Node node = new Node("UQL_EXPR");
        Stack<Token> parenthesis = new Stack<>();

        while (peekNext().equals(Token.openingParen())) {
            node.addFirst(new Node(currentToken.type.name(),
                    currentToken.value));
            parenthesis.add(currentToken);
            advance();
        }

        Node filtersNode = filters(new Stack<>());
        node.addLast(filtersNode);

        if (currentToken.equals(Token.closingParen())) {
            parenthesis.add(currentToken);
            node.addLast(new Node(currentToken.type.name(),
                    currentToken.value));
            advance();
        }

        checkMatchingParenthesis(parenthesis);

        return node;
    }

    void checkMatchingParenthesis(Stack<Token> parenthesis) {
        if (parenthesis.size() % 2 != 0) {
            while (parenthesis.size() > 1) {
                parenthesis.pop();
            }
            if (parenthesis.pop().equals(Token.closingParen())) {
                throw new UQLParseException("Expected '(' at the beginning of the" +
                        " expression");
            } else {
                throw new UQLParseException("Expected ')' at the end of the" +
                        " expression");
            }
        }
    }

    Node filters(Stack<Token> parenthesis) {
        Node node = new Node("FILTERS_NODE");

        if (currentToken.equals(Token.openingParen())) {
            node.addFirst(new Node(currentToken.type.name(),
                    currentToken.value));
            parenthesis.add(currentToken);
            advance();
        }

        Node filterNode = filter();
        node.addLast(filterNode);

        if (currentToken.equals(Token.closingParen())) {
            node.addLast(new Node(currentToken.type.name(),
                    currentToken.value));
            parenthesis.add(currentToken);
            advance();
        }

        if (currentToken.type == LOGICAL_OPERATOR) {
            node.addLast(new Node("LOGICAL_OPERATOR", currentToken.value));
            advance();
            Node filtersNode = filters(parenthesis);
            node.addLast(filtersNode);
        }

        checkMatchingParenthesis(parenthesis);

        return node;
    }

    Node filter() {
        Node filterNode = new Node("FILTER_NODE");

        Node keyNode = key();
        Node compExprNode = comparisonExpr();

        filterNode.addLast(keyNode);
        filterNode.addLast(compExprNode);
        return filterNode;
    }

    Node key() {
        if (currentToken.type == EOF) {
            return new Node("EOF", "NULL");
        }
        if (currentToken.type != IDENTIFIER) {
            throw new UQLParseException("Expect identifier, got '"
                    + currentToken.value + "'");
        }
        Node node = new Node("IDENTIFIER", currentToken.value);
        advance();
        return node;
    }

    Node comparisonExpr() {
        Node operatorNode;
        Node operandNode;

        if (currentToken.equals(Token.comparisonOpIn())) {
            operatorNode = comparisonOperatorIn();
            operandNode = comparisonOperandIn();
        } else {
            operatorNode = comparisonOperator();
            operandNode = comparisonOperand();
        }

        Node exprNode = new Node("COMPARISON_EXP");
        exprNode.addLast(operatorNode);
        exprNode.addLast(operandNode);
        return exprNode;
    }

    Node comparisonOperatorIn() {
        if (currentToken.type == EOF) {
            return new Node("EOF", "NULL");
        }
        if (currentToken.type != COMPARISON_OPERATOR
                && currentToken.value.equals("IN:")) {
            throw new UQLParseException("Expect comparison operator, got '"
                    + currentToken.value + "'");
        }
        Node node = new Node("COMPARISON_OPERATOR", currentToken.value);
        advance();
        return node;
    }

    Node comparisonOperandIn() {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            if (currentToken.type == EOF) {
                return new Node("EOF", "NULL");
            }
            if (currentToken.value.equals("]")) {
                tokens.add(currentToken);
                break;
            }
            tokens.add(currentToken);
            advance();
        }

        Node node = new Node("OPERAND_IN");

        // Check for alternate literal and ',' surrounded by brackets
        for (int i = 0; i < tokens.size(); i++) {
            var token = tokens.get(i);
            if (i == 0) {
                if (!token.value.equals("[")) {
                    throw new UQLParseException("Expect [, got '"
                            + token.value + "'");
                }
                node.addFirst(new Node(token.type.name(), token.value));
                continue;
            }
            if (i == tokens.size() - 1) {
                if (!token.value.equals("]")) {
                    throw new UQLParseException("Expect ], got '"
                            + token.value + "'");
                }
                node.addLast(new Node(token.type.name(), token.value));
                continue;
            }
            if (i % 2 != 0) { //odd
                if (token.type != LITERAL) {
                    throw new UQLParseException("Expect literal, got '"
                            + token.value + "'");
                }
                node.addLast(new Node(token.type.name(), token.value));
            } else { //even
                if (!token.value.equals(",")) {
                    throw new UQLParseException("Expect ',' separator , got '"
                            + token.value + "'");
                }
                node.addLast(new Node(token.type.name(), token.value));
            }
        }
        advance();
        return node;
    }

    Node comparisonOperator() {
        if (currentToken.type == EOF) {
            return new Node("EOF", "NULL");
        }

        var operators = new ArrayList<String>() {{
            add("eq:");
            add("gt:");
            add("lt:");
            add("gte:");
            add("lte:");
            add("not:");
        }};

        if (currentToken.type != COMPARISON_OPERATOR
                || !operators.contains(currentToken.value)) {
            throw new UQLParseException("Expect comparison operator, got '"
                    + currentToken.value + "'");
        }
        Node node = new Node("COMPARISON_OPERATOR", currentToken.value);
        advance();
        return node;
    }

    Node comparisonOperand() {
        if (currentToken.type == EOF) {
            return new Node("EOF", "NULL");
        }
        if (currentToken.type != LITERAL) {
            throw new UQLParseException("Expect literal, got '"
                    + currentToken.value + "'");
        }
        Node node = new Node("LITERAL", currentToken.value);
        advance();
        return node;
    }
}
