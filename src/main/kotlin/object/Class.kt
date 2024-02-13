package today.astrum.`object`

import today.astrum.function.Callable
import today.astrum.function.Function
import today.astrum.interpret.Interpreter
import today.astrum.tokenizer.Token

class Class(private val name: String, private val methods: HashMap<String, Function>) : Callable {
    override fun arity(): Int {
        val constructor = findMethod("constructor")
        constructor?.let {
            return it.arity()
        }
        return 0
    }

    override fun call(interpreter: Interpreter, arguments: List<Any>): ClassInstance {
        val instance = ClassInstance(this)
        val constructor = findMethod("constructor")
        constructor?.bind(instance)?.call(interpreter, arguments)
        return instance
    }

    override fun toString(): String {
        return "@class " + name
    }

    fun findMethod(token: Token): Function?{
        if(methods.containsKey(token.value)){
            return methods[token.value]
        }
        return null
    }
    fun findMethod(name: String): Function?{
        if(methods.containsKey(name)){
            return methods[name]
        }
        return null
    }
}