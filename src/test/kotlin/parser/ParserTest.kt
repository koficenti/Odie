package parser

import kotlin.test.Test
import StatementParser
import today.astrum.tokenizer.Token
import kotlin.test.assertEquals

class ParserTest{
    val parseTokens = { tokens: List<Token> -> StatementParser().parse(tokens)}
    @Test
    fun parseIfStatement(): Unit {

    }
}
