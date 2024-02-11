package today.astrum.interpret

import today.astrum.tokenizer.Token

sealed class Runtime{
    data class Error(val token: Token, override val message: String?) : RuntimeException()

    companion object {
        fun checkNumberOperands(token: Token, vararg operand: Any){
            for (op in operand){
                if(op !is Number){
                    throw Runtime.Error(token, "Operand must be a valid number")
                }
            }
        }

        fun checkDivisionByZero(token: Token, operand: Any){
            checkNumberOperands(token, operand)
            if((operand as Number) == 0){
                throw Runtime.Error(token, "Cannot divide by zero")
            }
        }
    }
}
