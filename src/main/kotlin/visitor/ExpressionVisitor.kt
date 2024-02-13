package today.astrum.visitor

import today.astrum.ast.Expression

interface ExpressionVisitor{
    fun visit(node: Expression.Grouping): Any
    fun visit(node: Expression.Binary): Any
    fun visit(node: Expression.Unary): Any
    fun visit(node: Expression.Identifier): Any
    fun visit(node: Expression.Literal): Any
    fun visit(node: Expression.Set): Any
    fun visit(node: Expression.Get): Any
    fun visit(node: Expression.ObjectLiteral): Any
    fun visit(node: Expression.FunctionCall): Any
    fun visit(node: Expression.Index): Any
    fun visit(node: Expression.VariableAssignment): Any
    fun visit(node: Expression.This): Any
    fun visit(node: Expression.AnonymousFunction): Any
}