package today.astrum.tokenizer

enum class TokenEnum {
    Function,
    Let, Const, Var,
    If, Else, While, For,
    Break, Continue,
    Return,

    Try, Catch, Finally,

    SingleQuote, DoubleQuote,

    NullLiteral,
    StringLiteral,
    NumberLiteral,
    FloatLiteral,
    BooleanLiteral,
    Identifier,
    TypeIdentifier,
    TrueLiteral,
    FalseLiteral,
    ObjectLiteral,
    UndefinedLiteral,
    ListLiteral,

    Pipe, And,


    StringType, NumberType, BooleanType, VoidType, AnyType, NeverType,

    Enum, Type, Interface, Namespace,

    Import, Export,
    As,

    Class, Implements, Extends, Public, Private, Protected,

    Equal, LessThan, GreaterThan,
    Plus, Minus, Asterisk, Slash,
    PlusEqual, MinusEqual,
    AsteriskEqual, SlashEqual,
    EqualEqual, NotEqual, LessThanOrEqual, GreaterThanOrEqual,
    LogicalAnd, LogicalOr, Not,

    Colon, QuestionMark, Increment, Decrement,
    Backtick,
    Dot,
    Arrow,
    SemiColon,

    LeftCurlyBrace, RightCurlyBrace,
    LeftParen, RightParen,
    LeftSquareBrace, RightSquareBrace,
    Comma,

    EOF, NewLine,

    Unknown,

    Print,
}