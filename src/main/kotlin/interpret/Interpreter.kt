package today.astrum.interpret

import today.astrum.ast.Expression
import today.astrum.ast.Statement
import today.astrum.function.AnonymousFunction
import today.astrum.function.Callable
import today.astrum.function.Function
import today.astrum.`object`.Class
import today.astrum.`object`.ClassInstance
import today.astrum.parser.Parser
import today.astrum.tokenizer.Token
import today.astrum.tokenizer.TokenEnum
import today.astrum.visitor.StatementVisitor


class Interpreter : InterpretExpression(), StatementVisitor {

    val globals = Scope()
    private var scope = Scope(globals)
    private val locals = HashMap<Expression, Int>()

    init {
        globals.define("print", object : Callable {
            override fun arity(): Int {
                return 1
            }

            override fun call(interpreter: Interpreter, arguments: List<Any>): Any{
                println(arguments[0])
                return Expression.Undefined
            }

        } )

        globals.define("readline", object : Callable {
            override fun arity(): Int {
                return 0
            }

            override fun call(interpreter: Interpreter, arguments: List<Any>): Any{
                return readln()
            }

        } )

        globals.define("map", object : Callable {
            override fun arity(): Int {
                return 2
            }

            override fun call(interpreter: Interpreter, arguments: List<Any>): Any{
                if(arguments[0] !is List<*>) throw Error("function map: Expected list as first parameter")
                if(arguments[1] !is Callable) throw Error("function map: Expected function as second parameter")
                return (arguments[0] as List<Any>).map { (arguments[1] as Callable).call(interpreter, listOf(it)) }
            }

        } )
    }

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
        val value = node.value.accept(this)
        throw Return(if(value == Unit) Expression.Null else value)
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

    override fun visit(node: Expression.VariableAssignment) {
        val value = node.right.accept(this)
        val distance = locals[node]

        if(distance != null){
            scope.assignAt(distance, node.left, value)
        } else {
            globals.assign(node.left, value)
        }

//        scope.assign(node.left, node.right.accept(this))

    }

    override fun visit(node: Expression.Set): Any {
        val obj = node.obj.accept(this)

        if(obj is Expression.ObjectLiteral){
            val value = obj.properties.set(node.name.value, node.value.accept(this))
            return Expression.Undefined
        }

        if(obj is ClassInstance){
            val value = node.value.accept(this)

            obj.set(node.name, value)
            return value
        }

        throw Runtime.Error(node.name,"Trying to set new value on non object/class value")
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
        val function: Function = Function(node, scope, false)
        scope.define(node.token, function)
    }

    override fun visit(node: Statement.Empty) {
        // Do nothing
    }

    override fun visit(node: Statement.Object): Any {
        return node
    }

    override fun visit(node: Statement.IndexAssignment) {
        val index = node.index.accept(this) as Number
        val data = (scope.get(node.leftToken) as MutableList<Any>)
        data[index.toInt()] = node.value.accept(this)
        scope.assign(node.leftToken, data)
    }

    override fun visit(node: Statement.WhileLoopStatement) {
        while(node.condition.accept(this) == true) {
            node.body.accept(this)
        }
    }

    override fun visit(node: Statement.Class) {
        scope.define(node.token, null)

        val methods = hashMapOf<String, Function>()

        node.methods?.forEach {
            val function = Function(it, scope, it.token.value.equals("constructor"))
            methods[it.name] = function
        }

        val klass = Class(node.token.value, methods)
        scope.assign(node.token, klass)
    }


    override fun visit(node: Expression.FunctionCall): Any {
        val callee = node.callee.accept(this)
        val arguments = mutableListOf<Any>()

        for (argument in node.arguments){
            arguments.add(argument.accept(this))
        }

        if(callee !is Callable) throw Runtime.Error(node.token, "Not Callable!")

        val function: Callable = callee

        if(function.arity() != arguments.count()) throw Runtime.Error(node.token, "Expected matching arguments")

        return function.call(this, arguments)
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

    override fun visit(node: Expression.Get): Any{
        val left = node.obj.accept(this)

        if (left is ClassInstance){
            return left.get(node.name)
        }
        if (left is Expression.ObjectLiteral){
            left.properties.get(node.name.value)?.let {
                return it
            }
        }

        when(left) {
            is List<*> ->
                return wrapLiteral("Array", left, node.name)

            is String ->
                return wrapLiteral("String", left, node.name)

            else -> {}
        }
        throw Error("Invalid Property Access Operation")
    }
    override fun visit(node: Expression.Identifier): Any {
        return lookUpVariable(node.token, node)
    }

    override fun visit(node: Expression.This): Any {
        return lookUpVariable(node.token, node)
    }

    override fun visit(node: Expression.AnonymousFunction): Any {
        return AnonymousFunction(node.source, scope)
    }

    fun wrapLiteral(className: String, value: Any,  node: Token): Any {
        return (scope.getAt(0, className) as Class).call(this, listOf(value)).get(node)
    }
    fun executeBlock(statements: List<Statement>, scope: Scope): Any {
        val previousScope = this.scope

        try {
            this.scope = scope

            for (statement in statements) {
                statement.accept(this)
            }
        } finally {
            this.scope = previousScope
        }

        return Unit
    }
}