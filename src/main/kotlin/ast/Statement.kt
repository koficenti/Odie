package today.astrum.ast

import today.astrum.tokenizer.Token
import today.astrum.visitor.StatementVisitor

sealed class Statement : Node() {

    abstract fun accept(visitor: StatementVisitor): Any

    data class Empty(private val empty: Boolean = true) : Statement() {
        override fun accept(visitor: StatementVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class Block(
        val statements: List<Statement>
    ) : Statement() {
        override fun accept(visitor: StatementVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class ExpressionStatement(
        val expression: Expression,
        val token: Token
    ) : Statement() {
        override fun accept(visitor: StatementVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class VariableDeclaration(
        val name: String,
        val declarationKind: DeclarationKind,
        val type: Type?,
        val initializer: Statement?,
        val token: Token,
    ) : Statement() {
        override fun accept(visitor: StatementVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class IndexAssignment(
        val index: Expression,
        val leftToken: Token,
        val value: Expression,
    ) : Statement() {
        override fun accept(visitor: StatementVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class FunctionDeclaration(
        val name: String,
        val token: Token,
        val parameters: List<Parameter>,
        val block: Block,
        val returnType: Type?,
    ) : Statement() {
        override fun accept(visitor: StatementVisitor): Any {
            return visitor.visit(this)
        }
    }

    enum class DeclarationKind {
        LET, CONST, VAR
    }

    data class IfStatement(
        val condition: Expression,
        val thenBranch: Statement,
        val elseBranch: Statement?,
    ) : Statement() {
        override fun accept(visitor: StatementVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class WhileLoopStatement(
        val condition: Expression,
        val body: Statement,
        val token: Token,
    ) : Statement() {
        override fun accept(visitor: StatementVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class ReturnStatement(
        val value: Statement
    ) : Statement() {
        override fun accept(visitor: StatementVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class Object(
        val properties: HashMap<String, Statement>
    ) : Statement() {
        override fun accept(visitor: StatementVisitor): Any {
            return visitor.visit(this)
        }
    }

    data class Class(
        val token: Token,
        val methods: List<FunctionDeclaration>?,
        val variables: List<Expression.VariableAssignment>?
    ) : Statement() {
        override fun accept(visitor: StatementVisitor): Any {
            return visitor.visit(this)
        }
    }

    // And a lot more work...
}