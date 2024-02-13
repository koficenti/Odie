package today.astrum

import today.astrum.ast.Expression
import today.astrum.ast.Statement
import today.astrum.interpret.InterpretExpression
import today.astrum.interpret.Interpreter
import today.astrum.interpret.Runtime
import today.astrum.interpret.ScopeResolver
import today.astrum.parser.ExpressionParser
import today.astrum.parser.Parser
import today.astrum.tokenizer.Tokenizer
import java.io.InputStream

//import today.astrum.visitor.StatementPrinter


fun readFileFromResources(fileName: String): String {
    val inputStream: InputStream? = object {}.javaClass.getResourceAsStream("/$fileName")

    if (inputStream == null) {
        throw IllegalArgumentException("File not found: $fileName")
    } else {
        return inputStream.bufferedReader().use { it.readText() }
    }
}

val example = """
   class Node {
      constructor(value){
        this.value = value
        this.next = null
      }
   }
   class LinkedList {
      constructor(){
        this.head = null
        this.size = 0
      }
      append(value) {
        if(this.head == null){
            this.head = Node(value)
            return
        }
        let node = this.head
        while(node.next != null){
            node = node.next
        }
        node.next = Node(value)
        this.size = this.size + 1
      }
      remove(index){

        if(this.head == null || this.size < index){
            return false // Failed
        }
        if(index == 0){
            this.head = this.head.next
            return true // Success
        }

        let node = this.head
        let i = 1

        while(node.next != null) {
            if(i == index){
                node.next = node.next.next
                return true
            }
            node = node.next
            i = i + 1
        }
        return false
      }
      toString() {
        let node = this.head
        let str = ""
        while(node != null){
            str = str + node.value + " "
            node = node.next
        }
        return str
      }
   }

   let x = LinkedList()

   for(let i = 0; i < 10; i = i + 1){
       x.append(i) 
   }
   
   print(x.toString())
   
   // 0.0 1.0 2.0 3.0 4.0 5.0 6.0 7.0 8.0 9.0 
   
   // This will run but...
   // Currently problems with static scope checking :(
""".trimIndent()
fun main() {
    val source = StringBuilder()

    val filename = "stdlib.odie"
    var file_contents: String? = null
    try {
        val value = readFileFromResources(filename)
        file_contents = value
    } catch (e: Error) { println(e) }

    file_contents?.let {
        source.append(it)
    }

    source.append(example)

    val tokens = Tokenizer().tokenize(source.toString())
    val ast = Parser().parse(tokens);


    if(ast is Statement){
        try {
            val interpreter = Interpreter()
            val resolver = ScopeResolver(interpreter)
            ast.accept(resolver)
            ast.accept(interpreter)
        } catch(error: Runtime.Error){
            println(error.message)
        }
    }
}