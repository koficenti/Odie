package today.astrum.lexer

sealed class Statement : Node() {
    data class Block(
        val statements: List<Statement>
    ) : Statement()

    data class VariableDeclaration(
        val name: String,
        val type: Type,
        val initializer: Expression?
    ) : Statement()

    data class IfStatement(
        val condition: Expression,
        val thenBranch: Statement,
        val elseBranch: Statement?,
    ) : Statement()

    // And a lot more work...
}