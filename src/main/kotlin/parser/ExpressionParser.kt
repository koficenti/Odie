package today.astrum.parser

import StatementParser
import today.astrum.ast.Expression
import today.astrum.ast.Node
import today.astrum.ast.Statement
import today.astrum.function.Function
import today.astrum.tokenizer.Token
import today.astrum.tokenizer.TokenEnum

open class ExpressionParser() : Parser() {

    private var lastImportantIndex = 0

    override fun advance(): Token {
        if (!check(TokenEnum.NewLine))
            lastImportantIndex = current
        return super.advance()
    }

    private fun skipWhitespace() {
        while (match(TokenEnum.NewLine)) continue
    }

    private fun undoSkipWhitespace() {
        current = lastImportantIndex
    }

    protected fun parseExpression(): Expression {
        return assignment() // ==, !=, >=, <=
    }

    private fun assignment(): Expression{
        val expr = equality()

        if(match(TokenEnum.Equal)){
            val token = previous()
            val value = assignment()

            if(expr is Expression.Identifier){
                val name = expr.name
                return Expression.VariableAssignment(name, expr.token, value)
            } else if(expr is Expression.Get){
                return Expression.Set(expr.name, expr.obj, value)
            }

            throw Error("Invalid assignment target")
        }

        return expr
    }

    private fun equality(): Expression {
        var expr: Expression = comparison()

        while (match(TokenEnum.NotEqual, TokenEnum.EqualEqual, TokenEnum.LogicalAnd, TokenEnum.LogicalOr)) {
            val operator = previous()
            val right = comparison()

            expr = Expression.Binary(expr, operator, right)
        }

        return expr
    }

    private fun comparison(): Expression {
        var expr: Expression = term()

        while (match(
                TokenEnum.GreaterThan,
                TokenEnum.LessThan,
                TokenEnum.GreaterThanOrEqual,
                TokenEnum.LessThanOrEqual,
            )
        ) {
            val operator = previous()
            val right = term()
            expr = Expression.Binary(expr, operator, right)
        }

        return expr
    }

    private fun term(): Expression {
        var expr: Expression = factor()

        while (match(TokenEnum.Minus, TokenEnum.Plus)) {
            val operator = previous()
            val right = factor()
            expr = Expression.Binary(expr, operator, right)
        }

        return expr
    }

    private fun factor(): Expression {
        var expr: Expression = unary()

        while (match(TokenEnum.Slash, TokenEnum.Asterisk)) {
            val operator = previous()
            val right = unary()
            expr = Expression.Binary(expr, operator, right)
        }

        return expr
    }

    private fun unary(): Expression {
        if (match(TokenEnum.Not, TokenEnum.Minus)) {
            val operator = previous()
            val right = unary()

            return Expression.Unary(operator, right)
        }
        return call()
    }

    private fun call(): Expression {
        var expr = primary()
        while(true){
            if(match(TokenEnum.LeftParen)){
                expr = finishCall(expr)
            } else if(match(TokenEnum.Dot)){
                val token = consume(TokenEnum.Identifier, "Expected Identifier after dot")
                expr = Expression.Get(expr, token)
            } else {
                break
            }
        }
        return expr
    }

    private fun finishCall(callee: Expression): Expression {
        val arguments = mutableListOf<Expression>()
        if(!check(TokenEnum.RightParen)){
            do {
                if (arguments.count() >= 255) {
                    throw Error("Can't have more than 255 arguments. ${peek().position}");
                }
                arguments.add(parseExpression())
            } while (match(TokenEnum.Comma))
        }
        val paren = consume(TokenEnum.RightParen, "Expected closing parenthesis at ${peek().position}")
        return Expression.FunctionCall(callee, paren, arguments)
    }

