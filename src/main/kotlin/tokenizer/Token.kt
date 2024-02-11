package today.astrum

data class Token (
    val value: String,
    val position: Pair<Int, Int>, // Line, column
    val type: TokenEnum,
    val length: Int,
)