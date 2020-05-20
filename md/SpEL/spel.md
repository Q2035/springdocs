---
[Spring Expression Language (SpEL)](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions)
---

Spring Expression Language（简称“ SpEL”）是一种功能强大的表达式语言，支持在运行时查询和操作对象图。语言语法与统一EL相似，但提供了其他功能，最著名的是方法调用和基本的字符串模板功能。

尽管还有其他几种Java表达式语言可用——OGNL，MVEL和JBoss EL，仅举几例——Spring表达式语言的创建是为了向Spring社区提供一种受良好支持的表达式语言，该语言可用于以下版本中的所有产品Spring产品组合。它的语言功能受Spring产品组合中项目的要求驱动，包括[Spring Tools for Eclipse](https://spring.io/tools)中代码完成支持的工具要求。也就是说，SpEL基于与技术无关的API，如果需要，可以将其他表达语言实现集成在一起。

虽然SpEL是Spring产品组合中表达评估的基础，但它并不直接与Spring绑定，可以独立使用。为了自成一体，本章中的许多示例都将SpEL用作独立的表达语言。这需要创建一些自举基础结构类，例如解析器。Spring的大多数用户不需要处理这种基础结构，而只需编写表达式字符串进行评估。这种典型用法的一个示例是将SpEL集成到创建XML或基于注解的Bean定义中，如[Expression support for defining bean definitions](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-beandef).

本章介绍了表达语言，其API和语言语法的功能。在许多地方，Inventor和Society类都用作表达评估的目标对象。这些类声明和用于填充它们的数据在本章末尾列出。

表达式语言支持以下功能:

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

在以下方法调用示例中，我们在字符串文字上调用concat方法:

```java
ExpressionParser parser = new SpelExpressionParser();
Expression exp = parser.parseExpression("'Hello World'.concat('!')"); 
String message = (String) exp.getValue();
```

> message的值现在是“ Hello World！”。

以下调用JavaBean属性的示例将调用String属性Bytes:

```java
ExpressionParser parser = new SpelExpressionParser();

// invokes 'getBytes()'
Expression exp = parser.parseExpression("'Hello World'.bytes"); 
// This line converts the literal to a byte array
byte[] bytes = (byte[]) exp.getValue();
```

SpEL还通过使用标准的点符号（例如prop1.prop2.prop3）以及相应的属性值设置来支持嵌套属性。也可以访问公共字段。

下面的示例演示如何使用点表示法获取文字的长度:

```java
ExpressionParser parser = new SpelExpressionParser();

// invokes 'getBytes().length'
// 'Hello World'.bytes.length gives the length of the literal.
Expression exp = parser.parseExpression("'Hello World'.bytes.length"); 
int length = (Integer) exp.getValue();
```

可以调用String的构造函数，而不是使用字符串文字，如以下示例所示:

```java
ExpressionParser parser = new SpelExpressionParser();
// Construct a new String from the literal and make it be upper case.
Expression exp = parser.parseExpression("new String('hello world').toUpperCase()"); 
String message = exp.getValue(String.class);
```

注意使用通用方法:public \<T> T getValue（Class \<T> requiredResultType）。使用此方法无需将表达式的值强制转换为所需的结果类型。如果该值不能转换为T类型或无法使用已注册的类型转换器转换，则将引发EvaluationException。

SpEL的更常见用法是提供一个针对特定对象实例（称为根对象）进行评估的表达式字符串。以下示例显示如何从Inventor类的实例检索name属性或如何创建布尔条件:

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

### 理解`EvaluationContext`

在评估表达式以解析属性、方法或字段并帮助执行类型转换时，使用EvaluationContext接口。 Spring提供了两种实现。

- `SimpleEvaluationContext`:针对不需要完整SpEL语言语法范围且应受到有意义限制的表达式类别，公开了SpEL基本语言功能和配置选项的子集。示例包括但不限于数据绑定表达式和基于属性的过滤器。
- `StandardEvaluationContext`:公开SpEL语言功能和配置选项的全部集合。你可以使用它来指定默认的根对象并配置每个可用的评估相关策略。

`SimpleEvaluationContext` 设计为仅支持SpEL语言语法的子集。它不包括Java类型引用，构造函数和Bean引用。它还要求你明确选择对表达式中的属性和方法的支持级别。默认情况下，create()静态工厂方法仅启用对属性的读取访问。你还可以获取构建器来配置所需的确切支持级别，并针对以下一种或某种组合:

- 仅自定义PropertyAccessor（无反射）
- 只读访问的数据绑定属性
- 读写的数据绑定属性

#### 类型转换

默认情况下，SpEL使用Spring核心中可用的转换服务（*org.springframework.core.convert.ConversionService*）。此转换服务附带许多内置转换器，用于常见转换，但也可以完全扩展，以便你可以在类型之间添加自定义转换。此外，它是泛型感知的。这意味着，当你在表达式中使用泛型类型时，SpEL会尝试进行转换以维护遇到的任何对象的类型正确性。

实际上这是什么意思？假设使用setValue()进行赋值来设置List属性。该属性的类型实际上是List \<Boolean>。 SpEL认识到列表中的元素在放入列表之前需要转换为Boolean。以下示例显示了如何执行此操作:

```java
class Simple {
    public List<Boolean> booleanList = new ArrayList<Boolean>();
}

Simple simple = new Simple();
simple.booleanList.add(true);

EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();

// "false" is passed in here as a String. SpEL and the conversion service
// will recognize that it needs to be a Boolean and convert it accordingly.
parser.parseExpression("booleanList[0]").setValue(context, simple, "false");

// b is false
Boolean b = simple.booleanList.get(0);
```

### 解析器配置

可以使用解析器配置对象（*org.springframework.expression.spel.SpelParserConfiguration*）配置SpEL表达式解析器。配置对象控制某些表达式组件的行为。例如，如果你索引到数组或集合中并且指定索引处的元素为null，则可以自动创建该元素。当使用由属性引用链组成的表达式时，这很有用。如果你索引到数组或列表中并指定了超出数组或列表当前大小末尾的索引，则可以自动增长数组或列表以容纳该索引。下面的示例演示如何自动增加列表:

```java
class Demo {
    public List<String> list;
}

// Turn on:
// - auto null reference initialization
// - auto collection growing
SpelParserConfiguration config = new SpelParserConfiguration(true,true);

ExpressionParser parser = new SpelExpressionParser(config);

Expression expression = parser.parseExpression("list[3]");

Demo demo = new Demo();

Object o = expression.getValue(demo);

// demo.list will now be a real collection of 4 entries
// Each entry is a new empty String
```

### SpEL 编译

Spring Framework 4.1包含一个基本的表达式编译器。通常对表达式进行解释，这样可以在评估过程中提供很大的动态灵活性，但不能提供最佳性能。对于偶尔使用表达式，这很好，但是，当与其他组件一起使用时，性能可能非常重要，并且不需要动态性。

SpEL编译器旨在满足这一需求。在评估过程中，编译器会生成一个Java类，该类体现了运行时的表达式行为，并使用该类来实现更快的表达式评估。由于缺少在表达式周围输入内容的信息，因此编译器在执行编译时会使用在表达式的解释式求值过程中收集的信息。例如，它不从表达式中知道属性引用的类型，而是在第一次解释求值时就知道它是什么。当然，如果各种表达元素的类型随时间变化，则基于此类派生信息进行编译会在以后引起麻烦。因此，编译最适合类型信息在重复求值时不会改变表达式。

考虑以下基本表达式:

```
someArray[0].someProperty.someOtherProperty < 0.1
```

由于前面的表达式涉及数组访问，一些属性取消引用和数字运算，因此性能提升可能非常明显。在一个示例中，进行了50000次迭代的微基准测试，使用解释器评估需要75毫秒，而使用表达式的编译版本仅需要3毫秒。

#### 编译器配置

默认情况下不打开编译器，但是你可以通过两种不同的方式之一来打开它。当SpEL用法嵌入到另一个组件中时，可以使用解析器配置过程（[前面讨论过](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-parser-configuration)）或使用系统属性来打开它。本节讨论这两个选项。

编译器可以在*org.springframework.expression.spel.SpelCompilerMode*枚举中捕获的三种模式之一进行操作。模式如下:

- `OFF` (default): The compiler is switched off.
- `IMMEDIATE`: In immediate mode, the expressions are compiled as soon as possible. This is typically after the first interpreted evaluation. If the compiled expression fails (typically due to a type changing, as described earlier), the caller of the expression evaluation receives an exception.
- `MIXED`: In mixed mode, the expressions silently switch between interpreted and compiled mode over time. After some number of interpreted runs, they switch to compiled form and, if something goes wrong with the compiled form (such as a type changing, as described earlier), the expression automatically switches back to interpreted form again. Sometime later, it may generate another compiled form and switch to it. Basically, the exception that the user gets in `IMMEDIATE` mode is instead handled internally.

存在IMMEDIATE模式是因为MIXED模式可能会导致具有副作用的表达式出现问题。如果已编译的表达式在部分成功后就崩溃了，则它可能已经完成了影响系统状态的操作。如果发生这种情况，调用者可能不希望它在解释模式下静默地重新运行，因为表达式的一部分可能运行了两次。

选择模式后，使用SpelParserConfiguration配置解析器。以下示例显示了如何执行此操作:

```java
SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE,
    this.getClass().getClassLoader());

SpelExpressionParser parser = new SpelExpressionParser(config);

Expression expr = parser.parseExpression("payload");

MyMessage message = new MyMessage();

Object payload = expr.getValue(message);
```

当指定编译器模式时，还可以指定一个类加载器（允许传递null）。编译的表达式在提供的任何子类加载器中定义。重要的是要确保，如果指定了类加载器，则它可以查看表达式评估过程中涉及的所有类型。如果未指定类加载器，则使用默认的类加载器（通常是在表达式求值期间运行的线程的上下文类加载器）。

第二种配置编译器的方法是将SpEL嵌入到其他组件中，并且可能无法通过配置对象进行配置。在这些情况下，可以使用系统属性。你可以将*spring.expression.compiler.mode*属性设置为SpelCompilerMode枚举值之一（关闭，立即或混合）。

#### 编译器限制

从Spring Framework 4.1开始，已经有了基本的编译框架。但是，该框架尚不支持编译每种表达式。最初的重点是可能在性能关键型上下文中使用的通用表达式。目前无法编译以下类型的表达式:

- 涉及赋值的表达
- 表达式依赖转换服务
- 使用自定义解析器或访问器的表达式
- 使用选择或投影的表达式

将来会编译更多类型的表达。

## Bean定义中的表达式

你可以将SpEL表达式与基于XML或基于注解的配置元数据一起使用，以定义BeanDefinition实例。在这两种情况下，用于定义表达式的语法都采用#{<表达式字符串>}的形式。

### XML配置

可以使用表达式来设置属性或构造函数参数值，如以下示例所示:

```xml
<bean id="numberGuess" class="org.spring.samples.NumberGuess">
    <property name="randomNumber" value="#{ T(java.lang.Math).random() * 100.0 }"/>

    <!-- other properties -->
</bean>
```

systemProperties变量是预定义的，因此你可以在表达式中使用它，如以下示例所示:

```xml
<bean id="taxCalculator" class="org.spring.samples.TaxCalculator">
    <property name="defaultLocale" value="#{ systemProperties['user.region'] }"/>

    <!-- other properties -->
</bean>
```

请注意，在这种情况下，不必在预定义变量前加上#符号。
你还可以按名称引用其他bean属性，如以下示例所示:

```xml
<bean id="numberGuess" class="org.spring.samples.NumberGuess">
    <property name="randomNumber" value="#{ T(java.lang.Math).random() * 100.0 }"/>

    <!-- other properties -->
</bean>

<bean id="shapeGuess" class="org.spring.samples.ShapeGuess">
    <property name="initialShapeSeed" value="#{ numberGuess.randomNumber }"/>

    <!-- other properties -->
</bean>
```

### 注解配置

若要指定默认值，可以将@Value注解放置在字段，方法以及方法或构造函数参数上。
下面的示例设置字段变量的默认值:

```java
public class FieldValueTestBean {

    @Value("#{ systemProperties['user.region'] }")
    private String defaultLocale;

    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public String getDefaultLocale() {
        return this.defaultLocale;
    }
}
```

以下示例显示了等效的但使用属性设置器方法的示例:

```java
public class PropertyValueTestBean {

    private String defaultLocale;

    @Value("#{ systemProperties['user.region'] }")
    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public String getDefaultLocale() {
        return this.defaultLocale;
    }
}
```

自动装配的方法和构造函数也可以使用@Value注解，如以下示例所示:

```java
public class SimpleMovieLister {

    private MovieFinder movieFinder;
    private String defaultLocale;

    @Autowired
    public void configure(MovieFinder movieFinder,
            @Value("#{ systemProperties['user.region'] }") String defaultLocale) {
        this.movieFinder = movieFinder;
        this.defaultLocale = defaultLocale;
    }

    // ...
}
```

```java
public class MovieRecommender {

    private String defaultLocale;

    private CustomerPreferenceDao customerPreferenceDao;

    public MovieRecommender(CustomerPreferenceDao customerPreferenceDao,
            @Value("#{systemProperties['user.country']}") String defaultLocale) {
        this.customerPreferenceDao = customerPreferenceDao;
        this.defaultLocale = defaultLocale;
    }

    // ...
}
```

## 语言参考

本节描述了Spring Expression Language的工作方式。它涵盖以下主题:

- [Literal Expressions](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-ref-literal)
- [Properties, Arrays, Lists, Maps, and Indexers](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-properties-arrays)
- [Inline Lists](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-inline-lists)
- [Inline Maps](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-inline-maps)
- [Array Construction](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-array-construction)
- [Methods](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-methods)
- [Operators](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-operators)
- [Types](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-types)
- [Constructors](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-constructors)
- [Variables](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-ref-variables)
- [Functions](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-ref-functions)
- [Bean References](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-bean-references)
- [Ternary Operator (If-Then-Else)](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-operator-ternary)
- [The Elvis Operator](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-operator-elvis)
- [Safe Navigation Operator](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-operator-safe-navigation)

### 文字表达

支持的文字表达式的类型为字符串，数值（int，实数，十六进制），布尔值和null。字符串由单引号引起来。要将单引号本身放在字符串中，请使用两个单引号字符。

以下清单显示了文字的简单用法。通常，它们不是像这样孤立地使用，而是作为更复杂的表达式的一部分使用——例如，在逻辑比较运算符的一侧使用文字。

```java
ExpressionParser parser = new SpelExpressionParser();

// evals to "Hello World"
String helloWorld = (String) parser.parseExpression("'Hello World'").getValue();

double avogadrosNumber = (Double) parser.parseExpression("6.0221415E+23").getValue();

// evals to 2147483647
int maxValue = (Integer) parser.parseExpression("0x7FFFFFFF").getValue();

boolean trueValue = (Boolean) parser.parseExpression("true").getValue();

Object nullValue = parser.parseExpression("null").getValue();
```

数字支持使用负号，指数符号和小数点。默认情况下，使用Double.parseDouble()解析实数。

#### Properties, Arrays, Lists, Maps, 和Indexers

使用属性引用进行导航很容易。为此，请使用句点来指示嵌套的属性值。 Inventor类的实例pupin和tesla填充类中列出的数据( [Classes used in the examples](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-example-classes) )。要向下导航并获取Tesla的出生年份和Pupin的出生城市，我们使用以下表达式:

```java
// evals to 1856
int year = (Integer) parser.parseExpression("Birthdate.Year + 1900").getValue(context);

String city = (String) parser.parseExpression("placeOfBirth.City").getValue(context);
```

属性名称的首字母允许不区分大小写。数组和列表的内容通过使用方括号表示法获得，如以下示例所示:

```java
ExpressionParser parser = new SpelExpressionParser();
EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();

// Inventions Array

// evaluates to "Induction motor"
String invention = parser.parseExpression("inventions[3]").getValue(
        context, tesla, String.class);

// Members List

// evaluates to "Nikola Tesla"
String name = parser.parseExpression("Members[0].Name").getValue(
        context, ieee, String.class);

// List and Array navigation
// evaluates to "Wireless communication"
String invention = parser.parseExpression("Members[0].Inventions[6]").getValue(
        context, ieee, String.class);
```

通过在方括号内指定文字键值可以获取映射的内容。在下面的示例中，由于Officer映射的键是字符串，因此我们可以指定字符串文字:

```java
// Officer's Dictionary

Inventor pupin = parser.parseExpression("Officers['president']").getValue(
        societyContext, Inventor.class);

// evaluates to "Idvor"
String city = parser.parseExpression("Officers['president'].PlaceOfBirth.City").getValue(
        societyContext, String.class);

// setting values
parser.parseExpression("Officers['advisors'][0].PlaceOfBirth.Country").setValue(
        societyContext, "Croatia");
```

### 内联Lists

你可以使用{}表示法在表达式中直接表达列表。

```java
// evaluates to a Java list containing the four numbers
List numbers = (List) parser.parseExpression("{1,2,3,4}").getValue(context);

List listOfLists = (List) parser.parseExpression("{{'a','b'},{'x','y'}}").getValue(context);
```

{}本身表示一个空列表。出于性能原因，如果列表本身完全由固定文字组成，则会创建一个常量列表来表示该表达式（而不是在每次求值时都建立一个新列表）。

### 内联Maps

你也可以使用{key:value}表示法在表达式中直接表达map。以下示例显示了如何执行此操作:

```java
// evaluates to a Java map containing the two entries
Map inventorInfo = (Map) parser.parseExpression("{name:'Nikola',dob:'10-July-1856'}").getValue(context);

Map mapOfMaps = (Map) parser.parseExpression("{name:{first:'Nikola',last:'Tesla'},dob:{day:10,month:'July',year:1856}}").getValue(context);
```

{:}本身意味着一个空的map。出于性能原因，如果映射图本身由固定的文字或其他嵌套的常量结构（列表或映射图）组成，则会创建一个常量映射图来表示该表达式（而不是在每次求值时都构建一个新的映射图）。映射键的引用是可选的。上面的示例不使用带引号的键。

### 数组构造

你可以使用熟悉的Java语法来构建数组，可以选择提供一个初始化程序，以在构造时填充该数组。以下示例显示了如何执行此操作:

```java
int[] numbers1 = (int[]) parser.parseExpression("new int[4]").getValue(context);

// Array with initializer
int[] numbers2 = (int[]) parser.parseExpression("new int[]{1,2,3}").getValue(context);

// Multi dimensional array
int[][] numbers3 = (int[][]) parser.parseExpression("new int[4][5]").getValue(context);
```

构造多维数组时，当前无法提供初始化程序。

### 方法

你可以使用典型的Java编程语法来调用方法。你还可以在文字上调用方法。还支持变量参数。下面的示例演示如何调用方法:

```java
// string literal, evaluates to "bc"
String bc = parser.parseExpression("'abc'.substring(1, 3)").getValue(String.class);

// evaluates to true
boolean isMember = parser.parseExpression("isMember('Mihajlo Pupin')").getValue(
        societyContext, Boolean.class);
```

### 运算符

Spring表达式语言支持以下几种运算符:

- [Relational Operators](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-operators-relational)
- [Logical Operators](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-operators-logical)
- [Mathematical Operators](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-operators-mathematical)
- [The Assignment Operator](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-assignment)

#### 关系运算符

使用标准运算符表示法支持关系运算符（等于，不等于，小于，小于或等于，大于和大于或等于）。以下清单显示了一些运算符示例:

```java
// evaluates to true
boolean trueValue = parser.parseExpression("2 == 2").getValue(Boolean.class);

// evaluates to false
boolean falseValue = parser.parseExpression("2 < -5.0").getValue(Boolean.class);

// evaluates to true
boolean trueValue = parser.parseExpression("'black' < 'block'").getValue(Boolean.class);
```

> 对null的大于和小于比较遵循一个简单的规则:null被视为无（不是零）。结果，任何其他值始终大于null（X> null始终为true），并且其他任何值都不小于零（X <null始终为false）。
>
> 如果你更喜欢数字比较，请避免使用基于数字的空比较，而建议使用零进行比较（例如，X> 0或X <0）。

除了标准的关系运算符外，SpEL还支持instanceof和基于正则表达式的匹配运算符。以下清单显示了两个示例:

```java
// evaluates to false
boolean falseValue = parser.parseExpression(
        "'xyz' instanceof T(Integer)").getValue(Boolean.class);

// evaluates to true
boolean trueValue = parser.parseExpression(
        "'5.00' matches '^-?\\d+(\\.\\d{2})?$'").getValue(Boolean.class);

//evaluates to false
boolean falseValue = parser.parseExpression(
        "'5.0067' matches '^-?\\d+(\\.\\d{2})?$'").getValue(Boolean.class);
```

> 请注意原始类型，因为它们会立即被包装为包装器类型，因此，按预期方式，1个instanceof T（int）的计算结果为false，而1个instanceof T（Integer）的计算结果为true。

每个符号运算符也可以指定为纯字母等效项。这样可以避免使用的符号对于嵌入表达式的文档类型具有特殊含义的问题（例如在XML文档中）。等效的文字是:

- `lt` (`<`)
- `gt` (`>`)
- `le` (`<=`)
- `ge` (`>=`)
- `eq` (`==`)
- `ne` (`!=`)
- `div` (`/`)
- `mod` (`%`)
- `not` (`!`).

所有的文本运算符都不区分大小写。

#### 逻辑运算符

SpEL支持以下逻辑运算符:

- `and` (`&&`)
- `or` (`||`)
- `not` (`!`)

下面的示例演示如何使用逻辑运算符

```java
// -- AND --

// evaluates to false
boolean falseValue = parser.parseExpression("true and false").getValue(Boolean.class);

// evaluates to true
String expression = "isMember('Nikola Tesla') and isMember('Mihajlo Pupin')";
boolean trueValue = parser.parseExpression(expression).getValue(societyContext, Boolean.class);

// -- OR --

// evaluates to true
boolean trueValue = parser.parseExpression("true or false").getValue(Boolean.class);

// evaluates to true
String expression = "isMember('Nikola Tesla') or isMember('Albert Einstein')";
boolean trueValue = parser.parseExpression(expression).getValue(societyContext, Boolean.class);

// -- NOT --

// evaluates to false
boolean falseValue = parser.parseExpression("!true").getValue(Boolean.class);

// -- AND and NOT --
String expression = "isMember('Nikola Tesla') and !isMember('Mihajlo Pupin')";
boolean falseValue = parser.parseExpression(expression).getValue(societyContext, Boolean.class);
```

#### 数学运算符

你可以在数字和字符串上使用加法运算符。你只能对数字使用减法，乘法和除法运算符。你还可以使用模数（％）和指数幂（^）运算符。强制执行标准运算符优先级。以下示例显示了正在使用的数学运算符:

```java
// Addition
int two = parser.parseExpression("1 + 1").getValue(Integer.class);  // 2

String testString = parser.parseExpression(
        "'test' + ' ' + 'string'").getValue(String.class);  // 'test string'

// Subtraction
int four = parser.parseExpression("1 - -3").getValue(Integer.class);  // 4

double d = parser.parseExpression("1000.00 - 1e4").getValue(Double.class);  // -9000

// Multiplication
int six = parser.parseExpression("-2 * -3").getValue(Integer.class);  // 6

double twentyFour = parser.parseExpression("2.0 * 3e0 * 4").getValue(Double.class);  // 24.0

// Division
int minusTwo = parser.parseExpression("6 / -3").getValue(Integer.class);  // -2

double one = parser.parseExpression("8.0 / 4e0 / 2").getValue(Double.class);  // 1.0

// Modulus
int three = parser.parseExpression("7 % 4").getValue(Integer.class);  // 3

int one = parser.parseExpression("8 / 5 % 2").getValue(Integer.class);  // 1

// Operator precedence
int minusTwentyOne = parser.parseExpression("1+2-3*8").getValue(Integer.class);  // -21
```

#### 赋值运算符

要设置属性，请使用赋值运算符（=）。这通常在对setValue的调用内完成，但也可以在对getValue的调用内完成。下面的清单显示了使用赋值运算符的两种方法:

```java
Inventor inventor = new Inventor();
EvaluationContext context = SimpleEvaluationContext.forReadWriteDataBinding().build();

parser.parseExpression("Name").setValue(context, inventor, "Aleksandar Seovic");

// alternatively
String aleks = parser.parseExpression(
        "Name = 'Aleksandar Seovic'").getValue(context, inventor, String.class);
```

### 类型

你可以使用特殊的T运算符来指定*java.lang.Class*（类型）的实例。静态方法也可以通过使用此运算符来调用。 StandardEvaluationContext使用TypeLocator查找类型，而StandardTypeLocator（可以替换）是在了解*java.lang*包的情况下构建的。这意味着对*java.lang*中的类型的T()引用不需要完全限定，但是所有其他类型引用都必须是完全限定的。下面的示例演示如何使用T运算符:

```java
Class dateClass = parser.parseExpression("T(java.util.Date)").getValue(Class.class);

Class stringClass = parser.parseExpression("T(String)").getValue(Class.class);

boolean trueValue = parser.parseExpression(
        "T(java.math.RoundingMode).CEILING < T(java.math.RoundingMode).FLOOR")
        .getValue(Boolean.class);
```

### 构造器

你可以使用new运算符来调用构造函数。除基本类型（int，float等）和String以外的所有其他类都应使用完全限定的类名。下面的示例演示如何使用new运算符调用构造函数:

```java
Inventor einstein = p.parseExpression(
        "new org.spring.samples.spel.inventor.Inventor('Albert Einstein', 'German')")
        .getValue(Inventor.class);

//create new inventor instance within add method of List
p.parseExpression(
        "Members.add(new org.spring.samples.spel.inventor.Inventor(
            'Albert Einstein', 'German'))").getValue(societyContext);
```

### 变量

你可以使用#variableName语法在表达式中引用变量。通过在EvaluationContext实现上使用setVariable方法设置变量。

> 有效的变量名称必须由以下一个或多个受支持的字符组成。
>
> - letters: `A` to `Z` and `a` to `z`
> - digits: `0` to `9`
> - underscore: `_`
> - dollar sign: `$`

以下示例显示了如何使用变量。

```java
Inventor tesla = new Inventor("Nikola Tesla", "Serbian");

EvaluationContext context = SimpleEvaluationContext.forReadWriteDataBinding().build();
context.setVariable("newName", "Mike Tesla");

parser.parseExpression("Name = #newName").getValue(context, tesla);
System.out.println(tesla.getName())  // "Mike Tesla"
```

#### #this和#root变量

#this变量始终是定义的，并且引用当前的评估对象。始终定义#root变量，并引用根上下文对象。#this可能随表达式的组成部分的求值而变化，但#root始终引用根。以下示例说明如何使用#this和#root变量:

```java
// create an array of integers
List<Integer> primes = new ArrayList<Integer>();
primes.addAll(Arrays.asList(2,3,5,7,11,13,17));

// create parser and set variable 'primes' as the array of integers
ExpressionParser parser = new SpelExpressionParser();
EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataAccess();
context.setVariable("primes", primes);

// all prime numbers > 10 from the list (using selection ?{...})
// evaluates to [11, 13, 17]
List<Integer> primesGreaterThanTen = (List<Integer>) parser.parseExpression(
        "#primes.?[#this>10]").getValue(context);
```

### Functions

你可以通过注册可以在表达式字符串中调用的用户定义函数来扩展SpEL。该函数通过EvaluationContext注册。下面的示例显示如何注册用户定义的函数:

```java
Method method = ...;

EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
context.setVariable("myFunction", method);
```

例如，考虑以下用于反转字符串的实用程序方法:

```java
public abstract class StringUtils {

    public static String reverseString(String input) {
        StringBuilder backwards = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            backwards.append(input.charAt(input.length() - 1 - i));
        }
        return backwards.toString();
    }
}
```

然后，你可以注册并使用前面的方法，如以下示例所示:

```java
ExpressionParser parser = new SpelExpressionParser();

EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
context.setVariable("reverseString",
        StringUtils.class.getDeclaredMethod("reverseString", String.class));

String helloWorldReversed = parser.parseExpression(
        "#reverseString('hello')").getValue(context, String.class);
```

### Bean引用

如果评估上下文已使用bean解析器配置，则可以使用@符号从表达式中查找bean。以下示例显示了如何执行此操作:

```java
ExpressionParser parser = new SpelExpressionParser();
StandardEvaluationContext context = new StandardEvaluationContext();
context.setBeanResolver(new MyBeanResolver());

// This will end up calling resolve(context,"something") on MyBeanResolver during evaluation
Object bean = parser.parseExpression("@something").getValue(context);
```

要访问工厂bean本身，你应该在bean名称前加上＆符号。以下示例显示了如何执行此操作:

```java
ExpressionParser parser = new SpelExpressionParser();
StandardEvaluationContext context = new StandardEvaluationContext();
context.setBeanResolver(new MyBeanResolver());

// This will end up calling resolve(context,"&foo") on MyBeanResolver during evaluation
Object bean = parser.parseExpression("&foo").getValue(context);
```

### 三元运算符（If-Then-Else）

你可以使用三元运算符在表达式内部执行if-then-else条件逻辑。以下清单显示了一个最小的示例:

```java
String falseString = parser.parseExpression(
        "false ? 'trueExp' : 'falseExp'").getValue(String.class);
```

在这种情况下，布尔值false导致返回字符串值'falseExp'。一个更现实的示例如下:

```java
parser.parseExpression("Name").setValue(societyContext, "IEEE");
societyContext.setVariable("queryName", "Nikola Tesla");

expression = "isMember(#queryName)? #queryName + ' is a member of the ' " +
        "+ Name + ' Society' : #queryName + ' is not a member of the ' + Name + ' Society'";

String queryResultString = parser.parseExpression(expression)
        .getValue(societyContext, String.class);
// queryResultString = "Nikola Tesla is a member of the IEEE Society"
```

有关三元运算符的更短语法，请参阅关于Elvis运算符的下一部分。

### The Elvis Operator（猫王算子？）

Elvis运算符是三元运算符语法的简化，并且在Groovy语言中使用。使用三元运算符语法，通常必须将变量重复两次，如以下示例所示:

```groovy
String name = "Elvis Presley";
String displayName = (name != null ? name : "Unknown");
```

相反，你可以使用Elvis运算符（其命名类似于猫王的发型）。以下示例显示了如何使用Elvis运算符:

```java
ExpressionParser parser = new SpelExpressionParser();

String name = parser.parseExpression("name?:'Unknown'").getValue(String.class);
System.out.println(name);  // 'Unknown'
```

以下清单显示了一个更复杂的示例:

```java
ExpressionParser parser = new SpelExpressionParser();
EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();

Inventor tesla = new Inventor("Nikola Tesla", "Serbian");
String name = parser.parseExpression("Name?:'Elvis Presley'").getValue(context, tesla, String.class);
System.out.println(name);  // Nikola Tesla

tesla.setName(null);
name = parser.parseExpression("Name?:'Elvis Presley'").getValue(context, tesla, String.class);
System.out.println(name);  // Elvis Presley
```

> 你可以使用Elvis运算符在表达式中应用默认值。以下示例显示了如何在@Value表达式中使用Elvis运算符:
>
> ```java
> @Value("#{systemProperties['pop3.port'] ?: 25}")
> ```
>
> 如果定义，将注入系统属性pop3.port，否则将注入25。

### 安全导航运算符

安全导航运算符用于避免NullPointerException，它来自Groovy语言。通常，当你引用一个对象时，可能需要在访问该对象的方法或属性之前验证其是否为null。为了避免这种情况，安全导航运算符返回null而不是引发异常。下面的示例演示如何使用安全导航运算符:

```java
ExpressionParser parser = new SpelExpressionParser();
EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();

Inventor tesla = new Inventor("Nikola Tesla", "Serbian");
tesla.setPlaceOfBirth(new PlaceOfBirth("Smiljan"));

String city = parser.parseExpression("PlaceOfBirth?.City").getValue(context, tesla, String.class);
System.out.println(city);  // Smiljan

tesla.setPlaceOfBirth(null);
city = parser.parseExpression("PlaceOfBirth?.City").getValue(context, tesla, String.class);
System.out.println(city);  // null - does not throw NullPointerException!!!
```

### 集合选择

选择是一种强大的表达语言功能，可让你通过从源集合中进行选择来将其转换为另一个集合。

选择使用.?[selectionExpression]的语法。它**过滤**集合并返回一个包含原始元素子集的新集合。例如，通过选择，我们可以轻松地获得Serbian发明人的列表，如以下示例所示:

```java
List<Inventor> list = (List<Inventor>) parser.parseExpression(
        "Members.?[Nationality == 'Serbian']").getValue(societyContext);
```

在lists和maps上都可以选择。对于lists，将针对每个单独的列表元素评估选择标准。针对maps，针对每个map条目（Java类型Map.Entry的对象）评估选择标准。每个map条目都有其键和值，可作为属性访问以供选择。

以下表达式返回一个新map，该map由原始map中条目值小于27的那些元素组成:

```java
Map newMap = parser.parseExpression("map.?[value<27]").getValue();
```

除了返回所有选定的元素外，你可以检索第一个或最后一个值。为了获得与选择匹配的第一个条目，语法为^[selectionExpression]。要获取最后一个匹配选择，语法为$[selectionExpression]。

### 集合投影

投影使集合可以驱动子表达式的求值，结果是一个新的集合。投影的语法为.![projectionExpression]。例如，假设我们有一个发明家列表，但是想要他们出生的城市列表。实际上，我们希望为发明人列表中的每个条目评估“placeOfBirth.city”。下面的示例使用投影来做到这一点:

```java
// returns ['Smiljan', 'Idvor' ]
List placesOfBirth = (List)parser.parseExpression("Members.![placeOfBirth.city]");
```

你还可以使用map来驱动投影，在这种情况下，将针对map中的每个条目（表示为Java Map.Entry）对投影表达式进行评估。跨map的投影结果是一个列表，其中包含针对每个map条目的投影表达式的评估。

### 表达式模板

表达式模板允许将文字文本与一个或多个评估块混合。每个评估块均以你可以定义的前缀和后缀字符分隔。常见的选择是使用#{}作为分隔符，如以下示例所示:

```java
String randomPhrase = parser.parseExpression(
        "random number is #{T(java.lang.Math).random()}",
        new TemplateParserContext()).getValue(String.class);

// evaluates to "random number is 0.7038186818312008"
```

通过将文字文本“随机数为”与评估#{}分隔符内的表达式的结果（在本例中为调用那个random()方法的结果）相连接来评估字符串。 parseExpression()方法的第二个参数的类型为ParserContext。 ParserContext接口用于影响表达式的解析方式，以支持表达式模板功能。 TemplateParserContext的定义如下:

```java
public class TemplateParserContext implements ParserContext {

    public String getExpressionPrefix() {
        return "#{";
    }

    public String getExpressionSuffix() {
        return "}";
    }

    public boolean isTemplate() {
        return true;
    }
}
```

## 示例中使用的类

本节列出了本章示例中使用的类。

```java
package org.spring.samples.spel.inventor;

import java.util.Date;
import java.util.GregorianCalendar;

public class Inventor {

    private String name;
    private String nationality;
    private String[] inventions;
    private Date birthdate;
    private PlaceOfBirth placeOfBirth;

    public Inventor(String name, String nationality) {
        GregorianCalendar c= new GregorianCalendar();
        this.name = name;
        this.nationality = nationality;
        this.birthdate = c.getTime();
    }

    public Inventor(String name, Date birthdate, String nationality) {
        this.name = name;
        this.nationality = nationality;
        this.birthdate = birthdate;
    }

    public Inventor() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public PlaceOfBirth getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(PlaceOfBirth placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public void setInventions(String[] inventions) {
        this.inventions = inventions;
    }

    public String[] getInventions() {
        return inventions;
    }
}
```

```java
package org.spring.samples.spel.inventor;

public class PlaceOfBirth {

    private String city;
    private String country;

    public PlaceOfBirth(String city) {
        this.city=city;
    }

    public PlaceOfBirth(String city, String country) {
        this(city);
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String s) {
        this.city = s;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
```

```java
package org.spring.samples.spel.inventor;

import java.util.*;

public class Society {

    private String name;

    public static String Advisors = "advisors";
    public static String President = "president";

    private List<Inventor> members = new ArrayList<Inventor>();
    private Map officers = new HashMap();

    public List getMembers() {
        return members;
    }

    public Map getOfficers() {
        return officers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isMember(String name) {
        for (Inventor inventor : members) {
            if (inventor.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
```

> 翻译：侧边翻译
>
> 校正：靓仔Q
>
> 时间：2020.5.18~2020.5.21
