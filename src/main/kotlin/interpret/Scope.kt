package today.astrum.interpret

import today.astrum.tokenizer.Token

class Scope {
    private var enclosing: Scope? = null
    private val variables = hashMapOf<String, Any?>()
    fun define(token: Token, value: Any?) {
        if(variables.containsKey(token.value)){
            throw Runtime.Error(token, "'${token.value}' already exist! ${token.position}")
        }
        variables[token.value] = value
    }

    fun assign(name: Token, value: Any?){
        if(variables.containsKey(name.value)) {
            variables[name.value] = value
        } else if(enclosing != null) {
            enclosing!!.assign(name, value)
        } else {
            throw Runtime.Error(name,"Trying to set value to nonexistent variable ${name.value}")
        }
    }

    fun get(name: Token): Any? {
        if (variables.containsKey(name.value)) {
            return variables.get(name.value)
        }
        if (enclosing != null) {
            return enclosing!!.get(name)
        }
        throw Runtime.Error(name, "Undefined variable '${name.value}'.")
    }

    fun getAt(distance: Int, name: String?): Any {
        return ancestor(distance).variables.get(name)!!
    }

    fun assignAt(distance: Int, name: Token, value: Any?) {
        ancestor(distance).variables.put(name.value, value)
    }

    fun ancestor(distance: Int): Scope {
        var environment: Scope = this
        for (i in 0 until distance) {
            environment = environment.enclosing!!
        }

        return environment
    }

    constructor()
    constructor(enclosing: Scope) {
        this.enclosing = enclosing
    }
}