package today.astrum.interpret

import today.astrum.ast.Expression
import today.astrum.ast.Statement
import today.astrum.tokenizer.Token
import today.astrum.visitor.ExpressionVisitor
import today.astrum.visitor.StatementVisitor
import kotlin.system.exitProcess

class ScopeResolver(val interpreter: Interpreter) : StatementVisitor, ExpressionVisitor {

    enum class FunctionType {
        METHOD,
        FUNCTION,
        CONSTRUCTOR,
        NONE
    }
    enum class ClassType {
        NONE,
        CLASS,
    }

    private var currentFunction = FunctionType.NONE
    private var currentClass = ClassType.NONE

    private val scopes: MutableList<HashMap<String, Boolean>> = mutableListOf()

    override fun visit(node: Expression.Grouping) {
        resolve(node.expression)
    }

    override fun visit(node: Expression.Binary) {
        resolve(node.left)
        resolve(node.right)
    }

    override fun visit(node: Expression.Unary) {
        resolve(node.operand)
    }

    override fun visit(node: Expression.Identifier) {
        if(scopes.isNotEmpty()){
            if(scopes.last().get(node.name) == false){
                throw Runtime.Error(node.token, "Variable needs to be initialized correctly!")
            }
        }
        resolveLocal(node, node.token)
    }

    override fun visit(node: Expression.Literal) {}
    override fun visit(node: Expression.Set) {
        resolve(node.obj)
        resolve(node.value)
    }

    override fun visit(node: Expression.Get) {
        resolve(node.obj)
    }

    override fun visit(node: Expression.ObjectLiteral) {
    }

    override fun visit(node: Expression.FunctionCall) {
        resolve(node.callee)

        for(expr in node.arguments){
            resolve(expr)
        }
    }

    override fun visit(node: Expression.Index) {
    }

    override fun visit(node: Statement.IfStatement) {
        // TODO: Fix what ever problem this is
//        resolve(node.condition)
//        resolve(node.thenBranch)
//        if(node.elseBranch != null){
//            resolve(node.elseBranch)
//        }
    }

    override fun visit(node: Statement.ReturnStatement) {
        if(currentFunction == FunctionType.NONE) {
            throw Error("Cannot return in global scope!")
        }
        if(currentFunction == FunctionType.CONSTRUCTOR){
            throw Error("Cannot return anything in constructor!")
        }
        resolve(node.value)
    }

    override fun visit(node: Statement.ExpressionStatement) {
        resolve(node.expression)
    }

    override fun visit(node: Expression.VariableAssignment) {
        resolve(node.right)
        resolveLocal(node, node.left)
    }

    override fun visit(node: Expression.This) {
        if(currentClass == ClassType.NONE){
            throw Runtime.Error(node.token, "Cannot use 'this' keyword outside of class")
        }
        resolveLocal(node, node.token)
    }

    override fun visit(node: Expression.AnonymousFunction) {
        resolveAnonymousFunction(node, FunctionType.FUNCTION)
    }

    override fun visit(node: Statement.VariableDeclaration) {
        declare(node.token)

        if(node.initializer != null){
            resolve(node.initializer)
        }

        define(node.token)
    }

    override fun visit(node: Statement.Block) {
        beginScope()
        resolve(node.statements)
        endScope()
    }

    override fun visit(node: Statement.FunctionDeclaration) {
        declare(node.token)
        define(node.token)

        resolveFunction(node, FunctionType.FUNCTION)
    }

    override fun visit(node: Statement.Empty) {
    }

    override fun visit(node: Statement.Object) {
    }

    override fun visit(node: Statement.IndexAssignment) {
    }

    override fun visit(node: Statement.WhileLoopStatement) {
        resolve(node.condition)
        beginScope()
        resolve(node.body)
        endScope()
    }

    override fun visit(node: Statement.Class) {
        currentClass = ClassType.CLASS

        declare(node.token)
        beginScope()
        scopes.last().put("this", true)
        if(node.methods != null){
            for (method in node.methods){
                var declaration = FunctionType.METHOD
                if(method.token.value.equals("constructor")){
                    declaration = FunctionType.CONSTRUCTOR
                }
                resolveFunction(method, declaration)

            }
        }
        endScope()

        define(node.token)
        currentClass = ClassType.NONE
    }

    private fun resolve(statements: List<Statement>){
        for (statement in statements){
            statement.accept(this)
        }
    }
    private fun resolve(statement: Statement){
        statement.accept(this)
    }
    private fun resolve(expression: Expression){
        expression.accept(this)
    }
    private fun beginScope(){
        scopes.add(hashMapOf())
    }
    private fun endScope(){
        scopes.removeLast()
    }
    private fun declare(name: Token){
        if(scopes.isEmpty()) return
        val scope = scopes.last()
        if(scope.containsKey(name.value))
            throw Runtime.Error(name, "Variable with name ${name.value} already exist")
        scope.put(name.value, false)
    }
    private fun define(name: Token){
        if(scopes.isEmpty()) return
        val scope = scopes.last()
        scope.put(name.value, true)
    }
    private fun resolveFunction(node: Statement.FunctionDeclaration, type: FunctionType){
        val enclosing = currentFunction
        currentFunction = type

        beginScope()
        for(i in node.parameters){
            declare(i.leftToken)
            define(i.leftToken)
        }
        resolve(node.block)
        endScope()
        currentFunction = enclosing
    }
    private fun resolveAnonymousFunction(node: Expression.AnonymousFunction, type: FunctionType){
        val enclosing = currentFunction
        currentFunction = type

        beginScope()
//        for(i in node.parameters){
//            declare(i.leftToken)
//            define(i.leftToken)
//        }
        resolve(node.source)
        endScope()
        currentFunction = enclosing
    }
    private fun resolveLocal(expr: Expression, name: Token){
        for (i in scopes.size - 1 downTo 0){
            if(scopes[i].containsKey(name.value)){
                interpreter.resolve(expr, scopes.size - 1 -  i)
                return
            }
        }
    }
}