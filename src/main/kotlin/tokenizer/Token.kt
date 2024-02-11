package today.astrum.tokenizer

data class Token (
    val value: String,
    val position: Pair<Int, Int>, // Line, column
    val type: TokenEnum,
    val length: Int,
    val literal: Any?  = null
)