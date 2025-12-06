# UQL Backus–Naur Form

```txt
<uql> ::= <query_param_key> "=" <uql_expr>

<query_param_key> ::= any URI symbol

<uql_expr> ::= <filters> | "(" <filters> ")"

<filters> ::= <filter>
            | <filter> <logical_operator> <filters>

<logical_operator> ::= && | || (double pipe)

<filter> ::= <key> ":=" <comparison_expr>
           | "(" <key> ":=" <comparison_expr> ")"

<key> ::= { <key_char> }+  ;One or more

<key_char> ::= "A–Z" | "a–z" | "-" | "." | "_"

<comparison_expr> :: = <comparison_operator> ":" <operand>
					 | <comparison_operator_in> <operand_in>

<comparison_operator> ::= "eq:" | "gt:" | "lt:" | "gte:" | "lte:" | "not:"

<comparison_operator_in> ::= "IN"

<operand> ::= """ any string """ | any number ;Literal

<operand_in> ::= "[" <in_elements> "]"

<in_elements> ::= <in_element> | <in_element> "," <in_elements>

<in_element> ::= """ any string """ | any number ;Literal
```