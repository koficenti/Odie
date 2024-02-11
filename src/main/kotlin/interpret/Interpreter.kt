package today.astrum.interpret

import today.astrum.ast.Expression
import today.astrum.ast.Parameter
import today.astrum.ast.Statement
import today.astrum.tokenizer.Token
import today.astrum.tokenizer.TokenEnum
import today.astrum.visitor.StatementVisitor


class Interpreter : InterpretExpression(), StatementVisitor {

    private var scope = Scope()
    private val locals = HashMap<Expression, Int>()


    fun resolve(expr: Expression, depth: Int) {
        locals.put(expr, depth)
    }

    fun lookUpVariable(name: Token, expr: Expression): Any{
        val distance = locals[expr]
        return if (distance != null) {
            scope.getAt(distance, name.value)
        } else {
            scope.get(name)!!
        }
    }

    override fun visit(node: Statement.IfStatement): Any {
        val condition = node.condition.accept(this) as? Boolean
        val currentScope = Scope(scope)
        if (condition == true) {
            executeBlock((node.thenBranch as Statement.Block).statements, currentScope)
        } else if (node.elseBranch != null) {
            executeBlock((node.elseBranch as Statement.Block).statements, currentScope)
        }
        return Unit
    }

    override fun visit(node: Statement.ReturnStatement): Any {
        return node
    }

    override fun visit(node: Statement.ExpressionStatement): Any {
        return node.expression.accept(this)
    }

    override fun visit(node: Expression.Literal): Any {
        if(node.type == TokenEnum.Dot){
            return Unit
        }
        return super.visit(node)
    }

    override fun visit(node: Statement.VariableAssignment) {
        scope.assign(node.leftToken, node.value.accept(this))
    }

    override fun visit(node: Statement.VariableDeclaration): Any {
        var value: Any = false
        if (node.initializer != null) {
            value = node.initializer.accept(this)
        }

        scope.define(node.token, value)

        return value
    }

    override fun visit(node: Statement.Block) {
        executeBlock(node.statements, Scope(scope))
    }

    override fun visit(node: Statement.FunctionDeclaration) {
//        this.executeBlock(node.block.statements, Scope(scope))
        scope.define(node.token, Triple(node.parameters, node.block, scope))
    }

    override fun visit(node: Statement.Empty) {
        // Do nothing
    }

    override fun visit(node: Statement.Object): Any {
        return node
    }

    override fun visit(node: Statement.PrintStatement): Any {
        println(node.value.accept(this))
        return Unit
    }

    override fun visit(node: Statement.ForLoopStatement): Any {
        val previous = scope
        scope = Scope(previous)
        scope.define(node.variable.token, node.variable.initializer?.accept(this))
        while(node.condition.accept(this) as Boolean){
            for (statement in (node.thenBranch as Statement.Block).statements){
                val result = statement.accept(this)
                if(result is Statement.ReturnStatement){
                    return result.value.accept(this)
                }
            }
            node.assignment?.accept(this)
        }
        scope = previous
        return Unit
    }

    override fun visit(node: Statement.IndexAssignment) {
        val index = node.index.accept(this) as Number
        val data = (scope.get(node.leftToken) as MutableList<Any>)
        data[index.toInt()] = node.value.accept(this)
        scope.assign(node.leftToken, data)
    }


    // Confused myself on this one a bit
    override fun visit(node: Expression.FunctionCall): Any {
        // Retrieve the function definition from the scope
        val func = scope.get(node.token) as Triple<List<Parameter>, Statement.Block, Scope>

        // Check if the number of provided arguments matches the number of expected parameters
        if (func.first.size != node.parameter.size) {
            throw Runtime.Error(node.token, "Incorrect number of parameters provided for function call!")
        }

        // Create a new scope for the function call, inheriting from the outer scope
        val functionScope = Scope(func.third)

        // Bind the provided arguments to their corresponding parameters in the new scope
        func.first.forEachIndexed { index, parameter ->
            val argumentValue = node.parameter[index].accept(this)
            functionScope.define(parameter.leftToken, argumentValue)
        }

        // Execute the function body with the new scope
        return executeBlock(func.second.statements, functionScope, insideFunction = true)
    }


    override fun visit(node: Expression.Index): Any {
        val value = scope.get(node.token)

        if(value is List<*>){
            val number = node.index.accept(this)
            if(number is Number){
                if((value.count() - 1 < number.toInt()) || (number.toInt() < 0)){
                    return Expression.Undefined
                }
                value[number.toInt()]?.let {
                    return it
                }
            }
        }
        throw Runtime.Error(node.token,"Invalid Indexing!")
    }

    override fun visit(node: Expression.PropertyAccess): Any{
        val left = node.left.accept(this)
        if (left is Expression.ObjectLiteral){
            if(node.right is Expression.Identifier){
                left.properties.get(node.right.name)?.let {
                    return it.accept(this)
                }
            }
        }
        throw Error("Invalid Property Access Operation")
    }
    override fun visit(node: Expression.Identifier): Any {
        scope.get(node.token)?.let {
            return it
        }
        throw Runtime.Error(node.token, "Variable does not exist '${node.name}'.")
    }

    private fun executeBlock(statements: List<Statement>, scope: Scope, insideFunction: Boolean = false): Any {
        val previousScope = this.scope
        var returnValue: Any? = null

        try {
            this.scope = scope

            for (statement in statements) {
                val result = statement.accept(this)

                if (result is Statement.ReturnStatement) {
                    returnValue = result.value.accept(this)
                    if (!insideFunction) break // Break only if not inside a function
                }
            }
        } finally {
            this.scope = previousScope
        }

        return returnValue ?: Unit
    }
}