package today.astrum.tokenizer

object TokenLookup {
    private val keywords = hashMapOf<String, TokenEnum>(
        "for" to TokenEnum.For,
        "while" to TokenEnum.While,
        "if" to TokenEnum.If,
        "else" to TokenEnum.Else,
        "function" to TokenEnum.Function,
        "let" to TokenEnum.Let,
        "var" to TokenEnum.Var,
        "const" to TokenEnum.Const,
        "return" to TokenEnum.Return,
        "try" to TokenEnum.Try,
        "catch" to TokenEnum.Catch,
        "finally" to TokenEnum.Finally,
        "class" to TokenEnum.Class,
        "extends" to TokenEnum.Extends,
        "public" to TokenEnum.Public,
        "private" to TokenEnum.Private,
        "protected" to TokenEnum.Protected,
        "string" to TokenEnum.StringType,
        "void" to TokenEnum.VoidType,
        "number" to TokenEnum.NumberType,
        "boolean" to TokenEnum.BooleanType,
        "any" to TokenEnum.AnyType,
        "never" to TokenEnum.NeverType,
        "undefined" to TokenEnum.UndefinedLiteral,
        "enum" to TokenEnum.Enum,
        "interface" to TokenEnum.Interface,
        "type" to TokenEnum.Type,
        "namespace" to TokenEnum.Namespace,
        "import" to TokenEnum.Import,
        "export" to TokenEnum.Export,
        "as" to TokenEnum.As,
        "null" to TokenEnum.NullLiteral,
        "break" to TokenEnum.Break,
        "continue" to TokenEnum.Continue,
        "true" to TokenEnum.TrueLiteral,
        "false" to TokenEnum.FalseLiteral,
        "{}" to TokenEnum.ObjectLiteral,
        "this" to TokenEnum.This
    )

    fun findKeyword(str: String): TokenEnum {
        keywords.get(str)?.let {
            return it
        }
        return TokenEnum.Unknown
    }
}