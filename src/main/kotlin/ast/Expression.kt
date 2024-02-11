package today.astrum.ast

import today.astrum.tokenizer.Token
import today.astrum.tokenizer.TokenEnum
import today.astrum.visitor.ExpressionVisitor

sealed class Expression : Node() {

    abstract fun accept(visitor: ExpressionVisitor): Any;

    data class Grouping(
        val expression: Expression,
    ) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class Binary(
        val left: Expression,
        val operator: Token,
        val right: Expression,
    ) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class Unary(
        val operator: Token,
        val operand: Expression
    ) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class Literal(
        val type: TokenEnum,
        val literal: Any?
    ) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class Identifier(
        val name: String,
        val token: Token
    ) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class FunctionCall(
        val name: String,
        val token: Token,
        val parameter: List<Expression>
    ) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class Index(
        val name: String,
        val token: Token,
        val index: Expression
    ) : Expression(){
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class PropertyAccess(
        val left: Expression,
        val right: Expression,
        val token: Token
    ) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class ObjectLiteral(
        val properties: HashMap<String, Expression>
    ) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data object Null
    data object Undefined

}