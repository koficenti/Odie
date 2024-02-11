import today.astrum.ast.*
import today.astrum.parser.ExpressionParser
import today.astrum.tokenizer.Token
import today.astrum.tokenizer.TokenEnum

class StatementParser : ExpressionParser() {
    override fun parse(tokens: List<Token>): Statement {
        this.tokens = tokens
        current = 0
        return parseStatements()
    }

    private fun parseReturn(): Statement.ReturnStatement {

        // Return value is undefined if nothing is specified
        if(match(TokenEnum.SemiColon, TokenEnum.NewLine, TokenEnum.EOF)){
            return Statement.ReturnStatement(
                Statement.ExpressionStatement(Expression.Literal(TokenEnum.UndefinedLiteral, Expression.Undefined), previous())
            )
        }

        val value = Statement.ExpressionStatement(parseExpression(), previous())

        checkValidEnd()

        return Statement.ReturnStatement(value)
    }

    private fun parseFunction(): Statement.FunctionDeclaration {
//        expect(TokenEnum.Function)
        val functionToken = consume(TokenEnum.Identifier, "Incorrect or missing identifier")
        expect(TokenEnum.LeftParen)

        val parameters = mutableListOf<Parameter>()
        while (!check(TokenEnum.RightParen)) {
            val paramName = consume(TokenEnum.Identifier, "Incorrect or missing parameter identifier")
            val paramType = parseTypeAssignments()
            parameters.add(Parameter(paramName.value, leftToken = paramName, type = paramType))
            if (!match(TokenEnum.Comma)) {
                break
            }
        }

        expect(TokenEnum.RightParen)
        val returnType = parseTypeAssignments()

        val body = parseBlock()

        return Statement.FunctionDeclaration(functionToken.value, functionToken, parameters, body, returnType)
    }

    private fun parseIfStatement(): Statement.IfStatement {
//        expect(TokenEnum.If)
        expect(TokenEnum.LeftParen)
        val condition = parseExpression()
        expect(TokenEnum.RightParen)

        val thenBranch = parseBlock()

        var elseBranch: Statement? = null
        if (match(TokenEnum.Else)) {
            if (match(TokenEnum.If)) {
                elseBranch = parseIfStatement()
            } else {
                elseBranch = parseBlock()
            }

        }

        return Statement.IfStatement(condition, thenBranch, elseBranch)
    }

    private fun parseTypeAssignments(): Type? {
        var type: Type? = null

        if (match(TokenEnum.Colon)) {
            if (match(
                    TokenEnum.NullLiteral,
                    TokenEnum.StringType,
                    TokenEnum.BooleanType,
                    TokenEnum.NumberType,
                    TokenEnum.VoidType,
                    TokenEnum.NeverType,
                    TokenEnum.StringLiteral,
                    TokenEnum.BooleanLiteral,
                    TokenEnum.FloatLiteral,
                    TokenEnum.NumberLiteral,
                )
            ) {
                type = Type.NamedType(null, previous().type, false)
            } else {
                throw Error("Expected type after colon ${previous().position}")
            }
        }
        return type
    }


    private fun parseAssignment(): Statement {
        val id = consume(TokenEnum.Identifier, "Expected Identifier at ${previous().position}")
        val left = previous()
        expect(TokenEnum.Equal)

        val result = parseExpression()


        return Statement.VariableAssignment(name = id.value, leftToken = left, rightToken = previous(), value = result)
    }

    private fun parseVariableDeclaration(): Statement {

        val kind = when (previous().type) {
            TokenEnum.Let -> Statement.DeclarationKind.LET
            TokenEnum.Var -> Statement.DeclarationKind.VAR
            TokenEnum.Const -> Statement.DeclarationKind.CONST
            else -> Statement.DeclarationKind.LET
        }

        val id = consume(TokenEnum.Identifier, "Unfinished Let Statement ${previous().position}")

        val type = parseTypeAssignments()

        if (check(TokenEnum.NewLine, TokenEnum.SemiColon)) {
            return Statement.VariableDeclaration(id.value, kind, type, null, token = id)
        }

        expect(TokenEnum.Equal)

        if (check(TokenEnum.EOF) || check(TokenEnum.NewLine)) {
            throw Error("Expecting a complete assignment statement! ${previous().position}")
        }

        val value: Statement = Statement.ExpressionStatement(parseExpression(), previous())


        val result = Statement.VariableDeclaration(id.value, kind, type, value, token = id)

        checkValidEnd()

        return result
    }

    private fun parseBlock(): Statement.Block {
        if (check(TokenEnum.ObjectLiteral)) {
            advance()
            return Statement.Block(listOf())
        }
        val statements = mutableListOf<Statement>()
        expect(TokenEnum.LeftCurlyBrace)

        while (match(TokenEnum.NewLine)) continue

        if (!check(TokenEnum.RightCurlyBrace)) {
            while (!check(TokenEnum.RightCurlyBrace)) {
                statements.add(parseStatement())
                while (match(TokenEnum.NewLine)) continue

            }
        }

        expect(TokenEnum.RightCurlyBrace)
        return Statement.Block(statements)
    }

    private fun parsePrintStatement(): Statement {
        expect(TokenEnum.LeftParen)

        val result = Statement.PrintStatement(parseExpression(), previous())

        expect(TokenEnum.RightParen)

        checkValidEnd()

        return result
    }

