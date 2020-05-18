---
[Validation, Data Binding, and Type Conversion](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#validation )(Version 5.2.6.RELEASE)
---

考虑将验证作为业务逻辑有利有弊，Spring提供了一种验证（和数据绑定）设计，但并不排除其中任何一个。具体来说，验证不应与Web层绑定，应该易于本地化，并且应该可以插入任何可用的验证器。考虑到这些问题，Spring提供了一个`Validator`，并使之可以在应用程序的每个层中使用。

数据绑定对于使用户输入动态绑定到应用程序的域模型（或用于处理用户输入的任何对象）非常有用。 Spring提供了恰当地命名为DataBinder的功能。 Validator和DataBinder组成了`validation` 包，该`validation` 包主要用于但不限于Web层。

BeanWrapper是Spring框架中的基本概念，并在很多地方使用。你可能不需要直接使用BeanWrapper。但是，因为这是参考文档，所以我们认为可能需要进行一些解释。我们将在本章中解释BeanWrapper，因为如果你要使用它，那么在尝试将数据绑定到对象时最有可能使用它。

Spring的DataBinder和较低级别的BeanWrapper都使用PropertyEditorSupport实现来解析和格式化属性值。 PropertyEditor和PropertyEditorSupport类型是JavaBeans规范的一部分，本章还将对此进行说明。 Spring 3引入了*core.convert*包，该包提供了常规的类型转换工具，以及用于格式化UI字段值的高级"format"包。你可以将这些包用作PropertyEditorSupport实现的更简单替代方案。本章还将对它们进行讨论。

Spring通过设置基础结构和Spring自己的Validator合同的适配器来支持[Java Bean Validation](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#validation-beanvalidation)。应用程序可以全局启用一次Bean验证，如Java Bean验证中所述，并将其专用于所有验证需求。在Web层中，应用程序可以将DataBinder进一步注册控制器本地的Spring Validator实例，如[Configuring a `DataBinder`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#validation-binder)中所述，这对于插入自定义验证逻辑很有用。

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

*org.springframework.beans*包遵循JavaBeans标准。 JavaBean是具有默认无参数构造函数的类，并且遵循命名约定，在该命名约定下，例如，名为bingoMadness的属性将具有setter方法setBingoMadness（..）和getter方法getBingoMadness()。有关JavaBean和规范的更多信息，请参见[javabeans](https://docs.oracle.com/javase/8/docs/api/java/beans/package-summary.html).

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

Spring使用PropertyEditor的概念来**实现对象和字符串之间的转换**。以不同于对象本身的方式表示属性可能很方便。例如，日期可以用人类可读的方式表示（如字符串：'2007-14-09'），而我们仍然可以将人类可读的形式转换回原始日期（或者更好的是，转换任何日期以人类可读的形式输入到Date对象）。通过注册类型为*java.beans.PropertyEditor*的自定义编辑器，可以实现此行为。在BeanWrapper上或在特定的IoC容器中注册自定义编辑器（如前所述），使它具有如何将属性转换为所需类型的能力。有关PropertyEditor的更多信息，请参见[the javadoc of the `java.beans` package from Oracle](https://docs.oracle.com/javase/8/docs/api/java/beans/package-summary.html).

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

Spring使用*java.beans.PropertyEditorManager*设置可能需要的属性编辑器的搜索路径。搜索路径还包括*sun.bean.editors*，其中包括针对诸如Font，Color和大多数基本类型的类型的PropertyEditor实现。还要注意，如果标准JavaBeans基础结构与它们处理的类在同一包中，与该类具有相同的名称，并附加了Editor，则标准JavaBeans基础结构将自动发现PropertyEditor类（无需显式注册它们）。例如，可能具有以下类和包结构，足以使SomethingEditor类被识别并用作Something类型的属性的PropertyEditor。

```
com
  chank
    pop
      Something
      SomethingEditor // the PropertyEditor for the Something class
```

注意，你也可以在此处使用标准的BeanInfo JavaBeans机制（在[这里](https://docs.oracle.com/javase/tutorial/javabeans/advanced/customization.html)进行了某种程度的描述）。以下示例使用BeanInfo机制使用关联类的属性显式注册一个或多个PropertyEditor实例：

```
com
  chank
    pop
      Something
      SomethingBeanInfo // the BeanInfo for the Something class
```

所引用的SomethingBeanInfo类的以下Java源代码将CustomNumberEditor与Something类的age属性相关联：

```java
public class SomethingBeanInfo extends SimpleBeanInfo {

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            final PropertyEditor numberPE = new CustomNumberEditor(Integer.class, true);
            PropertyDescriptor ageDescriptor = new PropertyDescriptor("age", Something.class) {
                public PropertyEditor createPropertyEditor(Object bean) {
                    return numberPE;
                };
            };
            return new PropertyDescriptor[] { ageDescriptor };
        }
        catch (IntrospectionException ex) {
            throw new Error(ex.toString());
        }
    }
}
```

#### 注册其他自定义PropertyEditor实现

当将bean属性设置为字符串值时，Spring IoC容器最终使用标准JavaBeans PropertyEditor实现将这些字符串转换为属性的复杂类型。 Spring预注册了许多自定义的PropertyEditor实现（例如，将表示为字符串的类名称转换为Class对象）。此外，Java的标准JavaBeans PropertyEditor查找机制允许适当地命名类的PropertyEditor，并将其与提供支持的类放在同一包中，以便可以自动找到它。

如果需要注册其他自定义PropertyEditor，则可以使用几种机制。最手动的方法（通常不方便或不建议使用）是使用ConfigurableBeanFactory接口的registerCustomEditor()方法，并假设你有BeanFactory引用。另一种（稍微方便些）的机制是使用一种称为CustomEditorConfigurer的特殊bean工厂后处理器。尽管可以将bean工厂后处理器与BeanFactory实现一起使用，但CustomEditorConfigurer具有嵌套的属性设置，因此我们强烈建议你将其与ApplicationContext一起使用，在其中可以将其以与其他任何Bean相似的方式进行部署，并且可以在任何位置进行部署。自动检测并应用。

请注意，所有的bean工厂和应用程序上下文通过使用BeanWrapper来处理属性转换，都会自动使用许多内置的属性编辑器。[previous section](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-beans-conversion)列出了BeanWrapper注册的标准属性编辑器。此外，ApplicationContext还以适合特定应用程序上下文类型的方式重写或添加其他编辑器，以处理资源查找。

标准JavaBeans PropertyEditor实例用于将以字符串表示的属性值转换为该属性的实际复杂类型。你可以使用bean工厂的后处理器CustomEditorConfigurer来方便地将对其他PropertyEditor实例的支持添加到ApplicationContext。

考虑以下示例，该示例定义了一个名为ExoticType的用户类和另一个名为DependsOnExoticType的类，该类需要将ExoticType设置为属性：

```java
package example;

public class ExoticType {

    private String name;

    public ExoticType(String name) {
        this.name = name;
    }
}

public class DependsOnExoticType {

    private ExoticType type;

    public void setType(ExoticType type) {
        this.type = type;
    }
}
```

正确设置之后，我们希望能够将type属性分配为字符串，PropertyEditor会将其转换为实际的ExoticType实例。以下bean定义显示了如何建立这种关系：

```xml
<bean id="sample" class="example.DependsOnExoticType">
    <property name="type" value="aNameForExoticType"/>
</bean>
```

PropertyEditor实现可能类似于以下内容：

```java
// converts string representation to ExoticType object
package example;

public class ExoticTypeEditor extends PropertyEditorSupport {

    public void setAsText(String text) {
        setValue(new ExoticType(text.toUpperCase()));
    }
}
```

最后，下面的示例演示如何使用CustomEditorConfigurer向ApplicationContext注册新的PropertyEditor，然后可以根据需要使用它：

```xml
<bean class="org.springframework.beans.factory.config.CustomEditorConfigurer">
    <property name="customEditors">
        <map>
            <entry key="example.ExoticType" value="example.ExoticTypeEditor"/>
        </map>
    </property>
</bean>
```

**使用PropertyEditorRegistrar**

使用Spring容器注册属性编辑器的另一种机制是创建和使用PropertyEditorRegistrar。当需要在几种不同情况下使用同一组属性编辑器时，此接口特别有用。你可以编写相应的注册器，并在每种情况下重复使用它。PropertyEditorRegistrar实例与一个名为PropertyEditorRegistry的接口一起工作，该接口由Spring BeanWrapper（和DataBinder）实现。当与CustomEditorConfigurer（在[这里](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-beans-conversion-customeditor-registration)描述）结合使用时，PropertyEditorRegistrar实例特别方便，后者公开了一个名为setPropertyEditorRegistrars（..）的属性。以这种方式添加到CustomEditorConfigurer的PropertyEditorRegistrar实例可以轻松地与DataBinder和Spring MVC控制器共享。此外，它避免了在自定义编辑器上进行同步的需求：希望PropertyEditorRegistrar为每次创建bean的尝试创建新的PropertyEditor实例。

以下示例说明如何创建自己的PropertyEditorRegistrar实现：

```java
package com.foo.editors.spring;

public final class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar {

    public void registerCustomEditors(PropertyEditorRegistry registry) {

        // it is expected that new PropertyEditor instances are created
        registry.registerCustomEditor(ExoticType.class, new ExoticTypeEditor());

        // you could register as many custom property editors as are required here...
    }
}
```

另请参阅*org.springframework.beans.support.ResourceEditorRegistrar*以获取示例PropertyEditorRegistrar实现。注意，在实现registerCustomEditors（..）方法时，它如何创建每个属性编辑器的新实例。

下一个示例显示了如何配置CustomEditorConfigurer并将其注入我们的CustomPropertyEditorRegistrar的实例：

```xml
<bean class="org.springframework.beans.factory.config.CustomEditorConfigurer">
    <property name="propertyEditorRegistrars">
        <list>
            <ref bean="customPropertyEditorRegistrar"/>
        </list>
    </property>
</bean>

<bean id="customPropertyEditorRegistrar"
    class="com.foo.editors.spring.CustomPropertyEditorRegistrar"/>
```

最后（对于使用 [Spring’s MVC web framework](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/web.html#mvc)的读者来说，与本章的重点有所偏离），将PropertyEditorRegistrars与数据绑定控制器（例如SimpleFormController）结合使用会非常方便。下面的示例在initBinder（..）方法的实现中使用PropertyEditorRegistrar：

```java
public final class RegisterUserController extends SimpleFormController {

    private final PropertyEditorRegistrar customPropertyEditorRegistrar;

    public RegisterUserController(PropertyEditorRegistrar propertyEditorRegistrar) {
        this.customPropertyEditorRegistrar = propertyEditorRegistrar;
    }

    protected void initBinder(HttpServletRequest request,
            ServletRequestDataBinder binder) throws Exception {
        this.customPropertyEditorRegistrar.registerCustomEditors(binder);
    }

    // other methods to do with registering a User
}
```

这种PropertyEditor注册样式可以使代码简洁（initBinder（..）的实现只有一行长），并且可以将通用的PropertyEditor注册代码封装在一个类中，然后根据需要在许多Controller之间共享。

## Spring类型转换

Spring 3引入了*core.convert*包，该包提供了通用的类型转换系统。系统定义了一个用于实现类型转换逻辑的SPI和一个用于在运行时执行类型转换的API。在Spring容器中，可以使用此系统作为PropertyEditor实现的替代方法，以将外部化的bean属性值字符串转换为所需的属性类型。你还可以在应用程序中需要类型转换的任何地方使用公共API。

### SPI转换器

如以下接口定义所示，用于实现类型转换逻辑的SPI非常简单且具有强类型。

```java
package org.springframework.core.convert.converter;

public interface Converter<S, T> {

    T convert(S source);
}
```

要创建自己的转换器，请实现Converter接口并将S设置为要转换的类型，并将T设置为要转换成的类型。如果还需要注册一个委托数组或集合转换器（默认情况下DefaultConversionService会这样做），则也可以透明地应用此类转换器，如果需要将S的集合或数组转换为T的数组或集合。

对于每次对convert（S）的调用，保证源参数不为null。如果转换失败，你的转换器可能会引发任何未经检查的异常。具体来说，它应该抛出IllegalArgumentException以报告无效的源值。注意确保你的Converter实现是线程安全的。

为了方便起见，在*core.convert.support*软件包中提供了几种转换器实现。这些包括从字符串到数字和其他常见类型的转换器。下面的清单显示了StringToInteger类，它是一个典型的Converter实现：

```java
package org.springframework.core.convert.support;

final class StringToInteger implements Converter<String, Integer> {

    public Integer convert(String source) {
        return Integer.valueOf(source);
    }
}
```

### 使用ConverterFactory

当需要集中整个类层次结构的转换逻辑时（例如，从String转换为Enum对象时），可以实现ConverterFactory，如以下示例所示：

```java
package org.springframework.core.convert.converter;

public interface ConverterFactory<S, R> {

    <T extends R> Converter<S, T> getConverter(Class<T> targetType);
}
```

参数化S为你要转换的类型，参数R为基础类型，定义可以转换为的类的范围。然后实现getConverter（Class \<T>），其中T是R的子类。

以StringToEnumConverterFactory为例：

```java
package org.springframework.core.convert.support;

final class StringToEnumConverterFactory implements ConverterFactory<String, Enum> {

    public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToEnumConverter(targetType);
    }

    private final class StringToEnumConverter<T extends Enum> implements Converter<String, T> {

        private Class<T> enumType;

        public StringToEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        public T convert(String source) {
            return (T) Enum.valueOf(this.enumType, source.trim());
        }
    }
}
```

### 使用GenericConverter

当需要复杂的Converter实现时，请考虑使用GenericConverter接口。与Converter相比，GenericConverter具有比Converter更灵活但类型强度不高的签名，支持在多种源类型和目标类型之间进行转换。此外，GenericConverter使你可以在实现转换逻辑时使用可用的源字段和目标字段上下文。这种上下文允许类型转换由字段注解或在字段签名上声明的通用信息驱动。以下清单显示了GenericConverter的接口定义：

```java
package org.springframework.core.convert.converter;

public interface GenericConverter {

    public Set<ConvertiblePair> getConvertibleTypes();

    Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);
}
```

要实现GenericConverter，请让getConvertibleTypes()返回支持的源→目标类型对。然后实现convert（Object，TypeDescriptor，TypeDescriptor）包含你的转换逻辑。源TypeDescriptor提供对包含正在转换的值的源字段的访问。使用目标TypeDescriptor，可以访问要设置转换值的目标字段。

GenericConverter的一个很好的例子是在Java数组和集合之间进行转换的转换器。这样的ArrayToCollectionConverter会对声明目标集合类型的字段进行内省，以解析集合的元素类型。这样就可以在将集合设置到目标字段上之前，将源数组中的每个元素转换为集合元素类型。

> 由于GenericConverter是一个更复杂的SPI接口，因此仅应在需要时使用它。支持Converter或ConverterFactory以满足基本的类型转换需求。

#### 使用ConditionalGenericConverter

有时，你希望Converter仅在满足特定条件时才运行。例如，你可能只想在目标字段上存在特定注解时才运行Converter，或者可能仅在目标类上定义了特定方法（例如静态valueOf方法）时才运行Converter。ConditionalGenericConverter是GenericConverter和ConditionalConverter接口的联合，可让你定义以下自定义匹配条件：

```java
public interface ConditionalConverter {

    boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType);
}

public interface ConditionalGenericConverter extends GenericConverter, ConditionalConverter {
}
```

ConditionalGenericConverter的一个很好的例子是EntityConverter，它在持久实体标识符和实体引用之间进行转换。仅当目标实体类型声明静态查找器方法（例如findAccount（Long））时，此类EntityConverter才可能匹配。你可以在matchs（TypeDescriptor，TypeDescriptor）的实现中执行这种查找方法检查。

### The ConversionService API

ConversionService定义了一个统一的API，用于在运行时执行类型转换逻辑。转换器通常在以下外观接口后面执行：

```java
package org.springframework.core.convert;

public interface ConversionService {

    boolean canConvert(Class<?> sourceType, Class<?> targetType);

    <T> T convert(Object source, Class<T> targetType);

    boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType);

    Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType);

}
```

大多数ConversionService实现也都实现ConverterRegistry，该转换器提供用于注册转换器的SPI。在内部，ConversionService实现委派其注册的转换器执行类型转换逻辑。

*core.convert.support*软件包中提供了一个强大的ConversionService实现。 
GenericConversionService是适用于大多数环境的通用实现。 ConversionServiceFactory提供了一个方便的工厂来创建通用的ConversionService配置。

### 配置ConversionService

ConversionService是无状态对象，旨在在应用程序启动时实例化，然后在多个线程之间共享。在Spring应用程序中，通常为每个Spring容器（或ApplicationContext）配置一个ConversionService实例。当框架需要执行类型转换时，Spring会使用该ConversionService并使用它。你还可以将此ConversionService注入到任何bean中，然后直接调用它。

> 如果未向Spring注册任何ConversionService，则使用原始的基于PropertyEditor的系统。

要向Spring注册默认的ConversionService，请添加以下bean定义，其id为conversionService：

```xml
<bean id="conversionService"
    class="org.springframework.context.support.ConversionServiceFactoryBean"/>
```

默认的ConversionService可以在字符串，数字，枚举，集合，映射和其他常见类型之间进行转换。要用你自己的自定义转换器补充或覆盖默认转换器，请设置converters属性。属性值可以实现Converter，ConverterFactory或GenericConverter接口中的任何一个。

```xml
<bean id="conversionService"
        class="org.springframework.context.support.ConversionServiceFactoryBean">
    <property name="converters">
        <set>
            <bean class="example.MyCustomConverter"/>
        </set>
    </property>
</bean>
```

在Spring MVC应用程序中使用ConversionService也很常见。参见Spring MVC一章中的[Conversion and Formatting](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/web.html#mvc-config-conversion)。

在某些情况下，你可能希望在转换过程中应用格式设置。有关使用FormattingConversionServiceFactoryBean的详细信息，请参见[The `FormatterRegistry` SPI](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#format-FormatterRegistry-SPI)。

### 以编程方式使用ConversionService

要以编程方式使用ConversionService实例，可以像对其他任何bean一样注入对该实例的引用。以下示例显示了如何执行此操作：

```java
@Service
public class MyService {

    public MyService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public void doIt() {
        this.conversionService.convert(...)
    }
}
```

对于大多数用例，可以使用指定targetType的convert方法，但不适用于更复杂的类型，例如参数化元素的集合。例如，如果要以编程方式将整数列表转换为字符串列表，则需要提供源类型和目标类型的正式定义。

幸运的是，如下面的示例所示，TypeDescriptor提供了各种选项来使操作变得简单明了：

```java
DefaultConversionService cs = new DefaultConversionService();

List<Integer> input = ...
cs.convert(input,
    TypeDescriptor.forObject(input), // List<Integer> type descriptor
    TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class)));
```

请注意，DefaultConversionService自动注册适用于大多数环境的转换器。这包括集合转换器，标量转换器和基本的对象到字符串转换器。你可以使用DefaultConversionService类上的静态addDefaultConverters方法向任何ConverterRegistry注册相同的转换器。

值类型的转换器可重用于数组和集合，因此，假设标准集合处理适当，则无需创建特定的转换器即可将S的集合转换为T的集合。

## Spring字段格式

如上一节所述，[`core.convert`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#core-convert)是一种通用类型转换系统。它提供了统一的ConversionService API和强类型的Converter SPI，用于实现从一种类型到另一种类型的转换逻辑。 Spring容器使用此系统绑定bean属性值。此外，Spring Expression Language（SpEL）和DataBinder都使用此系统绑定字段值。例如，当SpEL需要强制将Short转换为Long来完成expression.setValue（Object bean，Object value）尝试时，*core.convert*系统将执行强制转换。

现在考虑典型客户端环境（例如Web或桌面应用程序）的类型转换要求。在这样的环境中，你通常会从String转换为支持客户端回发过程，然后又转换为String以支持视图渲染过程。另外，你通常需要本地化String值。更通用的core.convert Converter SPI不能直接满足此类格式化要求。为了直接解决这些问题，Spring 3引入了方便的Formatter SPI，它为客户端环境提供了PropertyEditor实现的简单而强大的替代方案。

通常，当你需要实现通用类型转换逻辑时（例如，用于在*java.util.Date*和Long之间进行转换），可以使用Converter SPI。在客户端环境（例如Web应用程序）中工作并且需要解析和打印本地化的字段值时，可以使用Formatter SPI。 ConversionService为两个SPI提供统一的类型转换API。

### 格式化器SPI

用于实现字段格式化逻辑的Formatter SPI非常简单且类型严格。以下清单显示了Formatter接口定义：

```java
package org.springframework.format;

public interface Formatter<T> extends Printer<T>, Parser<T> {
}
```

Formatter从Printer和Parser构建块接口扩展。以下清单显示了这两个接口的定义：

```java
public interface Printer<T> {

    String print(T fieldValue, Locale locale);
}
```

```java
public interface Parser<T> {

    T parse(String clientValue, Locale locale) throws ParseException;
}
```

要创建自己的Formatter，请实现前面显示的Formatter接口。将T参数化为你希望格式化的对象的类型（例如*java.util.Date*）。实现print()操作以打印T的实例以在客户端语言环境中显示。实现parse()操作，以从客户端语言环境返回的格式化表示形式解析T的实例。如果解析尝试失败，则Formatter应该抛出ParseException或IllegalArgumentException。注意确保你的Formatter实现是线程安全的。

format子包为方便起见提供了几种Formatter实现。number包提供NumberStyleFormatter，CurrencyStyleFormatter和PercentStyleFormatter来格式化使用*java.text.NumberFormat*的Number对象。datetime包提供了一个DateFormatter，用于使用*java.text.DateFormat*格式化java.util.Date对象。*datetime.joda*包基于 [Joda-Time library](https://www.joda.org/joda-time/)提供了全面的日期时间格式支持。

以下DateFormatter是Formatter实现的示例：

```java
package org.springframework.format.datetime;

public final class DateFormatter implements Formatter<Date> {

    private String pattern;

    public DateFormatter(String pattern) {
        this.pattern = pattern;
    }

    public String print(Date date, Locale locale) {
        if (date == null) {
            return "";
        }
        return getDateFormat(locale).format(date);
    }

    public Date parse(String formatted, Locale locale) throws ParseException {
        if (formatted.length() == 0) {
            return null;
        }
        return getDateFormat(locale).parse(formatted);
    }

    protected DateFormat getDateFormat(Locale locale) {
        DateFormat dateFormat = new SimpleDateFormat(this.pattern, locale);
        dateFormat.setLenient(false);
        return dateFormat;
    }
}
```

Spring团队欢迎社区推动的Formatter贡献。请参阅[GitHub Issues](https://github.com/spring-projects/spring-framework/issues) 以做出贡献。

### 注解驱动的格式

可以通过字段类型或注解配置字段格式。要将注解绑定到Formatter，请实现AnnotationFormatterFactory。以下清单显示了AnnotationFormatterFactory接口的定义：

```java
package org.springframework.format;

public interface AnnotationFormatterFactory<A extends Annotation> {

    Set<Class<?>> getFieldTypes();

    Printer<?> getPrinter(A annotation, Class<?> fieldType);

    Parser<?> getParser(A annotation, Class<?> fieldType);
}
```

要创建一个实现：将A参数化为要与格式逻辑关联的字段注解类型，例如*org.springframework.format.annotation.DateTimeFormat*。让getFieldTypes()返回可在其上使用注解的字段类型。 让getPrinter()返回打印机以打印带注解的字段的值。让getParser()返回解析器以解析带注解字段的clientValue。

以下示例AnnotationFormatterFactory实现将@NumberFormat注解绑定到格式化程序，以指定数字样式或模式：

```java
public final class NumberFormatAnnotationFormatterFactory
        implements AnnotationFormatterFactory<NumberFormat> {

    public Set<Class<?>> getFieldTypes() {
        return new HashSet<Class<?>>(asList(new Class<?>[] {
            Short.class, Integer.class, Long.class, Float.class,
            Double.class, BigDecimal.class, BigInteger.class }));
    }

    public Printer<Number> getPrinter(NumberFormat annotation, Class<?> fieldType) {
        return configureFormatterFrom(annotation, fieldType);
    }

    public Parser<Number> getParser(NumberFormat annotation, Class<?> fieldType) {
        return configureFormatterFrom(annotation, fieldType);
    }

    private Formatter<Number> configureFormatterFrom(NumberFormat annotation, Class<?> fieldType) {
        if (!annotation.pattern().isEmpty()) {
            return new NumberStyleFormatter(annotation.pattern());
        } else {
            Style style = annotation.style();
            if (style == Style.PERCENT) {
                return new PercentStyleFormatter();
            } else if (style == Style.CURRENCY) {
                return new CurrencyStyleFormatter();
            } else {
                return new NumberStyleFormatter();
            }
        }
    }
}
```

要触发格式，可以使用@NumberFormat注解字段，如以下示例所示：

```java
public class MyModel {

    @NumberFormat(style=Style.CURRENCY)
    private BigDecimal decimal;
}
```

#### 格式注解API

*org.springframework.format.annotation*包中存在一个可移植的格式注解API。你可以使用@NumberFormat格式化数字字段（例如Double和Long），并使用@DateTimeFormat格式化java.util.Date，*java.util.Calendar*，Long（用于毫秒时间戳）以及JSR-310
*java.time*和Joda-Time值类型。

### The FormatterRegistry SPI

FormatterRegistry是用于注册格式器和转换器的SPI。 FormattingConversionService是适用于大多数环境的FormatterRegistry的实现。你可以通过编程方式或声明方式将此变体配置为Spring Bean，例如通过使用FormattingConversionServiceFactoryBean。由于此实现还实现了ConversionService，因此你可以直接将其配置为与Spring的DataBinder和Spring Expression Language（SpEL）一起使用。

以下清单显示了FormatterRegistry SPI：

```java
package org.springframework.format;

public interface FormatterRegistry extends ConverterRegistry {

    void addFormatterForFieldType(Class<?> fieldType, Printer<?> printer, Parser<?> parser);

    void addFormatterForFieldType(Class<?> fieldType, Formatter<?> formatter);

    void addFormatterForFieldType(Formatter<?> formatter);

    void addFormatterForAnnotation(AnnotationFormatterFactory<?> factory);
}
```

如前面的清单所示，你可以按字段类型或注解注册格式化程序。

FormatterRegistry SPI使你可以集中配置格式设置规则，而不必在控制器之间复制此类配置。例如，你可能要强制所有日期字段以某种方式设置格式或带有特定注解的字段以某种方式设置格式。使用共享的FormatterRegistry，你可以一次定义这些规则，并在需要格式化时应用它们。

### The FormatterRegistrar SPI

FormatterRegistrar是一个SPI，用于通过FormatterRegistry注册格式器和转换器。以下清单显示了其接口定义：

```java
package org.springframework.format;

public interface FormatterRegistrar {

    void registerFormatters(FormatterRegistry registry);
}
```

为给定的格式类别（例如日期格式）注册多个相关的转换器和格式器时，FormatterRegistrar很有用。在声明式注册不充分的情况下它也很有用。例如，当格式化程序需要在不同于其自身\<T>的特定字段类型下进行索引时，或者在注册`Printer`/`Parser`对时。下一节将提供有关转换器和格式化程序注册的更多信息。

### 在Spring MVC中配置格式

参见Spring MVC一章中的 [Conversion and Formatting](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/web.html#mvc-config-conversion)。

## 配置全局日期和时间格式

默认情况下，未使用@DateTimeFormat注解的日期和时间字段是使用*DateFormat.SHORT*样式从字符串转换的。如果愿意，可以通过定义自己的全局格式来更改此设置。

为此，请确保Spring不注册默认格式器。相反，可以借助以下方法手动注册格式化程序：

- `org.springframework.format.datetime.standard.DateTimeFormatterRegistrar`
- `org.springframework.format.datetime.DateFormatterRegistrar`, or `org.springframework.format.datetime.joda.JodaTimeFormatterRegistrar` for Joda-Time.

例如，以下Java配置注册全局yyyyMMdd格式：

```java
@Configuration
public class AppConfig {

    @Bean
    public FormattingConversionService conversionService() {

        // Use the DefaultFormattingConversionService but do not register defaults
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService(false);

        // Ensure @NumberFormat is still supported
        conversionService.addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());

        // Register JSR-310 date conversion with a specific global format
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(DateTimeFormatter.ofPattern("yyyyMMdd"));
        registrar.registerFormatters(conversionService);

        // Register date conversion with a specific global format
        DateFormatterRegistrar registrar = new DateFormatterRegistrar();
        registrar.setFormatter(new DateFormatter("yyyyMMdd"));
        registrar.registerFormatters(conversionService);

        return conversionService;
    }
}
```

如果你喜欢基于XML的配置，则可以使用FormattingConversionServiceFactoryBean。以下示例显示了如何执行此操作（这次使用Joda Time）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd>

    <bean id="conversionService" class="org.springframework.format.support.FormattingConversionServiceFactoryBean">
        <property name="registerDefaultFormatters" value="false" />
        <property name="formatters">
            <set>
                <bean class="org.springframework.format.number.NumberFormatAnnotationFormatterFactory" />
            </set>
        </property>
        <property name="formatterRegistrars">
            <set>
                <bean class="org.springframework.format.datetime.joda.JodaTimeFormatterRegistrar">
                    <property name="dateFormatter">
                        <bean class="org.springframework.format.datetime.joda.DateTimeFormatterFactoryBean">
                            <property name="pattern" value="yyyyMMdd"/>
                        </bean>
                    </property>
                </bean>
            </set>
        </property>
    </bean>
</beans>
```

请注意，在Web应用程序中配置日期和时间格式时，还有其他注意事项。请参阅[WebMVC Conversion and Formatting](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/web.html#mvc-config-conversion) or [WebFlux Conversion and Formatting](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/web-reactive.html#webflux-config-conversion).

## Java Bean验证

Spring框架提供了对[Java Bean Validation](https://beanvalidation.org/) API的支持。

### Bean验证概述

Bean验证为Java应用程序提供了通过约束声明和元数据进行验证的通用方法。要使用它，你需要使用声明性验证约束对域模型属性进行注解，然后由运行时强制实施。有内置的约束，你也可以定义自己的自定义约束。

考虑以下示例，该示例显示了具有两个属性的简单PersonForm模型：

```java
public class PersonForm {
    private String name;
    private int age;
}
```

Bean验证使你可以声明约束，如以下示例所示：

```java
public class PersonForm {

    @NotNull
    @Size(max=64)
    private String name;

    @Min(0)
    private int age;
}
```

然后，Bean验证验证器根据声明的约束来验证此类的实例。有关该API的一般信息，请参见Bean验证。有关特定限制，请参见[Hibernate Validator](https://hibernate.org/validator/) 文档。要学习如何将bean验证提供程序设置为Spring bean，请继续阅读。

### 配置Bean验证提供程序

Spring提供了对Bean验证API的全面支持，包括将Bean验证提供程序作为Spring 
Bean进行引导。这使你可以在应用程序中需要验证的任何地方注入*javax.validation.ValidatorFactory*或*javax.validation.Validator*。

你可以使用LocalValidatorFactoryBean将默认的Validator配置为Spring Bean，如以下示例所示：

```java
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration

public class AppConfig {

    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean;
    }
}
```

前面示例中的基本配置触发Bean验证以使用其默认引导机制进行初始化。 Bean验证提供程序，例如Hibernate Validator，应该存在于类路径中并被自动检测到。

#### 注入验证器

LocalValidatorFactoryBean同时实现*javax.validation.ValidatorFactory*和*javax.validation.Validator*以及Spring的*org.springframework.validation.Validator*。你可以将对这些接口之一的引用注入需要调用验证逻辑的bean中。

如果你希望直接使用Bean Validation API，则可以注入对*javax.validation.Validator*的引用，如以下示例所示：

```java
import javax.validation.Validator;

@Service
public class MyService {

    @Autowired
    private Validator validator;
}
```

如果你的bean需要使用Spring Validation API，则可以注入对*org.springframework.validation.Validator*的引用，如以下示例所示：

```java
import org.springframework.validation.Validator;

@Service
public class MyService {

    @Autowired
    private Validator validator;
}
```

#### 配置自定义约束

每个bean验证约束都包括两个部分：

- @Constraint注解，用于声明约束及其可配置属性。
- *javax.validation.ConstraintValidator*接口的实现，用于实现约束的行为。

要将声明与实现相关联，每个@Constraint注解都引用一个对应的ConstraintValidator实现类。在运行时，当在域模型中遇到约束注解时，ConstraintValidatorFactory实例化引用的实现。

默认情况下，LocalValidatorFactoryBean配置一个SpringConstraintValidatorFactory，该工厂使用Spring创建ConstraintValidator实例。这使你的自定义ConstraintValidators像其他任何Spring bean一样受益于依赖项注入。

以下示例显示了一个自定义@Constraint声明，后跟一个关联的ConstraintValidator实现，该实现使用Spring进行依赖项注入：

```java
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=MyConstraintValidator.class)
public @interface MyConstraint {
}
```

```java
import javax.validation.ConstraintValidator;

public class MyConstraintValidator implements ConstraintValidator {

    @Autowired;
    private Foo aDependency;

    // ...
}
```

如前面的示例所示，ConstraintValidator实现可以像其他任何Spring bean一样具有其@Autowired依赖项。

#### Spring驱动方法验证

你可以通过MethodValidationPostProcessor bean定义将Bean Validation 1.1（以及作为自定义扩展，还包括Hibernate Validator 4.3）支持的方法验证功能集成到Spring上下文中：

```java
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration

public class AppConfig {

    @Bean
    public MethodValidationPostProcessor validationPostProcessor() {
        return new MethodValidationPostProcessor;
    }
}
```

为了有资格进行Spring驱动的方法验证，所有目标类都必须使用Spring的@Validated注解进行注解，该注解也可以选择声明要使用的验证组。有关使用Hibernate
Validator和Bean Validation 1.1提供程序的设置详细信息，请参见[`MethodValidationPostProcessor`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/validation/beanvalidation/MethodValidationPostProcessor.html)。

> 方法验证依赖于目标类周围的AOP代理，即接口上方法的JDK动态代理或CGLIB代理。代理的使用存在某些限制，[Understanding AOP Proxies](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-understanding-aop-proxies)中介绍了其中的一些限制。另外，请记住在代理类上始终使用方法和访问器；直接访问将不起作用。

#### 其他配置选项

在大多数情况下，默认LocalValidatorFactoryBean配置就足够了。从消息插值到遍历解析，有许多用于各种Bean验证构造的配置选项。有关这些选项的更多信息，请参见[`LocalValidatorFactoryBean`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/validation/beanvalidation/LocalValidatorFactoryBean.html)Javadoc。

### 配置一个DataBinder

从Spring 3开始，你可以使用Validator配置DataBinder实例。配置完成后，你可以通过调用binder.validate()来调用Validator。任何验证`Errors` 都会自动添加到binder的BindingResult中。

下面的示例演示如何在绑定到目标对象后，以编程方式使用DataBinder来调用验证逻辑：

```java
Foo target = new Foo();
DataBinder binder = new DataBinder(target);
binder.setValidator(new FooValidator());

// bind to the target object
binder.bind(propertyValues);

// validate the target object
binder.validate();

// get BindingResult that includes any validation errors
BindingResult results = binder.getBindingResult();
```

你还可以通过*dataBinder.addValidators*和*dataBinder.replaceValidators*配置具有多个Validator实例的DataBinder。当将全局配置的bean验证与在DataBinder实例上本地配置的Spring Validator结合使用时，这很有用。请参阅[Spring MVC Validation Configuration](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/web.html#mvc-config-validation).

#### Spring MVC 3验证

参见Spring MVC一章中的 [Validation](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/web.html#mvc-config-validation).

> 翻译：侧边翻译
>
> 校正：靓仔Q
>
> 时间：2020.5.16~2020.5.18

