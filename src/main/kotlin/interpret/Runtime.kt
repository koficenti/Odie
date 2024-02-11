package today.astrum.interpret

import today.astrum.tokenizer.Token

data class RuntimeError(val token: Token, override val message: String?) : RuntimeException()