    private fun primary(): Expression {
        if(match(TokenEnum.Function)){
            val errorToken = previous()
            expect(TokenEnum.LeftParen)
            expect(TokenEnum.RightParen)
            if(match(TokenEnum.ObjectLiteral)){
                return Expression.AnonymousFunction(errorToken, Statement.Block(listOf()))
            }
            expect(TokenEnum.LeftCurlyBrace)
            val source = mutableListOf<Token>()

            var stack = 0

            while(true){
                if(check(TokenEnum.LeftCurlyBrace)){
                    stack++
                }
                if(check(TokenEnum.EOF)){
                    throw Error("Expecting closing bracket but reached end of file ${errorToken.position}")
                }
                if(check(TokenEnum.RightCurlyBrace)){
                        if(stack == 0) {
                        break
                    }
                    stack--
                }
                source.add(peek())
                advance()
            }
            expect(TokenEnum.RightCurlyBrace)

            try {
                source.add(Token(
                    type = TokenEnum.EOF,
                    length = 0,
                    position = peek().position,
                    value = ""
                ))
                val statement = Parser().parse(source)
                return Expression.AnonymousFunction(errorToken, statement as Statement.Block)
            } catch(e : Error){
                throw e
            }
        }
        if(match(TokenEnum.This)){
            return Expression.This(previous())
        }
        if (match(
                TokenEnum.BooleanLiteral,
                TokenEnum.NumberLiteral,
                TokenEnum.FloatLiteral,
                TokenEnum.StringLiteral,
                TokenEnum.TrueLiteral,
                TokenEnum.FalseLiteral,
                TokenEnum.ObjectLiteral,
                TokenEnum.NullLiteral,
                TokenEnum.UndefinedLiteral,
            )
        ) {
            var literal = previous().literal
            if (previous().type == TokenEnum.NullLiteral) {
                literal = Expression.Null
            } else if (previous().type == TokenEnum.UndefinedLiteral) {
                literal = Expression.Undefined
            }
            return Expression.Literal(previous().type, literal)
        }
        if (match(TokenEnum.Identifier)) {
            val token = previous()

            // data[0] <- parses indexing

            if (match(TokenEnum.LeftSquareBrace)){
                val expression = parseExpression()
                expect(TokenEnum.RightSquareBrace)
                return Expression.Index(name = token.value, token, expression)
            }

            return Expression.Identifier(name = token.value, token)
        }
        if (match(TokenEnum.LeftParen)) {
            val expr: Expression = parseExpression()
            consume(TokenEnum.RightParen, "Expected ')' after expression ${previous().position}")
            return Expression.Grouping(expr)
        }
        if (match(TokenEnum.LeftSquareBrace)){
            val list = mutableListOf<Expression>()
            while(!check(TokenEnum.RightSquareBrace)){
                list.add(parseExpression())
                if(!check(TokenEnum.RightSquareBrace)){
                    expect(TokenEnum.Comma)
                }
            }
            expect(TokenEnum.RightSquareBrace)
            return Expression.Literal(TokenEnum.ListLiteral, list)
        }
        if (checkIfObject()) {
            val properties = hashMapOf<String, Any>()

            while (!check(TokenEnum.RightCurlyBrace)) {
                skipWhitespace()
                val left = consume(TokenEnum.Identifier, "Expected Identifier after Right Curly Brace")
                expect(TokenEnum.Colon)
                val right = parseExpression()
                skipWhitespace()
                if (!check(TokenEnum.RightCurlyBrace)) {
                    expect(TokenEnum.Comma)
                }
                skipWhitespace()
                properties[left.value] = right
            }

            skipWhitespace()
            expect(TokenEnum.RightCurlyBrace)

            return Expression.ObjectLiteral(properties)
        }
        throw Error("Problem parsing expression! ${peek()}")
    }

    private fun checkIfObject(): Boolean {
        if (match(TokenEnum.LeftCurlyBrace) && !hasEnded()) {
            skipWhitespace()
            if (check(TokenEnum.Identifier) && tokens.count() > current + 1) {
                if (tokens[current + 1].type == (TokenEnum.Colon)) {
                    return true
                }
            }
        }
        return false
    }

    private fun checkValidEnd() {
        if (!match(TokenEnum.SemiColon, TokenEnum.NewLine) && peek().type != TokenEnum.EOF) {
            if (!check(TokenEnum.RightCurlyBrace)) {
                throw Error("Expects either semicolon, newline, or eof after assignment statement! ${peek().position}")
            }
        }
    }

    override fun parse(tokens: List<Token>): Node {
        this.tokens = tokens

        return parseExpression()
    }
}