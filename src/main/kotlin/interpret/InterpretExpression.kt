package today.astrum.interpret

import today.astrum.ast.Expression
import today.astrum.tokenizer.TokenEnum
import today.astrum.visitor.ExpressionVisitor

open class InterpretExpression : ExpressionVisitor {
    override fun visit(node: Expression.Grouping): Any {
        return node.expression.accept(this)
    }

    override fun visit(node: Expression.Binary): Any {

        when (node.operator.type) {
            TokenEnum.LogicalAnd -> {
                val left = node.left.accept(this)
                if(left == false) return false
                val right = node.right.accept(this)
                return right is Boolean
            }
            else -> {
                val left: Any = node.left.accept(this)
                val right: Any = node.right.accept(this)

                when (node.operator.type) {

                    TokenEnum.LogicalOr -> return left as Boolean || right as Boolean
                    TokenEnum.EqualEqual -> {
                        if (left is Number && right is Number) {
                            return left.toDouble() == right.toDouble()
                        }
                        return left == right
                    }

                    TokenEnum.NotEqual -> {
                        if (left is Number && right is Number) {
                            return left.toDouble() != right.toDouble()
                        }
                        return left != right
                    }

                    TokenEnum.GreaterThan -> {
                        Runtime.checkNumberOperands(node.operator, left, right)
                        return (left as Number).toDouble() > (right as Number).toDouble()
                    }

                    TokenEnum.GreaterThanOrEqual -> {
                        Runtime.checkNumberOperands(node.operator, left, right)
                        return (left as Number).toDouble() >= (right as Number).toDouble()
                    }

                    TokenEnum.LessThan -> {
                        Runtime.checkNumberOperands(node.operator, left, right)
                        return (left as Number).toDouble() < (right as Number).toDouble()
                    }

                    TokenEnum.LessThanOrEqual -> {
                        Runtime.checkNumberOperands(node.operator, left, right)
                        return (left as Number).toDouble() <= (right as Number).toDouble()
                    }

                    TokenEnum.Minus -> {
                        Runtime.checkNumberOperands(node.operator, left, right)
                        return (left as Number).toDouble() - (right as Number).toDouble()
                    }

                    TokenEnum.Slash -> {
                        Runtime.checkNumberOperands(node.operator, left, right)
                        Runtime.checkDivisionByZero(
                            node.operator,
                            right
                        ) // Javascript will not throw any errors for this, wonder why?
                        return (left as Number).toDouble() / (right as Number).toDouble()
                    }

                    TokenEnum.Asterisk -> {
                        Runtime.checkNumberOperands(node.operator, left, right)
                        return (left as Number).toDouble() * (right as Number).toDouble()
                    }

                    TokenEnum.Plus -> {
                        if (left is Number && right is Number) {
                            return left.toDouble() + right.toDouble()
                        }
                        if (left is String && right is String) {
                            return "${left}${right}"
                        }
                        if (left is Number && right is String || left is String && right is Number) {
                            return "$left$right"
                        }
                        throw Runtime.Error(node.operator, "Addition of different types")
                    }

                    else -> {}
                }
            }
        }
        return Unit
    }

    override fun visit(node: Expression.Unary): Any {
        val right = node.operand.accept(this)
        when (node.operator.type) {
            TokenEnum.Minus -> {
                Runtime.checkNumberOperands(node.operator, right)
                return -(right as Number).toDouble()
            }

            TokenEnum.Not -> {
                if (right is Boolean) {
                    return !right
                }
            }

            else -> throw Error("Not valid unary expression")
        }
        throw Error("Not valid unary expression")
    }

    override fun visit(node: Expression.Identifier): Any {
        return node.name
    }

    override fun visit(node: Expression.Literal): Any {
        if(node.literal is Number){
            return node.literal.toDouble()
        }
        if(node.type == TokenEnum.ObjectLiteral){
            return Expression.ObjectLiteral(hashMapOf())
        }
        if(node.type == TokenEnum.ListLiteral){
            val list = node.literal as MutableList<Expression>
            return list.map { it.accept(this) }.toMutableList()
        }
        return node.literal ?: throw Error("Expected literal?")
    }

    override fun visit(node: Expression.PropertyAccess): Any {
        return node
    }

    override fun visit(node: Expression.ObjectLiteral): Any {
        return node
    }

    override fun visit(node: Expression.FunctionCall): Any {
        return node
    }

    override fun visit(node: Expression.Index): Any {
        return node
    }
}