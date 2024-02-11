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
import today.astrum.visitor.StatementPrinter

const val source = """
    function add(x: number, y: number): number {
        return x + y
    }
    
    function remove(value){
        value = null
    }
    
    const test: number = 100.0
    const test2: number = 100
    
    var result = 0
    
    var watermelon
    
    var tuna: boolean
    
    if(test > test2){
        result = test
    } else if(test == test2) {
        result = 0
    } else {
        result = test2
    }
    
    var obj = {
        name: "string",
        address: "other string"
    }
    
    obj = {
        name: "different string",
        address: "other different string"
    }
    
    print("Hello")
    
"""

val expression = """
    let arr = [6, 7, 10, 22, 10, 2, 3, 1, 0, 0, 2, 3]
   
    function sort_list(list){
        function swap(x, y){
            let tmp = list[x]
            list[x] = list[y]
            list[y] = tmp
        }
        
        for(let sorted = false; !sorted; sorted = sorted){ // Don't have while loop yet
            sorted = true
            for(let i = 0; list[i] != undefined; i++){
                if(list[i + 1] != undefined){
                    if(list[i] > list[i+1]){
                        swap(i, i+1)
                        sorted = false
                    }
                }
            }
 
        }
    }
    
    sort_list(arr)
    
    print(arr)
    
    // [0.0, 0.0, 1.0, 2.0, 2.0, 3.0, 3.0, 6.0, 7.0, 10.0, 10.0, 22.0]
    
    
    function add(x: number, y: number){
        return x + y
    }
    
    print(add(10, 9))
    
    // 19
    
        let data = [1, 2, 3, 4, 5, 6]
        
        function greaterThan(num: number){
            function helper(other: number){
                return other > num
            }
            
            return helper
        }
        
        function filter(arr, func){
            for(let i = 0; arr[i] != undefined; i++){
                if(func(arr[i])){
                    print(arr[i])
                }
            }
        }
        
        filter(data, greaterThan(3))
        
""".trimIndent()
fun main() {
//    try {
//        val tokenizer = Tokenizer()
//        val parser = Parser()
//
//        val tokens = tokenizer.tokenize(source)
//        val ast = parser.parse(tokens)
//
//        if(ast is Statement){
//            ast.accept(StatementPrinter())
//        }
//
//    } catch(e: Error){
//        println(e.message)
//    }

    val tokens = Tokenizer().tokenize(expression)
//    println(tokens)
    val ast = Parser().parse(tokens);


    if(ast is Statement){
        try {
            val interpreter = Interpreter()
            val resolver = ScopeResolver(interpreter)
            resolver.resolve(ast)
            ast.accept(interpreter)

        } catch(error: Runtime.Error){
            println(error.message)
        }
    }
}