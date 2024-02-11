package today.astrum.ast

import today.astrum.tokenizer.Token

data class Parameter(
    val name: String,
    val type: Type? = null,
    val leftToken: Token,
    val rightToken: Token? = null
)