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

    data class VariableAssignment(
        val name: String,
        val leftToken: Token,
        val rightToken: Token,
        val value: Expression,
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

    data class ForLoopStatement(
        val condition: Expression,
        val variable: VariableDeclaration,
        val assignment: VariableAssignment?,
        val thenBranch: Statement,
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

    data class PrintStatement(
        val value: Expression,
        val token: Token,
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

    // And a lot more work...
}