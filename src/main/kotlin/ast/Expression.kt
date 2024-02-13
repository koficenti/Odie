package today.astrum.ast

import today.astrum.function.Function
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

    data class This(val token: Token) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class AnonymousFunction(
        val token: Token,
        val source: Statement.Block,
    ) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class FunctionCall(
        val callee: Expression,
        val token: Token,
        val arguments: List<Expression>
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

    data class Get(
        val obj: Expression,
        val name: Token,
    ) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }
    data class Set(
        val name: Token,
        val obj: Expression,
        val value: Expression,
    ) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class ObjectLiteral(
        val properties: HashMap<String, Any>
    ) : Expression() {
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class VariableAssignment(
        val name: String,
        val left: Token,
        val right: Expression,
    ) : Expression(){
        override fun accept(visitor: ExpressionVisitor): Any {
            return visitor.visit(this)
        }
    }

    data object Null
    data object Undefined

}