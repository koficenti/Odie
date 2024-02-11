package today.astrum.interpret

import today.astrum.ast.Expression
import today.astrum.ast.Statement
import today.astrum.tokenizer.Token
import today.astrum.visitor.ExpressionVisitor
import today.astrum.visitor.StatementVisitor

class ScopeResolver(val interpreter: Interpreter) : StatementVisitor, ExpressionVisitor {

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
            val result = scopes.last().get(node.name)
            if(result == false){
                throw Runtime.Error(node.token,"Cannot set initializer as self")
            }
        }
        resolveLocal(node, node.token)
    }

    override fun visit(node: Expression.Literal) {}

    override fun visit(node: Expression.PropertyAccess) {
    }

    override fun visit(node: Expression.ObjectLiteral) {
    }

    override fun visit(node: Expression.FunctionCall) {
        if(scopes.isNotEmpty()){
            val result = scopes.last().get(node.name)
            if(result == false){
                throw Runtime.Error(node.token,"Cannot call function not in scope")
            }
        }
        for(expr in node.parameter){
            resolve(expr)
        }
        // Potential error
    }

    override fun visit(node: Expression.Index) {
    }

    override fun visit(node: Statement.IfStatement) {
        resolve(node.condition)
        resolve(node.thenBranch)
        if(node.elseBranch != null){
            resolve(node.elseBranch)
        }
    }

    override fun visit(node: Statement.ReturnStatement) {
    }

    override fun visit(node: Statement.ExpressionStatement) {
        resolve(node.expression)
    }

    override fun visit(node: Statement.VariableAssignment) {
        if(scopes.isNotEmpty()){
            val result = scopes.last().get(node.leftToken.value)
            if(result == false){
                throw Runtime.Error(node.leftToken,"Assignment error!")
            }
        }
        resolve(node.value)
    }

    override fun visit(node: Statement.VariableDeclaration) {
    }

    override fun visit(node: Statement.Block) {
        beginScope()
        resolve(node.statements)
        endScope()
    }

    override fun visit(node: Statement.FunctionDeclaration) {
        declare(node.token)
        define(node.token)

        resolveFunction(node)
    }

    override fun visit(node: Statement.Empty) {
    }

    override fun visit(node: Statement.Object) {
    }

    override fun visit(node: Statement.PrintStatement) {
        resolve(node.value)
    }

    override fun visit(node: Statement.ForLoopStatement) {
    }

    override fun visit(node: Statement.IndexAssignment) {
    }

    private fun resolve(statements: List<Statement>){
        for (statement in statements){
            statement.accept(this)
        }
    }
    fun resolve(statement: Statement){
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
    private fun resolveFunction(node: Statement.FunctionDeclaration){
        beginScope()
        for(i in node.parameters){
            declare(i.leftToken)
            define(i.leftToken)
        }
        resolve(node.block)
        endScope()
    }
    private fun resolveLocal(expr: Expression, name: Token){
        for (i in scopes.size - 1 downTo 0){
            if(scopes.get(i).containsKey(name.value)){
                interpreter.resolve(expr, scopes.size - 1 -  i)
                return
            }
        }
    }
}