---
[Validation, Data Binding, and Type Conversion](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#validation )(Version 5.2.6.RELEASE)
---

考虑将验证作为业务逻辑有利有弊，Spring提供了一种验证（和数据绑定）设计，但并不排除其中任何一个。具体来说，验证不应与Web层绑定，应该易于本地化，并且应该可以插入任何可用的验证器。考虑到这些问题，Spring提供了一个`Validator`合同，该合同既基本又可以在应用程序的每个层中使用。

数据绑定对于使用户输入动态绑定到应用程序的域模型（或用于处理用户输入的任何对象）非常有用。 Spring提供了恰当地命名为DataBinder的功能。 Validator和DataBinder组成了`validation` 包，该`validation` 包主要用于但不限于Web层。

BeanWrapper是Spring框架中的基本概念，并在很多地方使用。但是，你可能不需要直接使用BeanWrapper。但是，因为这是参考文档，所以我们认为可能需要进行一些解释。我们将在本章中解释BeanWrapper，因为如果你要使用它，那么在尝试将数据绑定到对象时最有可能使用它。

Spring的DataBinder和较低级别的BeanWrapper都使用PropertyEditorSupport实现来解析和格式化属性值。 PropertyEditor和PropertyEditorSupport类型是JavaBeans规范的一部分，本章还将对此进行说明。 Spring 3引入了core.convert包，该包提供了常规的类型转换工具，以及用于格式化UI字段值的高级"format"包。你可以将这些包用作PropertyEditorSupport实现的更简单替代方案。本章还将对它们进行讨论。

Spring通过设置基础结构和Spring自己的Validator合同的适配器来支持[Java Bean Validation](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#validation-beanvalidation)。应用程序可以全局启用一次Bean验证，如Java Bean验证中所述，并将其专用于所有验证需求。在Web层中，应用程序可以每个DataBinder进一步注册控制器本地的Spring Validator实例，如[Configuring a `DataBinder`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#validation-binder),中所述，这对于插入自定义验证逻辑很有用。

## 使用Spring的Validator接口进行验证

Spring具有Validator接口，可用于验证对象。 Validator接口通过使用Errors对象来工作，以便验证器在验证时可以将验证失败报告给Errors对象。

考虑以下小数据对象的示例：

```java
public class Person {

    private String name;
    private int age;

    // the usual getters and setters...
}
```

下一个示例通过实现*org.springframework.validation.Validator*接口的以下两个方法来提供Person类的验证行为：

- `supports(Class)`：此验证程序可以验证提供的Class的实例吗？
- `validate(Object, org.springframework.validation.Errors)`：验证给定的对象，并在发生验证错误的情况下，向给定的Errors对象注册这些对象。

实施Validator非常简单，尤其是当你知道Spring Framework也提供的ValidationUtils类时。以下示例实现了用于Person实例的Validator：

```java
public class PersonValidator implements Validator {

    /**
     * This Validator validates only Person instances
     */
    public boolean supports(Class clazz) {
        return Person.class.equals(clazz);
    }

    public void validate(Object obj, Errors e) {
        ValidationUtils.rejectIfEmpty(e, "name", "name.empty");
        Person p = (Person) obj;
        if (p.getAge() < 0) {
            e.rejectValue("age", "negativevalue");
        } else if (p.getAge() > 110) {
            e.rejectValue("age", "too.darn.old");
        }
    }
}
```

ValidationUtils类上的静态rejectIfEmpty（..）方法用于拒绝name属性（如果该属性为null或为空字符串）。可以阅读[`ValidationUtils`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/validation/ValidationUtils.html)\*javadoc，看看它除了提供前面显示的示例外还提供什么功能。

> 译者注:ValidationUtils是一个抽象类，它的主要作用是Utility class offering convenient methods for invoking a {@link Validator}and for rejecting empty fields.

虽然可以实现单个Validator类来验证丰富对象中的每个嵌套对象，但最好在其自己的Validator实现中封装对象的每个嵌套类的验证逻辑。一个“丰富”对象的简单示例是一个Customer，它由两个String属性（第一个和第二个名称）和一个复杂的Address对象组成。Address对象可以独立于客户对象使用，因此已实现了不同的AddressValidator。如果希望CustomerValidator重用AddressValidator类中包含的逻辑而不求助于复制粘贴，则可以在CustomerValidator中依赖注入或实例化一个AddressValidator，如以下示例所示：

```java
public class CustomerValidator implements Validator {

    private final Validator addressValidator;

    public CustomerValidator(Validator addressValidator) {
        if (addressValidator == null) {
            throw new IllegalArgumentException("The supplied [Validator] is " +
                "required and must not be null.");
        }
        if (!addressValidator.supports(Address.class)) {
            throw new IllegalArgumentException("The supplied [Validator] must " +
                "support the validation of [Address] instances.");
        }
        this.addressValidator = addressValidator;
    }

    /**
     * This Validator validates Customer instances, and any subclasses of Customer too
     */
    public boolean supports(Class clazz) {
        return Customer.class.isAssignableFrom(clazz);
    }

    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "field.required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "surname", "field.required");
        Customer customer = (Customer) target;
        try {
            errors.pushNestedPath("address");
            ValidationUtils.invokeValidator(this.addressValidator, customer.getAddress(), errors);
        } finally {
            errors.popNestedPath();
        }
    }
}
```

验证错误将报告给传递给验证器的Errors对象。对于Spring Web MVC，可以使用\<spring：bind />标记检查错误消息，但是也可以自己检查Errors对象。关于它提供的方法的更多信息可以在[javadoc](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframeworkvalidation/Errors.html)中找到。

### 将代码解析为错误消息

我们介绍了数据绑定和验证。本节介绍与验证错误相对应的输出消息。在上一节显示的示例中，我们拒绝了name和age字段。如果要使用MessageSource输出错误消息，可以使用拒绝字段时提供的错误代码（在这种情况下为“name”和“age”）来进行输出。当你从Errors接口调用（直接或间接通过使用诸如ValidationUtils类的）rejectValue或其他reject方法之一时，基础实现不仅注册你传入的代码，还注册许多其他错误代码。
MessageCodesResolver确定Errors接口寄存器中的哪个错误代码。默认情况下，使用DefaultMessageCodesResolver，它（例如）不仅使用你提供的代码注册消息，而且还注册包含传递给拒绝方法的字段名称的消息。因此，如果你通过使用rejectValue（“
age”，“too.darn.old”）拒绝字段，除了too.darn.old代码外，Spring还会注册too.darn.old.age和too.darn.old.age.int（第一个包含字段名称，第二个包含字段类型）。这样做是为了方便开发人员定位错误消息。

有关MessageCodesResolver和默认策略的更多信息，可以分别在[`MessageCodesResolver`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/validation/MessageCodesResolver.html) 和[`DefaultMessageCodesResolver`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/validation/DefaultMessageCodesResolver.html)的javadoc中找到。

## Bean操作和BeanWrapper

*org.springframework.beans*包遵循JavaBeans标准。 JavaBean是具有默认无参数构造函数的类，并且遵循命名约定，在该命名约定下，例如，名为bingoMadness的属性将具有setter方法setBingoMadness（..）和getter方法getBingoMadness（）。有关JavaBean和规范的更多信息，请参见[javabeans](https://docs.oracle.com/javase/8/docs/api/java/beans/package-summary.html).

Bean包中的一个非常重要的类是BeanWrapper接口及其相应的实现（BeanWrapperImpl）。就像从Javadoc引用的那样，BeanWrapper提供了以下功能：设置和获取属性值（单独或批量），获取属性描述符以及查询属性以确定它们是否可读或可写。此外，BeanWrapper还支持嵌套属性，从而可以将子属性上的属性设置为无限深度。
BeanWrapper还支持添加标准JavaBeans PropertyChangeListeners和VetoableChangeListeners的功能，而无需在目标类中支持代码。最后但并非最不重要的一点是，BeanWrapper支持设置索引属性。BeanWrapper通常不直接由应用程序代码使用，而是由DataBinder和BeanFactory使用。

BeanWrapper的工作方式部分由其名称表示：它包装一个Bean，以对该Bean执行操作，例如设置和检索属性。

### 设置和获取基本以及嵌套属性

设置和获取属性是通过BeanWrapper的setPropertyValue和getPropertyValue重载方法变体完成的。有关详细信息，请参见其Javadoc。下表显示了这些约定的一些示例：

| Expression             | Explanation                                                  |
| ---------------------- | ------------------------------------------------------------ |
| `name`                 | Indicates the property `name` that corresponds to the `getName()` or `isName()` and `setName(..)` methods. |
| `account.name`         | Indicates the nested property `name` of the property `account` that corresponds to (for example) the `getAccount().setName()` or `getAccount().getName()` methods. |
| `account[2]`           | Indicates the *third* element of the indexed property `account`. Indexed properties can be of type `array`, `list`, or other naturally ordered collection. |
| `account[COMPANYNAME]` | Indicates the value of the map entry indexed by the `COMPANYNAME` key of the `account` `Map` property. |

（如果你不打算直接使用BeanWrapper，那么下一部分对你而言并不是至关重要的。如果仅使用DataBinder和BeanFactory及其默认实现，则应跳到[section on `PropertyEditors`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-beans-conversion)）

以下两个示例类使用BeanWrapper来获取和设置属性：

```java
public class Company {

    private String name;
    private Employee managingDirector;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Employee getManagingDirector() {
        return this.managingDirector;
    }

    public void setManagingDirector(Employee managingDirector) {
        this.managingDirector = managingDirector;
    }
}
```

```java
public class Employee {

    private String name;

    private float salary;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }
}
```

以下代码段显示了一些有关如何检索和操纵实例化的`Companies` 和`Employees`的某些属性的示例：

```java
BeanWrapper company = new BeanWrapperImpl(new Company());
// setting the company name..
company.setPropertyValue("name", "Some Company Inc.");
// ... can also be done like this:
PropertyValue value = new PropertyValue("name", "Some Company Inc.");
company.setPropertyValue(value);

// ok, let's create the director and tie it to the company:
BeanWrapper jim = new BeanWrapperImpl(new Employee());
jim.setPropertyValue("name", "Jim Stravinsky");
company.setPropertyValue("managingDirector", jim.getWrappedInstance());

// retrieving the salary of the managingDirector through the company
Float salary = (Float) company.getPropertyValue("managingDirector.salary");
```

### 内置的PropertyEditor实现

Spring使用PropertyEditor的概念来实现对象和字符串之间的转换。以不同于对象本身的方式表示属性可能很方便。例如，日期可以用人类可读的方式表示（如字符串：'2007-14-09'），而我们仍然可以将人类可读的形式转换回原始日期（或者更好的是，转换任何日期以人类可读的形式输入到Date对象）。通过注册类型为*java.beans.PropertyEditor*的自定义编辑器，可以实现此行为。在BeanWrapper上或在特定的IoC容器中注册自定义编辑器（如前所述），使它具有如何将属性转换为所需类型的能力。有关PropertyEditor的更多信息，请参见[the javadoc of the `java.beans` package from Oracle](https://docs.oracle.com/javase/8/docs/api/java/beans/package-summary.html).

在Spring中使用属性编辑的两个示例：

- 通过使用PropertyEditor实现在bean上设置属性。当使用String作为在XML文件中声明的某些bean的属性的值时，Spring（如果相应属性的设置器具有Class参数）将使用ClassEditor尝试将参数解析为Class对象。
- 在Spring MVC框架中，通过使用各种PropertyEditor实现来解析HTTP请求参数，你可以在CommandController的所有子类中手动绑定这些实现。

Spring具有许多内置的PropertyEditor实现，以简化使用。它们都位于*org.springframework.beans.propertyeditors*包中。默认情况下，大多数（但不是全部，如下表所示）由BeanWrapperImpl注册。如果可以通过某种方式配置属性编辑器，则仍可以注册自己的变体以覆盖默认变体。下表描述了Spring提供的各种PropertyEditor实现：

| Class                     | Explanation                                                  |
| ------------------------- | ------------------------------------------------------------ |
| `ByteArrayPropertyEditor` | Editor for byte arrays. Converts strings to their corresponding byte representations. Registered by default by `BeanWrapperImpl`. |
| `ClassEditor`             | Parses Strings that represent classes to actual classes and vice-versa. When a class is not found, an `IllegalArgumentException` is thrown. By default, registered by `BeanWrapperImpl`. |
| `CustomBooleanEditor`     | Customizable property editor for `Boolean` properties. By default, registered by `BeanWrapperImpl` but can be overridden by registering a custom instance of it as a custom editor. |
| `CustomCollectionEditor`  | Property editor for collections, converting any source `Collection` to a given target `Collection` type. |
| `CustomDateEditor`        | Customizable property editor for `java.util.Date`, supporting a custom `DateFormat`. NOT registered by default. Must be user-registered with the appropriate format as needed. |
| `CustomNumberEditor`      | Customizable property editor for any `Number` subclass, such as `Integer`, `Long`, `Float`, or `Double`. By default, registered by `BeanWrapperImpl` but can be overridden by registering a custom instance of it as a custom editor. |
| `FileEditor`              | Resolves strings to `java.io.File` objects. By default, registered by `BeanWrapperImpl`. |
| `InputStreamEditor`       | One-way property editor that can take a string and produce (through an intermediate `ResourceEditor` and `Resource`) an `InputStream` so that `InputStream` properties may be directly set as strings. Note that the default usage does not close the `InputStream` for you. By default, registered by `BeanWrapperImpl`. |
| `LocaleEditor`            | Can resolve strings to `Locale` objects and vice-versa (the string format is `*[country]*[variant]`, same as the `toString()` method of `Locale`). By default, registered by `BeanWrapperImpl`. |
| `PatternEditor`           | Can resolve strings to `java.util.regex.Pattern` objects and vice-versa. |
| `PropertiesEditor`        | Can convert strings (formatted with the format defined in the javadoc of the `java.util.Properties` class) to `Properties` objects. By default, registered by `BeanWrapperImpl`. |
| `StringTrimmerEditor`     | Property editor that trims strings. Optionally allows transforming an empty string into a `null` value. NOT registered by default — must be user-registered. |
| `URLEditor`               | Can resolve a string representation of a URL to an actual `URL` object. By default, registered by `BeanWrapperImpl`. |