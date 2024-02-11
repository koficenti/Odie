package today.astrum.lexer

sealed class Type : Node() {
    data class NamedType(
        val name: String, // Custom types or built in types
        val isCustom: Boolean = false
    ) : Type()

    data class FunctionType(
        val parameters: List<Parameter>,
        val returnType: Type
    ) : Type()

    data class UnionType(
        val types: List<Type>
    ) : Type()

    data class IntersectionType(
        val types: List<Type>
    ) : Type()

    data class ArrayType(
        val elementType: Type
    ) : Type()

    data class TupleType(
        val elements: List<Type>
    ) : Type()

    data class ObjectType(
        val properties: List<Property>
    ) : Type()

    data class Property(
        val name: String,
        val type: Type
    ) : Node()
}