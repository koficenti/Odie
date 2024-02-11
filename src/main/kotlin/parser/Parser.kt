package today.astrum.parser

import StatementParser
import today.astrum.ast.Expression
import today.astrum.ast.Node
import today.astrum.ast.Statement
import today.astrum.tokenizer.Token
import today.astrum.tokenizer.TokenEnum

open class Parser {
    protected var tokens = listOf<Token>()
    protected var current = 0


    protected fun consume(type: TokenEnum, s: String): Token {
        if (check(type)) return advance()

        throw Error(s)
    }

    protected fun match(vararg tokens: TokenEnum): Boolean {
        for (token in tokens) {
            if (check(token)) {
                advance()
                return true
            }
        }

        return false
    }

    protected open fun advance(): Token {
        if (!hasEnded()) current++
        return previous()
    }

    protected fun hasEnded(): Boolean {
        return peek().type == TokenEnum.EOF
    }

    protected fun check(vararg token: TokenEnum): Boolean {
        if (hasEnded()) return false
        for (t in token){
            if(peek().type == t){
                return true
            }
        }
        return false
    }

    protected open fun peek(): Token {
        return tokens[current]
    }

    protected open fun previous(): Token {
        return tokens[current - 1]
    }

    protected fun expect(tokenType: TokenEnum) {
        val token = advance()
        if (token.type != tokenType) {
            throw IllegalStateException("Expected token type $tokenType, found ${token.type}")
        }
    }


    open fun parse(tokens: List<Token>): Node {
        return StatementParser().parse(tokens)
    }
}