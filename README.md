
### Codename Odie: Typescript Interpreter

#### Features Implemented:

- Basic scoping
- function / returning functions / function calls (no function expressions yet, () => {}, function () { })
- objects and property access operator ( obj.name and {} creates empty object literal )
- let, var, const keywords (all treated the same currently)
- return keyword
- for loops (traditional)
- blocks { }
- arrays literals and indexing (set or get) with some bound checks
- undefined and null, array out of bounds will give undefined when indexing
- does parse some types but doesn't do anything with them yet!
- ##### conditions
  - false && doSomething() <- will never run! (probably one of my favorite features)
  - ==, !=, >=, <=, >, <

#### Example of what can be run as of now
```typescript

let arr = [6, 7, 10, 22, 10, 2, 3, 1, 0, 0, 2, 3]

function sort_list(list) {
    function swap(x, y) {
        let tmp = list[x]
        list[x] = list[y]
        list[y] = tmp
    }

    for (let sorted = false; !sorted; sorted = sorted) { // Don't have while loop yet
        sorted = true
        for (let i = 0; list[i] != undefined; i++) {
            if (list[i + 1] != undefined) {
                if (list[i] > list[i + 1]) {
                    swap(i, i + 1)
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

// 4.0, 5.0, 6.0

const project = {
    name: "Odie",
    version: "0.0.1"
}

print(project.name + " -- " + project.version)

// Odie -- 0.0.1

```

### TODO

- advanced scoping (scope resolution / static scope checking)
- automatic primitive wrapping (so Number(0.0) or String("example")) when using dot operator
- const as immutable
- more array features
- object property assignments
- eventually some basic type checking
- while loop and switch statements
- ternary operator ( true ? action() : never() )
- better error handling
- testing (most definitely going to be 'unknown' bugs)
- a ton of other stuff

### Why?

Just trying to learn some things

### References
https://craftinginterpreters.com (good stuff)