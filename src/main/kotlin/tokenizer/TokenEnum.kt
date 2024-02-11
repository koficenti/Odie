package today.astrum

enum class TokenEnum {
    Function,
    Let, Const, Var,
    If, Else, While, For,
    Return,

    Try, Catch, Finally,

    SingleQuote, DoubleQuote,

    Number,
    Identifier,
    TypeIdentifier,

    Pipe, And,

    StringType, NumberType, BooleanType, VoidType, AnyType, NeverType,

    Enum, Type, Interface, Namespace,

    Import, Export,
    As,

    Class, Implements, Extends, Public, Private, Protected,

    Equal, LessThan, GreaterThan,
    Plus, Minus, Asterisk, Slash,
    EqualEqual, NotEqual, LessThanOrEqual, GreaterThanOrEqual,
    LogicalAnd, LogicalOr, Not,

    Colon, QuestionMark, Increment, Decrement,
    Backtick,
    Dot,
    Arrow,
    SemiColon,

    LeftCurlyBrace, RightCurlyBrace,
    LeftParen, RightParen,
    Comma,

    SingleLineComment, MultiLineComment,

    Unknown,
}