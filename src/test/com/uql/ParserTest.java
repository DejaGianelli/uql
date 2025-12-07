package com.uql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserTest {
    @Test
    void should_throw_exception_when_parenthesis_not_match() {
        String uql = "city eq: \"São João\")";
        Tokenizer tokenizer = new Tokenizer(uql);
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer.tokens);
        Executable exec = parser::parse;

        assertThrows(UQLParseException.class, exec);
    }

    @Test
    void should_generate_tree() {
        String uql = "(city eq: \"São João\")";
        Tokenizer tokenizer = new Tokenizer(uql);
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer.tokens);

        Node result = parser.parse();

        Node expected = new Node("UQL_EXPR");
        Node filtersNode = new Node("FILTERS_NODE");
        filtersNode.addFirst(new Node("SEPARATOR", "("));
        Node filterNode = new Node("FILTER_NODE");
        filtersNode.addLast(filterNode);
        filterNode.addFirst(new Node("IDENTIFIER", "city"));
        Node comparisonExp = new Node("COMPARISON_EXP");
        filterNode.addLast(comparisonExp);
        comparisonExp.addLast(new Node("COMPARISON_OPERATOR", "eq:"));
        comparisonExp.addLast(new Node("LITERAL", "\"São João\""));
        filtersNode.addLast(new Node("SEPARATOR", ")"));
        expected.addFirst(filtersNode);

        assertTrue(isTreesEqual(result, expected), result.treeString(0));
    }

    @Test
    void should_generate_tree_2() {
        String uql = "((city eq: \"São João\") && (number eq: 123))";
        Tokenizer tokenizer = new Tokenizer(uql);
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer.tokens);
        Node result = parser.parse();
        assertEquals("""
                        Node[UQL_EXPR = 'null']
                        └─ Node[SEPARATOR = '(']
                        └─ Node[FILTERS_NODE = 'null']
                           └─ Node[SEPARATOR = '(']
                           └─ Node[FILTER_NODE = 'null']
                              └─ Node[IDENTIFIER = 'city']
                              └─ Node[COMPARISON_EXP = 'null']
                                 └─ Node[COMPARISON_OPERATOR = 'eq:']
                                 └─ Node[LITERAL = '"São João"']
                           └─ Node[SEPARATOR = ')']
                           └─ Node[LOGICAL_OPERATOR = '&&']
                           └─ Node[FILTERS_NODE = 'null']
                              └─ Node[SEPARATOR = '(']
                              └─ Node[FILTER_NODE = 'null']
                                 └─ Node[IDENTIFIER = 'number']
                                 └─ Node[COMPARISON_EXP = 'null']
                                    └─ Node[COMPARISON_OPERATOR = 'eq:']
                                    └─ Node[LITERAL = '123']
                              └─ Node[SEPARATOR = ')']
                        └─ Node[SEPARATOR = ')']""", result.treeString(0),
                result.treeString(0));
    }

    @Test
    void should_generate_tree_3() {
        String uql = "(city eq: \"São João\") && (number eq: 123)";
        Tokenizer tokenizer = new Tokenizer(uql);
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer.tokens);
        Node result = parser.parse();
        assertEquals("""
                        Node[UQL_EXPR = 'null']
                        └─ Node[FILTERS_NODE = 'null']
                           └─ Node[SEPARATOR = '(']
                           └─ Node[FILTER_NODE = 'null']
                              └─ Node[IDENTIFIER = 'city']
                              └─ Node[COMPARISON_EXP = 'null']
                                 └─ Node[COMPARISON_OPERATOR = 'eq:']
                                 └─ Node[LITERAL = '"São João"']
                           └─ Node[SEPARATOR = ')']
                           └─ Node[LOGICAL_OPERATOR = '&&']
                           └─ Node[FILTERS_NODE = 'null']
                              └─ Node[SEPARATOR = '(']
                              └─ Node[FILTER_NODE = 'null']
                                 └─ Node[IDENTIFIER = 'number']
                                 └─ Node[COMPARISON_EXP = 'null']
                                    └─ Node[COMPARISON_OPERATOR = 'eq:']
                                    └─ Node[LITERAL = '123']
                              └─ Node[SEPARATOR = ')']""", result.treeString(0),
                result.treeString(0));
    }

    @Test
    void should_generate_tree_4() {
        String uql = "city eq: \"São João\" && number eq: 123";
        Tokenizer tokenizer = new Tokenizer(uql);
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer.tokens);
        Node result = parser.parse();
        assertEquals("""
                        Node[UQL_EXPR = 'null']
                        └─ Node[FILTERS_NODE = 'null']
                           └─ Node[FILTER_NODE = 'null']
                              └─ Node[IDENTIFIER = 'city']
                              └─ Node[COMPARISON_EXP = 'null']
                                 └─ Node[COMPARISON_OPERATOR = 'eq:']
                                 └─ Node[LITERAL = '"São João"']
                           └─ Node[LOGICAL_OPERATOR = '&&']
                           └─ Node[FILTERS_NODE = 'null']
                              └─ Node[FILTER_NODE = 'null']
                                 └─ Node[IDENTIFIER = 'number']
                                 └─ Node[COMPARISON_EXP = 'null']
                                    └─ Node[COMPARISON_OPERATOR = 'eq:']
                                    └─ Node[LITERAL = '123']""", result.treeString(0),
                result.treeString(0));
    }

    @Test
    void should_generate_tree_5() {
        String uql = "city IN: [\"São João\", 123]";
        Tokenizer tokenizer = new Tokenizer(uql);
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer.tokens);
        Node result = parser.parse();
        assertEquals("""
                        Node[UQL_EXPR = 'null']
                        └─ Node[FILTERS_NODE = 'null']
                           └─ Node[FILTER_NODE = 'null']
                              └─ Node[IDENTIFIER = 'city']
                              └─ Node[COMPARISON_EXP = 'null']
                                 └─ Node[COMPARISON_OPERATOR = 'IN:']
                                 └─ Node[OPERAND_IN = 'null']
                                    └─ Node[SEPARATOR = '[']
                                    └─ Node[LITERAL = '"São João"']
                                    └─ Node[SEPARATOR = ',']
                                    └─ Node[LITERAL = '123']
                                    └─ Node[SEPARATOR = ']']""", result.treeString(0),
                result.treeString(0));
    }

    @Test
    void should_generate_tree_6() {
        String uql = "city IN: [\"São João\", 123] && number eq: 123";
        Tokenizer tokenizer = new Tokenizer(uql);
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer.tokens);
        Node result = parser.parse();
        assertEquals("""
                        Node[UQL_EXPR = 'null']
                        └─ Node[FILTERS_NODE = 'null']
                           └─ Node[FILTER_NODE = 'null']
                              └─ Node[IDENTIFIER = 'city']
                              └─ Node[COMPARISON_EXP = 'null']
                                 └─ Node[COMPARISON_OPERATOR = 'IN:']
                                 └─ Node[OPERAND_IN = 'null']
                                    └─ Node[SEPARATOR = '[']
                                    └─ Node[LITERAL = '"São João"']
                                    └─ Node[SEPARATOR = ',']
                                    └─ Node[LITERAL = '123']
                                    └─ Node[SEPARATOR = ']']
                           └─ Node[LOGICAL_OPERATOR = '&&']
                           └─ Node[FILTERS_NODE = 'null']
                              └─ Node[FILTER_NODE = 'null']
                                 └─ Node[IDENTIFIER = 'number']
                                 └─ Node[COMPARISON_EXP = 'null']
                                    └─ Node[COMPARISON_OPERATOR = 'eq:']
                                    └─ Node[LITERAL = '123']""", result.treeString(0),
                result.treeString(0));
    }

    @Test
    void should_generate_tree_7() {
        String uql = """
                (
                    city IN: ["São João", "São Paulo"] &&
                    number eq: 123
                ) || (
                    zipCode eq: "13860123"
                )
                """;
        Tokenizer tokenizer = new Tokenizer(uql);
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer.tokens);
        Node result = parser.parse();
        assertEquals("""
                        Node[UQL_EXPR = 'null']
                        └─ Node[FILTERS_NODE = 'null']
                           └─ Node[SEPARATOR = '(']
                           └─ Node[FILTER_NODE = 'null']
                              └─ Node[IDENTIFIER = 'city']
                              └─ Node[COMPARISON_EXP = 'null']
                                 └─ Node[COMPARISON_OPERATOR = 'IN:']
                                 └─ Node[OPERAND_IN = 'null']
                                    └─ Node[SEPARATOR = '[']
                                    └─ Node[LITERAL = '"São João"']
                                    └─ Node[SEPARATOR = ',']
                                    └─ Node[LITERAL = '"São Paulo"']
                                    └─ Node[SEPARATOR = ']']
                           └─ Node[LOGICAL_OPERATOR = '&&']
                           └─ Node[FILTERS_NODE = 'null']
                              └─ Node[FILTER_NODE = 'null']
                                 └─ Node[IDENTIFIER = 'number']
                                 └─ Node[COMPARISON_EXP = 'null']
                                    └─ Node[COMPARISON_OPERATOR = 'eq:']
                                    └─ Node[LITERAL = '123']
                              └─ Node[SEPARATOR = ')']
                              └─ Node[LOGICAL_OPERATOR = '||']
                              └─ Node[FILTERS_NODE = 'null']
                                 └─ Node[SEPARATOR = '(']
                                 └─ Node[FILTER_NODE = 'null']
                                    └─ Node[IDENTIFIER = 'zipCode']
                                    └─ Node[COMPARISON_EXP = 'null']
                                       └─ Node[COMPARISON_OPERATOR = 'eq:']
                                       └─ Node[LITERAL = '"13860123"']
                                 └─ Node[SEPARATOR = ')']""", result.treeString(0),
                result.treeString(0));
    }

    @Test
    void should_generate_tree_8() {
        String uql = """
                ((
                    city IN: ["São João", "São Paulo"] &&
                    number eq: 123
                ) || (
                    zipCode eq: "13860123"
                ))
                """;
        Tokenizer tokenizer = new Tokenizer(uql);
        tokenizer.tokenize();
        Parser parser = new Parser(tokenizer.tokens);
        Node result = parser.parse();
        assertEquals("""
                        Node[UQL_EXPR = 'null']
                        └─ Node[SEPARATOR = '(']
                        └─ Node[FILTERS_NODE = 'null']
                           └─ Node[SEPARATOR = '(']
                           └─ Node[FILTER_NODE = 'null']
                              └─ Node[IDENTIFIER = 'city']
                              └─ Node[COMPARISON_EXP = 'null']
                                 └─ Node[COMPARISON_OPERATOR = 'IN:']
                                 └─ Node[OPERAND_IN = 'null']
                                    └─ Node[SEPARATOR = '[']
                                    └─ Node[LITERAL = '"São João"']
                                    └─ Node[SEPARATOR = ',']
                                    └─ Node[LITERAL = '"São Paulo"']
                                    └─ Node[SEPARATOR = ']']
                           └─ Node[LOGICAL_OPERATOR = '&&']
                           └─ Node[FILTERS_NODE = 'null']
                              └─ Node[FILTER_NODE = 'null']
                                 └─ Node[IDENTIFIER = 'number']
                                 └─ Node[COMPARISON_EXP = 'null']
                                    └─ Node[COMPARISON_OPERATOR = 'eq:']
                                    └─ Node[LITERAL = '123']
                              └─ Node[SEPARATOR = ')']
                              └─ Node[LOGICAL_OPERATOR = '||']
                              └─ Node[FILTERS_NODE = 'null']
                                 └─ Node[SEPARATOR = '(']
                                 └─ Node[FILTER_NODE = 'null']
                                    └─ Node[IDENTIFIER = 'zipCode']
                                    └─ Node[COMPARISON_EXP = 'null']
                                       └─ Node[COMPARISON_OPERATOR = 'eq:']
                                       └─ Node[LITERAL = '"13860123"']
                                 └─ Node[SEPARATOR = ')']
                        └─ Node[SEPARATOR = ')']""", result.treeString(0),
                result.treeString(0));
    }

    boolean isTreesEqual(Node root1, Node root2) {
        if (root1 == null && root2 == null) {
            return true;
        }
        if (root1 == null || root2 == null) {
            return false;
        }
        if (!root1.equals(root2)) {
            return false;
        }
        if (root1.children.size() != root2.children.size()) {
            return false;
        }
        for (int i = 0; i < root1.children.size(); i++) {
            if (!isTreesEqual(root1.children.get(i), root2.children.get(i))) {
                return false;
            }
        }
        return true;
    }

}
