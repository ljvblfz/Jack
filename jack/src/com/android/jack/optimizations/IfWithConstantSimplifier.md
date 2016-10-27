# Implementation notes for IfWithConstantSimplifier

The IfWithConstantSimplifier simplifies JIfStatements (on synthetic variables) by inserting
goto and label statements.

The implementation visits these 'if' statements in post-order traversal so that we do not insert
invalid goto statements.

# Example

Let's consider the following IR where:
* `<COND>` is a variable of boolean type
* `<THEN>` represents a list of statements of the 'then' block
* `<ELSE>` represents a list of statements of the 'else' block

```
 [...]
 if (<COND>) {
   -tac0 = true;
 } else {
   -tac0 = false;
 }
 if (-tac0) {
   -tac1 = true;
 } else {
   -tac1 = false;
 }
 if (-tac1) {
   <THEN>
 } else {
   <ELSE>
 }
 [...]
```

The IfWithConstantSimplifier is able to optimize away the synthetic variables `-tac0` and `-tac1`.

In post-order, we visit the `if (-tac1)` statement first. Here is how the IR is transformed:

```
[...]
if (<COND>) {
  -tac0 = true;
} else {
  -tac0 = false;
}
if (-tac0) {
  goto ifSimplierThen_0;
} else {
  goto ifSimplierElse_0;
}
{
  ifSimplierThen_0: {
  }
  <THEN>
  goto ifSimplifierStepOverElse_0;
}
{
  ifSimplierElse_0: {
  }
  <ELSE>
}
ifSimplifierStepOverElse_0: {
}
[...]
```

This first optimization removed the synthetic variable `-tac1` and introduced labels
`ifSimplierThen_0` and `ifSimplierElse_0`, respectively corresponding to the start of the 'then'
and 'else' block. The label `ifSimplifierStepOverElse_0` allows the 'then' block to step over the
'else' block.

Then we visit the statement `if (-tac0)`. Here is how the IR is transformed:

```
[...]
if (<COND>) {
  goto ifSimplierThen_1;
} else {
  goto ifSimplierElse_1;
}
{
  ifSimplierThen_1: {
  }
  goto ifSimplierThen_0;
}
{
  ifSimplierElse_1: {
  }
  goto ifSimplierElse_0;
}
{
  ifSimplierThen_0: {}
  }
  <THEN>
  goto ifSimplifierStepOverElse_0;
}
{
  ifSimplierElse_0: {
  }
  <ELSE>
}
ifSimplifierStepOverElse_0: {
}
[...]
```

This second optimization removed the synthetic variable `-tac0` and introduced labels
`ifSimplierThen_1` and `ifSimplierElse_1`, respectively corresponding to the start of the 'then'
and 'else' block (that were already transformed in the previous step).

The post-order traversal allows to know that it is not necessary to insert the "step over"
statements (goto + label) because the 'then' block already contains a goto statement, thus already
step over the 'else' block.
