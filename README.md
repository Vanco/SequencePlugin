[![GitHub release](https://img.shields.io/github/v/release/Vanco/SequencePlugin)](https://github.com/Vanco/SequencePlugin/releases)
[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/8286)](https://plugins.jetbrains.com/plugin/8286-sequencediagram)
[![GitHub release (release name instead of tag name)](https://img.shields.io/github/v/release/Vanco/SequencePlugin?include_prereleases)](https://github.com/Vanco/SequencePlugin/releases)

# SequenceDiagram
<!-- Plugin description -->
Sequence Diagram is tool to generate simple sequence diagram(UML) from java, kotlin, scala(Beta) and groovy(limited) code.
https://vanco.github.io/SequencePlugin.

with this plugin, you can
+ generate Simple Sequence Diagram.
+ Navigate the code by click the diagram shape.
+ Delete Class from diagram.
+ Export the diagram as image(SVG, JPEG, PNG, TIFF).
+ Export the diagram as PlantUML, Mermaid format file.
+ Exclude classes from diagram by Settings > Tools > Sequence Diagram
+ Smart Interface(experimental)
+ Lambda Expression(experimental)
+ Kotlin Support(Experimental)
+ Scala support(Experimental, Beta)
+ Groovy Support(Experimental, limited)
<!-- Plugin description end -->

## Experimental features
**The experimental features created by myself, which is not part of UML standard. Use this feature in your own risk.**

### UAST support ([version 3.0.0-alpha](https://plugins.jetbrains.com/plugin/8286-sequencediagram/versions/alpha))
UAST (Unified Abstract Syntax Tree) is an abstraction layer on the PSI of different programming languages targeting the 
JVM (Java Virtual Machine). It provides a unified API for working with common language elements like classes and method 
declarations, literal values, and control flow operators.

Which languages are supported?
- Java: full support
- Kotlin: full support
- Scala: beta, but full support
- Groovy: declarations only, method bodies not supported

Try to use UAST api to generate SequenceDiagram. 

### Smart Interface
Find the implementation of the interface smartly.  e.g.
```java
public interface Fruit {
    int eat();
}

public class Apple implements Fruit {
    @Override
    public int eat() {
        return 5;
    }
}
```
`Apple` implemented the `Fruit` interface. When we generate sequence diagram for the `eatFruit` method:
```java
public class People {
    
    private Fruit fruit = new Apple();

    public void eatFruit() {
        fruit.eat();
    }
}
```
I draw dummy `implementation call` in dash line.

![Smart Interface](imges/smart_interface.png)

For the interface or abstract class, if there is only one implementation found, it will draw in diagram automatically. 
More than one implementation, you need to choose one to draw. this is an option in settings.

### Lambda Expression
No standard for the lambda expression in the sequence diagram yet. So I create mine. e.g.
```java
public interface Service<Int, String> {

    String invoke(Int a);
}
```
 I need draw the sequence diagram for `hello` method:
```java
public class Lambda {

    public Service<Integer, String> hello() {
        return a -> {
            Fruit fruit = new Apple();
            fruit.eat();
            return "I'm good!";
        };
    }
}
```
I draw a dummy `() ->` self call in diagram.

![Lambda Expression](imges/lambda_expr.png)

## How to use
1. Open Java/Kotlin/Scala/Groovy file
2. Generate SequenceDiagram with shortcut `Alt S` for windows, `Option S` for macOS

Please try to experience it and find what happen. 

Have fun!

## Version History
Current Version

[![GitHub release](https://img.shields.io/github/v/release/Vanco/SequencePlugin)](https://github.com/Vanco/SequencePlugin/releases)

### Version and API comparison

| Open API          | v2.x.x                                          | v3.x.x                    |
|-------------------|-------------------------------------------------|---------------------------|
| IGenerator        | SequenceGenerator <br> KtSequenceGenerator      | UastSequenceGenerator     |
| GeneratorFactory  | JavaGeneratorFactory <br> KtGeneratorFactory    | UastGeneratorFactory      |
| ElementTypeFinder | JavaElementTypeFinder  <br> KtElementTypeFinder |                           |
| ActionFinder      | JavaActionFinder  <br>  KtActionFinder          | UastActionFinder          |
| SequenceNavigable | JavaSequenceNavigable  <br> KtSequenceNavigable | JavaSequenceNavigable[^*] |

[^*] `JavaSequenceNavigable` work for Java, Kotlin, Scala, Groovy

### Function comparison

|                                                        | v2.x.x                | v3.x.x      |
|--------------------------------------------------------|-----------------------|-------------|
| **Language:**                                          |                       |             |
| Java                                                   | [x]                   | [x]         |
| Kotlin                                                 | [x] Partial           | [x]         |
| Scala                                                  | [ ]                   | [x] Beta    |
| Groovy                                                 | [ ]                   | [x] Partial |
| **Entry:**                                             |                       |             |
| Navigation Bar                                         | [x]                   | [ ]         |
| Tools Menu                                             | [x]                   | [x]         |
| Editor Context Menu                                    | [x]                   | [x]         |
| Shortcut `Alt S` for windows <br> `Option S` for macOS | [x]                   | [x]         |
| Project view popup menu                                | [ ]                   | [ ]         |
| Structure view popup menu                              | [ ]                   | [ ]         |
| **Feature:**                                           |                       |             |
| Smart Interface[^2]                                    | [x] EXC: 2.2.4, 2.2.5 | [x]         |
| Smart Interface configuration                          | [ ]                   | [x]         |
| Lambda call configuration                              | [x]                   | [x]         |

[^2] `Smart interface` will scan entire file and spend more generate time.

### versions history:
[Changelog](CHANGELOG.md)

## Known issue
#### Scala for comprehension calls is not supported #151
for example:
```scala
class B() {
  def bar() = Option("bar")
}

class A(b: B) {
  def foo() = {
    val r = "foo" + b.bar().getOrElse("?")
    r
  }

  def foo2() = {
    val r = for {
      x <- b.bar()
    } yield "foo" + x
    r.getOrElse("?")
  }
}
```
the for comprehension call
```scala
val r = for {
      x <- b.bar()
    } yield "foo" + x
```
it's UAST tree is `UastEmptyexpression`. 
```
 UMethod (name = foo2)
            UBlockExpression
                UDeclarationsExpression
                    ULocalVariable (name = r)
                        UastEmptyExpression(type = PsiType:Option<String>)
                UReturnExpression
                    UQualifiedReferenceExpression
                        USimpleNameReferenceExpression (identifier = r)
                        UMethodCall(name = getOrElse)
                            UIdentifier (Identifier (getOrElse))
                            ULiteralExpression (value = "?")
```
so the `b.bar()` method call will not generate base on `UastEmptyexpression`. Hopefully, the UAST api will solve this problem sooner.

## Acknowledgement

#### Name history
+ **SequencePlugin** Maintained by Kentaur(Kesh Sibilev, ksibilve@yahoo.com) until 2011
+ **SequencePluginReload** Maintained by Vanhg(Evan Fan, fanhuagang@gmail.com) 2011 - 2015
+ **SequenceDiagram** Maintained by Vanco(Evan Fan, fanhuagang@gmail.com) since 2016 
  ![new](imges/new.svg){:height="32px" width="32px"}

#### Why change name?
Since 2011, I found a solution of NPE of original **SequencePlugin**, so I write email to Kentaur with my solution,
He said he was not coding anymore. Instead, he sent me the code. I fix the NPE issue and publish to plugin
repository with new name **SequencePluginReload**.

In 2015, the IntelliJ change the login system, and I lost my account, cannot continue to publish new version to
the repository.

In 2016, I change the Name again to **SequenceDiagram** and host the source code on [github](https://github.com/Vanco/SequencePlugin).
Now it is open source.

Thanks Kentaur for the great work on the original source.