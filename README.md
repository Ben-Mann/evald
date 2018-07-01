#evald

Evaluator-Double

A simple Java expression parser and evaluator for numeric expressions using doubles. 
Lightweight, small footprint for fast parsing and rapid evaluation of numeric expressions.

## Usage
There are a few ways to use evald

### Optimal Usage
Avoid hash table lookups when updating variables by caching an index for each variable that may be updated by your expression.

```java
//Create a parser context
Evald evald = new Evald();

//Add a variable, and get an index
int aIndex = evald.addVariable("a");
int bIndex = evald.addVariable("b");

//Parse something
evald.parse("a * b");

//Set some values
evald.setVariable(aIndex, aValue);
evald.setVariable(bIndex, bValue);

//Evaluate
double result = evald.evaluate();
```

### Simplified Usage
Pay the hash table lookup cost for a slightly slower update before evaluation

```java
//Create a parser context
Evald evald = new Evald();

//Parse something
evald.parse("a * b");

//Set the values
evald.addVariable("a", aValue);
evald.addVariable("b", bValue);

//Evaluate
double result = evald.evaluate();
```

## Performance
On parsing an expression, constant parts of the expression will be simplified to a single constant value.
 
For expressions not so reduced, evaluation time for each node in an expression's parse tree is usually equivalent to a function call, a Double unboxing/boxing*, and the java equivalent to the operation.

For example, the call

```java
Evald evald = new Evald();
evald.parse("v * (sin(toRadians(90))^2) + 1");
```

is, on parsing, reduced to ```v + 1``` (as ```sin(toRadians(90))``` evaluates to ```1```; ```1^2``` evaluates to ```1```, and ```v*1``` optimises to ```v```)

## Multiple Expressions
Evald supports evaluation of multiple expressions, creating multiple outputs, and manages expression dependencies for you.

Each expression must be of the format [variable] = [expression] and a semicolon must be provided before specifying another expression. That is, the following is legal:

```java
x = (a + b + a * b) / (a + 2 * b) / b;
y = (a + 2 * b) / (a + b + a * b) / a;
out1 = 2 * x * y; out2 = 2 / x / y;
```

and creates a sequence of expressions which require two inputs `a` and `b`, and which creates four intermediate or output variables - `x`, `y`, `out1` and `out2`.

The expression can be evaluated identically to those above, however note that the `evaluate()` method will return the _last_ expression result (`out2` in this case). To retrieve multiple outputs from the expression you can request the variable's value directly.

```java
//get out1 using a hashmap lookup internally
double out1Value = evald.getVariableValue("out1");
//or, record the index of a variable up front, and then later get its value by index without the hash lookup penalty
int ix = evald.getVariableIndex("x");
double xValue = evald.getVariableValue("x");
```

### Only Execute Needed Expressions
If you know which outputs you need from an expression, it's most efficient to declare these up front. In the example above, if we only need `x` and `y`, there's no need to compute `out1` or `out2`.

```java
evald.enableOutputs("x", "y");
//Now, when we call evald.evaluate(), the out1 and out2 expressions will be bypassed
```

You can supply either tokens or indices to `enableOutputs`; note also that you can clear these optimisations with `enableAllOutputs`.

```java
//If you needed to repeatedly change the output this will avoid a hash table lookup
evald.enableOutputs(out1Index);
//And to eventually simply output everything:
evald.enableAllOutputs();
```

### Inputs vs Intermediate & Output variables
Callers may wish to validate all inputs have been provided, or to provide the user with a list of possible outputs. These can be obtained via `listAllInputs()` and `listAllOutputOrIntermediateVariables()`. Note that the order of tokens returned is not guaranteed.

```java
//From the example above, returns an array containing "a" and "b".
String[] allInputs = evald.listAllInputs();
//From the example above, returns an array containing "x", "y", "out1" and "out2".
//There is no way for Evald to know which variables the user really wants as an output
String[] availableOutputs = evald.listAllOutputOrIntermediateVariables();
```

Note that at present, if a variable is reused but required as an input then these methods will list it only as an input. In the following example, `w` is an output, and `a` is an input.

