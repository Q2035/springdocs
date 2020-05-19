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

### 理解`EvaluationContext`

在评估表达式以解析属性、方法或字段并帮助执行类型转换时，使用EvaluationContext接口。 Spring提供了两种实现。

- `SimpleEvaluationContext`：针对不需要完整SpEL语言语法范围且应受到有意义限制的表达式类别，公开了SpEL基本语言功能和配置选项的子集。示例包括但不限于数据绑定表达式和基于属性的过滤器。
- `StandardEvaluationContext`：公开SpEL语言功能和配置选项的全部集合。你可以使用它来指定默认的根对象并配置每个可用的评估相关策略。

`SimpleEvaluationContext` 设计为仅支持SpEL语言语法的子集。它不包括Java类型引用，构造函数和Bean引用。它还要求你明确选择对表达式中的属性和方法的支持级别。默认情况下，create()静态工厂方法仅启用对属性的读取访问。你还可以获取构建器来配置所需的确切支持级别，并针对以下一种或某种组合：

- 仅自定义PropertyAccessor（无反射）
- 只读访问的数据绑定属性
- 读写的数据绑定属性

#### 类型转换

默认情况下，SpEL使用Spring核心中可用的转换服务（org.springframework.core.convert.ConversionService）。此转换服务附带许多内置转换器，用于常见转换，但也可以完全扩展，以便你可以在类型之间添加自定义转换。此外，它是泛型感知的。这意味着，当你在表达式中使用泛型类型时，SpEL会尝试进行转换以维护遇到的任何对象的类型正确性。

实际上这是什么意思？假设使用setValue()进行赋值来设置List属性。该属性的类型实际上是List \<Boolean>。 SpEL认识到列表中的元素在放入列表之前需要转换为Boolean。以下示例显示了如何执行此操作：

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

可以使用解析器配置对象（*org.springframework.expression.spel.SpelParserConfiguration*）配置SpEL表达式解析器。配置对象控制某些表达式组件的行为。例如，如果你索引到数组或集合中并且指定索引处的元素为null，则可以自动创建该元素。当使用由属性引用链组成的表达式时，这很有用。如果你索引到数组或列表中并指定了超出数组或列表当前大小末尾的索引，则可以自动增长数组或列表以容纳该索引。下面的示例演示如何自动增加列表：

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

考虑以下基本表达式：

```
someArray[0].someProperty.someOtherProperty < 0.1
```

由于前面的表达式涉及数组访问，一些属性取消引用和数字运算，因此性能提升可能非常明显。在一个示例中，进行了50000次迭代的微基准测试，使用解释器评估需要75毫秒，而使用表达式的编译版本仅需要3毫秒。

#### 编译器配置

默认情况下不打开编译器，但是你可以通过两种不同的方式之一来打开它。当SpEL用法嵌入到另一个组件中时，可以使用解析器配置过程（[前面讨论过](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-parser-configuration)）或使用系统属性来打开它。本节讨论这两个选项。

编译器可以在*org.springframework.expression.spel.SpelCompilerMode*枚举中捕获的三种模式之一进行操作。模式如下：

- `OFF` (default): The compiler is switched off.
- `IMMEDIATE`: In immediate mode, the expressions are compiled as soon as possible. This is typically after the first interpreted evaluation. If the compiled expression fails (typically due to a type changing, as described earlier), the caller of the expression evaluation receives an exception.
- `MIXED`: In mixed mode, the expressions silently switch between interpreted and compiled mode over time. After some number of interpreted runs, they switch to compiled form and, if something goes wrong with the compiled form (such as a type changing, as described earlier), the expression automatically switches back to interpreted form again. Sometime later, it may generate another compiled form and switch to it. Basically, the exception that the user gets in `IMMEDIATE` mode is instead handled internally.

存在IMMEDIATE模式是因为MIXED模式可能会导致具有副作用的表达式出现问题。如果已编译的表达式在部分成功后就崩溃了，则它可能已经完成了影响系统状态的操作。如果发生这种情况，调用者可能不希望它在解释模式下静默地重新运行，因为表达式的一部分可能运行了两次。

选择模式后，使用SpelParserConfiguration配置解析器。以下示例显示了如何执行此操作：

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

从Spring Framework 4.1开始，已经有了基本的编译框架。但是，该框架尚不支持编译每种表达式。最初的重点是可能在性能关键型上下文中使用的通用表达式。目前无法编译以下类型的表达式：

- 涉及赋值的表达
- 表达式依赖转换服务
- 使用自定义解析器或访问器的表达式
- 使用选择或投影的表达式

将来会编译更多类型的表达。

## Bean定义中的表达式

你可以将SpEL表达式与基于XML或基于注解的配置元数据一起使用，以定义BeanDefinition实例。在这两种情况下，用于定义表达式的语法都采用#{<表达式字符串>}的形式。

### XML配置

可以使用表达式来设置属性或构造函数参数值，如以下示例所示：

```xml
<bean id="numberGuess" class="org.spring.samples.NumberGuess">
    <property name="randomNumber" value="#{ T(java.lang.Math).random() * 100.0 }"/>

    <!-- other properties -->
</bean>
```

systemProperties变量是预定义的，因此你可以在表达式中使用它，如以下示例所示：

```xml
<bean id="taxCalculator" class="org.spring.samples.TaxCalculator">
    <property name="defaultLocale" value="#{ systemProperties['user.region'] }"/>

    <!-- other properties -->
</bean>
```

请注意，在这种情况下，不必在预定义变量前加上#符号。
你还可以按名称引用其他bean属性，如以下示例所示：

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
下面的示例设置字段变量的默认值：

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

以下示例显示了等效的但使用属性设置器方法的示例：

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

自动装配的方法和构造函数也可以使用@Value注解，如以下示例所示：

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

本节描述了Spring Expression Language的工作方式。它涵盖以下主题：

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

使用属性引用进行导航很容易。为此，请使用句点来指示嵌套的属性值。 Inventor类的实例pupin和tesla填充类中列出的数据( [Classes used in the examples](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions-example-classes) )。要向下导航并获取特斯拉的出生年份和普平的出生城市，我们使用以下表达式：

