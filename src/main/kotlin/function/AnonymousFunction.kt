package today.astrum.function

import today.astrum.ast.Expression
import today.astrum.ast.Statement
import today.astrum.interpret.Interpreter
import today.astrum.interpret.Return
import today.astrum.interpret.Scope

class AnonymousFunction(val block: Statement.Block, val scope: Scope) : Callable {
    override fun arity(): Int {
        return 0
    }

    override fun call(interpreter: Interpreter, arguments: List<Any>): Any {
        try{
            interpreter.executeBlock(block.statements, Scope(scope))
        } catch(returnValue: Return){
            return returnValue.value!!
        }
        return Expression.Undefined
    }

    override fun toString(): String {
        return "@AnonymousFunction"
    }
}