package today.astrum.function

import today.astrum.ast.Expression
import today.astrum.ast.Statement
import today.astrum.interpret.Interpreter
import today.astrum.interpret.Return
import today.astrum.interpret.Scope
import today.astrum.`object`.ClassInstance

class Function(
    private val function: Statement.FunctionDeclaration,
    private val closure: Scope,
    private val isConstructor: Boolean
) : Callable {


    override fun arity(): Int {
        return function.parameters.count()
    }

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        val scope = Scope(closure)
        function.parameters.forEachIndexed { index, item ->
            scope.define(item.leftToken, arguments[index])
        }

        try {
            interpreter.executeBlock(function.block.statements, scope)
        }catch (returnValue: Return) {
            return returnValue.value!!
        }

        if(isConstructor) return closure.getAt(0, "this")

        return Expression.Undefined
    }

    fun bind(value: Any): Function {
        when(value){
            is ClassInstance -> {
                val newScope = Scope(closure)
                newScope.define("this", value)
                return Function(function, newScope, isConstructor)
            }
        }
        throw Error("Cannot bind ${value} to 'this'")
    }
}