    private fun parseStatements(): Statement.Block {
        val statements = mutableListOf<Statement>()

        while (match(TokenEnum.NewLine)) continue
        while (!hasEnded()) {
            statements.add(parseStatement())
            while (match(TokenEnum.NewLine)) continue
        }
        return Statement.Block(statements)
    }

    private fun parsePropertyAccess(left: Expression): Statement {
        expect(TokenEnum.Dot)
        val right = parseExpression()

        return Statement.ExpressionStatement(
            Expression.PropertyAccess(left, right, previous()), previous()
        )
    }

    private fun parseForStatement(): Statement {
        expect(TokenEnum.LeftParen)
        match(TokenEnum.Let, TokenEnum.Var)
        val variable = parseVariableDeclaration() as Statement.VariableDeclaration
        val condition = parseExpression()
        expect(TokenEnum.SemiColon)

        val assignment = if(checkNext(TokenEnum.Increment)) {
            parseIncrement() as Statement.VariableAssignment
        } else if(checkNext(TokenEnum.Decrement)) {
            parseDecrement() as Statement.VariableAssignment
        } else {
            parseAssignment() as Statement.VariableAssignment
        }

        expect(TokenEnum.RightParen)

        val block = parseBlock()

        match(TokenEnum.SemiColon)

        return Statement.ForLoopStatement(condition, variable, assignment, block)
    }

    private fun parseIncrement(): Statement {
        val id = peek()
        expect(TokenEnum.Identifier)
        expect(TokenEnum.Increment)
        val expression = Expression.Binary(
            Expression.Identifier(id.value, id),
            Token(previous().value, type = TokenEnum.Plus, position = previous().position, length = 2),
            Expression.Literal(TokenEnum.NumberLiteral, 1)
        )
        match(TokenEnum.SemiColon)
        return Statement.VariableAssignment(
            leftToken = id,
            name = id.value,
            rightToken = previous(),
            value = expression
        )
    }

    private fun parseDecrement(): Statement {
        val id = peek()
        expect(TokenEnum.Identifier)
        expect(TokenEnum.Decrement)
        val expression = Expression.Binary(
            Expression.Identifier(id.value, id),
            Token(previous().value, type = TokenEnum.Minus, position = previous().position, length = 2),
            Expression.Literal(TokenEnum.NumberLiteral, 1)
        )
        match(TokenEnum.SemiColon)
        return Statement.VariableAssignment(
            leftToken = id,
            name = id.value,
            rightToken = previous(),
            value = expression
        )
    }

    private fun parseIndexAssignment(): Statement {
        val id = consume(TokenEnum.Identifier, "Expected Identifier before index assignment")
        expect(TokenEnum.LeftSquareBrace)
        val index = parseExpression()
        expect(TokenEnum.RightSquareBrace)
        expect(TokenEnum.Equal)
        val result = parseExpression()
        checkValidEnd()
        return Statement.IndexAssignment(index = index, leftToken = id, value = result)
    }

    private fun parseStatement(): Statement {
        while (match(TokenEnum.NewLine)) continue

        return when {
            match(TokenEnum.For) -> parseForStatement()
            match(TokenEnum.Print) -> parsePrintStatement()
            match(TokenEnum.Let, TokenEnum.Const, TokenEnum.Var) -> parseVariableDeclaration()
            match(TokenEnum.If) -> parseIfStatement()
            match(TokenEnum.Function) -> parseFunction()
            match(TokenEnum.Return) -> parseReturn()
            check(TokenEnum.LeftCurlyBrace) -> {
                if (checkNext(TokenEnum.Identifier) && tokens.count() > current + 2) {
                    if (tokens[current + 2].type == TokenEnum.Colon) {
                        return Statement.ExpressionStatement(parseExpression(), previous())
                    }
                }
                if (checkNext(TokenEnum.RightCurlyBrace)) {
                    expect(TokenEnum.LeftCurlyBrace)
                    expect(TokenEnum.RightCurlyBrace)
                    return Statement.Object(hashMapOf())
                }
                return parseBlock()
            }

            check(TokenEnum.EOF) -> Statement.Empty()
            check(TokenEnum.Identifier) && checkNext(TokenEnum.Equal) -> parseAssignment()
            check(TokenEnum.Identifier) && checkNext(TokenEnum.Increment) -> parseIncrement()
            check(TokenEnum.Identifier) && checkNext(TokenEnum.Decrement) -> parseDecrement()
            !hasEnded() -> {
                val previous = current
                if(match(TokenEnum.Identifier)) {
                    if(match(TokenEnum.LeftSquareBrace)){
                        while(!match(TokenEnum.RightSquareBrace)){
                            advance()
                            if(match(TokenEnum.EOF)){
                                throw Error("Expecting Right Square Brace at ${peek().position}")
                            }
                        }
                        match(TokenEnum.RightSquareBrace)
                        if(match(TokenEnum.Equal)){
                            current = previous
                            return parseIndexAssignment()
                        }
                    }
                }
                current = previous

                val expression = Statement.ExpressionStatement(parseExpression(), previous())
                if (check(TokenEnum.Dot)) {
                    return parsePropertyAccess(expression.expression)
                }
                return expression
            }

            else -> throw IllegalStateException("Unexpected token: ${peek().type}")
        }
    }


    // Extra Helpers

    private fun checkNext(type: TokenEnum): Boolean {
        if (current + 1 < tokens.count()) {
            return tokens[current + 1].type == type
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
}