```java
w = a + 3;
a = w * 2;
```

## Libraries
Built-in functions are associated into libraries. 

### Core

This is the default library added if you construct an Evald instance without supplying any parameters.
It can be added manually with add(Library.CORE).
The core library (LibArithmetic) provides the following basic expression functionality:

#### Binary Operators

* ```+``` Addition
* ```-``` Subtraction
* ```*``` Multiplication
* ```/``` Division
* ```^``` Power of
* ```%``` Modulus

#### Unary operators

* ```+``` Positive
* ```-``` Negative

####Core functionality

* Braces
* Constant values (numeric values in expressions)
* ```nan``` for NaN

### Logic

The conditional library can be added to an Eval instance with add(Library.LOGIC).
The logic library (LibConditional) provides operations for comparing values and performing binary operations as follows:

#### IF/THEN/ELSE

Adds a three-argument ```if```. Given ```if(v,t,f)```, if ```v``` evaluates to ```true``` (see below), ```if()``` evaluates to the value of ```t```, otherwise it evaluates to ```f```.

#### true/false

Binary operations operate internally on doubles, and are evaluated to true if the double is non-zero.

The library adds the following constants:

* ```true``` with value 1
* ```false``` with value 0

#### Boolean operators

* ```&&``` boolean AND
* ```||``` boolean OR
* ```!``` boolean NOT 

#### Comparison operators

* ```<``` less than
* ```<=``` less than or equal
* ```>=``` greater than or equal
* ```>``` greater than
* ```==``` equality
* ```!=``` not equal

#### Value tests

* ```isnan(v)``` nonzero if v is nan 
* ```isinf(v)``` nonzero if v is +infinity or -infinity

### Binary

The binary library can be added to an Eval instance with add(Library.BINARY).
The binary library (LibBinary) provides support for the following bitwise operations:

* ```~``` one's complement
* ```&``` binary AND
* ```|``` binary OR
* ```xor``` binary XOR*

### Math

The math library can be added to an Eval instance with add(Library.MATH).
The math library (LibMath) provides functions and constants as follows:

* ```mod(a, d)``` return ```a``` % ```d```, the remainder of ```a``` / ```d```
* ```pow(x, y)``` return ```x``` raised to the power of ```y```
* ```e``` constant value for euler's number _e_
* ```pi``` constant value for _pi_
* ```random()``` return a random number greater than or equal to 0 and less than 1
* ```log(n)``` return the natural (base ```e```) logarithm of ```n```
* ```log2(n)``` return the base 2 logarithm of ```n```
* ```log10(n)``` return the base 10 logarithm of ```n```
* ```exp(n)``` returns ```e``` raised to ```n```
* ```floor(n)``` return the largest integer equal to or less than ```n```
* ```cbrt(n)``` return the cube root of ```n```
* ```ceil(n)``` return the smallest integer equal to or greater than ```n``` 
* ```abs(n)``` returns the absolute value of ```n```
* ```min(a,b)``` return the minimum of ```a``` and ```b```
* ```max(a,b)``` return the maximum of ```a``` and ```b```
* ```sqrt(n)``` return the square root of ```n```
* ```rint(n)``` round ```n``` to the closest integer, with ties rounding to the nearest even integer.
* ```round(n)``` round ```n``` to the closest integer, with ties rounding up
* ```sign(n)``` return -1 if the sign of n is negative, +1 if positive, 0 if zero.
* ```toDegrees(r)``` convert radians ```r``` to degrees
* ```toRadians(d)``` convert degrees ```d``` to radians
* ```tan(a)``` return the trignometric tangent of ```a```
* ```tanh(x)``` return the hyperbolic tangent of ```x```
* ```hypot(x,y)``` return the square root of x<sup>2</sup>+y<sup>2</sup>
* ```sinh(x)``` returns the hyperbolic sine of ```x```
* ```cos(a)``` returns the trignometric cosine of ```a```
* ```cosh(x)``` return the hyperbolic cosine of ```x```
* ```asin(a)``` returns the arc sine of ```a```
* ```acos(a)``` returns the arc cosine of ```a```
* ```atan(a)``` returns the arc tangent of ```a```
* ```atan2(y,x)``` returns the angle _theta_ from conversion of rectangular (```x```,```y```) to the polar (r,_theta_)
* ```sin(a)``` return the trignometric sine of ```a```
* ```atanh(a)``` return the inverse hyperbolic tangent of ```a```
* ```acosh(a)``` return the inverse hyperbolic cosine of ```a```
* ```asinh(a)``` return the inverse hyperbolic sine of ```a```
* ```cot(a)``` return the cotangent of angle ```a``` in radians
* ```cosec(a)``` return the cosecant of angle ```a``` in radians
* ```sec(a)``` return the secant of angle ```a``` in radians

