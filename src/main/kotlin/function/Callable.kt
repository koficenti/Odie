package today.astrum.function

import today.astrum.interpret.Interpreter

internal interface Callable {
    fun arity(): Int
    fun call(interpreter: Interpreter, arguments: List<Any>): Any
}