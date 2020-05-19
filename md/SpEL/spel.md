---
[Spring Expression Language (SpEL)](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions)
---

Spring Expression Language（简称“ SpEL”）是一种功能强大的表达式语言，支持在运行时查询和操作对象图。语言语法与统一EL相似，但提供了其他功能，最著名的是方法调用和基本的字符串模板功能。

尽管还有其他几种Java表达式语言可用——OGNL，MVEL和JBoss EL，仅举几例——Spring表达式语言的创建是为了向Spring社区提供一种受良好支持的表达式语言，该语言可用于以下版本中的所有产品Spring产品组合。它的语言功能受Spring产品组合中项目的要求驱动，包括[Spring Tools for Eclipse](https://spring.io/tools)中代码完成支持的工具要求。也就是说，SpEL基于与技术无关的API，如果需要，可以将其他表达语言实现集成在一起。

虽然SpEL是Spring产品组合中表达评估的基础，但它并不直接与Spring绑定，可以独立使用。为了自成一体，本章中的许多示例都将SpEL用作独立的表达语言。这需要创建一些自举基础结构类，例如解析器。Spring的大多数用户不需要处理这种基础结构，而只需编写表达式字符串进行评估。这种典型用法的一个示例是将SpEL集成到创建XML或基于注解的Bean定义中，如[Expression support for defining bean definitions](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-beandef).

本章介绍了表达语言，其API和语言语法的功能。在许多地方，Inventor和Society类都用作表达评估的目标对象。这些类声明和用于填充它们的数据在本章末尾列出。

表达式语言支持以下功能：

- Literal expressions
- Boolean and relational operators
- Regular expressions
- Class expressions
- Accessing properties, arrays, lists, and maps
- Method invocation
- Relational operators
- Assignment
- Calling constructors
- Bean references
- Array construction
- Inline lists
- Inline maps
- Ternary operator
- Variables
- User-defined functions
- Collection projection
- Collection selection
- Templated expressions

## 开始

本节介绍SpEL接口及其表达语言的简单用法。完整的语言参考可以在“[Language Reference](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-language-ref).”中找到。

以下代码介绍了SpEL API，用于体验文字字符串表达式Hello World。

```java
ExpressionParser parser = new SpelExpressionParser();
Expression exp = parser.parseExpression("'Hello World'"); 
String message = (String) exp.getValue();
```

你最可能使用的SpEL类和接口位于*org.springframework.expression*包及其子包中，例如*spel.support*。

ExpressionParser接口负责解析表达式字符串。在前面的示例中，表达式字符串是由周围的单引号表示的字符串文字。 Expression接口负责评估先前定义的表达式字符串。分别调用parser.parseExpression和exp.getValue时，可以引发两个异常，ParseException和EvaluationException。

SpEL支持多种功能，例如调用方法，访问属性和调用构造函数。

在以下方法调用示例中，我们在字符串文字上调用concat方法：

```java
ExpressionParser parser = new SpelExpressionParser();
Expression exp = parser.parseExpression("'Hello World'.concat('!')"); 
String message = (String) exp.getValue();
```

> message的值现在是“ Hello World！”。

以下调用JavaBean属性的示例将调用String属性Bytes：

```java
ExpressionParser parser = new SpelExpressionParser();

// invokes 'getBytes()'
Expression exp = parser.parseExpression("'Hello World'.bytes"); 
// This line converts the literal to a byte array
byte[] bytes = (byte[]) exp.getValue();
```

SpEL还通过使用标准的点符号（例如prop1.prop2.prop3）以及相应的属性值设置来支持嵌套属性。也可以访问公共字段。

下面的示例演示如何使用点表示法获取文字的长度：

```java
ExpressionParser parser = new SpelExpressionParser();

// invokes 'getBytes().length'
// 'Hello World'.bytes.length gives the length of the literal.
Expression exp = parser.parseExpression("'Hello World'.bytes.length"); 
int length = (Integer) exp.getValue();
```

可以调用String的构造函数，而不是使用字符串文字，如以下示例所示：

```java
ExpressionParser parser = new SpelExpressionParser();
// Construct a new String from the literal and make it be upper case.
Expression exp = parser.parseExpression("new String('hello world').toUpperCase()"); 
String message = exp.getValue(String.class);
```

注意使用通用方法：public \<T> T getValue（Class \<T> requiredResultType）。使用此方法无需将表达式的值强制转换为所需的结果类型。如果该值不能转换为T类型或无法使用已注册的类型转换器转换，则将引发EvaluationException。

SpEL的更常见用法是提供一个针对特定对象实例（称为根对象）进行评估的表达式字符串。以下示例显示如何从Inventor类的实例检索name属性或如何创建布尔条件：

```java
// Create and set a calendar
GregorianCalendar c = new GregorianCalendar();
c.set(1856, 7, 9);

// The constructor arguments are name, birthday, and nationality.
Inventor tesla = new Inventor("Nikola Tesla", c.getTime(), "Serbian");

ExpressionParser parser = new SpelExpressionParser();

Expression exp = parser.parseExpression("name"); // Parse name as an expression
String name = (String) exp.getValue(tesla);
// name == "Nikola Tesla"

exp = parser.parseExpression("name == 'Nikola Tesla'");
boolean result = exp.getValue(tesla, Boolean.class);
// result == true
```

