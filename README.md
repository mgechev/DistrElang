# DistrElang

Fork of ELang, with distributed call stack.

# Example

```
def add(a, b)
  return a + b;
enddef;

def add2(a)
  return add(a, 2);
enddef;

print add(1, 2);
print '\n';
print add2(40);
```

The result from the execution above will be:

```
3
42
```

...as expected. The interesting thing is that the program will be executed on 3 different machines:

- The "main" program will be executed on the machine onto which is run the lexer (lets call this machine `A`)
- The `add` function will be parsed and executed on different machine (lets call it `B`)
- The `add2` function will be parsed and executed on third machine (lets call it `C`)

When machine `A` tries to execute `add(1, 2)` it will call machine `B` where the `add` function resides. Later, during the execution of `add2(40)` machine `C` will call machine `B` in order to execute `add(40, 2)`.

This will happen using the current scheduling strategy. The scheduling strategies are (supposed to be) pluggable with different strategies.

# License

MIT
