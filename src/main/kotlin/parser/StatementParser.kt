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

        skipNewLine()

        if (!check(TokenEnum.RightCurlyBrace)) {
            while (!check(TokenEnum.RightCurlyBrace)) {
                statements.add(parseStatement())
                skipNewLine()

            }
        }

        expect(TokenEnum.RightCurlyBrace)
        return Statement.Block(statements)
    }

    private fun parseStatements(): Statement.Block {
        val statements = mutableListOf<Statement>()

        skipNewLine()
        while (!hasEnded()) {
            statements.add(parseStatement())
            skipNewLine()
        }
        return Statement.Block(statements)
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

    private fun parseWhileStatement(): Statement {
        val start_token = previous() // For enriching error messages later
        expect(TokenEnum.LeftParen)
        val condition = parseExpression()
        expect(TokenEnum.RightParen)
        val body = parseStatement()

        return Statement.WhileLoopStatement(condition, body, start_token)
    }

    private fun parseForStatement(): Statement {
        val start_token = previous()

        expect(TokenEnum.LeftParen)

        val init: Statement? = if(match(TokenEnum.SemiColon)){
            null
        } else if(match(TokenEnum.Let, TokenEnum.Var)){
            parseVariableDeclaration()
        } else {
            Statement.ExpressionStatement(parseExpression(), previous())
        }

        val condition = if(!check(TokenEnum.SemiColon)){
            parseExpression()
        } else {
            null
        }

        expect(TokenEnum.SemiColon)

        val increment = if(!check(TokenEnum.RightParen)){
            parseExpression()
        } else {
            null
        }

        expect(TokenEnum.RightParen)

        var body: Statement? = null

        if(increment != null){
            body = Statement.Block(
                listOf(parseStatement(), Statement.ExpressionStatement(increment, previous()))
            )
        } else {
            body = parseStatement()
        }

        if(condition != null){
            body = Statement.WhileLoopStatement(condition, body, start_token)
        }

        if(init != null) {
            body = Statement.Block(
                listOf(init, body)
            )
        }

        return body
    }

    private fun parseClass(): Statement {
        val id = consume(TokenEnum.Identifier, "Expected identifier at ${peek().position}")
        skipNewLine()
        expect(TokenEnum.LeftCurlyBrace)
        val methods = mutableListOf<Statement.FunctionDeclaration>()
        skipNewLine()
        while(!check(TokenEnum.RightCurlyBrace) && !hasEnded()){
            skipNewLine()
            methods.add(parseFunction())
            skipNewLine()
        }

        expect(TokenEnum.RightCurlyBrace)

        return Statement.Class(id, methods, null)
    }

    private fun parseStatement(): Statement {
        skipNewLine()

        return when {
            match(TokenEnum.Class) -> parseClass()
            match(TokenEnum.For) -> parseForStatement()
            match(TokenEnum.While) -> parseWhileStatement()
            match(TokenEnum.Let, TokenEnum.Const, TokenEnum.Var) -> parseVariableDeclaration()
            match(TokenEnum.If) -> parseIfStatement()
            check(TokenEnum.Function) -> {
                if(checkNext(TokenEnum.Identifier)){
                    expect(TokenEnum.Function)
                    return parseFunction()
                }
                return Statement.ExpressionStatement(parseExpression(), peek())
            }
            match(TokenEnum.Return) -> parseReturn()
            match(TokenEnum.LeftCurlyBrace) -> {
                val previous = current - 1
                skipNewLine()
                if (match(TokenEnum.Identifier)) {
                    if (match(TokenEnum.Colon)) {
                        current = previous
                        return Statement.ExpressionStatement(parseExpression(), previous())
                    }
                }
                current = previous
                if (checkNext(TokenEnum.RightCurlyBrace)) {
                    expect(TokenEnum.LeftCurlyBrace)
                    expect(TokenEnum.RightCurlyBrace)
                    return Statement.Object(hashMapOf())
                }
                return parseBlock()
            }

            check(TokenEnum.EOF) -> Statement.Empty()
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

                return expression
            }

            else -> throw IllegalStateException("Unexpected token: ${peek().type}")
        }
    }


    // Extra Helpers

    private fun skipNewLine(){
        while (match(TokenEnum.NewLine)) continue
    }
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