package today.astrum.ast

import today.astrum.tokenizer.TokenEnum

sealed class Type : Node() {
    data class NamedType(
        // Custom types or built in types
        val identifier: String?,
        val type: TokenEnum,
        val isCustom: Boolean = false
    ) : Type()

    data class TypeAlias(
        // Custom type using typealias
        val aliasName: String,
        val type: Type
    ) : Type()

    data class LiteralType(
        // let fruit: 'apple' | 'orange' = 'orange'
        // Any literal can also be a type
        val value: Any
    )

    data class FunctionType(
        // (x: number, y: number) => number
        val parameters: List<Parameter>,
        val returnType: Type
    ) : Type()

    data class UnionType(
        // boolean | null
        val types: List<Type>
    ) : Type()

    data class IntersectionType(
        // String & Number
        val types: List<Type>
    ) : Type()

    data class ArrayType(
        // number[]
        val elementType: Type
    ) : Type()

    data class TupleType(
        // [boolean, number[]]
        val elements: List<Type>
    ) : Type()

    data class ObjectType(
        // { x: boolean, y: boolean }
        val properties: List<Property>
    ) : Type()

    data class Property(
        val name: String,
        val type: Type
    ) : Node()

    data class GenericType(
        // <T>
        val name: String,
        val typeParameters: List<TypeParameter>,
        val type: Type
    ) : Type()

    data class TypeParameter(
        // <T extends String>
        val name: String,
        val extendsType: Type?
    ) : Type()

}