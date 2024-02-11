package today.astrum.visitor

import today.astrum.ast.Statement

interface StatementVisitor {
    fun visit(node: Statement.IfStatement): Any
    fun visit(node: Statement.ReturnStatement): Any
    fun visit(node: Statement.ExpressionStatement): Any
    fun visit(node: Statement.VariableAssignment): Any
    fun visit(node: Statement.VariableDeclaration): Any
    fun visit(node: Statement.Block): Any
    fun visit(node: Statement.FunctionDeclaration): Any
    fun visit(node: Statement.Empty): Any
    fun visit(node: Statement.Object): Any
    fun visit(node: Statement.PrintStatement): Any
    fun visit(node: Statement.ForLoopStatement): Any
    fun visit(node: Statement.IndexAssignment): Any
}