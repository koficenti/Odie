package today.astrum.`object`

import today.astrum.interpret.Runtime
import today.astrum.tokenizer.Token

data class ClassInstance(val klass: Class) {
    val fields: HashMap<String, Any> = HashMap()
    fun get(token: Token): Any{
        val data = fields.get(token.value)
        if(data != null) {
            return data
        }

        val method = klass.findMethod(token)

        method?.let {
            return method.bind(this)
        }

        throw Runtime.Error(token, "Could not find field name '${token.value}' on class ${klass}")
    }

    fun set(objectToken: Token, value: Any) {
        fields.set(objectToken.value, value)
    }
}