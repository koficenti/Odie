package today.astrum.tokenizer

class Tokenizer {
    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 1
    private var column: Int = 0

    private val tokens = mutableListOf<Token>()

    private var source: String = ""


    private fun MutableList<Token>.add(type: TokenEnum) {
        tokens.add(
            Token(
                value = source.substring(start, current),
                position = Pair(line, column),
                type = type,
                length = current - start,
                literal = null
            )
        )
    }

    private fun MutableList<Token>.add(type: TokenEnum, literal: Any? = null) {
        tokens.add(
            Token(
                value = source.substring(start, current),
                position = Pair(line, column),
                type = type,
                length = current - start,
                literal = literal
            )
        )
    }

    private fun advance(): Char {
        column++
        return source[current++]
    }

    private fun peek(): Char? {
        if (hasEnded()) return null;
        return source[current]
    }

    private fun peekNext(): Char? {
        if (current + 1 < source.length) {
            return source[current + 1]
        }
        return null
    }

    private fun match(expected: Char): Boolean {
        if (hasEnded()) return false
        if (peek() != expected) return false

        advance()
        return true
    }

    private fun hasEnded() = current >= source.length

    private fun parseString() {
        var c = peek()
        while (c != null && c != '"' && !hasEnded()) {
            advance()
            c = peek()
        }
        if (hasEnded()) {
            throw Error("Incomplete string ${line},${column}")
        }

        advance()

        tokens.add(TokenEnum.StringLiteral, source.substring(start + 1, current - 1))
    }

    private fun parseNumber() {
        var c = peek()
        while (c != null && c.isDigit() && !hasEnded()) {
            advance()
            c = peek()
        }

        val after = peekNext()
        if (c == '.' && after != null && after.isDigit()) {
            advance()

            c = peek()
            while (c != null && c.isDigit() && !hasEnded()) {
                advance()
                c = peek()
            }
            tokens.add(TokenEnum.FloatLiteral, source.substring(start, current).toFloatOrNull())
            return
        }

        tokens.add(TokenEnum.NumberLiteral, source.substring(start, current).toInt())
    }

    private fun parseIdentifier() {
        var c = peek()

        while (c != null && (c.isLetterOrDigit() || c == '_') && !hasEnded()) {
            c = peekNext()
            advance()
        }

        var result = TokenLookup.findKeyword(source.substring(start, current))

        if (result == TokenEnum.Unknown) {
            result = TokenEnum.Identifier
        }

        when (result) {
            TokenEnum.TrueLiteral -> tokens.add(result, true)
            TokenEnum.FalseLiteral -> tokens.add(result, false)
            else -> tokens.add(result)
        }
    }

    private fun scanToken() {
        val char = advance()
        when (char) {
            '[' -> tokens.add(TokenEnum.LeftSquareBrace)
            ']' -> tokens.add(TokenEnum.RightSquareBrace)
            '(' -> tokens.add(TokenEnum.LeftParen)
            ')' -> tokens.add(TokenEnum.RightParen)
            '{' -> {
                if (match('}')) {
                    tokens.add(TokenEnum.ObjectLiteral)
                } else {
                    tokens.add(TokenEnum.LeftCurlyBrace)
                }
            }

            '}' -> tokens.add(TokenEnum.RightCurlyBrace)
            ',' -> tokens.add(TokenEnum.Comma)
            '.' -> tokens.add(TokenEnum.Dot)
            '!' -> tokens.add(if (match('=')) TokenEnum.NotEqual else TokenEnum.Not)
            '=' -> tokens.add(
                if (match('='))
                    TokenEnum.EqualEqual
                else if (match('>'))
                    TokenEnum.Arrow
                else TokenEnum.Equal
            )

            '+' -> {
                if (match('+')) tokens.add(TokenEnum.Increment) else
                    tokens.add(if (match('=')) TokenEnum.PlusEqual else TokenEnum.Plus)
            }

            '-' -> {
                if (match('-')) tokens.add(TokenEnum.Decrement) else
                    tokens.add(if (match('=')) TokenEnum.MinusEqual else TokenEnum.Minus)
            }
            '/' ->
                if (match('*'))
                    TODO("Implement multiline comments")
                else if (match('/')) {
                    var c = peek()
                    while (c != null && c != '\n' && !hasEnded()) {
                        advance()
                        c = peek()
                    }
                } else if (match('='))
                    tokens.add(TokenEnum.SlashEqual)
                else
                    tokens.add(TokenEnum.Slash)

            '*' -> tokens.add(if (match('=')) TokenEnum.AsteriskEqual else TokenEnum.Asterisk)
            ';' -> tokens.add(TokenEnum.SemiColon)
            ':' -> tokens.add(TokenEnum.Colon)
            '>' -> tokens.add(if (match('=')) TokenEnum.GreaterThanOrEqual else TokenEnum.GreaterThan)
            '<' -> tokens.add(if (match('=')) TokenEnum.LessThanOrEqual else TokenEnum.LessThan)
            '&' -> tokens.add(if (match('&')) TokenEnum.LogicalAnd else TokenEnum.And)
            '|' -> tokens.add(if (match('|')) TokenEnum.LogicalOr else TokenEnum.Pipe)
            ' ', '\r', '\t' -> {}
            '\n' -> {
                line++
                column = 0
                tokens.add(TokenEnum.NewLine)
            }

            '"' -> {
                parseString()
            }

            else -> {
                if (char.isDigit()) {
                    parseNumber()
                } else if (char.isLetter()) {
                    parseIdentifier()
                } else {
                    TODO("Add error here!")
                }
            }
        }
    }

    fun tokenize(code: String): List<Token> {
        source = code

        while (!hasEnded()) {
            start = current
            scanToken()
        }

        start = current

        tokens.add(TokenEnum.EOF, null)

        return tokens
    }
}