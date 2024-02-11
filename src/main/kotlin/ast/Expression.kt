package today.astrum.lexer

import today.astrum.tokenizer.TokenEnum

sealed class Expression : Node() {
    data class Binary(
        val left: Expression,
        val operator: TokenEnum,
        val right: Expression,
    ) : Expression()

    data class Unary(
        val operator: TokenEnum,
        val operand: Expression
    ) : Expression()

    data class Literal(
        val type: TokenEnum,
        val literal: Any
    ) : Expression()

    data class Identifier(
        val name: String
    ) : Expression()

    data class ObjectLiteral(
        val properties: List<Property>
    ) : Expression()

    data class Property(
        val name: String,
        val value: Expression
    ) : Node()
}