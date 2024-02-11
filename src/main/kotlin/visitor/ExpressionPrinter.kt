package today.astrum.visitor

import today.astrum.ast.Expression

class ExpressionPrinter : ExpressionVisitor {
    override fun visit(node: Expression.Grouping) {
        print("(")
        node.expression.accept(this)
        print(")")
    }

    override fun visit(node: Expression.Binary) {
        node.left.accept(this)
        print(" ${node.operator.type} ")
        node.right.accept(this)
    }

    override fun visit(node: Expression.Unary) {
        print("${node.operator.type} ")
        node.operand.accept(this)
    }

    override fun visit(node: Expression.Identifier) {
        print(node.name)
    }

    override fun visit(node: Expression.Literal) {
        print(node.type)
    }

    override fun visit(node: Expression.PropertyAccess): Any {
        TODO("Not yet implemented")
    }

    override fun visit(node: Expression.ObjectLiteral): Any {
        TODO("Not yet implemented")
    }

    override fun visit(node: Expression.FunctionCall): Any {
        return node
    }

    override fun visit(node: Expression.Index): Any {
        TODO("Not yet implemented")
    }
}