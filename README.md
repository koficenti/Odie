
### Codename Odie: Typescript Interpreter

#### Features Implemented:

- Basic scoping
- function / returning functions / function calls (no function expressions yet, () => {}, function () { })
- objects and property access operator ( obj.name and {} creates empty object literal )
- let, var, const keywords (all treated the same currently)
- return keyword
- for loops (traditional)
- blocks { }
- automatic primitive wrapping (so Number(0.0) or String("example")) when using dot operator
- arrays literals and indexing (set or get) with some bound checks
- undefined and null, array out of bounds will give undefined when indexing
- does parse some types but doesn't do anything with them yet!
- basic class support (also 'this' keyword works)
- ##### conditions
  - false && doSomething() <- will never run! (probably one of my favorite features)
  - ==, !=, >=, <=, >, <

#### Example of what can be run as of now
```typescript

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

```

### TODO

- advanced scoping (scope resolution / static scope checking) *partially done
- const as immutable
- more array features
- eventually some basic type checking
- switch statements
- ternary operator ( true ? action() : never() )
- better error handling
- improve syntax handling (handling newlines and semicolons better for example)
- testing (most definitely going to be 'unknown' bugs)
- a ton of other stuff

### Why?

Just trying to learn some things

### References
https://craftinginterpreters.com (good stuff)