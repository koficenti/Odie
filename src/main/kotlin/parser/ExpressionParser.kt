package today.astrum.parser

import today.astrum.ast.Expression
import today.astrum.ast.Node
import today.astrum.ast.Parameter
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
        return equality() // ==, !=, >=, <=
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

        while (match(TokenEnum.Slash, TokenEnum.Asterisk, TokenEnum.Dot)) {
            val operator = previous()
            val right = unary()
            if (operator.type == TokenEnum.Dot) {
                expr = Expression.PropertyAccess(left = expr, right = right, token = operator)
            } else {
                expr = Expression.Binary(expr, operator, right)
            }
        }

        return expr
    }

    private fun unary(): Expression {
        if (match(TokenEnum.Not, TokenEnum.Minus)) {
            val operator = previous()
            val right = unary()

            return Expression.Unary(operator, right)
        }
        return primary()
    }

    private fun primary(): Expression {
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
            val parameters = mutableListOf<Expression>()

            // doSomething() <- parses function calls
            if (check(TokenEnum.LeftParen)) {
                expect(TokenEnum.LeftParen)
                while(!check(TokenEnum.RightParen)){
                    parameters.add(parseExpression())

                    if(!check(TokenEnum.RightParen)){
                        expect(TokenEnum.Comma)
                    }
                }
                expect(TokenEnum.RightParen)
                return Expression.FunctionCall(name = token.value, token, parameters)
            }

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
            val properties = hashMapOf<String, Expression>()

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

    private fun isLiteral(): Boolean{
        return check(TokenEnum.BooleanLiteral,
            TokenEnum.NumberLiteral,
            TokenEnum.FloatLiteral,
            TokenEnum.StringLiteral,
            TokenEnum.TrueLiteral,
            TokenEnum.FalseLiteral,
            TokenEnum.ObjectLiteral,
            TokenEnum.NullLiteral,
            TokenEnum.UndefinedLiteral)
    }

    override fun parse(tokens: List<Token>): Node {
        this.tokens = tokens

        return parseExpression()
    }
}