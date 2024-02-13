//package today.astrum.visitor
//
//import today.astrum.ast.Statement
//import today.astrum.ast.Type
//
//class StatementPrinter : StatementVisitor {
//    private val expressionPrinter = ExpressionPrinter()
//
//    private var indent = 0
//
//    private fun indent(){
//        indent += 2
//    }
//
//    private fun unindent(){
//        indent -= 2
//    }
//    private fun print_indent(){
//        if(indent != 0){
//            for(_index in 0..indent){
//                print(" ")
//            }
//        }
//    }
//
//    override fun visit(node: Statement.IfStatement) {
//        println()
//        print_indent()
//        print("if(")
//        node.condition.accept(expressionPrinter)
//        print(") {\n")
//        indent()
//        node.thenBranch.accept(this)
//        unindent()
//        print_indent()
//        print("}")
//        node.elseBranch?.let{
//            println(" else {")
//            indent()
//            it.accept(this)
//            unindent()
//            print_indent()
//            println("}")
//        }
//        println()
//    }
//
//    override fun visit(node: Statement.ReturnStatement) {
//        print_indent()
//        print("return ")
//        node.value.accept(this)
//    }
//
//    override fun visit(node: Statement.ExpressionStatement) {
//        node.expression.accept(expressionPrinter)
//    }
//
//
//    override fun visit(node: Statement.VariableDeclaration) {
//        node.type?.let {
//            when(it){
//                is Type.NamedType -> print("${it.type} ")
//                else -> {}
//            }
//        }
//        if(node.type == null) print("Unknown ")
//        print("${node.name}")
//        node.initializer?.let {
//            print(" = ")
//            it.accept(this)
//        }
//        if(node.initializer == null){
//            if(node.type != null){
//                when(node.type){
//                    is Type.NamedType -> print(" = ${node.type.type} ")
//                    else -> print(" = NotLiteralType?")
//                }
//            } else {
//                print(" = Unknown")
//            }
//        }
//    }
//
//    override fun visit(node: Statement.Block) {
//        node.statements.forEach { it.accept(this); println() }
//    }
//
//    override fun visit(node: Statement.FunctionDeclaration) {
//        val parameters: StringBuilder = StringBuilder()
//
//        node.parameters.forEach {
//            parameters.append(it.name)
//            if(it.type is Type.NamedType) {
//                it.type.type?.let { parameters.append(": ${it}") }
//            }
//            parameters.append(", ")
//        }
//        parameters.delete(parameters.length - 2, parameters.length) // delete extra comma
//
//        println("Function ${node.name} (${parameters}) {")
//        indent()
//        node.block.accept(this)
//        unindent()
//        println("}")
//    }
//
//    override fun visit(node: Statement.Empty) {
//        // Handle empty statement
//        println("<empty>")
//    }
//
//    override fun visit(node: Statement.Object) {
//        print("{ \n")
//
//        node.properties.forEach {
//            indent()
//            print_indent()
//            print("${it.key} : ")
//            it.value.accept(this)
//            println(",")
//            unindent()
//        }
//        print("}\n")
//    }
//
//    override fun visit(node: Statement.ForLoopStatement): Any {
//        TODO("Not yet implemented")
//    }
//
//    override fun visit(node: Statement.IndexAssignment): Any {
//        TODO("Not yet implemented")
//    }
//
//}