## Extensibility

The evaluator and parser can be customised by adding or removing functionality as described below. 

### Add a library
One or more libraries (a collection of parsers & evaluators) can be added using the Evald.addLibrary() method. 

```java
//Create a parser context
Evald evald = new Evald();

//This fails - the Math lib hasn't been added yet. 
//evald.parse("sin(a)"); 

//Add a library with the sin function defined
evald.addLibrary(Library.MATH);

evald.parse("sin(a)");
evald.addVariable("a", 0.45);
evald.evaluate();
```

### Remove a library
Evald is lightweight, and it's often more efficient to simply create a new instance, adding only the required libraries, before parsing a new expression.
If an existing Evald instance is to be reused, an added library can be removed with the removeLibrary call. 
Note that any already parsed expression will continue to use the parser & expression from the old library - you must reparse the expression for the removed library to become completely dereferenced. 

```java
//Do something with the math library
Evald evald = new Evald();
evald.addLibrary(Library.MATH);
evald.parse("sin(a)"); 
evald.addVariable("a", 0.45);
Double result = evald.evaluate();

//Remove the math library
evald.removeLibrary(Library.MATH);

//Note that evaluate still has a reference to sin: 
assert(result == evald.evaluate());

//Attempt to parse sin again, and an UnknownMethodEvaldException results - the method is undefined.
evald.parse("sin(a)");
```


### Add a Function
A new function can be added with Evald.addUserFunction() by passing an instance of an ArgFunction subclass.

Note that function tokens must start with an underscore or alphabetic character; subsequent characters in the token may be alphanumeric or underscores. Evald parsing is case sensitive.

```java
Evald evald = new Evald();
evald.addUserFunction("sphereVolume", new OneArgFunction() {
    @Override public Double get(Double r) {
        return Math.PI * Math.pow(r, 3) * 4 / 3;
    }
});
evald.parse("sphereVolume(r)");
```

Additional overloads allow for multiple arguments (TwoArgFunction, ThreeArgFunctions), and varargs (NArgFunction)

Adding a function always removes a previous function using the same token from the parser, but any already parsed expression will continue to use the previously added function. 

Note that as well as supporting varargs, you can use NArgFunction with argument constraints to create a function with an arbitrary min & max number of arguments.

### Remove Functions
Individual functions can be removed using ```Evald.removeFunction()```. 
Removing a function always removes any previous function using the same token from the parser, but any already parsed expression will continue to use the previously added function.

### Enumerate Variables
Variables in use by an Evald instance can be enumerated:

* ```listUndeclared()``` lists any variable names referenced by the expression which have not yet been added with ```addVariable()```.
* ```listAllVariables()``` lists all variables - undeclared variables, as well as those added with calls to ```addVariable()```
* ```listActiveVariables()``` lists variables used by the most recently parsed expression.

Note that undeclared variable behaviour is controlled with ```setAllowUndeclared```; if undeclared variables are disallowed an exception will be raised when parsing.


### Extending Constants and Operators
At this time, these cannot be modified.

If you need this facility, let me know - or you can simply change the visibility of Evald.addParser(), Parser, and your desired Parser subclass (ie ConstantParser to add constants, BinaryOperatorParser to add binary operators).
Examples can be found in PackageTests.

This will likely be exposed and more thoroughly documented in a future release.

## License

MIT. See LICENSE.md

## Footnotes

\* ```^``` is reserved for the power operator. 

