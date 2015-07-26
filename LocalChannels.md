# Introduction #

A local channel is a channel calculated from other channels. The channel is given by a name and optionally a unit and is defined by a formula. This [formula](#Formulas.md) can contain variables representing other channels along with the constants and functions described below.

# Adding a channel #

  1. To create a local channel, go to the `File -> Add channel` menu.
  1. Fill in the channel name and optionally the unit. The channel name must be unique and not already taken by another channel. The channel name can contain letters, numbers, spaces, parentheses, the dash, the dot, and the forward slash. To put a channel into a folder, separate the folder name and channel name with a forward slash, like `folder1/channel1`.
  1. Choose the variables you would like to use in the channel formula. For each variable click the `Add` button, and select the desired channel. The name of the variables will be automatically filled in, but it can be changed by double clicking on it and typing in the new name. The variable name can contain letters and numbers, but must start with a letter.
  1. Define the formula using the variables you defined before. See the [formula reference](#Formulas.md) for more information about the syntax and functions that can be used in a formula.
  1. Click `Add Channel` to add the channel.

_Note: The `Add Channel` button won't be enabled until a valid name, list of variables and formula are entered. Also, the formula must contain all the variables that have been defined._

![http://rdv.googlecode.com/svn/wiki/add-local-channel.png](http://rdv.googlecode.com/svn/wiki/add-local-channel.png)

The new channel will be displayed in the channel list. This channel can be used just like any other channel in RDV and can be viewed with any of the panels.

![http://rdv.googlecode.com/svn/wiki/add-local-channel2.png](http://rdv.googlecode.com/svn/wiki/add-local-channel2.png)

# Removing a channel #

To remove a channel, right-click on the channel in the channe list and select the `Remove channel` menu item.

![http://rdv.googlecode.com/svn/wiki/add-local-channel3.png](http://rdv.googlecode.com/svn/wiki/add-local-channel3.png)

# Formulas #

The formula language is based on [Mathematica](http://reference.wolfram.com/mathematica/guide/LanguageOverview.html) and supports most of its [constants](http://reference.wolfram.com/mathematica/guide/MathematicalConstants.html), [arithmetic functions](http://reference.wolfram.com/mathematica/guide/ArithmeticFunctions.html), [numerical functions](http://reference.wolfram.com/mathematica/guide/NumericalFunctions.html), and [elementary functions](http://reference.wolfram.com/mathematica/guide/ElementaryFunctions.html).

For example, the following will divide the sum of _x_ and _y_ by _z_:
```
(x + y) / z
```

To call a function, such as sine:
```
Sin[x]
```

Notice that function names are capitalized and brackets are surrounding the arguments.

A more advanced formula:
```
Max[x,y] + (Log[c1,c2] + z^3) 
```

See below for a reference of all supported constants and functions.

## Constants ##

| **Name** | **Value** |
|:---------|:----------|
| Pi       | _pi_ = 3.14159... |
| E        | _e_ = 2.71828... |
| Degree   | _pi_ / 180 = 1.01745... |

## Arithmetic Functions ##

| **Operator**  | **Name** | **Description** |
|:--------------|:---------|:----------------|
| +             | Plus[_x<sub>1</sub>_, _x<sub>2</sub>_, ...] | addition (sum of _x<sub>i</sub>_) |
| -             |          | subtraction     |
| `*`           | Times[_x<sub>1</sub>_, _x<sub>2</sub>_, ...] | multiplication (product of _x<sub>i</sub>_) |
| /             |          | division        |
| ^             | Power[_x_, _y_] | _x_ to the power _y_ |
|               | Sqrt[_x_] | square root of _x_ (_x_<sup>1/2</sup>) |

## Numerical Functions ##

| **Name** | **Description** |
|:---------|:----------------|
| Round[_x_] | integer closest to _x_ |
| Floor[_x_] | greatest integer less than or equal to _x_ |
| Ceil[_x_] | smallest integer greater than or equal to _x_ |
| Min[_x<sub>1</sub>_, _x<sub>2</sub>_, ...] | numerically smallest of the _x<sub>i</sub>_ |
| Max[_x<sub>1</sub>_, _x<sub>2</sub>_, ...] | numerically largest of the _x<sub>i</sub>_ |
| Sign[_x_] | sign of _x_ (-1, 0, 1) |
| Random[.md](.md) | random number   |

## Elementary Functions ##

| **Name** |  | **Description** |
|:---------|:-|:----------------|
| Exp[_x_] | _e__<sup>x</sup>_ |  exponential of _x_ |
| Log[_x_] | ln _x_| natural logarithm of _x_ |
| Log[_b_, _x_] | log_<sub>b</sub>__x_ | logarithm of _x_ to base _b_ |
| Sin[_x_] | sin _x_ | sine of _x_     |
| Cos[_x_] | cos _x_ | cosine of _x_   |
| Tan[_x_] | tan _x_ | tangent of _x_  |
| ArcSin[_x_] | sin<sup>-1</sup> _x_ | arc sine of _x_ |
| ArcCos[_x_] | cos<sup>-1</sup> _x_ | arc cosine of _x_ |
| ArcTan[_x_] | tan<sup>-1</sup> _x_ | arc tangent of _x_ |
| Sinh[_x_] | sinh _x_ | hyperbolic sine of _x_ |
| Cosh[_x_] | cosh _x_ | hyperbolic cosine of _x_ |
| Tanh[_x_] | tanh _x_ | hyperbolic tangent of _x_ |