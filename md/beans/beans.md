---
[The IoC Container](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans )(Version 5.2.6.RELEASE)

---

参考文档的这一部分涵盖了Spring框架必不可少的内容。

其中最重要的是Spring框架的控制反转（IoC）容器。

对Spring框架的IoC容器有了了解之后将全面介绍Spring的面向切面编程（AOP）技术。Spring框架拥有自己的AOP框架，该框架拥有易于理解的概念同时可以解决Java企业编程中大部分难题。

## Spring IoC容器和Bean

本篇介绍控制反转原理。

控制反转是定义对象依赖关系的过程，譬如通过构造器参数，工厂方法参数或者对象实例化后进行设值。

> 原文：It is a process whereby objects define their dependencies (that is, the other objects they work with) only through constructor arguments, arguments to a factory method, or properties that are set on the object instance after it is constructed or returned from a factory method. 

之后容器在程序创建bean的时候将bean所需要的依赖注入给bean。这个过程也就是为何会被称为控制反转的原因，程序通过直接使用构造器或者注入服务器定位器模式<sup>*</sup>自己控制实例化或者依赖的定位。

> 译者注:服务定位器模式（Service Locator Pattern）用在我们想使用 JNDI 查询定位各种服务的时候。考虑到为某个服务查找 JNDI 的代价很高，服务定位器模式充分利用了缓存技术。在首次请求某个服务时，服务定位器在 JNDI 中查找服务，并缓存该服务对象。当再次请求相同的服务时，服务定位器会在它的缓存中查找，这样可以在很大程度上提高应用程序的性能。
>
> 可以参考[Java Service Locator Pattern(服务器定位模式)](https://www.jianshu.com/p/b04fdb2d336d)

<i>org.springframework.beans</i>和<i>org.springframework.context</i>包是Spring框架IoC容器的基础。BeanFactory接口提供了一种更高级的配置机制，能够管理任何类型的对象。ApplicationContext则是BeanFactory的子接口，它添加了：

- 与Spring AOP更方便的集成
- 消息资源处理（多用于国际化）
- 事件发布
- 应用层特定的上下文，例如Web应用程序中使用的WebApplicationContext

简而言之，BeanFactory提供了配置框架以及基本功能，而ApplicationContext则添加了很多企业级开发的功能。AppliationContext是BeanFactory的完整超集(大概是说ApplicationContext功能比BeanFacotry丰富很多)。如果想了解更多关于使用BeanFactory替代ApplicationContext信息的请移步[这里](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-beanfactory )。

在Spring中,构成应用程序主干并由Sprng IoC容器管理的对象称之为bean。bean是由Spring IoC容器实例化、组装以及以其他方式管理的对象，否则，bean与一般对象无异。bean及其之间的依赖关系通过配置的元数据所反映。

## 容器概览

<i>org.springframework.context.ApplicationContext</i>接口表示Spring IoC容器，并负责bean实例化、配置和组装。容器通过读取配置元数据来获取有关要实例化、配置和组装对象的指令。配置元数据可以以XML、Java注解或Java代码表示。它可以让使用者表达出应用程序对象的各种复杂的依赖关系。

Spring提供了ApplicationContext接口的几种实现。在独立应用程序中，通常会创建ClassPathXmlApplicationContext或FileSystemXmlApplicationContext的实例。尽管XML是定义配置元数据的传统格式，但是还是可以通过提供少量XML配置来声明性地启用对一些其他元数据格式的支持，从而指示容器将Java注解或代码用作元数据格式。

在大多数应用场景中，不需要显式用户代码即可实例化Spring IoC容器的一个或多个实例。例如，在Web应用程序场景中，应用程序的web.xml文件中简单的8行（大约）样板通常就足够了（请参阅[Convenient ApplicationContext Instantiation for Web Applications](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#context-create )）。如果使用Spring Tools for Eclipse（Eclipse支持的开发环境），则只需单击几下鼠标即可轻松创建此样板配置。

下图显示了Spring的工作原理的高级视图。你的应用程序类与配置元数据结合在一起，在创建和初始化ApplicationContext之后，你将拥有一个完全配置且可执行的系统或应用程序。

![](https://www.hellooooo.top/image/blog/2020/05/spring/container-magic.png)

### 配置元数据

如上图所示，Spring IoC容器使用一种形式的配置元数据（Configuration Metadata）。

> 大概是说，不管是XML或者Annotation以及Java Code都可以表示出同一类型的配置信息，使用他们所表现出的是同一种配置元数据，效果是等同的。

> 原文：As the preceding diagram shows, the Spring IoC container consumes a form of configuration metadata.

这个配置元数据表示作为应用程序开发人员告诉Spring容器如何在应用程序中实例化，配置和组装对象。

传统上，配置元数据以简单直观的XML格式提供，这是本章大部分内容用来传达Spring IoC容器的关键概念和功能的内容。

> 基于XML的配置元数据并不是唯一的选择，实际上，Spring获取元数据的方式与元数据的格式无关。当前，推荐使用基于Java的配置元数据。

Spring配置由容器必须管理的至少一个（通常是一个以上）bean定义组成。基于XML的配置元数据将这些bean配置为\<beans/>元素内的\<bean />元素。 Java配置通常在@Configuration类中使用@Bean注解的方法。

这些bean定义对应于组成应用程序的实际对象，比如你定义服务层对象，数据访问对象（DAO），表示对象（例如Struts Action实例），基础结构对象（例如Hibernate 
SessionFactories，JMS队列）等等。通常，不会在容器中配置细粒度的域对象，因为创建和加载域对象通常是DAO和业务逻辑的职责。但是，可以使用Spring与AspectJ的集成来配置在IoC容器控制之外创建的对象。请参阅[Using AspectJ to dependency-inject domain objects with Spring](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-atconfigurable)。

以下示例显示了基于XML的配置元数据的基本结构

~~~XML
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="..." class="...">  
        <!-- collaborators and configuration for this bean go here -->
    </bean>

    <bean id="..." class="...">
        <!-- collaborators and configuration for this bean go here -->
    </bean>

    <!-- more bean definitions go here -->

</beans>


~~~

> id属性是一个标识单个bean定义的字符串。
>
> class属性定义bean的类型并使用类的完全限定名。

### 实例化容器

提供给ApplicationContext构造函数的一个或多个资源字符串是位置路径，这些资源字符串使容器可以从各种外部资源（例如本地文件系统，Java CLASSPATH等）加载配置元数据。

~~~java
ApplicationContext context = new ClassPathXmlApplicationContext("services.xml", "daos.xml");
~~~

### 构成基于XML的配置元数据

使bean的定义跨越多个XML文件可能很有用。通常，每个单独的XML配置文件都代表体系结构中的逻辑层或模块。

可以使用应用程序上下文构造函数从所有这些XML片段中加载bean定义。如上一节中所示，此构造函数可以接收多个Resource字符串。或者，使用一个或多个\<import />元素从另一个文件中加载bean定义。以下示例显示了如何执行此操作：

~~~xml
<beans>
    <import resource="services.xml"/>
    <import resource="resources/messageSource.xml"/>
    <import resource="/resources/themeSource.xml"/>

    <bean id="bean1" class="..."/>
    <bean id="bean2" class="..."/>
</beans>
~~~

在前面的示例中，外部bean定义是从三个文件加载的：services.xml，messageSource.xml和themeSource.xml。所有位置路径都相对于进行导入的定义文件的位置，因此，services.xml必须与进行导入的文件位于同一目录或类路径位置，而messageSource.xml和themeSource.xml必须位于该位置下方的resource文件夹。如你所见，斜杠被忽略了。但是，鉴于这些路径是相对的，最好不要使用任何斜线。根据Spring架构，导入的文件的内容（包括\<beans />元素）必须是有效的XML bean定义。

> 最好不要使用".."操作符访问父路径资源，尤其是在使用"classpath"时，它可能会改变程序读取文件的属性，造成不必要的麻烦。
>
> 你始终可以使用完全限定的资源位置来代替相对路径：例如，file:C:/config/services.xml或classpath:/config/services.xml。但是请注意，你正在将应用程序的配置耦合到特定的绝对路径。通常最好为这样的绝对路径保留一个间接寻址，例如通过在运行时针对JVM系统属性解析的“ $ {…}”占位符。

### The Groovy Bean Definition DSL

作为外部化配置元数据的另一个示例，bean定义也可以在Spring的Groovy bean定义DSL中表达，正如Grails框架。通常，这种配置位于“ .groovy”文件中，其结构如以下示例所示：

~~~groovy
beans {
    dataSource(BasicDataSource) {
        driverClassName = "org.hsqldb.jdbcDriver"
        url = "jdbc:hsqldb:mem:grailsDB"
        username = "sa"
        password = ""
        settings = [mynew:"setting"]
    }
    sessionFactory(SessionFactory) {
        dataSource = dataSource
    }
    myService(MyService) {
        nestedbean = { AnotherBean bean ->
            dataSource = dataSource
        }
    }
}
~~~

这种配置样式在很大程度上等同于XML bean定义，甚至支持Spring的XML配置名称空间。它还允许通过importBeans指令导入XML bean定义文件。

### 使用容器

ApplicationContext是维护不同bean及其依赖关系的注册表的高级工厂接口。通过使用方法T getbean(String name，Class \<T> requiredType)，可以检索bean的实例。

使用ApplicationContext可以读取bean定义并访问它们，如以下示例所示：

~~~java
// create and configure beans
ApplicationContext context = new ClassPathXmlApplicationContext("services.xml", "daos.xml");

// retrieve configured instance
PetStoreService service = context.getBean("petStore", PetStoreService.class);

// use configured instance
List<String> userList = service.getUsernameList();
~~~

使用Groovy配置，引导类看起来非常相似。ApplicationContext有一个不同的上下文实现类，该类可识别Groovy（也可以解析XML bean定义）。以下示例显示了Groovy配置：

~~~groovy
ApplicationContext context = new GenericGroovyApplicationContext("services.groovy", "daos.groovy");
~~~

最灵活的变体是GenericApplicationContext与读取器委托结合使用，例如，与XML文件的XmlBeanDefinitionReader结合使用，如以下示例所示：

~~~java
GenericApplicationContext context = new GenericApplicationContext();
new XmlBeanDefinitionReader(context).loadBeanDefinitions("services.xml", "daos.xml");
context.refresh();
~~~

还可以将GroovyBeanDefinitionReader用于Groovy文件，如以下示例所示：

~~~java
GenericApplicationContext context = new GenericApplicationContext();
new GroovyBeanDefinitionReader(context).loadBeanDefinitions("services.groovy", "daos.groovy");
context.refresh();
~~~

可以在同一ApplicationContext上混合匹配此类阅读器委托，从不同的配置源读取bean定义。

然后，你可以使用getBean检索bean的实例。 ApplicationContext接口还有其他几种检索bean的方法，但是理想情况下，应用程序代码应该永远不要显式使用它们。实际上，应用程序代码应该根本不调用getBean()方法，如此可以完全不依赖于Spring API。例如，Spring与Web框架的集成为各种Web框架组件（例如控制器和JSF管理的bean）提供了依赖项注入，使你可以通过元数据（例如自动装配注解@Autowired）声明对特定bean的依赖项。

## Bean概览

Spring IoC容器管理一个或多个bean。这些bean是使用你提供给容器的配置元数据创建的（例如，以XML \<bean />定义的形式）。

在容器本身内，这些bean定义表示为BeanDefinition对象，其中包含（除其他信息外）以下元数据：

- 包限定的类名：通常，定义了bean的实际实现类。
- bean的行为配置元素，用于声明bean在容器中的行为（作用域，生命周期回调等）。
- 对其他bean进行引用。这些引用也称为协作者或依赖项。
- 要在新创建的对象中设置的其他配置设置，例如，池的大小限制或管理连接池的bean中使用的连接数。

这些元数据转换为构成每个bean定义的一组属性。下表描述了这些属性：

| Property                 | Explained in…                                                |
| ------------------------ | ------------------------------------------------------------ |
| Class                    | [Instantiating Beans](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-class)（实例化Bean） |
| Name                     | [Naming Beans](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-beanname)（为Bean命名） |
| Scope                    | [Bean Scopes](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes)（单例、多例等） |
| Constructor arguments    | [Dependency Injection](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-collaborators)（依赖注入） |
| Properties               | [Dependency Injection](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-collaborators) |
| Autowiring mode          | [Autowiring Collaborators](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-autowire)（自动装配协作） |
| Lazy initialization mode | [Lazy-initialized Beans](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-lazy-init)（懒加载） |
| Initialization method    | [Initialization Callbacks](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-lifecycle-initializingbean)（初始化回调） |
| Destruction method       | [Destruction Callbacks](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-lifecycle-disposablebean)（销毁回调） |

除了包含有关如何创建特定bean的信息的bean定义之外，ApplicationContext实现还允许注册在容器外部（由用户）创建的现有对象。这是通过getBeanFactory()方法访问ApplicationContext的BeanFactory来完成的，该方法返回BeanFactory的实现类
DefaultListableBeanFactory实现。 DefaultListableBeanFactory通过registerSingleton（..）和registerBeanDefinition（..）方法支持此注册。但是，典型的应用程序只能与通过常规元数据定义的bean一起使用。

> bean元数据和手动提供的单例实例需要尽早注册，以便容器在自动装配和其他步骤中正确地推理它们。虽然在某种程度上Spring支持覆盖现有元数据和现有单例实例，但官方不支持在运行时（与对工厂的实时访问同时）对新bean的注册，这可能导致并发访问异常，bean容器中的状态不一致等问题。

### Bean命名

每个bean具有一个或多个标识符。这些标识符在承载bean的容器内必须是唯一的。一个bean通常只有一个标识符。但是，如果需要多个标识符，则可以设置别名。

在基于XML的配置元数据中，可以使用id属性和name属性来指定bean标识符。 
id属性精确指定一个id。通常，这些名称是字母数字（“ mybean”，“ someService”等），但它们也可以包含特殊字符。如果要为bean引入其他别名，还可以在name属性中指定它们，并用逗号（,），分号（;）或空格分隔。Spring历史上，在3.1之前的版本中，id属性定义为xsd：ID类型，该类型限制了可能的字符。从3.1开始，它被定义为xsd：string类型。需要注意，bean ID唯一性不再由XML解析器执行，但仍由容器强制保证。

你也可以不提供bean的名称或ID。如果未明确提供名称或ID，则容器将为该bean生成一个唯一的名称。但是，如果要通过名称引用该bean，则必须通过使用ref元素或服务定位器样式查找，你必须提供一个名称。不提供名称的动机与使用内部bean和自动装配合作有关。

> bean命名的约定是将标准Java约定用于实例字段名称。也就是说，bean名称以小写字母开头，并启用驼峰式大小写。此类名称的示例包括accountManager，accountService，userDao，loginController等。
> 一致地命名bean使配置更易于阅读和理解。另外，如果使用Spring AOP，则在将Advice应用于名称相关的一组bean时，它会很有帮助。

> 通过在类路径中进行组件扫描，Spring会按照前面描述的规则为未命名的组件生成bean名称：从本质上讲，采用简单的类名称并将其初始字符转换为小写。但是，在（不寻常的）特殊情况下，如果有多个字符并且第一个和第二个字符均为大写字母，则会保留原始大小写。这些规则与*java.beans.Introspector.decapitalize*定义的规则相同。
>
> ~~~java
>  public static String decapitalize(String name) {
>      if (name == null || name.length() == 0) {
>          return name;
>      }
>      if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
>                      Character.isUpperCase(name.charAt(0))){
>          return name;
>      }
>      char chars[] = name.toCharArray();
>      chars[0] = Character.toLowerCase(chars[0]);
>      return new String(chars);
>  }
> ~~~
>
> 

#### 在Bean定义之外为Bean定义别名

在bean定义中，可以使用由id属性指定的一个名称和name属性中任意数量的其他名称的组合来为bean提供多个名称。这些名称可以是同一个bean的等效别名，并且在某些情况下很有用，例如使应用中的多个组件通过bean名字指向一个通用的依赖。

但是，在实际定义bean的地方指定所有别名并不总是足够的。有时需要为在别处定义的bean引入别名。这在大型系统中通常是这种情况，在大型系统中，配置分配在每个子系统之间，每个子系统都有自己的对象定义集。在基于XML的配置元数据中，可以使用\<alias/>元素来完成此操作。以下示例显示了如何执行此操作：

~~~java
<alias name="fromName" alias="toName"/>
~~~

在这种情况下，使用该别名定义之后，也可以将名为fromName的bean（在同一容器中）称为toName。

例如，子系统A的配置元数据可以通过子系统A-dataSource的名称引用数据源。子系统B的配置元数据可以通过子系统B-dataSource的名称引用数据源。组成使用这两个子系统的主应用程序时，主应用程序通过myApp-dataSource的名称引用DataSource。要使所有三个名称都引用相同的对象，可以将以下别名定义添加到配置元数据中：

~~~xml
<alias name="myApp-dataSource" alias="subsystemA-dataSource"/>
<alias name="myApp-dataSource" alias="subsystemB-dataSource"/>
~~~

现在，每个组件和主应用程序都可以通过唯一的名称引用数据源，并保证不与任何其他定义冲突（有效地创建名称空间），但它们引用的是同一bean。

> 如果使用Java配置，则@Bean注解可用于提供别名。有关详细信息，请参见 [Using the `@Bean` Annotation](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-java-bean-annotation)。

### Bean实例化

bean定义本质上是创建一个或多个对象的方法。当被请求时，容器将查看命名bean的方法，并使用该bean定义封装的配置元数据来创建（或获取）实际对象。

如果使用基于XML配置元数据，则在\<bean/>元素的class属性中指定要实例化的对象的类型（或类）。这个class属性（在内部是BeanDefinition实例的Class属性）通常是必需的。
（有关异常，请参见[Instantiation by Using an Instance Factory Method](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-class-instance-factory-method) 以及[Bean Definition Inheritance](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-child-bean-definitions)。）可以通过以下两种方式之一使用Class属性：

- 通常，在容器本身通过反射调用其构造函数直接创建bean的情况下，指定要构造的bean类，这在某种程度上等同于使用new运算符的Java代码。
- 指定包含要创建对象的静态工厂方法的类，在不太常见的情况下，容器将在类上调用静态工厂方法以创建bean。从静态工厂方法的调用返回的对象类型可以是同一类，也可以是完全不同的另一类。

> 内部类名称：如果要为静态内部类配置bean定义，则必须使用嵌套类的二进制名称。例如，如果com.example包中有一个名为SomeThing的类，并且此SomeThing类具有一个名为OtherThing的静态嵌套类，则bean定义上的class属性的值为com.example.SomeThing$OtherThing。

#### 使用构造器实例化

当通过构造方法创建一个bean时，所有普通类都可以被Spring使用并与之兼容。也就是说，正在开发的类不需要实现任何特定的接口或以特定的方式进行编码。只需指定bean类就足够了。但是，根据用于该特定bean的IoC的类型，你可能需要一个默认（空）构造函数。

Spring IoC容器几乎可以管理你要管理的任何类。它不仅限于管理真正的JavaBean。大多数Spring用户更喜欢实际的JavaBean，它们仅具有默认（无参数）构造函数，并适当的setter和getter。你还可以在容器中具有更多奇特的非bean样式类。例如，如果你需要使用绝对不符合Java bean规范的旧式连接池，Spring也可以对其进行管理。

使用基于XML的配置元数据，可以如下指定bean类：

~~~xml
<bean id="examplebean" class="examples.Examplebean"/>
<bean name="anotherExample" class="examples.ExamplebeanTwo"/>
~~~

有关向构造函数提供参数（如果需要）并在构造对象之后设置对象实例属性的机制的详细信息，请参见[Injecting Dependencies](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-collaborators).

#### 通过静态工厂方法实例化

定义使用静态工厂方法创建的bean时，请使用class属性指定包含静态工厂方法的类，并使用名为factory-method的属性指定工厂方法本身的名称。你应该能够调用此方法（带有可选参数，如后面所述），并返回一个活动对象，该对象随后将被视为已通过构造函数创建。这种bean定义的一种用法是在旧版代码中调用静态工厂。

以下bean定义指定通过调用工厂方法来创建bean。该定义不指定返回对象的类型（类），而仅指定包含工厂方法的类。在此示例中，createInstance()方法必须是静态方法。以下示例显示如何指定工厂方法：

~~~xml
<bean id="clientService"
    class="examples.ClientService"
    factory-method="createInstance"/>
~~~

以下示例展示可与前面的bean定义一起使用的类：

~~~java
public class ClientService {
    private static ClientService clientService = new ClientService();
    private ClientService() {}

    public static ClientService createInstance() {
        return clientService;
    }
}
~~~

有关为工厂方法提供（可选）参数并在工厂返回对象后设置对象实例属性的机制的详细信息，请参见[Dependencies and Configuration in Detail](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-properties-detailed).

#### 使用实例工厂方法实例化

类似于通过静态工厂方法进行实例化，使用实例工厂方法进行实例化会从容器中调用现有bean的非静态方法来创建新bean。要使用这种机制，请将class属性保留为空，并在factory-bean属性中，在当前（或父、祖先）容器中指定包含要创建该对象的实例方法的bean的名称。使用factory-method属性设置工厂方法本身的名称。以下示例显示了如何配置此类bean：

~~~xml
<!-- the factory bean, which contains a method called createInstance() -->
<bean id="serviceLocator" class="examples.DefaultServiceLocator">
    <!-- inject any dependencies required by this locator bean -->
</bean>

<!-- the bean to be created via the factory bean -->
<bean id="clientService"
    factory-bean="serviceLocator"
    factory-method="createClientServiceInstance"/>
~~~

以下示例显示了相应的类：

~~~java
public class DefaultServiceLocator {

    private static ClientService clientService = new ClientServiceImpl();

    public ClientService createClientServiceInstance() {
        return clientService;
    }
}
~~~

一个工厂类也可以包含一个以上的工厂方法，如以下示例所示：

~~~xml
<bean id="serviceLocator" class="examples.DefaultServiceLocator">
    <!-- inject any dependencies required by this locator bean -->
</bean>

<bean id="clientService"
    factory-bean="serviceLocator"
    factory-method="createClientServiceInstance"/>

<bean id="accountService"
    factory-bean="serviceLocator"
    factory-method="createAccountServiceInstance"/>
~~~

以下示例显示了相应的类：

~~~java
public class DefaultServiceLocator {

    private static ClientService clientService = new ClientServiceImpl();

    private static AccountService accountService = new AccountServiceImpl();

    public ClientService createClientServiceInstance() {
        return clientService;
    }

    public AccountService createAccountServiceInstance() {
        return accountService;
    }
}
~~~

这种方法表明，工厂bean本身可以通过依赖项注入（DI）进行管理和配置。详细信息，请参见[Dependencies and Configuration in Detail](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-properties-detailed).

> 在Spring文档中，“ factory bean”是指在Spring容器中配置并通过实例或静态工厂方法创建对象的bean。相比之下，FactoryBean（请注意大小写）是指特定于Spring的[`FactoryBean`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-extension-factorybean)实现类。

#### 确定Bean的运行时类型

确定特定bean的运行时类型并非易事。 bean元数据定义中的指定类只是初始类引用，可能与声明的工厂方法结合使用，或者是FactoryBean类，这可能导致bean的运行时类型不同，或者在实例的情况下完全不进行设值工厂方法（通过指定的factory-bean名称解析）。此外，AOP代理可以使用基于接口的代理包装bean实例，而目标bean的实际类型（仅是其实现的接口）的暴露程度有限。

> 原文：The runtime type of a specific bean is non-trivial to determine. A specified class in the bean metadata definition is just an initial class reference, potentially combined with a declared factory method or being a `FactoryBean` class which may lead to a different runtime type of the bean, or not being set at all in case of an instance-level factory method (which is resolved via the specified `factory-bean` name instead). Additionally, AOP proxying may wrap a bean instance with an interface-based proxy with limited exposure of the target bean’s actual type (just its implemented interfaces).

找出特定bean的实际运行时类型的推荐方法是调用指定bean名称的BeanFactory.getType。这考虑了以上所有情况，并返回了BeanFactory.getBean针对同一bean名称返回的对象类型。

## 依赖

典型的企业应用程序不由单个对象（或Spring术语中的bean）组成。即使是最简单的应用程序，也有一些对象可以协同工作，以呈现最终用户视为一致的应用程序。下一部分将说明如何从定义多个独立的bean到实现对象协作以目标实现的完全实现的应用程序。

### 依赖注入( Dependency Injection)

依赖注入（DI）是一个过程，通过该过程，对象仅通过构造函数参数，工厂方法的参数或在创建对象实例后在对象实例上设置属性来定义其依赖关系（即，与它们一起工作的其他对象）。从工厂方法返回。然后，容器在创建bean时注入那些依赖项。此过程从根本上讲是通过使用类的直接构造或服务定位器模式来控制bean自身依赖关系的实例化的bean本身的逆过程（因此称为Inversion of Control，控制反转）。

使用依赖注入，代码更加简洁，当为对象提供依赖项时，解耦会更有效。该对象不查找其依赖项，并且不知道依赖项的位置或类。因此，你的类变得更易于测试，尤其是当依赖项依赖于接口或抽象基类时，它们允许在单元测试中使用存根(stub)或模拟实现。

DI存在两个主要变体: [Constructor-based dependency injection](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-constructor-injection) 和 [Setter-based dependency injection](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-setter-injection).

#### 基于构造函数的依赖注入

基于构造函数的DI是通过容器调用具有多个参数的构造函数来完成的，每个参数表示一个依赖项。调用带有特定参数的静态工厂方法来构造bean几乎是等效的，并且本次讨论也将构造函数和静态工厂方法的参数视为类似。以下示例显示了只能通过构造函数注入进行依赖项注入的类：

~~~java
public class SimpleMovieLister {

    // the SimpleMovieLister has a dependency on a MovieFinder
    private MovieFinder movieFinder;

    // a constructor so that the Spring container can inject a MovieFinder
    public SimpleMovieLister(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // business logic that actually uses the injected MovieFinder is omitted...
}
~~~

注意，该类没有什么特别的。它是一个POJO(普通Java类)，不依赖特定于容器的接口，基类或注解。

#### 构造函数参数解析

构造函数参数解析匹配通过使用参数的类型进行。如果bean定义的构造函数参数中不存在潜在的歧义，则在实例化bean时，在bean定义中定义构造函数参数的顺序就是将这些参数提供给适当的构造函数的顺序。考虑以下类：

~~~java
package x.y;

public class ThingOne {

    public ThingOne(ThingTwo thingTwo, ThingThree thingThree) {
        // ...
    }
}
~~~

假设ThingTwo和ThingThree类没有通过继承关联，不存在潜在的歧义。因此，以下配置可以正常工作，并且无需在\<constructor-arg />元素中显式指定构造函数参数索引或类型。

~~~xml
<beans>
    <bean id="beanOne" class="x.y.ThingOne">
        <constructor-arg ref="beanTwo"/>
        <constructor-arg ref="beanThree"/>
    </bean>

    <bean id="beanTwo" class="x.y.ThingTwo"/>

    <bean id="beanThree" class="x.y.ThingThree"/>
</beans>
~~~

当引用另一个bean时，类型是已知的，并且可以发生匹配（与前面的示例一样）。当使用简单类型（例如\<value> true </ value>）时，Spring无法确定值的类型，因此在没有帮助的情况下无法按类型进行匹配。考虑以下类别：

~~~java
package examples;

public class ExampleBean {

    // Number of years to calculate the Ultimate Answer
    private int years;

    // The Answer to Life, the Universe, and Everything
    private String ultimateAnswer;

    public ExampleBean(int years, String ultimateAnswer) {
        this.years = years;
        this.ultimateAnswer = ultimateAnswer;
    }
}
~~~

#### 构造函数参数类型匹配

在上述情况下，通过使用type属性显式指定构造函数参数的类型，则容器可以使用简单类型的类型匹配。如以下示例所示：

~~~xml
<bean id="exampleBean" class="examples.ExampleBean">
    <constructor-arg type="int" value="7500000"/>
    <constructor-arg type="java.lang.String" value="42"/>
</bean>
~~~

#### 构造函数参数索引

可以使用index属性来显式指定构造函数参数的索引，如以下示例所示：

~~~xml
<bean id="exampleBean" class="examples.ExampleBean">
    <constructor-arg index="0" value="7500000"/>
    <constructor-arg index="1" value="42"/>
</bean>
~~~

除了解决多个简单值的歧义性之外，指定索引还可以解决构造函数具有两个相同类型的参数的问题。

> index属性从0开始

#### 构造函数参数名称

还可以使用构造函数参数名称来消除歧义，如以下示例所示：

```xml
<bean id="exampleBean" class="examples.ExampleBean">
    <constructor-arg name="years" value="7500000"/>
    <constructor-arg name="ultimateAnswer" value="42"/>
</bean>
```

请记住，要立即使用该功能，必须在启用debug标志的情况下编译代码，以便Spring可以从构造函数中查找参数名称。如果你不能或不想使用debug标志编译代码，则可以使用@ConstructorProperties注解显式命名构造函数参数。样本类如下所示：

```java
package examples;

public class Examplebean {
    // Fields omitted
    @ConstructorProperties({"years", "ultimateAnswer"})
    public Examplebean(int years, String ultimateAnswer) {
        this.years = years;
        this.ultimateAnswer = ultimateAnswer;
    }
}
```

#### 基于Setter的依赖注入

通过调用无参数构造函数或无参数静态工厂方法来实例化bean之后，容器通过在bean上调用setter方法来完成基于setter的DI。

下面的示例显示通过使用setter注入来依赖的类。此类是常规的Java类，不依赖特定于容器的接口，基类或注解

~~~java
public class SimpleMovieLister {

    // the SimpleMovieLister has a dependency on the MovieFinder
    private MovieFinder movieFinder;

    // a setter method so that the Spring container can inject a MovieFinder
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // business logic that actually uses the injected MovieFinder is omitted...
}
~~~

ApplicationContext支持其管理的bean基于构造函数和基于setter的依赖注入。在通过构造函数方法注入了某些依赖项之后，它还支持基于setter的注入。你可以以BeanDefinition的形式配置依赖项，将其与PropertyEditor实例结合使用以将属性从一种格式转换为另一种格式。但是，大多数Spring用户并不直接（即以编程方式）使用这些类，而是使用XML bean定义，带注解的组件（即以@Component，@Controller等注解过的类）或基于Java的@Configuration类中的@Bean方法来工作。然后将这些源在内部转换为BeanDefinition的实例，并用于加载整个Spring IoC容器实例。

> 基于构造函数还是基于setter的DI？
>
> 由于可以混合使用基于构造函数的DI和基于setter的DI，因此将构造函数用于强制性依赖项并将setter方法或配置方法用于可选依赖性是一个很好的做法。请注意，可以在setter方法上使用@Required注解，以使该属性成为必需的依赖项。但是，最好使用带有参数的程序验证的构造函数注入。
>
> Spring团队通常提倡构造函数注入，因为它可以让应用程序组件实现为不可变对象，并确保所需的依赖项不为null。此外，构造函数注入的组件始终以完全初始化的状态返回到客户端（调用）代码。附带说明一下，大量的构造函数自变量是一种不好的代码写法，这表明该类可能承担了太多的职责，应该对其进行重构以更好地解决关注点分离问题。
>
> setter注入主要应仅用于可以在类中分配合理的默认值的可选依赖项。否则，必须在代码使用依赖项的任何地方执行非空检查。 setter注入的一个好处是，setter方法可使该类的对象在以后重新配置或重新注入。因此，通过JMX Mbean进行管理是用于setter注入的引人注目的用例。
>
> 使用对特定类最有意义的DI样式。有时，在处理没有源代码的第三方类时，将为你做出选择。例如，如果第三方类未公开任何setter方法，则构造函数注入可能是DI的唯一可用形式。

#### 依赖解析过程

容器执行bean依赖解析，如下所示：

- 将使用描述所有bean的配置元数据来创建和初始化ApplicationContext。可以通过XML，Java代码或注解配置元数据。
- 对于每个bean，其依赖关系都以属性、构造函数参数或静态工厂方法的参数的形式表示（如果使用它而不是普通的构造函数）。实际创建bean时，会将这些依赖项提供给bean。
- 每个属性或构造函数参数都是要设置的值的实际定义，或者是对容器中另一个bean的引用。
- 每个值的属性或构造函数参数都将从其指定的格式转换为该属性或构造函数参数的实际类型。默认情况下，Spring可以将以字符串格式提供的值转换为所有内置类型，例如int，long，String，boolean等。

在创建容器时，Spring容器会验证每个bean的配置。但是，在实际创建bean之前，不会设置bean属性本身。创建容器时，将创建具有单例作用域并设置为预先实例化（默认）的bean。范围在 [Bean Scopes](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes)中定义。否则，仅在请求时才创建bean。创建bean时可能会导致创建一个bean图，也就是当bean的依赖依赖其他依赖（等等复杂的依赖）。注意，这些依赖项之间的解析不匹配可能会出现得很晚——也就是说，在创建首次受影响的bean时可能才会出现。

> 循环依赖
>
> 如果主要使用构造函数注入，则可能会出现无法解决的循环依赖问题。
>
> 例如：A类通过构造函数注入需要B类的实例，而B类通过构造函数注入需要A类的实例。如果你为类A和B相互注入配置了bean，则Spring IoC容器会在运行时检测到此循环引用，抛出BeanCurrentlyInCreationException。
>
> 一种可能的解决方案是编辑某些类的源代码，这些类的源代码由setter而不是构造函数来配置。或者，避免构造函数注入，而仅使用setter注入。换句话说，尽管不建议这样做，但是你可以使用setter注入配置循环依赖项。
>
> 与典型情况（没有循环依赖关系）不同，bean A和bean B之间的循环依赖关系迫使其中一个bean在完全初始化之前被注入另一个bean（经典的“鸡与蛋”场景）。

通常，你可以信任Spring会做正确的事。它在容器加载时检测配置问题，例如对不存在的bean的引用和循环依赖问题。在实际创建bean时，Spring设置属性并尽可能晚地解决依赖关系。这意味着如果创建该对象或其依赖项之一时出现问题，正确加载了的Spring容器以后可以在你请求对象时生成异常。例如，由于缺少或无效，bean引发异常属性。这可能会延迟某些配置问题的可见性，这就是为什么默认情况下ApplicationContext实现会预先实例化单例bean的原因。在实际需要使用这些bean之前要花一些前期时间和内存，你会在创建ApplicationContext时发现配置问题，而不是稍后。你仍然可以覆盖此默认行为，以便单例bean延迟初始化，而不是预先实例化。

如果不存在循环依赖关系，则在将一个或多个协作bean注入到依赖bean中时，每个协作bean都将在注入到依赖bean中之前被完全配置。这意味着，如果bean A依赖于bean B，则Spring IoC容器会在对bean A调用setter方法之前完全配置beanB。换句话说，实例化了bean（如果它不是预先实例化的单例），设置其依赖项，并调用相关的生命周期方法（例如 [configured init method](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-lifecycle-initializingbean)或[Initializingbean callback method](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-lifecycle-initializingbean)）。

#### 依赖注入的例子

以下示例将基于XML的配置元数据用于基于setter的DI。 Spring XML配置文件的一小部分指定了一些bean定义，如下所示：

```xml
<bean id="exampleBean" class="examples.ExampleBean">
    <!-- setter injection using the nested ref element -->
    <property name="beanOne">
        <ref bean="anotherExampleBean"/>
    </property>

    <!-- setter injection using the neater ref attribute -->
    <property name="beanTwo" ref="yetAnotherBean"/>
    <property name="integerProperty" value="1"/>
</bean>

<bean id="anotherExampleBean" class="examples.AnotherBean"/>
<bean id="yetAnotherBean" class="examples.YetAnotherBean"/>
```

以下示例显示了相应的Examplebean类：

```java
public class ExampleBean {

    private AnotherBean beanOne;

    private YetAnotherBean beanTwo;

    private int i;

    public void setBeanOne(AnotherBean beanOne) {
        this.beanOne = beanOne;
    }

    public void setBeanTwo(YetAnotherBean beanTwo) {
        this.beanTwo = beanTwo;
    }

    public void setIntegerProperty(int i) {
        this.i = i;
    }
}
```

在前面的示例中，声明了setter以与XML文件中指定的属性匹配。以下示例使用基于构造函数的DI：

```xml
<bean id="exampleBean" class="examples.ExampleBean">
    <!-- constructor injection using the nested ref element -->
    <constructor-arg>
        <ref bean="anotherExampleBean"/>
    </constructor-arg>

    <!-- constructor injection using the neater ref attribute -->
    <constructor-arg ref="yetAnotherBean"/>

    <constructor-arg type="int" value="1"/>
</bean>

<bean id="anotherExampleBean" class="examples.Anotherbean"/>
<bean id="yetAnotherBean" class="examples.YetAnotherbean"/>
```

以下示例显示了相应的ExampleBean类：

```java
public class ExampleBean {

    private AnotherBean beanOne;

    private YetAnotherBean beanTwo;

    private int i;

    public ExampleBean(
        AnotherBean anotherBean, YetAnotherBean yetAnotherBean, int i) {
        this.beanOne = anotherBean;
        this.beanTwo = yetAnotherBean;
        this.i = i;
    }
}
```

=========2021年4月13日

bean定义中指定的构造函数参数用作ExampleBean构造函数的参数。

现在考虑该示例的一个变体，在该变体中，不是使用构造函数，而是指示Spring调用静态工厂方法以返回对象的实例：

```xml
<bean id="exampleBean" class="examples.ExampleBean" factory-method="createInstance">
    <constructor-arg ref="anotherExampleBean"/>
    <constructor-arg ref="yetAnotherBean"/>
    <constructor-arg value="1"/>
</bean>

<bean id="anotherExampleBean" class="examples.AnotherBean"/>
<bean id="yetAnotherBean" class="examples.YetAnotherBean"/>
```

以下示例显示了相应的ExampleBean类：

```java
public class ExampleBean {

    // a private constructor
    private ExampleBean(...) {
        ...
    }

    // a static factory method; the arguments to this method can be
    // considered the dependencies of the bean that is returned,
    // regardless of how those arguments are actually used.
    public static ExampleBean createInstance (
        Anotherbean anotherBean, YetAnotherBean yetAnotherBean, int i) {

        ExampleBean eb = new ExampleBean (...);
        // some other operations...
        return eb;
    }
}
```

静态工厂方法的参数由\<constructor-arg />元素提供，与实际使用构造函数时完全相同。 
工厂方法返回的类的类型不必与包含静态工厂方法的类的类型相同（尽管在此示例中相同）。实例（非静态）工厂方法可以以基本上相同的方式使用（除了使用factory-bean属性代替class属性之外），因此在此不讨论这些细节。

### 详细的依赖和配置

如上一节所述，可以将bean属性和构造函数参数定义为对其他托管bean（协作者）的引用或内联定义的值。 Spring的基于XML的配置元数据为此目的在其\<property />和\<constructor-arg/>元素内支持子元素类型。

#### 直接值（基本类型，字符串等）

\<property />元素的value属性将属性或构造函数参数指定为人类可读的字符串表示形式。 Spring的转换服务用于将这些值从字符串转换为属性或参数的实际类型。以下示例显示了设置的各种值：

```xml
<bean id="myDataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
    <!-- results in a setDriverClassName(String) call -->
    <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
    <property name="url" value="jdbc:mysql://localhost:3306/mydb"/>
    <property name="username" value="root"/>
    <property name="password" value="masterkaoli"/>
</bean>
```

下面的示例使用p-namespace进行更简洁的XML配置：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:p="http://www.springframework.org/schema/p"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
    https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="myDataSource" class="org.apache.commons.dbcp.BasicDataSource"
        destroy-method="close"
        p:driverClassName="com.mysql.jdbc.Driver"
        p:url="jdbc:mysql://localhost:3306/mydb"
        p:username="root"
        p:password="masterkaoli"/>

</beans>
```

前面的XML更简洁。但是，除非在创建bean定义时使用支持自动属性完成的IDE（例如IntelliJ IDEA或Eclipse的Spring Tools），否则错字是在运行时而不是编写时发现的。强烈建议使用此类IDE帮助开发。

还可以配置java.util.Properties实例，如下所示：

```xml
<bean id="mappings"
  class="org.springframework.context.support.PropertySourcesPlaceholderConfigurer">
    <!-- typed as a java.util.Properties -->
    <property name="properties">
        <value>
            jdbc.driver.className=com.mysql.jdbc.Driver
            jdbc.url=jdbc:mysql://localhost:3306/mydb
        </value>
    </property>
</bean>
```

Spring容器通过使用Javabeans PropertyEditor机制将\<value />元素内的文本转换为java.util.Properties实例。这是一个不错的捷径，并且Spring团队更喜欢使用嵌套的\<value/>元素而不是value属性样式的几个地方之一。

#### idref元素

idref元素只是一种防错方法，可以将容器中另一个bean的id（字符串值-不是引用）传递给\<constructor-arg />或\<property />元素。以下示例显示了如何使用它：

```xml
<bean id="theTargetbean" class="..."/>

<bean id="theClientbean" class="...">
    <property name="targetName">
        <idref bean="theTargetbean"/>
    </property>
</bean>
```

前面的bean定义片段（在运行时）与下面的片段完全等效：

```xml
<bean id="theTargetbean" class="..." />
<bean id="client" class="...">
    <property name="targetName" value="theTargetbean"/>
</bean>
```

第一种形式优于第二种形式，因为使用idref标记可使容器在部署时验证所引用的命名bean是否实际存在。在第二个变体中，不对传递给客户端bean的targetName属性的值执行验证。拼写错误仅在实际实例化客户端bean时发现（最有可能导致致命的结果）。如果客户端bean是原型bean，则可能在部署容器很长时间后才发现此错字和所产生的异常。

> 在4.0 bean XSD中不再支持idref元素上的local属性，因为它不再提供常规bean引用上的值。升级到4.0模式时，请将现有的idref本地引用更改为idref bean。

\<idref />元素带来价值的一个常见地方（至少在Spring 2.0之前的版本中）是在ProxyFactoryBean bean定义中的AOP拦截器的配置中。指定拦截器名称时使用\<idref />元素可防止出现拼写错误的拦截器ID。

#### 对其他bean的引用（协作者）

ref元素是\<constructor-arg />或\<property />定义元素内的最后一个元素。在这里，将bean的指定属性的值设置为对容器管理的另一个bean（协作者）的引用。引用的bean是要设置其属性的bean的依赖关系，并且在设置属性之前根据需要对其进行初始化。（如果协作者是单例bean，则它可能已经由容器初始化了。）所有引用最终都是对另一个对象的引用。范围和验证取决于你是通过bean还是parent属性指定另一个对象的ID或名称。

通过\<ref />标记的bean属性指定目标bean是最通用的形式，并且允许创建对同一容器或父容器中任何bean的引用，而不管它是否在同一XML文件中。bean属性的值可以与目标bean的id属性相同，也可以与目标bean的name属性中的值之一相同。下面的示例演示如何使用ref元素：

```xml
<ref bean="somebean"/>
```

通过parent属性指定目标bean将创建对当前容器的父容器中bean的引用。parent属性的值可以与目标bean的id属性或目标bean的name属性中的值之一相同。目标bean必须位于当前容器的父容器中。主要在具有容器层次结构并且要使用与父bean名称相同的代理将现有bean封装在父容器中时，才应使用此bean参考变量。以下一对清单显示了如何使用parent属性：

```xml
<!-- in the parent context -->
<bean id="accountService" class="com.something.SimpleAccountService">
    <!-- insert dependencies as required as here -->
</bean>
```

```xml
<!-- in the child (descendant) context -->
<bean id="accountService" <!-- bean name is the same as the parent bean -->
    class="org.springframework.aop.framework.ProxyFactoryBean">
    <property name="target">
        <ref parent="accountService"/> <!-- notice how we refer to the parent bean -->
    </property>
    <!-- insert other configuration and dependencies as required here -->
</bean>
```

#### 内部Bean

\<property />或\<constructor-arg />元素内的\<bean />元素定义了一个内部bean，如以下示例所示：

```xml
<bean id="outer" class="...">
    <!-- instead of using a reference to a target bean, simply define the target bean inline -->
    <property name="target">
        <bean class="com.example.Person"> <!-- this is the inner bean -->
            <property name="name" value="Fiona Apple"/>
            <property name="age" value="25"/>
        </bean>
    </property>
</bean>
```

内部bean定义不需要定义ID或name。就算指定，容器也不使用该值作为标识符。容器在创建时也将忽略Scope标志，因为内部bean始终是匿名的，并且始终与外部bean一起创建。不可能独立访问内部bean或将它们注入到协作bean中而不是封装在bean中。

#### 集合

\<list />，\<set />，\<map />和\<props />元素分别设置Java集合类型List，Set，Map和Properties的属性和参数。以下示例显示了如何使用它们：

```xml
<bean id="moreComplexObject" class="example.ComplexObject">
    <!-- results in a setAdminEmails(java.util.Properties) call -->
    <property name="adminEmails">
        <props>
            <prop key="administrator">administrator@example.org</prop>
            <prop key="support">support@example.org</prop>
            <prop key="development">development@example.org</prop>
        </props>
    </property>
    <!-- results in a setSomeList(java.util.List) call -->
    <property name="someList">
        <list>
            <value>a list element followed by a reference</value>
            <ref bean="myDataSource" />
        </list>
    </property>
    <!-- results in a setSomeMap(java.util.Map) call -->
    <property name="someMap">
        <map>
            <entry key="an entry" value="just some string"/>
            <entry key ="a ref" value-ref="myDataSource"/>
        </map>
    </property>
    <!-- results in a setSomeSet(java.util.Set) call -->
    <property name="someSet">
        <set>
            <value>just some string</value>
            <ref bean="myDataSource" />
        </set>
    </property>
</bean>
```

map键或值的值或设置值也可以是以下任意元素：

```xml
bean | ref | idref | list | set | map | props | value | null
```

#### 合并集合

Spring容器还支持合并集合。应用程序开发人员可以定义父\<list/>，\<map/>，\<set/>或\<props />元素，并具有子\<list />，\<map />，\<set />或\<props />元素。从父集合继承和覆盖值。也就是说，子集合的值是合并父集合和子集合的元素的结果，子集合的元素会覆盖父集合中指定的值。

下面的示例演示了集合合并：

```xml
<beans>
    <bean id="parent" abstract="true" class="example.ComplexObject">
        <property name="adminEmails">
            <props>
                <prop key="administrator">administrator@example.com</prop>
                <prop key="support">support@example.com</prop>
            </props>
        </property>
    </bean>
    <bean id="child" parent="parent">
        <property name="adminEmails">
            <!-- the merge is specified on the child collection definition -->
            <props merge="true">
                <prop key="sales">sales@example.com</prop>
                <prop key="support">support@example.co.uk</prop>
            </props>
        </property>
    </bean>
<beans>
```

注意子bean定义的adminEmails属性的\<props />元素上使用merge = true属性。当子bean由容器解析并实例化后，生成的实例具有adminEmails Properties集合，其中包含将孩子的adminEmails集合与父对象的adminEmails集合合并的结果。以下清单显示了结果：

```
administrator=administrator@example.com
sales=sales@example.com
support=support@example.co.uk
```

子属性集合的值集继承了父\<props />的所有属性元素，子属性的support值将覆盖父集合中的值。

此合并行为类似地适用于\<list />，\<map />和\<set />集合类型。在\<list />元素的特定情况下，将维护与List集合类型关联的语义（即，值的有序集合的概念）。父级的值先于子级列表的所有值。对于“Map”，“Set”和“Properties”集合类型，不存在任何排序。因此，对于容器内部使用的关联Map，Set和Properties实现类型基础的集合类型，没有任何排序语义有效。

#### 集合合并的局限性

不能合并不同的集合类型（例如Map和List）。如果你尝试这样做，则会抛出适当的Exception。必须在继承的子bean定义中指定merge属性。在父集合定义上指定merge属性是多余的，不会引起所需的合并。

#### 强类型集合

随着Java 5中泛型类型的引入，用户可以使用强类型集合。也就是说，可以声明一个Collection类型，使其只能包含（例如）String元素。如果使用Spring将强类型的Collection依赖注入到bean中，则可以使用Spring的类型转换支持，以便在将强类型的Collection实例的元素添加到bean中之前将其转换为适当的类型。以下Java类和bean定义显示了如何执行此操作：

```java
public class SomeClass {

    private Map<String, Float> accounts;

    public void setAccounts(Map<String, Float> accounts) {
        this.accounts = accounts;
    }
}
```

```xml
<beans>
    <bean id="something" class="x.y.SomeClass">
        <property name="accounts">
            <map>
                <entry key="one" value="9.99"/>
                <entry key="two" value="2.75"/>
                <entry key="six" value="3.99"/>
            </map>
        </property>
    </bean>
</beans>
```

当准备注入bean somethind的accounts属性时，可以通过反射获得有关强类型Map <String，Float>的元素类型的泛型信息。因此，Spring的类型转换基础结构将各种值元素识别为Float类型，并将字符串值（9.99、2.75和3.99）转换为实际的Float类型。

#### 空值和空字符串

Spring将属性等的空参数视为空字符串。以下基于XML的配置元数据片段将email属性设置为空的String值（""）。

```xml
<bean class="Examplebean">
    <property name="email" value=""/>
</bean>
```

前面的示例等效于以下Java代码：

```java
examplebean.setEmail("");
```

\<null />元素处理空值。以下清单显示了一个示例：

```xml
<bean class="Examplebean">
    <property name="email">
        <null/>
    </property>
</bean>
```

上述配置等同于以下代码：

```java
examplebean.setEmail(null);
```

#### 带p命名空间的XML快捷方式

使用p-namespace，可以使用bean元素的属性（而不是嵌套的\<property />元素）来描述协作bean的属性值，或同时使用两者。

Spring支持带有名称空间的可扩展配置格式，这些名称空间基于XML Schema定义。本章讨论的bean配置格式在XML Schema文档中定义。但是，p命名空间未在XSD文件中定义，仅存在于Spring的核心中。

以下示例显示了两个XML代码段（第一个使用标准XML格式，第二个使用p-命名空间），它们可以解析为相同的结果：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:p="http://www.springframework.org/schema/p"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean name="classic" class="com.example.Examplebean">
        <property name="email" value="someone@somewhere.com"/>
    </bean>

    <bean name="p-namespace" class="com.example.Examplebean"
        p:email="someone@somewhere.com"/>
</beans>
```

该示例显示了p命名空间中的一个属性，该属性在bean定义中称为email。这告诉Spring包含一个属性声明。如前所述，p名称空间没有架构定义，因此可以将属性名称设置为属性名称(p-namespace)。

下一个示例包括两个bean定义，它们都引用了另一个bean：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:p="http://www.springframework.org/schema/p"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean name="john-classic" class="com.example.Person">
        <property name="name" value="John Doe"/>
        <property name="spouse" ref="jane"/>
    </bean>

    <bean name="john-modern"
        class="com.example.Person"
        p:name="John Doe"
        p:spouse-ref="jane"/>

    <bean name="jane" class="com.example.Person">
        <property name="name" value="Jane Doe"/>
    </bean>
</beans>
```

此示例不仅包括使用p-命名空间的属性值，而且还使用特殊格式来声明属性引用。第一个bean定义使用<property name =“ spouse” ref =“ jane” />创建从bean john到bean 
jane的引用，而第二个bean定义使用p：spouse-ref =“jane”作为属性来执行完全一样的东西。在这种情况下，`spouse`是属性名称，而-ref部分表示这不是一个直接值，而是对另一个bean的引用。

> p命名空间不如标准XML格式灵活。例如，声明属性引用的格式与以Ref结尾的属性发生冲突，而标准XML格式则没有。我们建议你仔细选择方法，并将其传达给团队成员，以避免同时使用这三种方法生成XML文档。

#### 带c命名空间的XML快捷方式

与带有p命名空间的XML快捷方式类似，在Spring 3.1中引入的c命名空间允许使用内联属性来配置构造函数参数，而不是嵌套的`constructor-arg`元素。

以下示例使用c：命名空间执行与基于构造函数的依赖注入中的相同的操作

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:c="http://www.springframework.org/schema/c"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="beanTwo" class="x.y.ThingTwo"/>
    <bean id="beanThree" class="x.y.ThingThree"/>

    <!-- traditional declaration with optional argument names -->
    <bean id="beanOne" class="x.y.ThingOne">
        <constructor-arg name="thingTwo" ref="beanTwo"/>
        <constructor-arg name="thingThree" ref="beanThree"/>
        <constructor-arg name="email" value="something@somewhere.com"/>
    </bean>

    <!-- c-namespace declaration with argument names -->
    <bean id="beanOne" class="x.y.ThingOne" c:thingTwo-ref="beanTwo"
        c:thingThree-ref="beanThree" c:email="something@somewhere.com"/>

</beans>
```

c：名称空间使用与p：相同约定（bean引用为尾随-ref）以按名称设置构造函数参数。同样，即使未在XSD模式中定义它（也存在于Spring内核中），也需要在XML文件中声明它。

对于极少数情况下无法使用构造函数自变量名称的情况（通常，如果字节码是在没有调试信息的情况下编译的），可以对参数索引使用后备，如下所示：

```xml
<!-- c-namespace index declaration -->
<bean id="beanOne" class="x.y.ThingOne" c:_0-ref="beanTwo" c:_1-ref="beanThree"
    c:_2="something@somewhere.com"/>
```

> 由于XML语法的原因，索引符号要求前导_的存在，因为XML属性名称不能以数字开头（即使某些IDE允许）。相应的索引符号也可用于\<constructor-arg>元素，但并不常用，因为声明的普通顺序在那里就足够了。

实际上，构造函数解析机制在匹配参数方面非常有效，因此除非确实需要，否则我们建议在整个配置过程中使用名称表示法。

#### 复合属性名称

设置bean属性时，可以使用复合属性名称或嵌套属性名称，只要路径中除最终属性名称以外的所有组件都不为空即可。考虑以下bean定义：

```xml
<bean id="something" class="things.ThingOne">
    <property name="fred.bob.sammy" value="123" />
</bean>
```

something bean具有fred属性，该属性具有bob属性，bob属性又具有sammy属性，并且最终的sammy属性被设置为123。为了使其正常工作，something的fred属性和bob属性构造完成后，fred和bob不能为null。否则，将引发NullPointerException。

### 使用`depends-on`

如果一个bean是另一个bean的依赖项，则通常意味着将一个bean设置为另一个bean的属性。通常，可以使用基于XML的配置元数据中的\<ref/>元素来完成此操作。但是，有时bean之间的依赖性不太直接。一个示例是何时需要触发类中的静态初始值设定项，例如用于数据库驱动程序注册。`depends-on`属性可以显式地强制初始化一个或多个使用该元素的bean之前的bean。下面的示例使用`depends-on`属性来表示对单个bean的依赖关系：

```xml
<bean id="beanOne" class="Examplebean" depends-on="manager"/>
<bean id="manager" class="Managerbean" />
```

要表达对多个bean的依赖关系，提供一个bean名称列表作为depends-on属性的值（逗号，空格和分号是有效的定界符）：

```xml
<bean id="beanOne" class="Examplebean" depends-on="manager,accountDao">
    <property name="manager" ref="manager" />
</bean>

<bean id="manager" class="Managerbean" />
<bean id="accountDao" class="x.y.jdbc.JdbcAccountDao" />
```

>  depends-on属性既可以指定初始化依赖，也可以仅在单例bean的情况下指定相应的销毁时间依赖。与给定bean定义依赖关系的从属bean首先被销毁，然后再销毁给定bean本身。因此，depends-on也可以控制关闭顺序。

### 懒加载的Bean

默认情况下，作为初始化过程的一部分，ApplicationContext实现类会急于创建和配置所有单例bean。通常，这种预初始化是可取的，因为与数小时甚至数天后相比，会立即发现配置可以早些发现周围环境中的错误。如果不希望出现这种情况，则可以通过将bean定义标记为延迟初始化来防止对singleton bean的预先实例化。延迟初始化的bean告诉IoC容器在首次请求时而不是在启动时创建一个bean实例。

在XML中，此行为由\<bean />元素上的lazy-init属性控制，如以下示例所示：

```xml
<bean id="lazy" class="com.something.ExpensiveToCreatebean" lazy-init="true"/>
<bean name="not.lazy" class="com.something.Anotherbean"/>
```

当前面的配置被ApplicationContext占用时，在ApplicationContext启动时不会急切地预先实例化懒惰的bean，而在not.lazy bean中则会急切地预先实例化。

但是，如果延迟初始化的bean是未延迟初始化的单例bean的依赖项，则ApplicationContext在启动时会创建延迟初始化的bean，因为它必须满足单例的依赖关系。延迟初始化的bean被注入到其他未延迟初始化的单例bean中。

还可以通过使用\<beans />元素上的default-lazy-init属性在容器级别控制延迟初始化，以下示例显示：

```xml
<beans default-lazy-init="true">
    <!-- no beans will be pre-instantiated... -->
</beans>
```

### 自动装配协作器

Spring容器可以自动装配协作bean之间的关系。你可以通过检查ApplicationContext的内容，让Spring为你的bean自动解决协作者（其他bean）。自动装配具有以下优点：

- 自动装配可以大大减少指定属性或构造函数参数的需要。 （在本章其他地方讨论的其他机制（例如bean模板）在这方面也很有价值。）
- 随着对象的发展，自动装配可以更新配置。例如，如果需要向类中添加依赖项，则无需修改配置即可自动满足该依赖项。因此，在代码库变得更稳定时，自动装配在开发过程中特别有用，而不必忽略切换到显式接线的选择。

使用基于XML的配置元数据时（请参阅[Dependency Injection](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-collaborators)），可以使用\<bean />元素的autowire属性为bean定义指定自动装配模式。自动装配功能具有四种模式。你可以为每个bean指定自动装配，因此可以选择要自动装配的bean装配。下表描述了四种自动装配模式：

| Mode          | Explanation                                                  |
| ------------- | ------------------------------------------------------------ |
| `no`          | （默认）无自动装配。 Bean引用必须由ref元素定义。对于较大的部署，建议不要更改默认设置，因为明确指定协作者可以提供更好的控制和清晰度。在某种程度上，它记录了系统的结构。 |
| `byName`      | 按属性名称自动装配。Spring寻找与需要自动装配的属性同名的bean。例如，如果一个bean定义被设置为按名称自动装配，并且包含一个master属性（即，它具有setMaster（..）方法），那么Spring将查找一个名为master的bean定义，并使用它来设置该属性。 |
| `byType`      | 如果容器中恰好存在一个该属性类型的bean，则使该属性自动装配。如果存在多个，则将引发致命异常，这表明你可能不该对bean使用byType自动装配。如果没有匹配的bean，则什么都不会发生（未设置该属性）。 |
| `constructor` | 与byType类似，但适用于构造函数参数。如果容器中不存在构造函数参数类型的一个bean，则将引发致命错误。 |

使用byType或constructor自动装配模式，你可以连接数组和类型化的集合。在这种情况下，将提供容器中与期望类型匹配的所有自动装配候选，以满足相关性。如果期望的键类型为String，则可以自动装配强类型Map实例。自动关联的Map实例的值包含所有与预期类型匹配的bean实例，并且Map实例的键包含相应的bean名称。

#### 自动装配的局限性和缺点

当在项目中一致使用自动装配时，自动装配效果最好。如果通常不使用自动装配，则可能使开发人员仅使用自动装配来连接一个或两个bean定义而感到困惑。

考虑以下自动装配的局限性和缺点：

- 属性和构造器参数设置中的显式依赖关系始终会覆盖自动装配。你无法自动连接简单属性，例如基本类型，字符串和Class（以及此类简单属性的数组）。此限制是设计使然。
- 自动装配不如显式装配精确。尽管如前所述，Spring还是谨慎避免在可能产生意外结果的模棱两可的情况下进行猜测。 Spring管理的对象之间的关系不再明确记录。
- 装配信息可能不适用于可能从Spring容器生成文档的工具。
- 容器内的多个bean定义可能与要自动装配的setter方法或构造函数参数指定的类型匹配。对于数组，集合或Map实例，这不一定是问题。但是，对于需要单个值的依赖项，不会任意解决此歧义。如果没有唯一的bean定义可用，则会引发异常。

在后一种情况下，你有几种选择：

- 放弃自动装配，转而使用显式装配。
- 通过将bean的autowire-candidate属性设置为false，避免自动装配bean定义，如下一节所述。
- 通过将其\<bean />元素的primary属性设置为true，将单个bean定义指定为主要候选对象。
- 如[基于注解的容器配置](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-annotation-config)中所述，通过基于注解的配置实现更细粒度的控件。

#### 从自动装配中排除bean

在每个bean的基础上，你可以从自动装配中排除一个bean。使用Spring的XML格式，将\<bean />元素的autowire-candidate属性设置为false。容器使特定的bean定义对自动装配基础结构不可用（包括注解样式配置，例如@Autowired）。

> autowire-candidate属性设计为仅影响基于类型的自动装配。它不会影响按名称装配的显式引用，即使指定的bean未标记为自动装配候选，该名称也可以解析。因此，如果名称匹配，按名称自动装配仍然会注入bean。

你还可以基于与bean名称的模式匹配来限制自动装配候选。顶级\<beans/>元素在其default-autowire-candidates属性内接受一个或多个模式。例如，要将自动装配候选状态限制为名称以Repository结尾的任何bean，请提供\*Repository值。要提供多种模式，请在以逗号分隔的列表中定义它们。bean定义的autowire-candidate属性的显式值true或false始终优先。对于此类bean，模式匹配规则不适用。

这些技术对于你不希望通过自动装配将其注入其他bean的bean非常有用。这并不意味着排除的bean本身不能使用自动装配进行配置。相反，bean本身不是自动装配其他bean的候选对象。

### 方法注入

在大多数应用场景中，容器中的大多数bean是单例的。当单例bean需要与另一个单例bean协作或非单例bean需要与另一个非单例bean协作时，通常可以通过将一个bean定义为另一个bean的属性来处理依赖性。当bean的生命周期不同时会出现问题。假设单例bean A需要使用非单例（prototype）bean B，也许在对A的每个方法调用上都使用它。容器仅创建一次单例bean A，因此只有一次机会来设置属性。每次需要一个容器时，容器都无法为bean A提供一个新的bean B实例。

一个解决方案是放弃某些控制反转。你可以通过实现ApplicationContextAware接口，并通过对容器进行getbean（“ B”）调用来使bean A得知容器的存在，以便每次bean A需要它时都请求一个（通常是新的）bean B实例。以下示例显示了此方法：

```java
// a class that uses a stateful Command-style class to perform some processing
package fiona.apple;

// Spring-API imports
import org.springframework.beans.beansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CommandManager implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public Object process(Map commandState) {
        // grab a new instance of the appropriate Command
        Command command = createCommand();
        // set the state on the (hopefully brand new) Command instance
        command.setState(commandState);
        return command.execute();
    }

    protected Command createCommand() {
        // notice the Spring API dependency!
        return this.applicationContext.getbean("command", Command.class);
    }

    public void setApplicationContext(
            ApplicationContext applicationContext) throws beansException {
        this.applicationContext = applicationContext;
    }
}
```

前面的内容是不理想的，因为业务代码知道并耦合到Spring框架。方法注入是Spring IoC容器的一项高级功能，使你可以干净地处理此用例。

> 你可以在[this blog entry](https://spring.io/blog/2004/08/06/method-injection/)中阅读有关方法注入动机的更多信息。

#### 查找方法注入

查找方法注入是容器重写(override)容器管理bean上的方法并返回容器中另一个已命名bean的查找结果的能力。查找通常涉及原型(prototype)bean，如上一节中所述。 Spring框架通过使用从CGLIB库生成字节码来动态生成覆盖该方法的子类来实现方法注入。

> - 为了使此动态子类起作用，Spring bean子类的类也不能是final，而要覆盖的方法也不能是final。
> - 对具有抽象方法的类进行单元测试需要你自己对该类进行子类化，并提供该抽象方法的存根实现(stub implementation)。
> - 组件扫描也需要具体方法，这需要具体的类别。
> - 另一个关键限制是，查找方法不适用于工厂方法，尤其不适用于配置类中的@Bean方法，因为在这种情况下，容器不负责创建实例，因此无法创建运行时生成的子类。

对于前面的代码片段中的CommandManager类，Spring容器动态地覆盖createCommand()方法的实现。如重新编写的示例所示，CommandManager类没有任何Spring依赖项：

```java
package fiona.apple;

// no more Spring imports!

public abstract class CommandManager {

    public Object process(Object commandState) {
        // grab a new instance of the appropriate Command interface
        Command command = createCommand();
        // set the state on the (hopefully brand new) Command instance
        command.setState(commandState);
        return command.execute();
    }

    // okay... but where is the implementation of this method?
    protected abstract Command createCommand();
}
```

在包含要注入的方法的客户端类（在本例中为CommandManager）中，要注入的方法需要以下形式的签名：

```xml
<public|protected> [abstract] <return-type> theMethodName(no-arguments);
```

如果该方法是抽象的，则动态生成的子类将实现该方法。否则，动态生成的子类将覆盖原始类中定义的具体方法。考虑以下示例：

```xml
<!-- a stateful bean deployed as a prototype (non-singleton) -->
<bean id="myCommand" class="fiona.apple.AsyncCommand" scope="prototype">
    <!-- inject dependencies here as required -->
</bean>

<!-- commandProcessor uses statefulCommandHelper -->
<bean id="commandManager" class="fiona.apple.CommandManager">
    <lookup-method name="createCommand" bean="myCommand"/>
</bean>
```

每当需要新的myCommand bean实例时，标识为commandManager的bean就会调用其自己的createCommand()方法。如果确实需要myCommand bean，则必须小心将其部署为原型(prototype)。如果是单例，则每次都返回myCommand bean的相同实例。

另外，在基于注解的组件模型中，可以通过@Lookup注解声明一个查找方法，如以下示例所示：

```java
public abstract class CommandManager {

    public Object process(Object commandState) {
        Command command = createCommand();
        command.setState(commandState);
        return command.execute();
    }

    @Lookup("myCommand")
    protected abstract Command createCommand();
}
```

或者，更惯用的是，可以依赖于目标bean根据lookup方法的声明的返回类型来解析：

```java
public abstract class CommandManager {

    public Object process(Object commandState) {
        MyCommand command = createCommand();
        command.setState(commandState);
        return command.execute();
    }

    @Lookup
    protected abstract MyCommand createCommand();
}
```

请注意，通常应使用具体的存根实现声明此类带注解的查找方法，以使其与Spring的组件扫描规则兼容，在默认情况下抽象类将被忽略。此限制不适用于显式注册或显式导入的bean类。

> 访问范围不同的目标bean的另一种方法是ObjectFactory / Provider注入点。请参阅[Scoped beans as Dependencies](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes-other-injection).
>
> 你可能还会发现ServiceLocatorFactorybean（在*org.springframework.beans.factory.config*包中）很有用。

#### 任意方法替换

与查找方法注入相比，方法注入的一种不太有用的形式是能够用另一种方法实现替换托管bean中的任意方法。你可以放心地跳过本节的其余部分，直到你真正需要此功能为止。

借助基于XML的配置元数据，可以使用replaced-method元素将现有的方法实现替换已部署的bean。考虑以下类，该类具有一个我们要覆盖的名为computeValue的方法：

```java
public class MyValueCalculator {

    public String computeValue(String input) {
        // some real code...
    }

    // some other methods...
}
```

实现*org.springframework.beans.factory.support.MethodReplacer*接口的类提供了新的方法定义，如以下示例所示：

```java
/**
 * meant to be used to override the existing computeValue(String)
 * implementation in MyValueCalculator
 */
public class ReplacementComputeValue implements MethodReplacer {

    public Object reimplement(Object o, Method m, Object[] args) throws Throwable {
        // get the input value, work with it, and return a computed result
        String input = (String) args[0];
        ...
        return ...;
    }
}
```

用于部署原始类并指定方法覆盖的bean定义类似于以下示例：

```xml
<bean id="myValueCalculator" class="x.y.z.MyValueCalculator">
    <!-- arbitrary method replacement -->
    <replaced-method name="computeValue" replacer="replacementComputeValue">
        <arg-type>String</arg-type>
    </replaced-method>
</bean>

<bean id="replacementComputeValue" class="a.b.c.ReplacementComputeValue"/>
```

可以在\<replaced-method />元素内使用一个或多个\<arg-type />元素来指示被覆盖的方法的方法签名。仅当方法重载且类中存在多个变体时，才需要对参数签名。为了方便起见，参数的类型字符串可以是完全限定类型名称的子字符串。例如，以下所有都匹配java.lang.String：

```java
java.lang.String
String
Str
```

因为参数的数量通常足以区分每个可能的选择，所以通过让你仅键入与参数类型匹配的最短字符串，此快捷方式可以节省很多输入。

## Bean Socpe

创建bean定义时，将创建一个食谱(recipe)，用于创建该bean定义所定义的类的实际实例。 将bean定义看成食谱的想法很重要，因为它意味着与类一样，你可以从一个配方中创建许多对象实例。

你不仅可以控制要插入到从特定bean定义创建的对象中的各种依赖项和配置值，还可以控制从特定bean定义创建的对象的范围。这种方法功能强大且灵活，因为你可以选择通过配置创建的对象的范围，而不必在Java类级别上确定对象的范围。可以将bean定义为部署在多个范围中。Spring框架支持六个范围，其中4个只有在使用Web感知的ApplicationContext时才可用。你还可以创建[custom scope.](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes-custom)。

| Scope                                                        | Description                                                  |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| [singleton](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes-singleton) | （默认）将每个Spring IoC容器的单个bean定义范围限定为**一个对象**实例 |
| [prototype](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes-prototype) | 将单个bean定义的作用域限定为**任意数量**的对象实例。         |
| [request](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes-request) | 将单个bean定义的范围限定为单个HTTP请求的生命周期。也就是说，每个HTTP请求都有一个在单个bean定义后面创建的bean实例。仅在Web环境的Spring ApplicationContext上下文中有效。 |
| [session](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes-session) | 将单个bean定义的作用域限定为HTTP Session的生命周期。仅在Web环境的Spring ApplicationContext上下文中有效。 |
| [application](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes-application) | 将单个bean定义的作用域限定为ServletContext的生命周期。仅在Web环境的Spring ApplicationContext上下文中有效。 |
| [websocket](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/web.html#websocket-stomp-websocket-scope) | 将单个bean定义的作用域限定为WebSocket的生命周期。仅在Web环境的Spring ApplicationContext上下文中有效。 |

> 从Spring 3.0开始，线程作用域可用，但默认情况下未注册。有关更多信息，请参见[`SimpleThreadScope`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/context/support/SimpleThreadScope.html).。有关如何注册此自定义范围或任何其他自定义范围的说明，请参阅[Using a Custom Scope](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes-custom-using).

### 单例作用域

仅管理一个singleton bean的一个共享实例，并且所有对具有ID或与该bean定义相匹配的ID的bean的请求都将导致该特定的bean实例由Spring容器返回。

换句话说，当你定义一个bean并且其作用域为单例时，Spring IoC容器将为该bean定义所定义的对象创建一个实例。该单个实例存储在此类单例bean的高速缓存中，并且对该命名bean的所有后续请求和引用都返回该高速缓存的对象。下图显示了单例作用域的工作方式：

![](https://www.hellooooo.top/image/blog/2020/05/spring/singleton.png)

Spring的“单例bean”概念与《设计模式 - 可重用的面向对象软件元素》一书中定义的单例模式有所不同。 GoF单例对对象的范围进行硬编码，这样每个ClassLoader只能创建一个特定类的一个实例。最好将Spring单例的范围描述为每个容器一个bean。这意味着，如果你在单个Spring容器中为特定类定义一个bean，则Spring容器将创建该bean所定义的类的一个且只有一个实例。单例作用域是Spring中的默认作用域。要将bean定义为XML中的单例，可以定义bean，如以下示例所示：

```xml
<bean id="accountService" class="com.something.DefaultAccountService"/>

<!-- the following is equivalent, though redundant (singleton scope is the default) -->
<bean id="accountService" class="com.something.DefaultAccountService" scope="singleton"/>
```

### 原型(prototype)作用域

每次对特定bean提出请求时，bean部署的非单一原型范围都会导致创建一个新bean实例。也就是说，该bean被注入到另一个bean中，或者可以通过容器上的getbean()方法调用来请求它。通常，应将原型作用域用于所有有状态bean，将单例作用域用于无状态bean。

下图说明了Spring原型范围：

![](https://www.hellooooo.top/image/blog/2020/05/spring/prototype.png)

（数据访问对象（DAO）通常不配置为原型，因为典型的DAO不拥有任何对话状态。对于我们而言，重用单例图的核心更为容易。）

以下示例将bean定义为XML原型：

```xml
<bean id="accountService" class="com.something.DefaultAccountService" scope="prototype"/>
```

与其他作用域相比，Spring不能管理原型bean的完整生命周期。容器实例化，配置或组装原型对象，然后将其交给客户端，而不会对该原型实例的进一步记录。因此，尽管在不考虑作用域的情况下在所有对象上都调用了初始化生命周期回调方法，但在原型的情况下，不会调用已配置的销毁生命周期回调。客户端代码必须清除原型作用域内的对象并释放原型bean拥有的昂贵资源。要使Spring容器释放由原型作用域的bean占用的资源，请尝试使用自定义[bean post-processor](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-extension-bpp),，该处理器包含对需要清理的bean的引用。

在某些方面，Spring容器在原型作用域bean方面的角色是Java new运算符的替代。超过该时间点的所有生命周期管理必须由客户端处理。 （有关Spring容器中bean生命周期的详细信息，请参阅 [Lifecycle Callbacks](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-lifecycle).）

### 具有原型bean依赖关系的单例bean

当你使用对原型bean有依赖性的单例作用域bean时，请注意，依赖性在实例化时已解决。因此，如果你将依赖范围的原型范围的bean注入到单例范围的bean中，则将实例化新的原型bean，然后将依赖项注入到该单例bean中。原型实例是提供给单例范围的bean的唯一实例。

但是，假设你希望单例作用域的bean在运行时重复获取原型作用域的bean的新实例。你不能将原型作用域的bean依赖项注入到你的单例bean中，因为当Spring容器实例化单例bean并解析并注入其依赖项时，该注入仅发生一次。如果在运行时不止一次需要原型bean的新实例，请参见[Method Injection](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-method-injection)。

### Request, Session, Application, and WebSocket Scopes

仅当你使用Web的Spring ApplicationContext实现（例如XmlWebApplicationContext）时，Request, Session, Application, 和WebSocket Scopes才可用。如果你将这些作用域与常规的Spring IoC容器（例如ClassPathXmlApplicationContext）一起使用，则会抛出一个IllegalStateException异常，该异常提示未知的bean作用域。

#### 初始Web配置

为了支持在Request, Session, Application, 和WebSocket Scopes（Web范围的bean）的bean范围界定，在定义bean之前，需要一些较小的初始配置。 （对于标准范围：单例和原型，不需要此初始设置。）

如何完成此初始设置取决于特定Servlet环境。

如果实际上在Spring Web MVC中访问由Spring DispatcherServlet处理的请求中的作用域bean，则不需要特殊的设置。 DispatcherServlet已经公开了所有相关状态。

如果使用Servlet 2.5 Web容器，并且在Spring的DispatcherServlet之外处理请求（例如，使用JSF或Struts时），则需要注册*org.springframework.web.context.request.RequestContextListener*
ServletRequestListener。对于Servlet 3.0+，可以使用WebApplicationInitializer接口以编程方式完成此操作。或者，对于较旧的容器，将以下声明添加到Web应用程序的web.xml文件中：

```xml
<web-app>
    ...
    <listener>
        <listener-class>
            org.springframework.web.context.request.RequestContextListener
        </listener-class>
    </listener>
    ...
</web-app>
```

另外，如果监听器设置存在问题，请考虑使用Spring的RequestContextFilter。过滤器映射取决于周围的Web应用程序配置，因此你必须适当地对其进行更改。以下清单显示了Web应用程序的过滤器部分：

```xml
<web-app>
    ...
    <filter>
        <filter-name>requestContextFilter</filter-name>
        <filter-class>org.springframework.web.filter.RequestContextFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>requestContextFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    ...
</web-app>
```

DispatcherServlet，RequestContextListener和RequestContextFilter都做完全相同的事情，即将HTTP请求对象绑定到为该请求提供服务的Thread。这使得在请求链和会话范围内的bean可以在调用链的更下游使用。

#### Request作用域

考虑以下XML配置来定义bean：

```xml
<bean id="loginAction" class="com.something.LoginAction" scope="request"/>
```

Spring容器通过为每个HTTP请求使用loginAction bean来创建LoginAction 
bean的新实例。也就是说，loginAction bean的作用域为HTTP请求级别。你可以根据需要更改创建实例的内部状态，因为从同一loginAction bean创建的其他实例不会看到这些状态更改。它们特定于单个请求。当请求完成处理时，bean也会被丢弃。

当使用注解驱动的组件或Java配置时，@RequestScope注解可用于将组件分配给请求范围。以下示例显示了如何执行此操作：

```java
@RequestScope
@Component
public class LoginAction {
    // ...
}
```

#### Session作用域

考虑以下XML配置来定义bean：

```xml
<bean id="userPreferences" class="com.something.UserPreferences" scope="session"/>
```

Spring容器通过在单个HTTP Session的生存期内使用userPreferences bean定义来创建UserPreferences bean的新实例。换句话说，userPreferences bean有效地作用于HTTP Session级别。与Request范围的bean一样，你可以根据需要任意更改所创建实例的内部状态，因为知道其他也在使用从同一userPreferences bean定义创建的实例的HTTP 
Session实例也看不到这些状态变化，因为它们特定于单个HTTP会话。当HTTP会话最终被丢弃时，作用于该特定HTTP会话的bean也将被丢弃。

使用注解驱动的组件或Java配置时，可以使用@SessionScope注解将组件分配给Session范围。

```java
@SessionScope
@Component
public class UserPreferences {
    // ...
}
```

#### Application作用域

考虑以下XML配置来定义bean：

```java
@ApplicationScope
@Component
public class AppPreferences {
    // ...
}
```

Spring容器通过对整个Web应用程序使用一次appPreferences bean定义来创建AppPreferences bean的新实例。也就是说，appPreferences bean的作用域位于ServletContext级别，并存储为常规ServletContext属性。这有点类似于Spring单例bean，但是在两个重要方面有所不同：它是每个ServletContext的单例，而不是每个Spring ApplicationContext的单例（在任何给定的Web应用程序中可能都有多个），并且实际上作为ServletContext属性是公开的。

使用注解驱动的组件或Java配置时，可以使用@ApplicationScope注解将组件分配给Application范围。以下示例显示了如何执行此操作：

```java
@ApplicationScope
@Component
public class AppPreferences {
    // ...
}
```

#### Scope bean作为依赖项

Spring IoC容器不仅管理对象（bean）的实例化，而且还管理协作者（或依赖项）的连接。如果要将（例如）HTTP请求范围的bean注入（例如）另一个作用域更长的bean，则可以选择注入AOP代理来代替作用域的bean。也就是说，你需要注入一个代理对象，该对象公开与Scope对象相同的公共接口，但也可以从相关范围（例如HTTP请求）中检索实际目标对象，并将方法调用委托给实际对象。

> 你还可以在范围为单例的bean之间使用<aop：scoped-proxy />，然后引用将通过可序列化的中间代理，因此能够在反序列化时重新获得目标单例bean。
>
> 当针对范围为原型的bean声明<aop：scoped-proxy />时，共享代理上的每个方法调用都会导致创建新的目标实例，然后将该调用转发到该目标实例。
>
> 同样，作用域代理不是以生命周期安全的方式从较短的作用域访问bean的唯一方法。也可以将注入点（即构造函数或setter参数或自动装配的字段）声明为ObjectFactory \<MyTargetbean>，从而允许getObject()调用在需要时按需检索当前实例。实例或将其单独存储。
>
> 作为扩展变体，可以声明ObjectProvider \<MyTargetbean>，它提供了几个附加的访问变体，包括getIfAvailable和getIfUnique。
>
> JSR-330的这种变体称为Provider，并与Provider \<MyTargetbean>声明和每次检索尝试的相应get()调用一起使用。有关JSR-330总体的更多详细信息，请参见[here](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-standard-annotations)。

以下示例中的配置只有一行，但是了解其背后的“原因”和“方式”很重要：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop
        https://www.springframework.org/schema/aop/spring-aop.xsd">

    <!-- an HTTP Session-scoped bean exposed as a proxy -->
    <bean id="userPreferences" class="com.something.UserPreferences" scope="session">
        <!-- instructs the container to proxy the surrounding bean -->
        <aop:scoped-proxy/> /**The line that defines the proxy.*/
    </bean>

    <!-- a singleton-scoped bean injected with a proxy to the above bean -->
    <bean id="userService" class="com.something.SimpleUserService">
        <!-- a reference to the proxied userPreferences bean -->
        <property name="userPreferences" ref="userPreferences"/>
    </bean>
</beans>
```

要创建这样的代理，请将子\<aop：scoped-proxy />元素插入到作用域bean定义中（请参阅 [Choosing the Type of Proxy to Create](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes-other-injection-proxies) 和[XML Schema-based configuration](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#xsd-schemas)）。为什么在请求，会话和自定义范围级别范围内定义的bean定义需要\<aop：scoped-proxy 
/>元素？考虑以下单例bean定义，并将其与你需要为上述范围定义的定义进行对比（请注意，以下userPreferences bean定义不完整）：

```xml
<bean id="userPreferences" class="com.something.UserPreferences" scope="session"/>

<bean id="userManager" class="com.something.UserManager">
    <property name="userPreferences" ref="userPreferences"/>
</bean>
```

在前面的示例中，单例bean（userManager）注入了对HTTP会话作用域bean（userPreferences）的引用。这里的重点是userManager
bean是单例的：每个容器只实例化一次，并且它的依赖项（在这种情况下，只有一个，userPreferences bean）也只注入一次。这意味着userManager bean仅在完全相同的userPreferences对象（即最初与之注入对象）上操作。

将寿命较短的作用域bean注入寿命较长的作用域bean时，这不是你想要的行为（例如，将HTTP Session范围的协作bean作为依赖项注入到单例bean中）。相反，你只需要一个userManager对象，并且在HTTP Session的生存期内，你需要一个特定于HTTP Session的userPreferences对象。因此，容器创建一个对象，该对象公开与UserPreferences类完全相同的公共接口（理想情况下是一个UserPreferences实例的对象），该对象可以从作用域机制（HTTP、Request、Session等）中获取实际的UserPreferences对象。容器将此代理对象注入到userManager bean中，而后者不知道此UserPreferences引用是代理。在此示例中，当UserManager实例在注入依赖项的UserPreferences对象上调用方法时，实际上是在代理上调用方法。然后，代理从HTTP Session（在本例中）获取真实的UserPreferences对象，并将方法调用委托给检索到的真实的UserPreferences对象。

因此，在将Requet范围和Session范围的bean注入到协作对象中时，你需要以下（正确和完整）配置，如以下示例所示：

```xml
<bean id="userPreferences" class="com.something.UserPreferences" scope="session">
    <aop:scoped-proxy/>
</bean>

<bean id="userManager" class="com.something.UserManager">
    <property name="userPreferences" ref="userPreferences"/>
</bean>
```

#### 选择要创建的代理类型

默认情况下，当Spring容器使用<aop：scoped-proxy />元素标记的bean创建代理时，将创建基于CGLIB的类代理。

> CGLIB代理仅拦截公共方法调用！不要在这样的代理上调用非公共方法。它们没有被委派给实际的作用域目标对象。

另外，你可以通过为<aop：scoped-proxy />元素的proxy-target-class属性值指定false，来配置Spring容器为此类作用域的bean创建基于标准JDK接口的代理。使用基于JDK接口的代理意味着不需要应用程序类路径中的其他库即可影响此类代理。但是，这也意味着作用域bean的类必须实现至少一个接口，并且作用域bean注入到其中的所有协作者必须通过其接口之一引用该bean。以下示例显示了基于接口的代理：

```xml
<!-- DefaultUserPreferences implements the UserPreferences interface -->
<bean id="userPreferences" class="com.stuff.DefaultUserPreferences" scope="session">
    <aop:scoped-proxy proxy-target-class="false"/>
</bean>

<bean id="userManager" class="com.stuff.UserManager">
    <property name="userPreferences" ref="userPreferences"/>
</bean>
```

有关选择基于类或基于接口的代理的更多详细信息，请参阅[代理机制](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-proxying)。

### 自定义作用域（Scope）

bean的作用域机制是可扩展的。你可以定义自己的范围，甚至重新定义现有范围，尽管后者被认为是不好的做法，并且你不能覆盖内置的单例（singleton）和原型（prototoype）范围。

#### 创建自定义作用域

要将自定义范围集成到Spring容器中，需要实现*org.springframework.beans.factory.config.Scope*接口，本节对此进行了介绍。有关如何实现自己的范围的想法，请参阅Spring框架本身提供的Scope实现和[`Scope`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/beans/factory/config/Scope.html) JavaDoc，其中详细说明了需要实现的方法。

Scope接口有四种方法可以从作用域中获取对象，将它们从作用域中删除，然后销毁它们。

例如，Session作用域实现返回Session作用域的bean（如果不存在，则该方法将其绑定到会话上以供将来参考之后，将返回该bean的新实例）。以下方法从基础作用域返回对象：

```java
Object get(String name, ObjectFactory<?> objectFactory)
```

Session作用域的实现，例如，从基础Session中删除了Session作用域的bean。应该返回该对象，但是如果找不到具有指定名称的对象，则可以返回null。以下方法从基础范围中删除该对象：

```java
Object remove(String name)
```

以下方法注册在销毁作用域或销毁作用域中的指定对象时应执行的回调：

```java
void registerDestructionCallback(String name, Runnable destructionCallback)
```

有关销毁回调的更多信息，请参见[javadoc](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/beans/factory/config/Scope.html#registerDestructionCallback)或Spring scope实现。

以下方法获取基础作用域的会话标识符：

```java
String getConversationId()
```

每个作用域的标识符都不相同。对于Session 作用域的实现，此标识符可以是Session标识符。

#### 使用自定义范围

在编写和测试一个或多个自定义作用域实现之后，需要使Spring容器知道你的新作用域。以下方法是在Spring容器中注册新范围的主要方法：

```java
void registerScope(String scopeName, Scope scope);
```

此方法在ConfigurableBeanFactory接口上声明，该接口可通过Spring附带的大多数具体ApplicationContext实现上的BeanFactory属性获得。

registerScope（..）方法的第一个参数是与作用域关联的唯一名称。 Spring容器本身中的此类名称示例包括单例和原型。 registerScope（..）方法的第二个参数是你希望注册和使用的自定义作用域实现的实际实例。

假设你编写了自定义的Scope实现，然后注册它，如下面的示例所示。

> 下一个示例使用SimpleThreadScope，它包含在Spring中，但默认情况下未注册。对于你自己的自定义范围实现，说明将是相同的。

```java
Scope threadScope = new SimpleThreadScope();
beanFactory.registerScope("thread", threadScope);
```

然后，按照你的自定义作用域的作用域规则创建bean定义，如下所示：

```xml
<bean id="..." class="..." scope="thread">
```

使用自定义范围实现，你不仅限于以编程方式注册范围。你还可以使用CustomScopeConfigurer类以声明方式进行注册，如以下示例所示：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop
        https://www.springframework.org/schema/aop/spring-aop.xsd">

    <bean class="org.springframework.beans.factory.config.CustomScopeConfigurer">
        <property name="scopes">
            <map>
                <entry key="thread">
                    <bean class="org.springframework.context.support.SimpleThreadScope"/>
                </entry>
            </map>
        </property>
    </bean>

    <bean id="thing2" class="x.y.Thing2" scope="thread">
        <property name="name" value="Rick"/>
        <aop:scoped-proxy/>
    </bean>

    <bean id="thing1" class="x.y.Thing1">
        <property name="thing2" ref="thing2"/>
    </bean>

</beans>
```

> 当将<aop：scoped-proxy />放置在FactoryBean实现中时，作用域是FactoryBean本身，而不是从getObject()返回的对象。

## 自定义bean的性质

- [Lifecycle Callbacks](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-lifecycle)
- [`ApplicationContextAware` and `beanNameAware`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-aware)
- [Other `Aware` Interfaces](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aware-list)

### 生命周期回调

为了与容器对bean生命周期的管理进行交互，你可以实现Spring InitializingBean和DisposableBean接口。容器为前者调用afterPropertiesSet()并为后者调用destroy()，以使bean在初始化和销毁bean时执行某些操作。

> JSR-250 @PostConstruct和@PreDestroy注解通常被认为是在现代Spring应用程序中接收生命周期回调的最佳实践。使用这些注解意味着你的bean没有耦合到特定于Spring的接口。有关详细信息，请参见[Using `@PostConstruct` and `@PreDestroy`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-postconstruct-and-predestroy-annotations).
> 如果不想使用JSR-250注解，但仍然想解除耦合，请考虑使用init-method和destroy-method 定义元数据。

在内部，Spring框架使用BeanPostProcessor实现来处理它可以找到的任何回调接口并调用适当的方法。如果你需要自定义功能或其他生命周期行为，Spring默认不提供，则你可以自己实现BeanPostProcessor。有关更多信息，请参见[Container Extension Points](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-extension).

除了初始化和销毁回调，Spring管理的对象还可以实现Lifecycle接口，以便这些对象可以在容器自身的生命周期的驱动下参与启动和关闭过程。

本节介绍了生命周期回调接口。

#### 初始化回调

使用*org.springframework.beans.factory.InitializingBean*接口，容器在容器上设置了所有必需的属性后，就可以执行初始化工作。 InitializingBean接口指定一个方法：

```java
void afterPropertiesSet() throws Exception;
```

我们建议不要使用InitializingBean接口，因为它不必要地将代码耦合到Spring。另外，我们建议使用@PostConstruct注解或指定POJO初始化方法。对于基于XML的配置元数据，可以使用init-method属性指定具有无返回值无参数签名的方法的名称。通过Java配置，可以使用@bean的initMethod属性。请参阅[Receiving Lifecycle Callbacks](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-java-lifecycle-callbacks). 考虑以下示例：

```xml
<bean id="exampleInitBean" class="examples.ExampleBean" init-method="init"/>
```

```java
public class ExampleBean {

    public void init() {
        // do some initialization work
    }
}
```

前面的示例与下面的示例（包含两个清单）几乎具有完全相同的效果：

```xml
<bean id="exampleInitBean" class="examples.AnotherExampleBean"/>
```

```java
public class AnotherExampleBean implements InitializingBean {

    @Override
    public void afterPropertiesSet() {
        // do some initialization work
    }
}
```

但是，前面两个示例中的第一个示例并未将代码耦合到Spring。

#### 销毁回调

通过实现*org.springframework.beans.factory.Disposablebean*接口，当包含bean的容器被销毁时，它可以获取回调。 Disposablebean接口指定一个方法：

```java
void destroy() throws Exception;
```

我们建议不要使用Disposablebean回调接口，因为它不必要地将代码耦合到Spring。我们建议使用@PreDestroy注解或指定bean定义支持的通用方法。使用基于XML的配置元数据时，可以在\<bean/>上使用destroy-method属性。通过Java配置，可以使用@bean的destroyMethod属性。请参阅[Receiving Lifecycle Callbacks](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-java-lifecycle-callbacks).考虑以下定义：

```xml
<bean id="exampleInitBean" class="examples.ExampleBean" destroy-method="cleanup"/>
```

```java
public class ExampleBean {

    public void cleanup() {
        // do some destruction work (like releasing pooled connections)
    }
}
```

前面的定义与下面的定义几乎具有完全相同的效果：

```xml
<bean id="exampleInitBean" class="examples.AnotherExampleBean"/>
```

```java
public class AnotherExampleBean implements DisposableBean {

    @Override
    public void destroy() {
        // do some destruction work (like releasing pooled connections)
    }
}
```

但是，前面两个定义中的第一个没有将代码耦合到Spring。

> 可以为\<bean>元素的destroy-method属性分配一个特殊的（推断的）值，该值指示Spring自动检测特定bean类上的public close或shutdown方法。（因此，任何实现*java.lang.AutoCloseable*或*java.io.Closeable*的类都将匹配。）还可以在\<beans>元素的default-destroy-method属性上设置此特殊（推断）值，以将此行为应用于一整套bean（请参[Default Initialization and Destroy Methods](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-lifecycle-default-init-destroy-methods)）。请注意，这是Java配置的默认行为。

#### 默认初始化和销毁方法

当编写不使用Spring特定的InitializingBean和DisposableBean回调接口的初始化和销毁方法回调时，通常会使用诸如init()，initialize()，dispose()等名称编写方法。理想情况下，此类生命周期回调方法的名称应在整个项目中标准化，以便所有开发人员都使用相同的方法名称并确保一致性。

你可以将Spring容器配置为“寻找”命名的初始化，并销毁每个bean上的回调方法名称。这意味着，作为应用程序开发人员，你可以编写应用程序类并使用称为init()的初始化回调，而不必为每个bean定义配置init-method=“init”属性。 Spring IoC容器在创建bean时（并根据前面描述的标准生命周期回调协定）调用该方法。此功能还对初始化和销毁方法回调强制执行一致的命名约定。

假设你的初始化回调方法命名为init()，而destroy回调方法命名为destroy()。然后，你的类将类似于以下示例中的类：

```java
public class DefaultBlogService implements BlogService {

    private BlogDao blogDao;

    public void setBlogDao(BlogDao blogDao) {
        this.blogDao = blogDao;
    }

    // this is (unsurprisingly) the initialization callback method
    public void init() {
        if (this.blogDao == null) {
            throw new IllegalStateException("The [blogDao] property must be set.");
        }
    }
}
```

你可以在类似于以下内容的bean中使用该类：

```xml
<beans default-init-method="init">

    <bean id="blogService" class="com.something.DefaultBlogService">
        <property name="blogDao" ref="blogDao" />
    </bean>

</beans>
```

顶级\<beans />元素属性上存在default-init-method属性，导致Spring IoC容器将bean类上称为init的方法识别为初始化方法回调。在创建和组装bean时，如果bean类具有这种方法，则会在适当的时间调用它。

你可以通过使用顶级\<beans />元素上的default-destroy-method属性类似地（在XML中）配置destroy方法回调。

如果现有的bean类已经具有按约定以不同的方式命名的回调方法，则可以通过使用\<bean />的init-method和destroy-method属性指定（在XML中）方法名称来覆盖默认值。 

Spring容器保证在为bean提供所有依赖项后立即调用配置的初始化回调。因此，在原始bean引用上调用了初始化回调，这意味着AOP拦截器等尚未应用于bean。首先完全创建目标bean，然后应用带有其拦截器链的AOP代理（例如）。如果目标bean和代理分别定义，则你的代码甚至可以绕过代理与原始目标bean进行交互。因此，将拦截器应用于init方法将是不一致的，因为这样做会将目标bean的生命周期耦合到其代理或拦截器，并且当你的代码直接与原始目标bean进行交互时会留下奇怪的语义。

#### 组合生命周期机制

从Spring 2.5开始，可以使用三个选项来控制bean生命周期行为：

- InitializingBean和DisposableBean回调接口
- 自定义init()和destroy()方法
- @PostConstruct和@PreDestroy注解。你可以结合使用这些机制来控制给定的bean。

> 如果为一个bean配置了多个生命周期机制，并且为每个机制配置了不同的方法名称，则将按照列出的顺序执行每个已配置的方法。但是，如果为多个生命周期机制中的多个生命周期机制配置了相同的方法名称（例如，为初始化方法使用init()），则该方法将执行一次，如上一节所述。

为同一个bean配置的具有不同初始化方法的多种生命周期机制如下：

- 用@PostConstruct注解的方法
- 由InitializingBean回调接口定义的afterPropertiesSet()
- 定制配置的init()方法

销毁方法的调用顺序相同：

- 用@PreDestroy注解的方法
- 由DisposableBean回调接口定义的destroy()
- 定制配置的destroy()方法

#### 启动和关机回调

Lifecycle接口为具有自己的生命周期要求（例如启动和停止某些后台进程）的任何对象定义了基本方法：

```java
public interface Lifecycle {

    void start();

    void stop();

    boolean isRunning();
}
```

任何Spring管理的对象都可以实现Lifecycle接口。然后，当ApplicationContext本身接收到启动和停止信号时（例如，对于运行时的停止/重新启动方案），它将把这些调用级联到在该上下文中定义的所有Lifecycle实现。它通过委派给LifecycleProcessor来做到这一点，如以下清单所示：

```java
public interface LifecycleProcessor extends Lifecycle {

    void onRefresh();

    void onClose();
}
```

请注意，LifecycleProcessor本身是Lifecycle接口的扩展。它还添加了其他两种方法来对正在刷新和关闭的上下文做出反应。

> 请注意，常规的*org.springframework.context.Lifecycle*接口是用于显式启动和停止通知的普通协议，并不意味着在上下文刷新时自动启动。为了对特定bean的自动启动（包括启动阶段）进行细粒度的控制，请考虑改为实现*org.springframework.context.SmartLifecycle*。
>
> 另外，请注意，不能保证会在销毁之前发出停止通知。在常规关闭时，在传播常规销毁回调之前，所有Lifecycle bean首先都会收到停止通知。但是，在上下文生命周期内进行热刷新或刷新尝试失败时，仅调用destroy方法。

启动和关闭调用的顺序可能很重要。如果任何两个对象之间存在“依赖”关系，则依赖方在其依赖之后开始，而在依赖之前停止。但是，有时直接依赖项是未知的。你可能只知道某种类型的对象应该先于另一种类型的对象开始。在这些情况下，SmartLifecycle接口定义了另一个选项，即在其超级接口Phased上定义的getPhase()方法。以下清单显示了Phased接口的定义：

```java
public interface Phased {

    int getPhase();
}
```

以下清单显示了SmartLifecycle接口的定义：

```java
public interface SmartLifecycle extends Lifecycle, Phased {

    boolean isAutoStartup();

    void stop(Runnable callback);
}
```

启动时，相位最低的对象首先启动。停止时，遵循相反的顺序。因此，实现SmartLifecycle并且其getPhase()方法返回Integer.MIN_VALUE的对象将是第一个启动且最后一个停止的对象。在频谱的另一端，相位值Integer.MAX_VALUE将指示该对象应最后启动并首先停止（可能是因为它取决于正在运行的其他进程）。在考虑相位值时，重要的是要知道未实现SmartLifecycle的任何“正常”Lifecycle对象的默认相位为0。因此，任何负相位值都表明对象应在这些标准组件之前开始（然后停止）在他们之后。对于任何正相位值，反之亦然。

SmartLifecycle定义的stop方法接受回调。任何实现都必须在该实现的关闭过程完成后调用该回调的run()方法。这将在必要时启用异步关闭，因为LifecycleProcessor接口的默认实现DefaultLifecycleProcessor会等待其超时值，等待每个阶段内的对象组调用该回调。默认的每阶段超时是30秒。可以通过在上下文中定义一个名为lifecycleProcessor的bean来覆盖默认的生命周期处理器实例。如果只想修改超时，则定义以下内容即可：

```xml
<bean id="lifecycleProcessor" class="org.springframework.context.support.DefaultLifecycleProcessor">
    <!-- timeout value in milliseconds -->
    <property name="timeoutPerShutdownPhase" value="10000"/>
</bean>
```

如前所述，LifecycleProcessor接口还定义了用于刷新和关闭上下文的回调方法。后者驱动关闭过程，就好像已经显式调用了stop()一样，但是它在上下文关闭时发生。另一方面，“刷新”回调启用SmartLifecycle bean的另一个功能。刷新上下文时（在所有对象都被实例化和初始化之后），该回调将被调用。届时，默认的生命周期处理器将检查每个SmartLifecycle对象的isAutoStartup()方法返回的布尔值。如果为true，则在该点启动该对象，而不是等待上下文或它自己的start()方法的显式调用（与上下文刷新不同，对于标准上下文实现，上下文启动不会自动发生）。相位值和任何“依赖”关系决定了启动顺序，如前所述。

#### 在非Web应用程序中正常关闭Spring IoC容器

本节仅适用于非Web应用程序。 Spring的基于Web的ApplicationContext实现已经具有适当的代码，可以在相关的Web应用程序关闭时正常关闭Spring IoC容器。

如果你在非Web应用程序环境中（例如，在富客户端桌面环境中）使用Spring的IoC容器，请向JVM注册一个关闭钩子。这样做可以确保正常关机，并在你的Singleton
bean上调用相关的destroy方法，以便释放所有资源。你仍然必须正确配置和实现这些destroy回调。

要注册关闭挂钩，请调用在ConfigurableApplicationContext接口上声明的registerShutdownHook()方法，如以下示例所示：

```java
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class Boot {

    public static void main(final String[] args) throws Exception {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");

        // add a shutdown hook for the above context...
        ctx.registerShutdownHook();

        // app runs here...

        // main method exits, hook is called prior to the app shutting down...
    }
}
```

### ApplicationContextAware和beanNameAware

当ApplicationContext创建实现org.springframework.context.ApplicationContextAware接口的对象实例时，将为该实例提供对该ApplicationContext的引用。以下清单显示了ApplicationContextAware接口的定义：

```java
public interface ApplicationContextAware {

    void setApplicationContext(ApplicationContext applicationContext) throws beansException;
}
```

因此，bean可以通过ApplicationContext接口或通过将引用转换为该接口的已知子类（例如ConfigurableApplicationContext，它公开了其他功能）来以编程方式操纵创建它们的ApplicationContext。一种用途是通过编程方式检索其他bean。有时，此功能很有用。但是，通常应避免使用它，因为它将代码耦合到Spring，并且不遵循控制反转样式，在该样式中，将协作者作为属性提供给bean。ApplicationContext的其他方法提供对文件资源的访问，发布应用程序事件以及访问MessageSource。这些附加功能在[Additional Capabilities of the `ApplicationContext`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#context-introduction)进行了描述。

自动装配是获得对ApplicationContext的引用的另一种选择。传统的构造函数和byType自动装配模式（如“[Autowiring Collaborators](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-autowire)”中所述）可以分别为构造函数参数或setter方法参数提供ApplicationContext类型的依赖项。要获得更大的灵活性，包括能够自动连接字段和使用多个参数方法，请使用基于注解的自动装配功能。如果这样做，则将ApplicationContext自动连接到需要使用ApplicationContext类型的字段，构造函数参数或方法参数中。有关更多信息，请参见[Using `@Autowired`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-autowired-annotation).

当ApplicationContext创建实现*org.springframework.beans.factory.BeanNameAware*接口的类时，该类将获得对在其关联对象定义中定义的名称的引用。以下清单显示了BeanNameAware接口的定义：

```java
public interface BeanNameAware {

    void setBeanName(String name) throws BeansException;
}
```

在填充正常的bean属性之后，但在初始化回调（例如InitializingBean，afterPropertiesSet或自定义init-method）之前，调用该回调方法。

### 其他Aware接口

除了ApplicationContextAware和BeanNameAware（前面已经讨论过）之外，Spring还提供了多种Aware回调接口，这些接口使bean向容器指示它们需要某种基础结构依赖性。通常，名称表示依赖项类型。下表总结了最重要的Aware接口：

| Name                             | Injected Dependency                                          | Explained in…                                                |
| -------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| `ApplicationContextAware`        | 声明ApplicationContext。                                     | [`ApplicationContextAware` and `beanNameAware`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-aware) |
| `ApplicationEventPublisherAware` | 封闭的ApplicationContext的事件发布者。                       | [Additional Capabilities of the `ApplicationContext`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#context-introduction) |
| `BeanClassLoaderAware`           | 类加载器，用于加载bean类。                                   | [Instantiating beans](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-class) |
| `BeanFactoryAware`               | 声明BeanFactory。                                            | [`ApplicationContextAware` and `beanNameAware`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-aware) |
| `BeanNameAware`                  | 声明Bean的名称。                                             | [`ApplicationContextAware` and `beanNameAware`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-aware) |
| `BootstrapContextAware`          | 容器在其中运行资源适配器BootstrapContext。通常仅在支持JCA的ApplicationContext实例中可用。 | [JCA CCI](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/integration.html#cci) |
| `LoadTimeWeaverAware`            | 定义的编织器，用于在加载时处理类定义。                       | [Load-time Weaving with AspectJ in the Spring Framework](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-aj-ltw) |
| `MessageSourceAware`             | 解决消息的已配置策略（支持参数化和国际化）。                 | [Additional Capabilities of the `ApplicationContext`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#context-introduction) |
| `NotificationPublisherAware`     | Spring JMX通知发布者。                                       | [Notifications](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/integration.html#jmx-notifications) |
| `ResourceLoaderAware`            | 配置的加载程序，用于对资源的低级别访问。                     | [Resources](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources) |
| `ServletConfigAware`             | 容器在其中运行的当前ServletConfig。仅在Web环境的Spring ApplicationContext中有效。. | [Spring MVC](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/web.html#mvc) |
| `ServletContextAware`            | 容器在其中运行的当前ServletContext。仅在Web环境的Spring ApplicationContext中有效。 | [Spring MVC](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/web.html#mvc) |

再次注意，使用这些接口会将代码与Spring API绑定在一起，并且不遵循“控制反转”样式。因此，我们建议将它们用于需要以编程方式访问容器的基础结构bean。

## Bean定义继承

bean定义可以包含许多配置信息，包括构造函数参数，属性值和特定于容器的信息，例如初始化方法，静态工厂方法名称等。子bean从父bean继承配置数据。子定义可以覆盖某些值或根据需要添加其他值。使用父bean和子bean定义可以节省很多输入。实际上，这是一种模板形式。

如果你以编程方式使用ApplicationContext接口，则子bean定义由ChildBeanDefinition类表示。大多数用户不在此级别上与他们合作。相反，它们在诸如ClassPathXmlApplicationContext之类的类中声明性地配置bean定义。当使用基于XML的配置元数据时，可以通过使用parent属性（将父bean指定为该属性的值）来指示子bean定义。以下示例显示了如何执行此操作：

```xml
<bean id="inheritedTestBean" abstract="true"
        class="org.springframework.beans.TestBean">
    <property name="name" value="parent"/>
    <property name="age" value="1"/>
</bean>

<bean id="inheritsWithDifferentClass"
        class="org.springframework.beans.DerivedTestBean"
        parent="inheritedTestbean" init-method="initialize">  
    <property name="name" value="override"/>
    <!-- the age property value of 1 will be inherited from parent -->
</bean>
```

如果未指定子bean定义，则使用父定义中的bean类，但也可以覆盖它。在后一种情况下，子bean类必须与父类兼容（也就是说，它必须接受父类的属性值）。

子bean从父对象继承作用域，构造函数参数值，属性值和方法重写，并可以选择添加新值。你指定的任何作用域，初始化方法，destroy方法或静态工厂方法设置都会覆盖相应的父设置。

其余设置始终从子定义中获取：依赖项，自动装配模式，依赖项检查，单例和惰性初始化。

前面的示例使用abstract属性将父bean定义显式标记为abstract。如果父定义未指定类，则需要将父bean定义显式标记为抽象，如以下示例所示：

```xml
<bean id="inheritedTestBeanWithoutClass" abstract="true">
    <property name="name" value="parent"/>
    <property name="age" value="1"/>
</bean>

<bean id="inheritsWithClass" class="org.springframework.beans.DerivedTestBean"
        parent="inheritedTestBeanWithoutClass" init-method="initialize">
    <property name="name" value="override"/>
    <!-- age will inherit the value of 1 from the parent bean definition-->
</bean>
```

父bean不能单独实例化，因为它不完整，并且还被明确标记为抽象。当定义是抽象的时，它只能用作纯模板bean定义，用作子bean的父定义。通过将其设为另一个bean的ref属性或使用父beanID进行显式getBean()调用来尝试单独使用这样的抽象父bean会返回错误。同样，容器的内部preInstantiateSingletons()方法将忽略定义为抽象的bean定义。

默认情况下，ApplicationContext会预先实例化所有单例。因此，重要的是（至少对于单例bean），如果有一个（父）bean定义仅打算用作模板，并且此定义指定了一个类，则必须确保将abstract属性设置为true，否则应用程序上下文将实际上（试图）预先实例化抽象Bean。

## 容器延伸点

通常，应用程序开发人员不需要为ApplicationContext实现类提供子类。相反，可以通过插入特殊集成接口的实现来扩展Spring IoC容器。接下来的几节描述了这些集成接口。

### 使用BeanPostProcessor自定义Bean

BeanPostProcessor接口定义了回调方法，你可以实施这些回调方法以提供自己的（或覆盖容器的默认值）实例化逻辑，依赖关系解析逻辑等。如果你想在Spring容器完成实例化，配置和初始化bean之后实现一些自定义逻辑，则可以插入一个或多个自定义BeanPostProcessor实现。

你可以配置多个BeanPostProcessor实例，并且可以通过设置order属性来控制这些BeanPostProcessor实例的执行顺序。仅当BeanPostProcessor实现Ordered接口时，才可以设置此属性。如果你编写自己的BeanPostProcessor，则也应该考虑实现Ordered接口。有关更多详细信息，请参见[`BeanPostProcessor`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/beans/factory/config/BeanPostProcessor.html)和[`Ordered`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/core/Ordered.html) 接口的javadoc。另请参见[programmatic registration of `BeanPostProcessor` instances](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-programmatically-registering-beanpostprocessors).。

>  BeanPostProcessor实例在bean（或对象）实例上运行。即，Spring IoC容器实例化一个bean实例，然后BeanPostProcessor实例完成其工作。
>
>  BeanPostProcessor实例是按容器划分作用域的。仅在使用容器层次结构时，这才有意义。如果在一个容器中定义BeanPostProcessor，则仅后处理该容器中的bean。换句话说，一个容器中定义的bean不会被另一个容器中定义的BeanPostProcessor进行后处理，即使这两个容器是同一层次结构的一部分也是如此。
>
>  要更改实际的bean定义（即定义bean的蓝图），你需要使用BeanFactoryPostProcessor，如[Customizing Configuration Metadata with a `BeanFactoryPostProcessor`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-extension-factory-postprocessors)中所述。

*org.springframework.beans.factory.config.BeanPostProcessor*接口恰好由两个回调方法组成。当此类被注册为容器的后处理器时，对于容器创建的每个bean实例，后处理器都会在容器初始化方法（例如*InitializingBean.afterPropertiesSet()*或任何声明的init方法）之前，并在任何bean初始化回调之后被调用。后处理器可以对bean实例执行任何操作，包括完全忽略回调。Bean后处理器通常检查回调接口，或者可以用代理包装bean。一些Spring AOP基础结构类被实现为bean后处理器，以提供代理包装逻辑。

ApplicationContext自动检测实现BeanPostProcessor接口的配置元数据中定义的所有bean。 ApplicationContext将这些bean注册为后处理器，以便以后在bean创建时可以调用它们。 Bean后处理器可以与其他Bean以相同的方式部署在容器中。

请注意，在配置类上使用@Bean工厂方法声明BeanPostProcessor时，工厂方法的返回类型应该是实现类本身，或者至少是*org.springframework.beans.factory.config.BeanPostProcessor*接口。指示该bean的后处理器性质。否则，ApplicationContext无法在完全创建之前按类型自动检测它。由于BeanPostProcessor需要及早实例化才能应用于上下文中其他bean的初始化，因此这种早期类型检测至关重要。

> 以编程方式注册BeanPostProcessor实例
>
> 虽然建议的BeanPostProcessor注册方法是通过ApplicationContext自动检测（如前所述），但是可以使用ConfigurableBeanFactory的addBeanPostProcessor方法以编程方式将其注册。当你需要在注册之前评估条件逻辑，甚至需要跨层次结构的上下文复制Bean后处理器时，这将非常有用。但是请注意，以编程方式添加的BeanPostProcessor实例不遵守Ordered接口。在这里，注册的顺序决定了执行的顺序。还要注意，以编程方式注册的BeanPostProcessor实例始终在通过自动检测注册的实例之前进行处理，而不考虑任何明确的顺序。

> BeanPostProcessor实例和AOP自动代理
>
> 实现BeanPostProcessor接口的类是特殊的，并且容器对它们的处理方式有所不同。它们直接引用的所有BeanPostProcessor实例和bean在启动时都会实例化，作为ApplicationContext特殊启动阶段的一部分。接下来，以排序方式注册所有BeanPostProcessor实例，并将其应用于容器中的所有其他bean。因为AOP自动代理是作为BeanPostProcessor本身实现的，所以BeanPostProcessor实例或它们直接引用的bean都不适合进行自动代理，因此，无法织入它们。
>
> 对于任何这样的bean，你应该可以看到一条参考性的日志消息：`Bean someBean is not eligible for getting processed by all BeanPostProcessor interfaces (for example: not eligible for auto-proxying)`.
>
> 如果使用自动装配或@Resource（可能会退回到自动装配）将bean连接到BeanPostProcessor中，则Spring在搜索类型匹配的依赖项候选对象时可能会访问意外的Bean，因此使它们不符合自动代理或其他种类的条件bean后处理。例如，如果有一个用@Resource注解的依赖项，其中字段或setter名称不直接与bean的声明名称相对应，并且不使用name属性，那么Spring将访问其他bean以按类型匹配它们。

下面的示例演示如何在ApplicationContext中编写，注册和使用BeanPostProcessor实例。

#### 示例：Hello World，BeanPostProcessor风格

第一个示例说明了基本用法。该示例显示了一个自定义BeanPostProcessor实现，该实现调用由容器创建的每个bean的toString()方法，并将结果字符串打印到系统控制台。

以下清单显示了自定义BeanPostProcessor实现类的定义：

```java
package scripting;

import org.springframework.beans.factory.config.BeanPostProcessor;

public class InstantiationTracingBeanPostProcessor implements BeanPostProcessor {

    // simply return the instantiated bean as-is
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean; // we could potentially return any object reference here...
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("Bean '" + beanName + "' created : " + bean.toString());
        return bean;
    }
}
```

以下beans元素使用InstantiationTracingBeanPostProcessor：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:lang="http://www.springframework.org/schema/lang"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/lang
        https://www.springframework.org/schema/lang/spring-lang.xsd">

    <lang:groovy id="messenger"
            script-source="classpath:org/springframework/scripting/groovy/Messenger.groovy">
        <lang:property name="message" value="Fiona Apple Is Just So Dreamy."/>
    </lang:groovy>

    <!--
    when the above bean (messenger) is instantiated, this custom
    BeanPostProcessor implementation will output the fact to the system console
    -->
    <bean class="scripting.InstantiationTracingBeanPostProcessor"/>

</beans>
```

注意如何仅定义InstantiationTracingBeanPostProcessor。它甚至没有名称，并且因为它是bean，所以可以像其他任何bean一样依赖注入。（前面的配置还定义了一个由Groovy脚本支持的bean。Spring动态语言支持在标题为[Dynamic Language Support](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/languages.html#dynamic-language).的章节中有详细介绍。）

以下Java应用程序运行上述代码和配置：

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scripting.Messenger;

public final class Boot {

    public static void main(final String[] args) throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("scripting/beans.xml");
        Messenger messenger = ctx.getBean("messenger", Messenger.class);
        System.out.println(messenger);
    }
}
```

前面的应用程序的输出类似于以下内容：

```
Bean 'messenger' created : org.springframework.scripting.groovy.GroovyMessenger@272961
org.springframework.scripting.groovy.GroovyMessenger@272961
```

#### 示例：RequiredAnnotationBeanPostProcessor

将回调接口或注解与自定义BeanPostProcessor实现结合使用是扩展Spring IoC容器的常用方法。 Spring的RequiredAnnotationBeanPostProcessor是一个示例，它是Spring发行版附带的BeanPostProcessor实现，可以确保标有（任意）注解的bean上的JavaBean属性实际上（配置为）依赖注入了一个值。

### 使用BeanFactoryPostProcessor自定义配置元数据

我们要看的下一个扩展点是*org.springframework.beans.factory.config.BeanFactoryPostProcessor*。该接口的语义与BeanPostProcessor的语义相似，但有一个主要区别：BeanFactoryPostProcessor对Bean配置元数据进行操作。也就是说，Spring IoC容器允许BeanFactoryPostProcessor读取配置元数据，并有可能在容器实例化除BeanFactoryPostProcessor实例以外的任何bean之前更改它。

可以配置多个BeanFactoryPostProcessor实例，并且可以通过设置order属性来控制这些BeanFactoryPostProcessor实例的运行顺序。但是，仅当BeanFactoryPostProcessor实现Ordered接口时才能设置此属性。如果你编写自己的BeanFactoryPostProcessor，则也应该考虑实现Ordered接口。有关更多详细信息，请参见[`BeanFactoryPostProcessor`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/beans/factory/config/BeanFactoryPostProcessor.html) 和[`Ordered`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/core/Ordered.html)的javadoc。

>  如果要更改实际的bean实例（即从配置元数据创建的对象），则需要使用BeanPostProcessor。从技术上讲，可以在BeanFactoryPostProcessor中使用bean实例（例如，通过使用*BeanFactory.getBean()*），但这样做会导致过早的bean实例化，从而违反了标准容器的生命周期。这可能会导致负面影响，例如绕过bean的后处理。
>
>  同样，BeanFactoryPostProcessor实例是按容器划分作用域的。仅在使用容器层次结构时才有意义。如果在一个容器中定义BeanFactoryPostProcessor，它将仅应用于该容器中的Bean定义。一个容器中的bean定义不会由另一个容器中的BeanFactoryPostProcessor实例进行后处理，即使两个容器都属于同一层次结构也是如此。

Bean工厂后处理器在ApplicationContext中声明时会自动执行，以便将更改应用于定义容器的配置元数据。 Spring包含许多预定义的bean工厂后处理器，例如PropertyOverrideConfigurer和PropertySourcesPlaceholderConfigurer。你还可以使用自定义BeanFactoryPostProcessor，例如注册自定义属性编辑器。

ApplicationContext自动检测实现BeanFactoryPostProcessor接口的部署到其中的所有Bean。它在适当的时候将这些bean用作bean工厂的后处理器。你可以像部署其他任何bean一样部署这些后处理器bean。

>  与BeanPostProcessors一样，你通常不想延迟初始化配置BeanFactoryPostProcessors。如果没有其他bean引用Bean（Factory）PostProcessor，则该后处理器将完全不会实例化。因此，将其标记为延迟初始化将被忽略，即使你在\<beans/>元素的声明中将default-lazy-init属性设置为true，Bean（Factory）PostProcessor也会被实例化。

#### 示例：类名替换PropertySourcesPlaceholderConfigurer

你可以使用PropertySourcesPlaceholderConfigurer通过使用标准Java Properties格式将外部bean定义的属性值外部化到单独的文件中。这样做使部署应用程序的人员可以自定义特定于环境的属性，例如数据库URL和密码，而不必为修改容器的一个或多个主要XML定义文件而感到复杂或有风险

考虑以下基于XML的配置元数据片段，其中定义了具有占位符值的DataSource：

```xml
<bean class="org.springframework.context.support.PropertySourcesPlaceholderConfigurer">
    <property name="locations" value="classpath:com/something/jdbc.properties"/>
</bean>

<bean id="dataSource" destroy-method="close"
        class="org.apache.commons.dbcp.BasicDataSource">
    <property name="driverClassName" value="${jdbc.driverClassName}"/>
    <property name="url" value="${jdbc.url}"/>
    <property name="username" value="${jdbc.username}"/>
    <property name="password" value="${jdbc.password}"/>
</bean>
```

该示例显示了从外部属性文件配置的属性。在运行时，将PropertySourcesPlaceholderConfigurer应用于替换数据源某些属性的元数据。将要替换的值指定为${property-name}格式的占位符，该格式遵循Ant、log4j和JSP EL样式。

实际值来自标准Java Properties格式的另一个文件：

```properties
jdbc.driverClassName=org.hsqldb.jdbcDriver
jdbc.url=jdbc:hsqldb:hsql://production:9002
jdbc.username=sa
jdbc.password=root
```

因此，$ {jdbc.username}字符串在运行时将被替换为值“ sa”，并且其他与属性文件中的键匹配的占位符值也将被替换。 PropertySourcesPlaceholderConfigurer检查Bean定义的大多数属性和属性中的占位符。此外，还可以自定义占位符前缀和后缀。

借助Spring 2.5中引入的上下文名称空间，你可以使用专用配置元素配置属性占位符。你可以在location属性中提供一个或多个位置作为逗号分隔的列表，如以下示例所示：

```xml
<context:property-placeholder location="classpath:com/something/jdbc.properties"/>
```

PropertySourcesPlaceholderConfigurer不仅在你指定的属性文件中查找属性。默认情况下，如果无法在指定的属性文件中找到属性，则会检查Spring Environment属性和常规Java System属性。

>  可以使用PropertySourcesPlaceholderConfigurer来替换类名称，这在你必须在运行时选择特定的实现类时有时很有用。以下示例显示了如何执行此操作：
>
>  ```xml
>  <bean class="org.springframework.beans.factory.config.PropertySourcesPlaceholderConfigurer">
>   <property name="locations">
>       <value>classpath:com/something/strategy.properties</value>
>   </property>
>   <property name="properties">
>       <value>custom.strategy.class=com.something.DefaultStrategy</value>
>   </property>
>  </bean>
>  
>  <bean id="serviceStrategy" class="${custom.strategy.class}"/>
>  ```
>
>  如果无法在运行时将类解析为有效的类，则将要创建的bean的解析将失败，这是在非延迟初始化bean的ApplicationContext的preInstantiateSingletons()阶段期间进行的。

#### 示例：PropertyOverrideConfigurer

另一个bean工厂后处理程序PropertyOverrideConfigurer与PropertySourcesPlaceholderConfigurer相似，但是与后者不同，原始定义可以具有默认值，也可以完全没有bean属性的值。如果覆盖的属性文件没有某个bean属性的条目，则使用默认的上下文定义。

注意，bean定义不知道会被覆盖，因此从XML定义文件中不能立即看出正在使用覆盖配置程序。如果有多个PropertyOverrideConfigurer实例为同一个bean属性定义了不同的值，则由于覆盖机制，最后一个实例将获胜。

属性文件配置行采用以下格式：

```
beanName.property=value
```

以下清单显示了格式的示例：

```
dataSource.driverClassName=com.mysql.jdbc.Driver
dataSource.url=jdbc:mysql:mydb
```

该示例文件可与包含一个名为dataSource的bean的容器定义一起使用，该bean具有driver和url属性。

只要路径的每个组成部分（最终属性被覆盖）之外的所有组成部分都已经为非空（可能是由构造函数初始化），则也支持复合属性名。在下面的示例中，将tom Bean的fred属性的bob属性的sammy属性设置为标量值123：

```
tom.fred.bob.sammy=123
```

>  指定的替代值始终是文字值。它们不会转换为bean引用。当XML bean定义中的原始值指定bean引用时，此约定也适用。

使用Spring 2.5中引入的context名称空间，可以使用专用配置元素配置属性覆盖，如以下示例所示：

```xml
<context:property-override location="classpath:override.properties"/>
```

### 使用FactoryBean自定义实例化逻辑

可以为本身就是工厂的对象实现*org.springframework.beans.factory.FactoryBean*接口。

FactoryBean接口是可插入Spring IoC容器的实例化逻辑的一点。如果你有复杂的初始化代码，而不是（可能）冗长的XML，可以用Java更好地表达，则可以创建自己的FactoryBean，在该类中编写复杂的初始化，然后将自定义FactoryBean插入容器。

FactoryBean接口提供了三种方法：

- Object getObject()：返回此工厂创建的对象的实例。实例可以共享，具体取决于该工厂是否返回单例或原型。
- boolean isSingleton()：如果此FactoryBean返回单例则返回true，否则返回false。
- Class getObjectType()：返回由getObject()方法返回的对象类型；如果类型未知，则返回null。

Spring框架中的许多地方都使用了FactoryBean概念和接口。 Spring附带了50多个FactoryBean接口实现。

当需要向容器要求一个实际的FactoryBean实例本身而不是由它产生的bean时，请在调用ApplicationContext的getBean()方法时在该bean的ID前面加上一个&符号。因此，对于给定的id为myBean的bean，在容器上调用getBean（“myBean”）返回FactoryBean的产品，而调用getBean（“&myBean”）则返回FactoryBean实例本身。

## 基于注解的容器配置

> 注解在配置Spring方面比XML更好吗？
>
> 基于注解的配置的引入提出了一个问题，即这种方法是否比XML“更好”。简短的答案是“取决于情况”。长话短说，每种方法都有其优缺点，通常，由开发人员决定哪种策略更适合他们。由于定义方式的不同，注解在声明中提供了很多上下文，从而使配置更短，更简洁。但是，XML擅长连接组件而不接触其源代码或重新编译它们。一些开发人员更喜欢将织入放置在靠近源的位置，而另一些开发人员则认为带注解的类不再是POJO，而且，该配置变得分散且难以控制。
>
> 无论选择如何，Spring都可以容纳两种样式，甚至可以将它们混合在一起。值得指出的是，通过其JavaConfig选项，Spring允许以非侵入方式使用注解，而无需接触目标组件的源代码，并且就工具而言，Spring Tools for Eclipse支持所有配置样式。

基于注解的配置提供了XML设置的替代方法，该配置依赖字节码元数据来连接组件，而不是尖括号声明。通过使用相关类，方法或字段声明上的注解，开发人员无需使用XML来描述bean的连接，而是将配置移入组件类本身。如 [Example: The `RequiredAnnotationBeanPostProcessor`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-extension-bpp-examples-rabpp)中所述：结合使用BeanPostProcessor和注解，RequiredAnnotationBeanPostProcessor是扩展Spring
IoC容器的常用方法。例如，Spring 2.0引入了使用@Required注解强制执行必需属性的可能性。 Spring 2.5使遵循相同的通用方法来驱动Spring的依赖注入成为可能。本质上，@ 
Autowired注解提供的功能与自动装配协作器中描述的功能相同，但具有更细粒度的控制和更广泛的适用性。 Spring 2.5还添加了对JSR-250注解的支持，例如@PostConstruct和@PreDestroy。 Spring 3.0增加了对javax.inject包中包含的JSR-330（Java依赖注入）注解的支持，例如@Inject和@Named。有关这些注解的详细信息，请参见[relevant section](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-standard-annotations)。

> 注解注入在XML注入之前执行。因此，XML配置将覆盖通过两种方法连接的属性的注解。

与往常一样，你可以将它们注册为单独的bean定义，但是也可以通过在基于XML的Spring配置中包含以下标记来隐式注册它们（请注意包括context名称空间）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

</beans>
```

（隐式注册的后处理器包括[`AutowiredAnnotationBeanPostProcessor`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/beans/factory/annotation/AutowiredAnnotationBeanPostProcessor.html),[`CommonAnnotationBeanPostProcessor`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/context/annotation/CommonAnnotationBeanPostProcessor.html),[`PersistenceAnnotationBeanPostProcessor`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/orm/jpa/support/PersistenceAnnotationBeanPostProcessor.html),和之前描述的[`RequiredAnnotationBeanPostProcessor`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/beans/factory/annotation/RequiredAnnotationBeanPostProcessor.html)）

> \<context：annotation-config />仅在定义它的相同应用程序上下文中在bean上查找注解。这意味着，如果将\<context：annotation-config />放入DispatcherServlet的WebApplicationContext中，它将仅检查控制器中的@Autowired bean，而不检查服务。有关更多信息，请参见[The DispatcherServlet](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/web.html#mvc-servlet) 。

### @Required

@Required注解适用于bean属性设置器方法，如以下示例所示：

```java
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Required
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

此注解指示必须在配置时通过bean定义中的显式属性值或通过自动装配来填充受影响的bean属性。如果尚未填充受影响的bean属性，则容器将引发异常。这允许急切和显式的失败，避免以后再出现NullPointerException实例等。我们仍然建议你将断言放入bean类本身中（例如，放入init方法中）。这样做会强制执行那些必需的引用和值，即使你在容器外部使用该类也是如此。

> 从Spring Framework 5.1开始，@ Required注解已正式弃用，转而使用构造函数注入进行必需的设置（或InitializingBean.afterPropertiesSet()的自定义实现以及bean属性setter方法）。

### 使用@Autowired

> 在本节中的示例中，可以使用JSR 330的@Inject注解代替Spring的@Autowired注解。有关更多详细信息，请参见[此处](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-standard-annotations)。

可以将@Autowired注解应用于构造函数，如以下示例所示：

```java
public class MovieRecommender {

    private final CustomerPreferenceDao customerPreferenceDao;

    @Autowired
    public MovieRecommender(CustomerPreferenceDao customerPreferenceDao) {
        this.customerPreferenceDao = customerPreferenceDao;
    }

    // ...
}
```

> 从Spring Framework 4.3开始，如果目标bean仅定义一个构造函数作为开始，则不再需要在此类构造函数上使用@Autowired注解。但是，如果有几个构造函数可用，并且没有主/默认构造函数，则至少一个构造函数必须使用@Autowired注解，以指示容器使用哪个构造函数。有关详细信息，请参见[constructor resolution](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-autowired-annotation-constructor-resolution)。

还可以将@Autowired注解应用于传统的setter方法，如以下示例所示：

```java
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Autowired
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

你还可以将注解应用于具有任意名称和多个参数的方法，如以下示例所示：

```java
public class MovieRecommender {

    private MovieCatalog movieCatalog;

    private CustomerPreferenceDao customerPreferenceDao;

    @Autowired
    public void prepare(MovieCatalog movieCatalog,
            CustomerPreferenceDao customerPreferenceDao) {
        this.movieCatalog = movieCatalog;
        this.customerPreferenceDao = customerPreferenceDao;
    }

    // ...
}
```

也可以将@Autowired应用于字段，甚至可以将其与构造函数混合使用，如以下示例所示：

```java
public class MovieRecommender {

    private final CustomerPreferenceDao customerPreferenceDao;

    @Autowired
    private MovieCatalog movieCatalog;

    @Autowired
    public MovieRecommender(CustomerPreferenceDao customerPreferenceDao) {
        this.customerPreferenceDao = customerPreferenceDao;
    }

    // ...
}
```

确保你的目标组件（例如MovieCatalog或CustomerPreferenceDao）由用于@Autowired注解的注入点的类型一致地声明。否则，注入可能会由于运行时出现“找不到类型匹配”错误而失败。

对于通过类路径扫描找到的XML定义的bean或组件类，容器通常预先知道具体的类型。但是，对于@Bean工厂方法，需要确保声明的返回类型具有足够的表现力。对于实现多个接口的组件或可能由其实现类型引用的组件，请考虑在工厂方法中声明最具体的返回类型（至少根据引用你的bean的注入点的要求具体声明）。

你还可以通过将@Autowired注解添加到需要该类型数组的字段或方法中，指示Spring从ApplicationContext提供特定类型的所有bean，如以下示例所示：

```java
public class MovieRecommender {

    @Autowired
    private MovieCatalog[] movieCatalogs;

    // ...
}
```

如下例所示，这同样适用于类型化集合：

```java
public class MovieRecommender {

    private Set<MovieCatalog> movieCatalogs;

    @Autowired
    public void setMovieCatalogs(Set<MovieCatalog> movieCatalogs) {
        this.movieCatalogs = movieCatalogs;
    }

    // ...
}
```

>  如果希望数组或列表中的项目以特定顺序排序，则目标bean可以实现*org.springframework.core.Ordered*接口或使用@Order或标准@Priority注解。否则，它们的顺序将遵循容器中相应目标bean定义的注册顺序。
>
>  你可以在目标类级别和@Bean方法上声明@Order注解，这可能适用于单个bean定义（如果使用同一bean类的多个定义）。 @Order值可能会影响注入点的优先级，但请注意它们不会影响单例启动顺序，这是由依赖关系和@DependsOn声明确定的正交关注点。
>
>  注意，标准*javax.annotation.Priority*注解在@Bean级别不可用，因为无法在方法上声明它。它的语义可以通过@Order值与@Primary结合在每种类型的单个bean上进行建模。

只要预期的键类型为String，即使是类型化的Map实例也可以自动装配。映射值包含所有预期类型的bean，并且键包含相应的bean名称，如以下示例所示：

```java
public class MovieRecommender {

    private Map<String, MovieCatalog> movieCatalogs;

    @Autowired
    public void setMovieCatalogs(Map<String, MovieCatalog> movieCatalogs) {
        this.movieCatalogs = movieCatalogs;
    }

    // ...
}
```

默认情况下，当给定注入点没有匹配的候选bean可用时，自动装配将失败。对于声明的arrays,collections, maps，至少应有一个匹配元素。

默认行为是将带注解的方法和字段视为指示所需的依赖项。可以按照以下示例中的说明更改此行为，从而使框架可以通过将其标记为不需要来跳过不满意的注入点（即，通过将@Autowired中的required属性设置为false）：

```java
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Autowired(required = false)
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

如果不需要的方法（或在多个参数的情况下，其中一个依赖项）不可用，则根本不会调用该方法。在这种情况下，完全不需要填充非必需字段，而将其默认值保留在适当的位置。

注入的构造函数和工厂方法参数是一种特殊情况，因为由于Spring的构造函数解析算法可能会处理多个构造函数，因此@Autowired中的required属性的含义有所不同。缺省情况下，实际上有效地需要构造函数和工厂方法参数，但是在单构造函数场景中有一些特殊规则，例如，如果没有可用的匹配bean，则多元素注入点（arrays,collections, maps）解析为空实例。这允许一种通用的实现模式，其中所有依赖项都可以在唯一的多参数构造函数中声明-例如，声明为没有@Autowired注解的单个公共构造函数。

> 任何给定bean类的构造函数都只能声明@Autowired，并将必填属性设置为true，这表示在用作Spring bean时可以自动装配的构造函数。因此，如果必填属性保留为默认值true，则@Autowired只能注解单个构造函数。如果多个构造函数声明了注解，则它们都必须声明required= false才能被视为自动装配的候选对象（类似于XML中的autowire = constructor）。将选择通过匹配Spring容器中的bean可以满足的依赖关系数量最多的构造函数。如果没有一个候选者满足条件，则将使用主/默认构造函数（如果存在）。同样，如果一个类声明了多个构造函数，但都没有使用@Autowired进行注解，则将使用主/默认构造函数（如果存在）。如果一个类仅声明一个单一的构造函数开始，即使没有注解，也将始终使用它。请注意，带注解的构造函数不必是public的。
>
> 建议在setter方法上使用@Autowired的required属性，而不建议使用不推荐使用的@Required注解。将required属性设置为false表示该属性对于自动装配而言不是必需的，并且如果无法自动装配该属性，则将其忽略。另一方面，@Required更为强大，因为它可以通过容器支持的任何方式强制设置属性，并且如果未定义任何值，则会引发相应的异常。

另外，可以通过Java 8的*java.util.Optional*表示特定依赖项的非必需性质，如以下示例所示：

```java
public class SimpleMovieLister {

    @Autowired
    public void setMovieFinder(Optional<MovieFinder> movieFinder) {
        ...
    }
}
```

从Spring Framework 5.0开始，还可以使用@Nullable注解（在任何包中都可以使用任何形式，例如JSR-305中的*javax.annotation.Nullable*）或仅利用Kotlin内置的null安全支持：

```java
public class SimpleMovieLister {

    @Autowired
    public void setMovieFinder(@Nullable MovieFinder movieFinder) {
        ...
    }
}
```

也可以将@Autowired用于众所周知的可解决依赖项的接口：BeanFactory，ApplicationContext，Environment，ResourceLoader，ApplicationEventPublisher和MessageSource。这些接口及其扩展接口（例如ConfigurableApplicationContext或ResourcePatternResolver）将自动解析，而无需进行特殊设置。以下示例自动装配ApplicationContext对象：

```java
public class MovieRecommender {

    @Autowired
    private ApplicationContext context;

    public MovieRecommender() {
    }

    // ...
}
```

@ Autowired，@Inject，@Value和@Resource注解由Spring BeanPostProcessor实现处理。这意味着你不能在自己的BeanPostProcessor或BeanFactoryPostProcessor类型（如果有）中应用这些注解。必须使用XML或Spring @Bean方法显式“连接”这些类型。

### 使用@Primary微调基于注解的自动装配

由于按类型自动装配可能会导致多个候选对象，因此通常有必要更好地控制选择过程。一种实现此目标的方法是使用Spring的@Primary注解。 @Primary表示当多个bean可以自动连接到单值依赖项的候选对象时，应优先使用特定的bean。如果候选对象中仅存在一个主bean，则它将成为自动装配的值。

考虑以下将firstMovieCatalog定义为主要MovieCatalog的配置：

```java
@Configuration
public class MovieConfiguration {

    @Bean
    @Primary
    public MovieCatalog firstMovieCatalog() { ... }

    @Bean
    public MovieCatalog secondMovieCatalog() { ... }

    // ...
}
```

通过前面的配置，以下MovieRecommender与firstMovieCatalog自动连接：

```java
public class MovieRecommender {

    @Autowired
    private MovieCatalog movieCatalog;

    // ...
}
```

相应的bean定义如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog" primary="true">
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean id="movieRecommender" class="example.MovieRecommender"/>

</beans>
```

### 使用@Qualifiers微调基于注解的自动装配

当可以确定一个主要候选者时，@ Primary是在几种情况下按类型使用自动装配的有效方法。当需要对选择过程进行更多控制时，可以使用Spring的@Qualifier注解。你可以将限定符值与特定的参数相关联，从而缩小类型匹配的范围，以便为每个参数选择特定的bean。在最简单的情况下，这可以是简单的描述性值，如以下示例所示：

```java
public class MovieRecommender {

    @Autowired
    @Qualifier("main")
    private MovieCatalog movieCatalog;

    // ...
}
```

还可以在各个构造函数参数或方法参数上指定@Qualifier注解，如以下示例所示：

```java
public class MovieRecommender {

    private MovieCatalog movieCatalog;

    private CustomerPreferenceDao customerPreferenceDao;

    @Autowired
    public void prepare(@Qualifier("main") MovieCatalog movieCatalog,
            CustomerPreferenceDao customerPreferenceDao) {
        this.movieCatalog = movieCatalog;
        this.customerPreferenceDao = customerPreferenceDao;
    }

    // ...
}
```

以下示例显示了相应的bean定义。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog">
        <qualifier value="main"/> 

        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <qualifier value="action"/> 

        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean id="movieRecommender" class="example.MovieRecommender"/>

</beans>
```

> 具有main限定符值的bean与构造函数参数具有相同的值。
> 具有action限定符值的bean与构造函数参数关联，该参数具有相同的值。

对于后备匹配，bean名称被认为是默认的限定符值。因此，可以使用id为main而不是嵌套的qualifier元素定义bean，从而得到相同的匹配结果。但是，尽管可以使用此约定按名称引用特定的bean，但@Autowired基本上是关于带有可选语义限定符的类型驱动的注入。这意味着，即使带有bean名称后备的限定符值，在类型匹配集中也始终具有狭窄的语义。它们没有在语义上表示对唯一bean id的引用。良好的限定符值是main或EMEA或persistent，表示特定组件的特征，这些特征独立于bean id，在使用匿名bean定义（例如前面的示例中的定义）的情况下，可以自动生成该特定组件的特征。

限定符还适用于类型化的集合，如前面所述（例如，应用于Set \<MovieCatalog>）。在这种情况下，根据声明的限定符，将所有匹配的bean作为集合注入。这意味着限定词不必是唯一的。相反，它们构成了过滤标准。例如，可以使用相同的限定符值“action”定义多个MovieCatalog bean，所有这些都注入到以@Qualifier（“ action”）注解的Set \<MovieCatalog>中。

> 在类型匹配的候选对象中，让限定符值针对目标bean名称进行选择，在注入点不需要@Qualifier注解。如果没有其他解析度指示符（例如限定词或主标记），则对于非唯一依赖性情况，Spring将注入点名称（即字段名称或参数名称）与目标bean名称进行匹配，然后选择同名候选人（如果有）。

也就是说，如果打算按名称表示注解驱动的注入，则即使它能够在类型匹配的候选对象中按bean名称进行选择，也不要主要使用@Autowired。而是使用JSR-250 @Resource注解，该注解的语义定义是通过其唯一名称标识特定的目标组件，而声明的类型与匹配过程无关。 @Autowired具有非常不同的语义：在按类型选择候选bean之后，仅在那些类型选择的候选中考虑指定的String限定符值（例如，将account限定符与标记有相同限定符标签的bean进行匹配）。

对于本身定义为Map，collection或array类型的bean，@Resource是一个很好的解决方案，它通过唯一的名称引用特定的集合或数组bean。也就是说，从4.3版本开始，只要元素类型信息保留在@Bean返回类型签名或集合继承层次结构中，就可以通过Spring的@Autowired类型匹配算法来匹配Map和数组类型。在这种情况下，可以使用限定符值在同类型的集合中进行选择，如上一段所述。

从4.3开始，@ Autowired还考虑了自我引用以进行注入（即，引用回当前注入的Bean）。请注意，自我注入是一个后备。对其他组件的常规依赖始终优先。从这个意义上说，自我推荐不参与常规的候选人选择，因此尤其是绝不是主要的。相反，它们总是以最低优先级结束。实际上，你应该仅将自我引用用作最后的手段（例如，通过bean的事务代理在同一实例上调用其他方法）。在这种情况下，请考虑将受影响的方法分解为单独的委托bean。或者，可以使用@Resource，它可以通过其唯一名称获取返回到当前bean的代理。

> 尝试将@Bean方法的结果注入相同的配置类也实际上是一种自引用方案。要么在实际需要的方法签名中延迟解析这些引用（与配置类中的自动装配字段相对），要么将受影响的@Bean方法声明为静态，将它们与包含的配置类实例及其生命周期脱钩。否则，仅在回退阶段考虑此类Bean，而将其他配置类上的匹配Bean选作主要候选对象（如果可用）。

@Autowired适用于字段，构造函数和多参数方法，从而允许在参数级别缩小限定符注解的范围。相反，只有具有单个参数的字段和bean属性设置器方法才支持@Resource。因此，如果注入目标是构造函数或多参数方法，则应坚持使用限定符。

可以创建自己的自定义限定符注解。为此，请定义一个注解并在定义中提供@Qualifier注解，如以下示例所示：

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Genre {

    String value();
}
```

然后，可以在自动连接的字段和参数上提供自定义限定符，如以下示例所示：

```java
public class MovieRecommender {

    @Autowired
    @Genre("Action")
    private MovieCatalog actionCatalog;

    private MovieCatalog comedyCatalog;

    @Autowired
    public void setComedyCatalog(@Genre("Comedy") MovieCatalog comedyCatalog) {
        this.comedyCatalog = comedyCatalog;
    }

    // ...
}
```

接下来，可以提供有关候选bean定义的信息。可以将\<qualifier />标记添加为\<bean />标记的子元素，然后指定类型和值以匹配自定义限定符注解。该类型与注解的标准类名匹配。另外，为方便起见，如果不存在名称冲突的风险，则可以使用简短的类名。下面的示例演示了两种方法：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="Genre" value="Action"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="example.Genre" value="Comedy"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean id="movieRecommender" class="example.MovieRecommender"/>

</beans>
```

在“ [Classpath Scanning and Managed Components](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-classpath-scanning)”中，可以看到基于注解的替代方法，以XML形式提供限定符元数据。具体来说，请参阅[Providing Qualifier Metadata with Annotations](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-scanning-qualifiers).

在某些情况下，使用没有值的注解就足够了。当注解用于更一般的用途并且可以应用于几种不同类型的依赖项时，这将很有用。例如，你可以提供一个脱机目录，当没有Internet连接可用时可以进行搜索。首先，定义简单的注解，如以下示例所示：

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface Offline {

}
```

然后将注解添加到要自动装配的字段或属性，如以下示例所示：

```java
public class MovieRecommender {

    @Autowired
    @Offline 
    private MovieCatalog offlineCatalog;

    // ...
}
```

现在，bean定义仅需要限定符类型，如以下示例所示：

```xml
<bean class="example.SimpleMovieCatalog">
    <qualifier type="Offline"/> 
    <!-- inject any dependencies required by this bean -->
</bean>
```

你还可以定义自定义限定符注解，以接受除简单值属性之外或代替简单值属性的命名属性。如果随后在要自动装配的字段或参数上指定了多个属性值，则bean定义必须与所有此类属性值匹配才能被视为自动装配候选。例如，请考虑以下注解定义：

```java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface MovieQualifier {

    String genre();

    Format format();
}
```

在这种情况下，Format是一个枚举类，定义如下：

```java
public enum Format {
    VHS, DVD, BLURAY
}
```

要自动装配的字段将用自定义限定符进行注解，并包括两个属性的值：genre和format，如以下示例所示：

```java
public class MovieRecommender {

    @Autowired
    @MovieQualifier(format=Format.VHS, genre="Action")
    private MovieCatalog actionVhsCatalog;

    @Autowired
    @MovieQualifier(format=Format.VHS, genre="Comedy")
    private MovieCatalog comedyVhsCatalog;

    @Autowired
    @MovieQualifier(format=Format.DVD, genre="Action")
    private MovieCatalog actionDvdCatalog;

    @Autowired
    @MovieQualifier(format=Format.BLURAY, genre="Comedy")
    private MovieCatalog comedyBluRayCatalog;

    // ...
}
```

最后，bean定义应包含匹配的限定符值。此示例还演示了可以使用bean元属性代替\<qualifier />元素。如果可用，\<qualifier/>元素及其属性优先，但是如果不存在这样的限定符，则自动装配机制将退回到\<meta />标记内提供的值，如以下示例中的最后两个bean定义：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:annotation-config/>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="MovieQualifier">
            <attribute key="format" value="VHS"/>
            <attribute key="genre" value="Action"/>
        </qualifier>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <qualifier type="MovieQualifier">
            <attribute key="format" value="VHS"/>
            <attribute key="genre" value="Comedy"/>
        </qualifier>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <meta key="format" value="DVD"/>
        <meta key="genre" value="Action"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

    <bean class="example.SimpleMovieCatalog">
        <meta key="format" value="BLURAY"/>
        <meta key="genre" value="Comedy"/>
        <!-- inject any dependencies required by this bean -->
    </bean>

</beans>
```

### 将泛型用作自动装配限定符

除了@Qualifier注解之外，你还可以将Java泛型类型用作资格的隐式形式。例如，假设你具有以下配置：

```java
@Configuration
public class MyConfiguration {

    @Bean
    public StringStore stringStore() {
        return new StringStore();
    }

    @Bean
    public IntegerStore integerStore() {
        return new IntegerStore();
    }
}
```

假设前面的bean实现了通用接口（即Store \<String>和Store \<Integer>），则可以@Autowire Store接口，并且通用接口用作限定符，如以下示例所示：

```java
@Autowired
private Store<String> s1; // <String> qualifier, injects the stringStore bean

@Autowired
private Store<Integer> s2; // <Integer> qualifier, injects the integerStore bean
```

当自动装配lists，Map实例和arrays时，通用限定符也适用。以下示例自动装配通用List：

```java
// Inject all Store beans as long as they have an <Integer> generic
// Store<String> beans will not appear in this list
@Autowired
private List<Store<Integer>> s;
```

### 使用`CustomAutowireConfigurer`

CustomAutowireConfigurer是BeanFactoryPostProcessor实现，即使你没有使用Spring的@Qualifier注解来注解自己的自定义限定符注解类型，也可以使用它来注册。以下示例显示如何使用CustomAutowireConfigurer：

```xml
<bean id="customAutowireConfigurer"
        class="org.springframework.beans.factory.annotation.CustomAutowireConfigurer">
    <property name="customQualifierTypes">
        <set>
            <value>example.CustomQualifier</value>
        </set>
    </property>
</bean>
```

AutowireCandidateResolver通过以下方式确定自动装配候选者：

- 每个bean定义的autowire-candidate值
- \<beans />元素上可用的任何默认autowire-candidates模式
- @Qualifier注解以及在CustomAutowireConfigurer中注册的所有自定义注解的存在

当多个bean符合自动装配候选条件时，“主要”的确定如下：如果候选中恰好有一个bean定义的primary属性设置为true，则将其选中。

### 用@Resource注入

Spring还通过对字段或bean属性设置器方法使用JSR-250 @Resource注解（*javax.annotation.Resource*）支持注入。这是Java EE中的常见模式：例如，在JSF管理的Bean和JAX-WS端点中。 Spring也为Spring管理的对象支持此模式。

@Resource具有名称属性。默认情况下，Spring将该值解释为要注入的bean名称。换句话说，它遵循名称语义，如以下示例所示：

```java
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Resource(name="myMovieFinder") 
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
}
```

如果未明确指定名称，则默认名称是从字段名称或setter方法派生的。如果是字段，则采用字段名称。在使用setter方法的情况下，它采用bean属性名称。以下示例将把名为movieFinder的bean注入其setter方法中：

```java
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Resource
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
}
```

> 注解提供的名称由CommonAnnotationBeanPostProcessor解析为bean名称。如果你明确配置Spring的SimpleJndiBeanFactory，则可以通过JNDI解析名称。但是，我们建议你依赖默认行为，并使用Spring的JNDI查找功能来保留间接级别。

在@Resource用法的特殊情况下，未指定显式名称，并且类似于@Autowired，@Resource查找主类型匹配而不是特定的命名Bean，并解析众所周知的可解决依赖项：BeanFactory，ApplicationContext，ResourceLoader，ApplicationEventPublisher和MessageSource接口。

因此，在以下示例中，customerPreferenceDao字段首先查找名为“customerPreferenceDao”的bean，然后回退到类型为CustomerPreferenceDao的主类型匹配项：

```java
public class MovieRecommender {

    @Resource
    private CustomerPreferenceDao customerPreferenceDao;
//The context field is injected based on the known resolvable dependency type: ApplicationContext.
    @Resource
    private ApplicationContext context; 

    public MovieRecommender() {
    }

    // ...
}
```

### 使用@Value

@Value通常用于注入外部属性：

```java
@Component
public class MovieRecommender {

    private final String catalog;

    public MovieRecommender(@Value("${catalog.name}") String catalog) {
        this.catalog = catalog;
    }
}
```

使用以下配置：

```java
@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig { }
```

以及以下application.properties文件：

```java
catalog.name=MovieCatalog
```

在这种情况下，catalog参数和字段将等于MovieCatalog值。

Spring提供了一个默认的宽松内嵌值解析器。它将尝试解析属性值，如果无法解析，则将属性名称（例如${catalog.name}）作为值注入。如果要严格控制不存在的值，则应声明一个PropertySourcesPlaceholderConfigurer bean，如以下示例所示：

```java
@Configuration
public class AppConfig {

     @Bean
     public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
           return new PropertySourcesPlaceholderConfigurer();
     }
}
```

> 使用JavaConfig配置PropertySourcesPlaceholderConfigurer时，@Bean方法必须是静态的。

如果无法解析任何${}占位符，则使用上述配置可确保Spring初始化失败。也可以使用setPlaceholderPrefix，setPlaceholderSuffix或setValueSeparator之类的方法来自定义占位符。

> Spring Boot默认配置一个PropertySourcesPlaceholderConfigurer bean，它将从application.properties和application.yml文件获取属性。

Spring提供的内置转换器支持允许自动处理简单的类型转换（例如，转换为Integer或int）。多个逗号分隔的值可以自动转换为String数组，而无需付出额外的开销。

可以提供如下默认值：

```java
@Component
public class MovieRecommender {

    private final String catalog;

    public MovieRecommender(@Value("${catalog.name:defaultCatalog}") String catalog) {
        this.catalog = catalog;
    }
}
```

Spring BeanPostProcessor在后台使用ConversionService处理将@Value中的String值转换为目标类型的过程。如果要为自己的自定义类型提供转换支持，则可以提供自己的ConversionService bean实例，如以下示例所示：

```java
@Configuration
public class AppConfig {

    @Bean
    public ConversionService conversionService() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
        conversionService.addConverter(new MyCustomConverter());
        return conversionService;
    }
}
```

当@Value包含[`SpEL` expression](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions) 时，该值将在运行时动态计算，如以下示例所示：

```java
@Component
public class MovieRecommender {

    private final String catalog;

    public MovieRecommender(@Value("#{systemProperties['user.catalog'] + 'Catalog' }") String catalog) {
        this.catalog = catalog;
    }
}
```

SpEL还可以使用更复杂的数据结构：

```java
@Component
public class MovieRecommender {

    private final Map<String, Integer> countOfMoviesPerCatalog;

    public MovieRecommender(
            @Value("#{{'Thriller': 100, 'Comedy': 300}}") Map<String, Integer> countOfMoviesPerCatalog) {
        this.countOfMoviesPerCatalog = countOfMoviesPerCatalog;
    }
}
```

### 使用@PostConstruct和@PreDestroy

CommonAnnotationBeanPostProcessor不仅可以识别@Resource注解，还可以识别JSR-250生命周期注解：*javax.annotation.PostConstruct*和*javax.annotation.PreDestroy*。在Spring 2.5中引入的对这些注解的支持为初始化回调和销毁回调中描述的生命周期回调机制提供了一种替代方法。假设CommonAnnotationBeanPostProcessor已在Spring
ApplicationContext中注册，则在生命周期中与相应的Spring生命周期接口方法或显式声明的回调方法在同一点调用带有这些注解之一的方法。在以下示例中，缓存在初始化时预先填充，并在销毁时清除：

```java
public class CachingMovieLister {

    @PostConstruct
    public void populateMovieCache() {
        // populates the movie cache upon initialization...
    }

    @PreDestroy
    public void clearMovieCache() {
        // clears the movie cache upon destruction...
    }
}
```

有关组合各种生命周期机制的效果的详细信息，请参见[Combining Lifecycle Mechanisms](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-lifecycle-combined-effects)。

> 与@Resource一样，@PostConstruct和@PreDestroy注解类型是JDK
> 6到8的标准Java库的一部分。但是，整个*javax.annotation*包与JDK 9中的核心Java模块分离，并最终在JDK 11中删除了。如果需要，现在需要通过Maven 
> Central获取javax.annotation-api工件，只需像其他任何库一样将其添加到应用程序的类路径中即可。

## 类路径扫描和托管组件

本章中的大多数示例都使用XML来指定在Spring容器中生成每个BeanDefinition的配置元数据。之前部分（[Annotation-based Container Configuration](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-annotation-config)）演示了如何通过源码级别的注解提供许多配置元数据。但是，即使在这些示例中，“基本”bean定义也已在XML文件中明确定义，而注解仅驱动依赖项注入。本节介绍了通过扫描类路径来隐式检测候选组件的选项。候选组件是与过滤条件匹配的类，并在容器中注册了相应的Bean定义。这消除了使用XML进行bean注册的需要。相反，你可以使用注解（例如，@Component），AspectJ类型表达式或你自己的自定义过滤条件来选择哪些类已向容器注册了bean定义。

> 从Spring 3.0开始，Spring JavaConfig项目提供的许多功能是核心Spring 
> Framework的一部分。这使你可以使用Java而不是使用传统的XML文件来定义bean。查看@Configuration，@Bean，@Import和@DependsOn注解，以获取有关如何使用这些新功能的示例。

### @Component和其他典型注解

@Repository注解是实现存储库的角色或构造型（也称为数据访问对象或DAO）的任何类的标记。该标记的用途包括自动翻译异常，如“[Exception Translation](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/data-access.html#orm-exception-translation)”中所述。

Spring提供了进一步的构造型注解：@Component，@Service和@Controller。 
@Component是任何Spring托管组件的通用注解。 @Repository，@Service和@Controller是@Component的特化，用于更特定的用例（分别在持久层，服务层和表示层中）。因此，你可以使用@Component来注解组件类，但是通过使用@Repository，@ 
Service或@Controller来注解组件类，你的类更适合通过工具进行处理或与方面相关联。例如，这些构造型注解成为切入点的理想目标。 @Repository，@Service和@Controller在Spring框架的将来版本中也可以带有其他语义。因此，如果在服务层使用@Component或@Service之间进行选择，则@Service显然是更好的选择。同样，如前所述，@Repository已被支持作为持久层中自动异常转换的标记。

### 使用元注解和组合注解

Spring提供的许多注解都可以在自己的代码中用作元注解。元注解是可以应用于另一个注解的注解。例如，前面提到的@Service注解使用@Component进行元注解，如以下示例所示：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component 
public @interface Service {

    // ...
}
```

> @Component注解使得@Service具有与之相同用处

还可以组合元注解来创建“组合注解”。例如，Spring MVC中的@RestController注解由@Controller和@ResponseBody组成。

此外，组合注解可以选择从元注解中重新声明属性，以允许自定义。当你只希望公开元注解属性的子集时，此功能特别有用。例如，Spring的@SessionScope注解将作用域名称硬编码为session，但仍允许自定义proxyMode。以下清单显示了SessionScope注解的定义：

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Scope(WebApplicationContext.SCOPE_SESSION)
public @interface SessionScope {

    /**
     * Alias for {@link Scope#proxyMode}.
     * <p>Defaults to {@link ScopedProxyMode#TARGET_CLASS}.
     */
    @AliasFor(annotation = Scope.class)
    ScopedProxyMode proxyMode() default ScopedProxyMode.TARGET_CLASS;

}
```

你可以使用@SessionScope而不用声明proxyMode：

```java
@Service
@SessionScope
public class SessionScopedService {
    // ...
}
```

你还可以覆盖proxyMode的值，如以下示例所示：

```java
@Service
@SessionScope(proxyMode = ScopedProxyMode.INTERFACES)
public class SessionScopedUserService implements UserService {
    // ...
}
```

有关更多详细信息，请参见[Spring Annotation Programming Model](https://github.com/spring-projects/spring-framework/wiki/Spring-Annotation-Programming-Model).

### 自动检测类并注册Bean

Spring可以自动检测构造型类，并向ApplicationContext注册相应的BeanDefinition实例。例如，以下两个类有资格进行这种自动检测：

```java
@Service
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    public SimpleMovieLister(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }
}
```

```java
@Repository
public class JpaMovieFinder implements MovieFinder {
    // implementation elided for clarity
}
```

要自动检测这些类并注册相应的bean，需要将@ComponentScan添加到@Configuration类中，其中basePackages属性是这两个类的公共父包。 （或者，可以指定一个逗号分隔，分号分隔或空格分隔的列表，其中包括每个类的父包。）

> 为简便起见，前面的示例可能使用了注解的value属性（即@ComponentScan（“ org.example”））。

以下替代方法使用XML：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/context
        https://www.springframework.org/schema/context/spring-context.xsd">

    <context:component-scan base-package="org.example"/>

</beans>
```

> \<context:component-scan>的使用隐式启用\<context:annotation-config>的功能。使用\<context:component-scan>时，通常无需包含\<context:annotation-config>元素。

> 扫描类路径包需要在类路径中存在相应的目录条目。使用Ant构建JAR时，请确保未激活JAR任务的仅文件开关。另外，在某些环境中，基于安全策略可能不会公开类路径目录。例如，JDK 1.7.0_45及更高版本上的独立应用程序（这需要在清单中设置“受信任的库”。）请参见https://stackoverflow.com/questions/19394570/java-jre-7u45-breaks-classloader-getresources）。
>
> 在JDK 9的模块路径（Jigsaw）上，Spring的类路径扫描通常可以正常进行。但是，请确保将组件类导出到模块信息描述符中。如果你希望Spring调用你的类的非公共成员，请确保将它们“open”（即，它们在module-info描述符中使用了opens声明而不是export声明）*。

> 译者注:
>
> ![1589543782716](https://www.hellooooo.top/image/blog/2020/05/spring/1589543782716.png)

此外，当使用component-scan元素时，将隐式包括AutowiredAnnotationBeanPostProcessor和CommonAnnotationBeanPostProcessor。这意味着两个组件将被自动检测并连接在一起，而所有这些都不需要XML中提供任何bean配置元数据。

> 可以通过将`annotation-config`设为false来禁用AutowiredAnnotationBeanPostProcessor和CommonAnnotationBeanPostProcessor的注册。

### 使用过滤器自定义扫描

默认情况下，用@Component，@Repository，@Service，@Controller，@Configuration进行注解的类或使用@Component进行注解的自定义注解是唯一检测到的候选组件。但是，可以通过应用自定义过滤器来修改和扩展此行为。将它们添加为@ComponentScan注解的includeFilters或excludeFilters属性（或作为XML配置中<context：component-scan>元素的<context：include-filter/>或<context：exclude-filter 
/>子元素）。每个过滤器元素都需要type和expression属性。下表描述了过滤选项：

| Filter Type          | Example Expression           | Description                                                  |
| -------------------- | ---------------------------- | ------------------------------------------------------------ |
| annotation (default) | `org.example.SomeAnnotation` | An annotation to be *present* or *meta-present* at the type level in target components. |
| assignable           | `org.example.SomeClass`      | A class (or interface) that the target components are assignable to (extend or implement). |
| aspectj              | `org.example..*Service+`     | An AspectJ type expression to be matched by the target components. |
| regex                | `org\.example\.Default.*`    | A regex expression to be matched by the target components' class names. |
| custom               | `org.example.MyTypeFilter`   | A custom implementation of the `org.springframework.core.type.TypeFilter` interface. |

以下示例显示了忽略所有@Repository注解并改为使用“stub”存储库的配置：

```java
@Configuration
@ComponentScan(basePackages = "org.example",
        includeFilters = @Filter(type = FilterType.REGEX, pattern = ".*Stub.*Repository"),
        excludeFilters = @Filter(Repository.class))
public class AppConfig {
    ...
}
```

以下清单显示了等效的XML：

```xml
<beans>
    <context:component-scan base-package="org.example">
        <context:include-filter type="regex"
                expression=".*Stub.*Repository"/>
        <context:exclude-filter type="annotation"
                expression="org.springframework.stereotype.Repository"/>
    </context:component-scan>
</beans>
```

> 还可以通过在注解上设置useDefaultFilters = false或通过将use-default-filters =“
> false”作为\<component-scan />元素的属性来禁用默认过滤器。这有效地禁用了自动检测通过@Component，@Repository，@Service，@Controller，@RestController或@Configuration进行注解或元注解的类的功能。

### 在组件中定义Bean元数据

Spring组件还可以将bean定义元数据贡献给容器。可以在@Configuration注解类中使用@Bean注解定义bean元数据。以下示例显示了如何执行此操作：

```java
@Component
public class FactoryMethodComponent {

    @Bean
    @Qualifier("public")
    public TestBean publicInstance() {
        return new TestBean("publicInstance");
    }

    public void doWork() {
        // Component method implementation omitted
    }
}
```

上一类是Spring组件，在其doWork()方法中具有特定于应用程序的代码。但是，它也提供了一个具有工厂方法的bean定义，该工厂方法引用了方法publicInstance()。
@Bean注解标识工厂方法和其他bean定义属性，例如通过@Qualifier注解的限定符值。可以指定的其他方法级别注解为@Scope，@Lazy和自定义限定符注解。

> 除了用于组件初始化的角色外，还可以将@Lazy注解放置在标有@Autowired或@Inject的注入点上。在这种情况下，它导致注入了惰性解析代理。

如前所述，支持自动装配的字段和方法，并自动装配@Bean方法。以下示例显示了如何执行此操作：

```java
@Component
public class FactoryMethodComponent {

    private static int i;

    @Bean
    @Qualifier("public")
    public TestBean publicInstance() {
        return new TestBean("publicInstance");
    }

    // use of a custom qualifier and autowiring of method parameters
    @Bean
    protected TestBean protectedInstance(
            @Qualifier("public") TestBean spouse,
            @Value("#{privateInstance.age}") String country) {
        TestBean tb = new TestBean("protectedInstance", 1);
        tb.setSpouse(spouse);
        tb.setCountry(country);
        return tb;
    }

    @Bean
    private TestBean privateInstance() {
        return new TestBean("privateInstance", i++);
    }

    @Bean
    @RequestScope
    public TestBean requestScopedInstance() {
        return new TestBean("requestScopedInstance", 3);
    }
}
```

该示例将String方法参数国家/地区自动连线到另一个名为privateInstance的bean上age属性的值。 Spring Expression Language元素通过符号#{\<expression>}定义属性的值。对于@Value注解，表达式解析程序已预先配置为在解析表达式文本时查找bean名称。

从Spring Framework 4.3开始，你还可以声明类型为InjectionPoint的工厂方法参数（或更具体的子类：DependencyDescriptor），以访问触发当前bean创建的请求注入点。请注意，这仅适用于实际创建的Bean实例，不适用于注入现有实例。因此，此功能对原型(prototype)范围的bean最有意义。对于其他作用域，factory方法仅在给定作用域中看到触发创建新bean实例的注入点（例如，触发创建惰性单例bean的依赖项）。在这种情况下，可以将提供的注入点元数据与语义一起使用。以下示例显示如何使用InjectionPoint：

```java
@Component
public class FactoryMethodComponent {

    @Bean 
    @Scope("prototype")
    public TestBean prototypeInstance(InjectionPoint injectionPoint) {
        return new TestBean("prototypeInstance for " + injectionPoint.getMember());
    }
}
```

> InjectionPoint :A simple descriptor for an injection point, pointing to a method/constructor parameter or a field. Exposed by UnsatisfiedDependencyException. Also available as an argument for factory methods, reacting to therequesting injection point for building a customized bean instance.

常规Spring组件中的@Bean方法的处理方式与Spring @Configuration类中的@Bean方法不同。区别在于，使用CGLIB不能增强@Component类，以拦截方法和字段的调用。 
CGLIB代理是一种调用@Configuration类中@Bean方法中的方法或字段的方法，用于创建Bean元数据引用以协作对象。此类方法不是使用常规Java语义调用的，而是通过容器进行的，以提供通常的生命周期管理和Spring Bean的代理，即使通过@Bean方法的编程调用引用其他Bean时也是如此。相反，在普通@Component类内的@Bean方法中调用方法或字段具有标准Java语义，而无需特殊的CGLIB处理或其他约束。

> 可以将@Bean方法声明为静态方法，从而允许在不将其包含配置类创建为实例的情况下调用它们。在定义后处理器Bean（例如BeanFactoryPostProcessor或BeanPostProcessor类型）时，这特别有意义，因为此类Bean在容器生命周期的早期进行了初始化，并且应避免在那时触发配置的其他部分。
>
> 由于技术限制，对静态@Bean方法的调用永远不会被容器拦截，即使在@Configuration类中也是如此（如本节前面所述），这是由于技术限制：CGLIB子类只能覆盖非静态方法。结果，直接调用另一个@Bean方法具有标准的Java语义，从而导致直接从工厂方法本身直接返回一个独立的实例。
>
> @Bean方法的Java语言可见性不会对Spring容器中的最终bean定义产生直接影响。你可以在非@Configuration类中自由声明自己的工厂方法，也可以在任何地方声明静态方法。但是，@Configuration类中的常规@Bean方法必须是可重写的—即，不得将它们声明为private或final。
>
> 还可以在给定组件或配置类的基类上以及在由组件或配置类实现的接口中声明的Java 8默认方法上发现@Bean方法。这为组合复杂的配置安排提供了很大的灵活性，从Spring 4.2开始，通过Java 8默认方法甚至可以进行多重继承。
>
> 最后，一个类可以为同一个bean保留多个@Bean方法，这取决于在运行时可用的依赖关系，从而可以使用多个工厂方法。这与在其他配置方案中选择“最贪婪”的构造函数或工厂方法的算法相同：在构造时选择具有最大可满足依赖关系数量的变量，类似于容器在多个@Autowired构造函数之间进行选择的方式。

### 命名自动检测的组件

在扫描过程中自动检测到组件时，其bean名称由该扫描程序已知的BeanNameGenerator策略生成。默认情况下，任何包含名称值的Spring构造型注解（@Component，@Repository，@Service和@Controller）都会将该名称提供给相应的bean定义。

如果这样的注解不包含名称值，或者不包含任何其他检测到的组件（例如，由自定义过滤器发现的组件），则缺省bean名称生成器将返回不使用大写字母的非限定类名称。例如，如果检测到以下组件类，则名称将为myMovieLister和movieFinderImpl：

```java
@Service("myMovieLister")
public class SimpleMovieLister {
    // ...
}
```

```java
@Repository
public class MovieFinderImpl implements MovieFinder {
    // ...
}
```

如果不想依赖默认的Bean命名策略，则可以提供自定义Bean命名策略。首先，实现BeanNameGenerator接口，并确保包括默认的无参数构造函数。然后，在配置扫描程序时提供完全限定名，如以下示例注解和Bean定义所示。

> 如果由于多个自动检测到的组件具有相同的非限定类名称（即，具有相同名称但位于不同程序包中的类）而导致命名冲突，则可能需要配置一个BeanNameGenerator，该BeanNameGenerator默认为标准名称。生成的bean名称。从Spring Framework 5.2.3开始，位于*org.springframework.context.annotation*包中的FullyQualifiedAnnotationBeanNameGenerator可以用于此类目的。

```java
@Configuration
@ComponentScan(basePackages = "org.example", nameGenerator = MyNameGenerator.class)
public class AppConfig {
    // ...
}
```

```xml
<beans>
    <context:component-scan base-package="org.example"
        name-generator="org.example.MyNameGenerator" />
</beans>
```

作为一般规则，每当其他组件可能对其进行显式引用时，请考虑使用注解指定名称。另一方面，只要容器负责织入，自动生成的名称就足够了。

### 提供自动检测组件的作用域

通常，与Spring管理的组件一样，自动检测到的组件的默认范围也是最常见的范围是单例。但是，有时你需要使用@Scope注解指定的其他范围。你可以在注解中提供作用域的名称，如以下示例所示：

```java
@Scope("prototype")
@Repository
public class MovieFinderImpl implements MovieFinder {
    // ...
}
```

> @Scope注解仅在具体的bean类（对于带注解的组件）或工厂方法（对于@Bean方法）上进行内省。与XML bean定义相反，没有bean定义继承的概念，并且在类级别的继承层次结构与元数据目的无关。

有关特定于Web的作用域的详细信息，例如Spring上下文中的“request”或“session”，请参阅[Request, Session, Application, and WebSocket Scopes](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes-other).与这些作用域的预构建注解一样，也可以使用Spring的元注解方法来编写自己的作用域注解：例如，使用@Scope（“
prototype”）元注解的自定义注解，也可以声明一个自定义注解作用域代理模式。

> 要提供用于作用域解析的自定义策略，而不是依赖于基于注解的方法，可以实现ScopeMetadataResolver接口。确保包括默认的无参数构造函数。然后，可以在配置扫描程序时提供完全限定的类名，如以下注解和Bean定义示例所示：

```java
@Configuration
@ComponentScan(basePackages = "org.example", scopeResolver = MyScopeResolver.class)
public class AppConfig {
    // ...
}
```

```xml
<beans>
    <context:component-scan base-package="org.example" scope-resolver="org.example.MyScopeResolver"/>
</beans>
```

使用某些非单作用域时，可能有必要为作用域对象生成代理。在作用域bean中将推理描述为依赖项。为此，在component-scan元素上可以使用scoped-proxy属性。三个可能的值是：no，interfaces和targetClass。例如，以下配置生成标准的JDK动态代理：

```java
@Configuration
@ComponentScan(basePackages = "org.example", scopedProxy = ScopedProxyMode.INTERFACES)
public class AppConfig {
    // ...
}
```

```xml
<beans>
    <context:component-scan base-package="org.example" scoped-proxy="interfaces"/>
</beans>
```

### 提供带有注解的限定符元数据

@Qualifier注解在 [Fine-tuning Annotation-based Autowiring with Qualifiers](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-autowired-annotation-qualifiers)中进行讨论。该部分中的示例演示了@Qualifier注解和自定义限定符注解的使用，以在解析自动装配候选时提供细粒度的控制。由于这些示例基于XML bean定义，因此通过使用XML中bean元素的限定符或meta子元素，在候选bean定义上提供了限定符元数据。当依靠类路径扫描来自动检测组件时，可以在候选类上为限定符元数据提供类型级别的注解。下面的三个示例演示了此技术：

```java
@Component
@Qualifier("Action")
public class ActionMovieCatalog implements MovieCatalog {
    // ...
}
```

```java
@Component
@Genre("Action")
public class ActionMovieCatalog implements MovieCatalog {
    // ...
}
```

```java
@Component
@Offline
public class CachingMovieCatalog implements MovieCatalog {
    // ...
}
```

> 与大多数基于注解的替代方法一样，请记住，注解元数据绑定到类定义本身，而XML的使用允许相同类型的多个bean提供其限定符元数据的变体，因为该元数据是按实例(instance)而不是按类(class)。

### 生成候选组件的索引

尽管类路径扫描非常快，但可以通过在编译时创建静态候选列表来提高大型应用程序的启动性能。在这种模式下，作为组件扫描目标的所有模块都必须使用此机制。

> 现有的@ComponentScan或<context：component-scan指令必须保留原样，以请求上下文扫描某些软件包中的候选对象。当ApplicationContext检测到这样的索引时，它将自动使用它，而不是扫描类路径。

要生成索引，请向每个包含组件的模块添加附加依赖关系，这些组件是组件扫描指令的目标。以下示例显示了如何使用Maven进行操作：

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context-indexer</artifactId>
        <version>5.2.6.RELEASE</version>
        <optional>true</optional>
    </dependency>
</dependencies>
```

对于Gradle 4.5及更早版本，应在compileOnly配置中声明依赖项，如以下示例所示：

```groovy
dependencies {
    annotationProcessor "org.springframework:spring-context-indexer:{spring-version}"
}
```

该过程将生成一个包含在jar文件中的META-INF/spring.components文件。

> 在IDE中使用此模式时，spring-context-indexer必须注册为注解处理器，以确保在更新候选组件时索引是最新的。

> 在类路径上找到META-INF/spring.components时，将自动启用索引。如果某些库（或用例）的索引部分可用，但无法为整个应用程序构建，则可以通过将spring.index.ignore设置为true来回退到常规的类路径安排（好像根本没有索引）。可以是系统属性，也可以是classpath根目录下的spring.properties文件。

## 使用JSR 330标准注解

从Spring 3.0开始，Spring提供对JSR-330标准注解（依赖注入）的支持。这些注解的扫描方式与Spring注解扫描方式相同。要使用它们，你需要在类路径中有相关的jar。

> 如果使用Maven，则标准Maven存储库（https://repo1.maven.org/maven2/javax/inject/javax.inject/1/）中提供了javax.inject工件。你可以将以下依赖项添加到文件pom.xml中：
>
> ```xml
> <dependency>
>  <groupId>javax.inject</groupId>
>  <artifactId>javax.inject</artifactId>
>  <version>1</version>
> </dependency>
> ```

### @Inject和@Named的依赖注入

可以使用@*javax.inject.Inject*代替@Autowired，如下所示：

```java
import javax.inject.Inject;

public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Inject
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    public void listMovies() {
        this.movieFinder.findMovies(...);
        // ...
    }
}
```

与@Autowired一样，你可以在字段级别，方法级别和构造函数参数级别使用@Inject。此外，你可以将注入点声明为Provider，从而允许按需访问范围较小的bean，或者通过Provider.get()调用来懒惰访问其他bean。以下示例提供了先前示例的变体：

```java
import javax.inject.Inject;
import javax.inject.Provider;

public class SimpleMovieLister {

    private Provider<MovieFinder> movieFinder;

    @Inject
    public void setMovieFinder(Provider<MovieFinder> movieFinder) {
        this.movieFinder = movieFinder;
    }

    public void listMovies() {
        this.movieFinder.get().findMovies(...);
        // ...
    }
}
```

如果要为应注入的依赖项使用限定名称，则应使用@Named注解，如以下示例所示：

```java
import javax.inject.Inject;
import javax.inject.Named;

public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Inject
    public void setMovieFinder(@Named("main") MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

与@Autowired一样，@Inject也可以与*java.util.Optional*或@Nullable一起使用。由于@Inject没有require属性，因此这在此处更为适用。以下一对示例显示了如何使用@Inject和@Nullable：

```java
public class SimpleMovieLister {

    @Inject
    public void setMovieFinder(Optional<MovieFinder> movieFinder) {
        // ...
    }
}
```

```java
public class SimpleMovieLister {

    @Inject
    public void setMovieFinder(@Nullable MovieFinder movieFinder) {
        // ...
    }
}
```

### @Named和@ManagedBean：@Component注解的标准等效项

可以使用@*javax.inject.Named*或*javax.annotation.ManagedBean*代替@Component，如以下示例所示：

```java
import javax.inject.Inject;
import javax.inject.Named;

@Named("movieListener")  // @ManagedBean("movieListener") could be used as well
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Inject
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

在没有指定组件名称的情况下使用@Component是非常常见的。可以类似的方式使用@Named，如以下示例所示：

```java
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class SimpleMovieLister {

    private MovieFinder movieFinder;

    @Inject
    public void setMovieFinder(MovieFinder movieFinder) {
        this.movieFinder = movieFinder;
    }

    // ...
}
```

当使用@Named或@ManagedBean时，可以使用与使用Spring注解完全相同的方式来使用组件扫描，如以下示例所示：

```java
@Configuration
@ComponentScan(basePackages = "org.example")
public class AppConfig  {
    // ...
}
```

> 与@Component相反，JSR-330 @Named和JSR-250 ManagedBean注解是不可组合的。你应该使用Spring的原型模型来构建自定义组件注解。

### JSR-330标准注解的局限性

当使用标准注解时，你应该知道某些重要功能不可用，如下表所示：

| Spring              | javax.inject.*        | javax.inject restrictions / comments                         |
| ------------------- | --------------------- | ------------------------------------------------------------ |
| @Autowired          | @Inject               | `@Inject` has no 'required' attribute. Can be used with Java 8’s `Optional` instead. |
| @Component          | @Named / @ManagedBean | JSR-330 does not provide a composable model, only a way to identify named components. |
| @Scope("singleton") | @Singleton            | The JSR-330 default scope is like Spring’s `prototype`. However, in order to keep it consistent with Spring’s general defaults, a JSR-330 bean declared in the Spring container is a `singleton` by default. In order to use a scope other than `singleton`, you should use Spring’s `@Scope` annotation. `javax.inject` also provides a [@Scope](https://download.oracle.com/javaee/6/api/javax/inject/Scope.html) annotation. Nevertheless, this one is only intended to be used for creating your own annotations. |
| @Qualifier          | @Qualifier / @Named   | `javax.inject.Qualifier` is just a meta-annotation for building custom qualifiers. Concrete `String` qualifiers (like Spring’s `@Qualifier` with a value) can be associated through `javax.inject.Named`. |
| @Value              | -                     | no equivalent                                                |
| @Required           | -                     | no equivalent                                                |
| @Lazy               | -                     | no equivalent                                                |
| ObjectFactory       | Provider              | `javax.inject.Provider` is a direct alternative to Spring’s `ObjectFactory`, only with a shorter `get()` method name. It can also be used in combination with Spring’s `@Autowired` or with non-annotated constructors and setter methods. |

## 基于Java的容器配置

本节介绍如何在Java代码中使用注解来配置Spring容器。它包括以下主题：

- [Basic Concepts: `@Bean` and `@Configuration`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-java-basic-concepts)
- [Instantiating the Spring Container by Using `AnnotationConfigApplicationContext`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-java-instantiating-container)
- [Using the `@Bean` Annotation](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-java-bean-annotation)
- [Using the `@Configuration` annotation](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-java-configuration-annotation)
- [Composing Java-based Configurations](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-java-composing-configuration-classes)
- [Bean Definition Profiles](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-definition-profiles)
- [`PropertySource` Abstraction](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-property-source-abstraction)
- [Using `@PropertySource`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-using-propertysource)
- [Placeholder Resolution in Statements](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-placeholder-resolution-in-statements)

### 基本概念：@Bean和@Configuration

Spring的新Java配置支持中的主要构件是@Configuration注解的类和@Bean注解的方法。

@Bean注解用于指示方法实例化，配置和初始化要由Spring IoC容器管理的新对象。对于那些熟悉Spring的\<beans />XML配置的人来说，@ Bean注解与\<bean />元素具有相同的作用。你可以将@Bean注解方法与任何Spring @Component一起使用。但是，它们最常与@Configuration bean一起使用。

用@Configuration注解类表示该类的主要目的是作为Bean定义的来源。此外，@Configuration类允许通过调用同一类中的其他@Bean方法来定义Bean间的依赖关系。最简单的@Configuration类的内容如下：

```java
@Configuration
public class AppConfig {

    @Bean
    public MyService myService() {
        return new MyServiceImpl();
    }
}
```

前面的AppConfig类等效于下面的Spring \<beans /> XML：

```xml
<beans>
    <bean id="myService" class="com.acme.services.MyServiceImpl"/>
</beans>
```

> 完整的@Configuration与“精简” @Bean模式？
>
> 如果在未使用@Configuration注解的类中声明@Bean方法，则将它们称为以“精简”模式进行处理。在@Component或在简单的旧类中声明的Bean方法被认为是“精简版”，其中包含类的主要目的不同，而@Bean方法在那里具有某种优势。例如，服务组件可以通过每个适用组件类上的其他@Bean方法将管理视图公开给容器。在这种情况下，@Bean方法是一种通用的工厂方法机制。
>
> 与完整的@Configuration不同，精简 @Bean方法无法声明Bean之间的依赖关系。取而代之的是，它们在其包含组件的内部状态上进行操作，并且还可以根据可能声明的参数进行操作。因此，此类@Bean方法不应调用其他@Bean方法。实际上，每个此类方法仅是用于特定bean引用的工厂方法，而没有任何特殊的运行时语义。这里的积极副作用是，不必在运行时应用CGLIB子类，因此在类设计方面没有任何限制（即，包含类可能是最终类，依此类推）。
>
> 在常见情况下，@Bean方法将在@Configuration类中声明，以确保始终使用“完全”模式，因此跨方法引用将重定向到容器的生命周期管理。这样可以防止通过常规Java调用意外地调用同一@Bean方法，从而有助于减少在“精简”模式下运行时难以追查的细微错误。

以下各节将详细讨论@Bean和@Configuration注解。但是，首先，我们介绍了使用基于Java的配置来创建Spring容器的各种方法。

### 使用AnnotationConfigApplicationContext实例化Spring容器

以下各节介绍了Spring 3.0中引入的Spring的AnnotationConfigApplicationContext。这种通用的ApplicationContext实现不仅能够接受@Configuration类作为输入，而且还可以接受普通的@Component类和带有JSR-330元数据注解的类。

当提供@Configuration类作为输入时，@Configuration类本身将注册为Bean定义，并且该类中所有已声明的@Bean方法也将注册为Bean定义。

提供@Component和JSR-330类时，它们将注册为bean定义，并且假定在必要时在这些类中使用了诸如@Autowired或@Inject之类的DI元数据。

#### 简单指引

与实例化ClassPathXmlApplicationContext时将Spring XML文件用作输入的方式几乎相同，实例化AnnotationConfigApplicationContext时可以将@Configuration类用作输入。如下面的示例所示，这允许完全不使用XML来使用Spring容器：

```java
public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
    MyService myService = ctx.getBean(MyService.class);
    myService.doStuff();
}
```

如前所述，AnnotationConfigApplicationContext不限于仅与@Configuration类一起使用。可以将任何@Component或JSR-330带注解的类作为输入提供给构造函数，如以下示例所示：

```java
public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(MyServiceImpl.class, Dependency1.class, Dependency2.class);
    MyService myService = ctx.getBean(MyService.class);
    myService.doStuff();
}
```

前面的示例假定MyServiceImpl，Dependency1和Dependency2使用Spring依赖项注入注解，例如@Autowired。

#### 使用编程方式构建容器通过register(Class<?...)

你可以使用无参构造器实例化AnnotationConfigApplicationContext，然后使用register()方法对其进行配置。以编程方式构建AnnotationConfigApplicationContext时，此方法特别有用。以下示例显示了如何执行此操作：

```java
public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.register(AppConfig.class, OtherConfig.class);
    ctx.register(AdditionalConfig.class);
    ctx.refresh();
    MyService myService = ctx.getBean(MyService.class);
    myService.doStuff();
}
```

#### 使用scan(String ...)启用组件扫描

要启用组件扫描，可以按如下方式注解@Configuration类：

```java
@Configuration
@ComponentScan(basePackages = "com.acme") 
public class AppConfig  {
    ...
}
```

> 经验丰富的Spring用户可能熟悉Spring的context：名称空间中的XML声明，如以下示例所示：
>
> ```xml
> <beans>
>  <context:component-scan base-package="com.acme"/>
> </beans>
> ```

在前面的示例中，对com.acme包进行了扫描以查找任何@Component注解的类，并将这些类注册为容器内的Spring bean定义。 AnnotationConfigApplicationContext公开了scan（String ...）方法以允许相同的组件扫描功能，如以下示例所示：

```java
public static void main(String[] args) {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.scan("com.acme");
    ctx.refresh();
    MyService myService = ctx.getBean(MyService.class);
}
```

> 请记住，@Configuration类使用@Component进行元注解，因此它们是组件扫描的候选对象。在前面的示例中，假定AppConfig在com.acme包（或下面的任何包）中声明，则在调用scan()时将其拾取。根据refresh()，其所有@Bean方法都将被处理并注册为容器内的Bean定义。

#### 通过AnnotationConfigWebApplicationContext支持Web应用程序

AnnotationConfigApplication的WebApplicationContext变体可用于AnnotationConfigWebApplicationContext。在配置Spring ContextLoaderListener Servlet侦听器，Spring MVC DispatcherServlet等时，可以使用此实现。以下web.xml代码片段配置了典型的Spring MVC Web应用程序（请注意contextClass context-param和init-param的用法）：

```xml
<web-app>
    <!-- Configure ContextLoaderListener to use AnnotationConfigWebApplicationContext
        instead of the default XmlWebApplicationContext -->
    <context-param>
        <param-name>contextClass</param-name>
        <param-value>
            org.springframework.web.context.support.AnnotationConfigWebApplicationContext
        </param-value>
    </context-param>

    <!-- Configuration locations must consist of one or more comma- or space-delimited
        fully-qualified @Configuration classes. Fully-qualified packages may also be
        specified for component-scanning -->
    <context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>com.acme.AppConfig</param-value>
    </context-param>

    <!-- Bootstrap the root application context as usual using ContextLoaderListener -->
    <listener>
        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
    </listener>

    <!-- Declare a Spring MVC DispatcherServlet as usual -->
    <servlet>
        <servlet-name>dispatcher</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <!-- Configure DispatcherServlet to use AnnotationConfigWebApplicationContext
            instead of the default XmlWebApplicationContext -->
        <init-param>
            <param-name>contextClass</param-name>
            <param-value>
                org.springframework.web.context.support.AnnotationConfigWebApplicationContext
            </param-value>
        </init-param>
        <!-- Again, config locations must consist of one or more comma- or space-delimited
            and fully-qualified @Configuration classes -->
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>com.acme.web.MvcConfig</param-value>
        </init-param>
    </servlet>

    <!-- map all requests for /app/* to the dispatcher servlet -->
    <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern>/app/*</url-pattern>
    </servlet-mapping>
</web-app>
```

#### 使用@Bean注解

@Bean是方法级注解，是XML \<bean />元素的直接类似物。注解支持\<bean />提供的某些属性，例如：* [init-method](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-lifecycle-initializingbean) * [destroy-method](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-lifecycle-disposablebean) * [autowiring](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-autowire) * `name`。

可以在@Configuration注解或@Component注解的类中使用@Bean注解。

### 声明Bean

要声明一个bean，可以用@Bean注解来标注一个方法。可以使用此方法在类型指定为该方法的返回值的ApplicationContext中注册bean定义。缺省情况下，bean名称与方法名称相同。以下示例显示了@Bean方法声明：

```java
@Configuration
public class AppConfig {

    @Bean
    public TransferServiceImpl transferService() {
        return new TransferServiceImpl();
    }
}
```

前面的配置与下面的Spring XML完全等效：

```xml
<beans>
    <bean id="transferService" class="com.acme.TransferServiceImpl"/>
</beans>
```

这两个声明都使一个名为transferService的bean在ApplicationContext中可用，并绑定到类型为TransferServiceImpl的对象实例，如以下文本图像所示：

```
transferService -> com.acme.TransferServiceImpl
```

你还可以使用接口（或基类）返回类型声明@Bean方法，如以下示例所示：

```java
@Configuration
public class AppConfig {

    @Bean
    public TransferService transferService() {
        return new TransferServiceImpl();
    }
}
```

但是，这将高级类型预测的可见性限制为指定的接口类型（TransferService）。然后，使用仅一次容器已知的完整类型（TransferServiceImpl），就可以实例化受影响的单例bean。非延迟单例bean根据其声明顺序实例化，因此你可能会看到不同的类型匹配结果，具体取决于另一个组件何时尝试按未声明的类型进行匹配（例如@Autowired
TransferServiceImpl，仅当transferService bean具有被实例化）。

> 如果你通过声明的服务接口一致地引用类型，则@Bean返回类型可以安全地加入该设计决策。但是，对于实现多个接口的组件或由其实现类型潜在引用的组件，声明可能的最具体的返回类型（至少与引用你的bean的注入点所要求的具体类型一样）更为安全。

#### Bean依赖

@Bean注解的方法可以具有任意数量的参数，这些参数描述构建该bean所需的依赖关系。例如，如果我们的TransferService需要一个AccountRepository，则可以使用方法参数来实现该依赖关系，如以下示例所示：

```java
@Configuration
public class AppConfig {

    @Bean
    public TransferService transferService(AccountRepository accountRepository) {
        return new TransferServiceImpl(accountRepository);
    }
}
```

解析机制与基于构造函数的依赖注入几乎相同。有关更多详细信息，请参见[the relevant section](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-constructor-injection).

#### 接收生命周期回调

任何使用@Bean注解定义的类都支持常规的生命周期回调，并且可以使用JSR-250中的@PostConstruct和@PreDestroy注解。有关更多详细信息，请参见[JSR-250 annotations](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-postconstruct-and-predestroy-annotations)。

完全支持常规的Spring生命周期回调。如果bean实现InitializingBean，DisposableBean或Lifecycle，则容器将调用它们各自的方法。

还完全支持标准的* Aware接口集（例如 [BeanFactoryAware](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-beanfactory),[BeanNameAware](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-aware),[MessageSourceAware](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#context-functionality-messagesource),[ApplicationContextAware](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-aware)等）。

@Bean注解支持指定任意的初始化和销毁回调方法，非常类似于bean元素上Spring XML的init-method和destroy-method属性，如以下示例所示：

```java
public class BeanOne {

    public void init() {
        // initialization logic
    }
}

public class BeanTwo {

    public void cleanup() {
        // destruction logic
    }
}

@Configuration
public class AppConfig {

    @Bean(initMethod = "init")
    public BeanOne beanOne() {
        return new BeanOne();
    }

    @Bean(destroyMethod = "cleanup")
    public BeanTwo beanTwo() {
        return new BeanTwo();
    }
}
```

> 缺省情况下，使用Java配置定义的具有公共close或shutdown方法的bean会自动通过销毁回调进行登记。如果你有一个公共close或shutdown方法，并且不希望在容器关闭时调用它，则可以在bean定义中添加@Bean（destroyMethod =“”）以禁用默认（推断）模式。
>
> 默认情况下，你可能要对通过JNDI获取的资源执行此操作，因为其生命周期是在应用程序外部进行管理的。特别是，请确保始终对数据源执行此操作，因为在Java EE应用程序服务器上已知这是有问题的。
>
> 以下示例显示如何防止对数据源的自动销毁回调：
>
> ```java
> @Bean(destroyMethod="")
> public DataSource dataSource() throws NamingException {
>  return (DataSource) jndiTemplate.lookup("MyDS");
> }
> ```
>
> 另外，对于@Bean方法，通常使用程序化JNDI查找，方法是使用Spring的JndiTemplate或JndiLocatorDelegate帮助器，或者直接使用JNDI
> InitialContext用法，而不使用JndiObjectFactoryBean变体（这将迫使你将返回类型声明为FactoryBean类型，而不是实际的类型。目标类型，因此很难在打算引用此处提供的资源的其他@Bean方法中用于交叉引用调用）。

对于前面示例中的BeanOne，在构造过程中直接调用init()方法同样有效，如以下示例所示：

```java
@Configuration
public class AppConfig {

    @Bean
    public BeanOne beanOne() {
        BeanOne beanOne = new BeanOne();
        beanOne.init();
        return beanOne;
    }

    // ...
}
```

> 当你直接使用Java工作时，你可以对对象执行任何操作，而不必总是依赖于容器生命周期。

#### 指定Bean作用域

Spring包含@Scope注解，以便可以指定bean的作用域

#### 使用@Scope注解

你可以指定使用@Bean注解定义的bean应该具有特定范围。你可以使用[Bean Scopes](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes) 部分中指定的任何标准范围。

默认范围是单例，但是可以使用@Scope注解覆盖它，如以下示例所示：

```java
@Configuration
public class MyConfiguration {

    @Bean
    @Scope("prototype")
    public Encryptor encryptor() {
        // ...
    }
}
```

**@Scope和scoped-proxy**

Spring提供了一种通过作用域代理处理作用域依赖性的便捷方法。使用XML配置时创建此类代理的最简单方法是\<aop：scoped-proxy/>元素。使用@Scope注解在Java中配置bean，可以通过proxyMode属性提供同等的支持。默认值为无代理（ScopedProxyMode.NO），但是可以指定ScopedProxyMode.TARGET_CLASS或ScopedProxyMode.INTERFACES。

如果使用Java从XML参考文档（请参阅[scoped proxies](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-scopes-other-injection)）将作用域代理示例移植到我们的@Bean，则它类似于以下内容：

```java
// an HTTP Session-scoped bean exposed as a proxy
@Bean
@SessionScope
public UserPreferences userPreferences() {
    return new UserPreferences();
}

@Bean
public Service userService() {
    UserService service = new SimpleUserService();
    // a reference to the proxied userPreferences bean
    service.setUserPreferences(userPreferences());
    return service;
}
```

#### 自定义Bean命名

默认情况下，配置类使用@Bean方法的名称作为结果bean的名称。但是，可以使用name属性覆盖此功能，如以下示例所示：

```java
@Configuration
public class AppConfig {

    @Bean(name = "myThing")
    public Thing thing() {
        return new Thing();
    }
}
```

#### Bean别名

如在[Naming Beans](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-beanname)中讨论的那样，有时希望为单个Bean提供多个名称，否则称为Bean别名。为此，@Bean注解的name属性接受一个String数组。以下示例显示了如何为bean设置多个别名：

```java
@Configuration
public class AppConfig {

    @Bean({"dataSource", "subsystemA-dataSource", "subsystemB-dataSource"})
    public DataSource dataSource() {
        // instantiate, configure and return DataSource bean...
    }
}
```

#### Bean描述

有时，提供有关bean的更详细的文本描述会很有帮助。当暴露出bean（可能通过JMX）以进行监视时，这特别有用。

要向@Bean添加描述，可以使用@Description注解，如以下示例所示：

### 使用@Configuration注解

@Configuration是类级别的注解，指示对象是Bean定义的源。 @Configuration类通过公共@Bean注解方法声明bean。对@Configuration类的@Bean方法的调用也可以用于定义Bean之间的依赖关系。有关一般性介绍，请参见 [Basic Concepts: `@Bean` and `@Configuration`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-java-basic-concepts).

#### 注入bean间的依赖关系

当bean相互依赖时，表示依赖关系就像让一个bean方法调用另一个依赖一样简单，如以下示例所示：

```java
@Configuration
public class AppConfig {

    @Bean
    public BeanOne beanOne() {
        return new BeanOne(beanTwo());
    }

    @Bean
    public BeanTwo beanTwo() {
        return new BeanTwo();
    }
}
```

在前面的示例中，beanOne通过构造函数注入接收对beanTwo的引用。

> 仅当在@Configuration类中声明@Bean方法时，此声明bean间依赖关系的方法才有效。不能使用简单的@Component类声明Bean间的依赖关系。

#### 查找方法注入

如前所述，查找方法注入是一项高级功能，你应该很少使用。在单例作用域的bean对原型(prototype)作用域的bean有依赖性的情况下，这很有用。将Java用于这种类型的配置为实现这种模式提供了自然的方法。以下示例显示如何使用查找方法注入：

```java
public abstract class CommandManager {
    public Object process(Object commandState) {
        // grab a new instance of the appropriate Command interface
        Command command = createCommand();
        // set the state on the (hopefully brand new) Command instance
        command.setState(commandState);
        return command.execute();
    }

    // okay... but where is the implementation of this method?
    protected abstract Command createCommand();
}
```

通过使用Java配置，可以创建CommandManager的子类，在该子类中，抽象的createCommand()方法将被覆盖，以使其查找新的（原型）命令对象。以下示例显示了如何执行此操作：

```java
@Bean
@Scope("prototype")
public AsyncCommand asyncCommand() {
    AsyncCommand command = new AsyncCommand();
    // inject dependencies here as required
    return command;
}

@Bean
public CommandManager commandManager() {
    // return new anonymous implementation of CommandManager with createCommand()
    // overridden to return a new prototype Command object
    return new CommandManager() {
        protected Command createCommand() {
            return asyncCommand();
        }
    }
}
```

#### 有关基于Java的配置在内部如何工作的更多信息

考虑以下示例，该示例显示了一个@Bean注解方法被调用两次：

```java
@Configuration
public class AppConfig {

    @Bean
    public ClientService clientService1() {
        ClientServiceImpl clientService = new ClientServiceImpl();
        clientService.setClientDao(clientDao());
        return clientService;
    }

    @Bean
    public ClientService clientService2() {
        ClientServiceImpl clientService = new ClientServiceImpl();
        clientService.setClientDao(clientDao());
        return clientService;
    }

    @Bean
    public ClientDao clientDao() {
        return new ClientDaoImpl();
    }
}
```

clientDao()在clientService1()中被调用一次，并在clientService2()中被调用一次。由于此方法会创建一个ClientDaoImpl的新实例并返回它，因此通常希望有两个实例（每个服务一个）。那肯定是有问题的：在Spring中，实例化的bean默认情况下具有单例作用域。这就是神奇之处所在：所有@Configuration类在启动时都使用CGLIB进行了子类化。在子类中，子方法在调用父方法并创建新实例之前，首先检查容器中是否有任何缓存（作用域）的bean。

> 根据bean的范围，行为可能有所不同。我们在这里谈论单例。

> 从Spring 3.2开始，不再需要将CGLIB添加到你的类路径中，因为CGLIB类已经在org.springframework.cglib下重新打包并直接包含在spring-core JAR中。

> 由于CGLIB在启动时会动态添加功能，因此存在一些限制。特别是，配置类不能是final的。但是，从4.3版本开始，配置类中允许使用任何构造函数，包括使用@Autowired或单个非默认构造函数声明进行默认注入。
>
> 如果你希望避免任何CGLIB施加的限制，请考虑在非@Configuration类（例如，在普通的@Component类上）声明@Bean方法。CGLIB不会拦截@Bean方法之间的跨方法调用，因此你必须专门依赖于那里的构造函数或方法级别的依赖项注入。

### 组成基于Java的配置

Spring的基于Java的配置功能使你可以撰写注解，从而降低配置的复杂性。

#### 使用@Import注解

与在Spring XML文件中使用\<import />元素来帮助模块化配置一样，@Import注解允许从另一个配置类加载@Bean定义，如以下示例所示：

```java
@Configuration
public class ConfigA {

    @Bean
    public A a() {
        return new A();
    }
}

@Configuration
@Import(ConfigA.class)
public class ConfigB {

    @Bean
    public B b() {
        return new B();
    }
}
```

现在，无需在实例化上下文时同时指定ConfigA.class和ConfigB.class，只需显式提供ConfigB，如以下示例所示：

```java
public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(ConfigB.class);

    // now both beans A and B will be available...
    A a = ctx.getBean(A.class);
    B b = ctx.getBean(B.class);
}
```

这种方法简化了容器的实例化，因为只需要处理一个类，而不需要你在构造过程中记住大量的@Configuration类。

> 从Spring Framework 4.2开始，@Import还支持对常规组件类的引用，类似于AnnotationConfigApplicationContext.register方法。如果要通过使用一些配置类作为入口点来显式定义所有组件，从而避免组件扫描，则此功能特别有用。

#### 在导入的@Bean定义上注入依赖项

前面的示例有效，但过于简单。在大多数实际情况下，Bean在配置类之间相互依赖。使用XML时，这不是问题，因为不涉及任何编译器，并且你可以声明ref=“someBean”并委托Spring在容器初始化期间进行处理。使用@Configuration类时，Java编译器会在配置模型上施加约束，因为对其他bean的引用必须是有效的Java语法。

幸运的是，解决这个问题很简单。正如[我们已经讨论的](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-java-dependencies)，@ Bean方法可以具有任意数量的参数来描述Bean的依赖关系。考虑以下具有多个@Configuration类的更实际的场景，每个类取决于在其他类中声明的bean：

```java
public class ServiceConfig {

    @Bean
    public TransferService transferService(AccountRepository accountRepository) {
        return new TransferServiceImpl(accountRepository);
    }
}

@Configuration
public class RepositoryConfig {

    @Bean
    public AccountRepository accountRepository(DataSource dataSource) {
        return new JdbcAccountRepository(dataSource);
    }
}

@Configuration
@Import({ServiceConfig.class, RepositoryConfig.class})
public class SystemTestConfig {

    @Bean
    public DataSource dataSource() {
        // return new DataSource
    }
}

public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);
    // everything wires up across configuration classes...
    TransferService transferService = ctx.getBean(TransferService.class);
    transferService.transfer(100.00, "A123", "C456");
}
```

还有另一种方法可以达到相同的结果。请记住，@Configuration类最终仅是容器中的一个bean：这意味着它们可以利用@Autowired和@Value注入以及与任何其他bean相同的其他功能。

> 确保以这种方式注入的依赖项只是最简单的一种。 @Configuration类是在上下文初始化期间非常早地处理的，并且强制以这种方式注入依赖项可能导致意外的早期初始化。如上例所示，尽可能使用基于参数的注入。
>
> 另外，通过@Bean使用BeanPostProcessor和BeanFactoryPostProcessor定义时要特别小心。通常应将这些声明为静态@Bean方法，而不触发其包含的配置类的实例化。否则，@Autowired和@Value可能不适用于配置类本身，因为可以将其创建为比AutowiredAnnotationBeanPostProcessor早的bean实例。

以下示例说明如何将一个bean自动连接到另一个bean：

```java
@Configuration
public class ServiceConfig {

    @Autowired
    private AccountRepository accountRepository;

    @Bean
    public TransferService transferService() {
        return new TransferServiceImpl(accountRepository);
    }
}

@Configuration
public class RepositoryConfig {

    private final DataSource dataSource;

    public RepositoryConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public AccountRepository accountRepository() {
        return new JdbcAccountRepository(dataSource);
    }
}

@Configuration
@Import({ServiceConfig.class, RepositoryConfig.class})
public class SystemTestConfig {

    @Bean
    public DataSource dataSource() {
        // return new DataSource
    }
}

public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);
    // everything wires up across configuration classes...
    TransferService transferService = ctx.getBean(TransferService.class);
    transferService.transfer(100.00, "A123", "C456");
}
```

> 从Spring Framework 4.3开始，仅支持@Configuration类中的构造方法注入。还要注意，如果目标bean仅定义一个构造函数，则无需指定@Autowired。

**易于浏览引入的全限定Bean**

在前面的场景中，使用@Autowired可以很好地工作并提供所需的模块化，但是确切地确定在何处声明自动装配的Bean定义仍然有些模棱两可。例如，当开发人员查看ServiceConfig时，你如何确切知道@Autowired AccountRepository bean的声明位置？它在代码中不是明确的，这可能很好。请记住，Spring Tools for Eclipse提供了可以渲染图形的工具，这些图形显示了所有接线的方式，这可能就是你所需要的。另外，Java IDE可以轻松找到AccountRepository类型的所有声明和使用，并快速向你显示返回该类型的@Bean方法的位置。

如果这种歧义是不可接受的，并且你希望从IDE内部直接从一个@Configuration类导航到另一个@Configuration类，请考虑自动装配配置类本身。以下示例显示了如何执行此操作：

```java
@Configuration
public class ServiceConfig {

    @Autowired
    private RepositoryConfig repositoryConfig;

    @Bean
    public TransferService transferService() {
        // navigate 'through' the config class to the @Bean method!
        return new TransferServiceImpl(repositoryConfig.accountRepository());
    }
}
```

在上述情况下，定义AccountRepository是完全显式的。但是，ServiceConfig现在与RepositoryConfig紧密耦合。通过使用基于接口或基于抽象类的@Configuration类，可以在某种程度上缓解这种紧密耦合。考虑以下示例：

```java
@Configuration
public class ServiceConfig {

    @Autowired
    private RepositoryConfig repositoryConfig;

    @Bean
    public TransferService transferService() {
        return new TransferServiceImpl(repositoryConfig.accountRepository());
    }
}

@Configuration
public interface RepositoryConfig {

    @Bean
    AccountRepository accountRepository();
}

@Configuration
public class DefaultRepositoryConfig implements RepositoryConfig {

    @Bean
    public AccountRepository accountRepository() {
        return new JdbcAccountRepository(...);
    }
}

@Configuration
@Import({ServiceConfig.class, DefaultRepositoryConfig.class})  // import the concrete config!
public class SystemTestConfig {

    @Bean
    public DataSource dataSource() {
        // return DataSource
    }

}

public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(SystemTestConfig.class);
    TransferService transferService = ctx.getBean(TransferService.class);
    transferService.transfer(100.00, "A123", "C456");
}
```

现在，ServiceConfig与具体的DefaultRepositoryConfig松散耦合，并且内置的IDE工具仍然有用：你可以轻松地获得RepositoryConfig实现的类型层次结构。通过这种方式，导航@Configuration类及其依赖项与导航基于接口的代码的通常过程没有什么不同。

> 如果要影响某些bean的启动创建顺序，请考虑将其中一些声明为@Lazy（用于在首次访问时创建而不是在启动时创建）或声明为@DependsOn某些其他bean（确保在创建其他特定bean之前）当前的bean，而不是后者的直接依赖项所暗示的）。

#### 有条件地包含@Configuration类或@Bean方法

根据某些系统状态，有条件地启用或禁用完整的@Configuration类甚至单个@Bean方法通常很有用。一个常见的示例是仅在Spring环境中启用了特定配置文件后，才使用@Profile注解来激活Bean（有关详细信息，请参见[Bean Definition Profiles](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-definition-profiles)）。

@Profile注解实际上是通过使用更灵活的称为@Conditional的注解来实现的。 @Conditional批注指示在注册@Bean之前应参考的特定*org.springframework.context.annotation.Condition*实现。

Condition接口的实现提供了一个matches（…）方法，该方法返回true或false。例如，以下清单显示了用于@Profile的实际Condition实现：

```java
@Override
public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    // Read the @Profile annotation attributes
    MultiValueMap<String, Object> attrs = metadata.getAllAnnotationAttributes(Profile.class.getName());
    if (attrs != null) {
        for (Object value : attrs.get("value")) {
            if (context.getEnvironment().acceptsProfiles(((String[]) value))) {
                return true;
            }
        }
        return false;
    }
    return true;
}
```

有关更多详细信息，请参见[`@Conditional`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/context/annotation/Conditional.html) javadoc。

#### 结合Java和XML配置

Spring的@Configuration类支持并非旨在100％完全替代Spring XML。某些工具（例如Spring XML名称空间）仍然是配置容器的理想方法。在使用XML方便或有必要的情况下，你可以选择：使用“ClassPathXmlApplicationContext”以“XML中心”方式实例化容器，或通过使用AnnotationConfigApplicationContext和“ Java中心”方式实例化容器。 
@ImportResource注解可根据需要导入XML。

**以XML为中心的@Configuration类的使用**

最好从XML引导Spring容器并以即席方式(ad-hoc fashion)包含@Configuration类。例如，在使用Spring XML的大型现有代码库中，根据需要创建@Configuration类并从现有XML文件中将它们包含在内会变得更加容易。在本节的后面，我们将介绍在这种“以XML为中心”的情况下使用@Configuration类的选项。

**将@Configuration类声明为纯Spring \<bean />元素**

请记住，@Configuration类最终是容器中的bean定义。在本系列示例中，我们创建一个名为AppConfig的@Configuration类，并将其作为\<bean/>定义包含在system-test-config.xml中。因为<context：annotation-config />已打开，所以容器可以识别@Configuration注解并正确处理AppConfig中声明的@Bean方法。

以下示例显示了Java中的普通配置类：

```java
@Configuration
public class AppConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public AccountRepository accountRepository() {
        return new JdbcAccountRepository(dataSource);
    }

    @Bean
    public TransferService transferService() {
        return new TransferService(accountRepository());
    }
}
```

以下示例显示了样本system-test-config.xml文件的一部分：

```xml
<beans>
    <!-- enable processing of annotations such as @Autowired and @Configuration -->
    <context:annotation-config/>
    <context:property-placeholder location="classpath:/com/acme/jdbc.properties"/>

    <bean class="com.acme.AppConfig"/>

    <bean class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="url" value="${jdbc.url}"/>
        <property name="username" value="${jdbc.username}"/>
        <property name="password" value="${jdbc.password}"/>
    </bean>
</beans>
```

以下示例显示了可能的jdbc.properties文件：

```
jdbc.url=jdbc:hsqldb:hsql://localhost/xdb
jdbc.username=sa
jdbc.password=
```

```java
public static void main(String[] args) {
    ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:/com/acme/system-test-config.xml");
    TransferService transferService = ctx.getBean(TransferService.class);
    // ...
}
```

在system-test-config.xml文件中，AppConfig \<bean />没有声明id元素。尽管这样做是可以接受的，但由于没有其他bean引用过它，因此这是不必要的，并且不太可能通过名称从容器中显式获取。同样，DataSource bean只能按类型自动装配，因此也不严格要求显式bean id。

**使用<context：component-scan />来选择@Configuration类**

因为@Configuration用@Component进行元注解，所以@Configuration注解的类自动成为组件扫描的候选对象。使用与先前示例中描述的场景相同的场景，我们可以重新定义system-test-config.xml以利用组件扫描的优势。请注意，在这种情况下，我们无需显式声明<context：annotation-config/>，因为<context：component-scan />可启用相同的功能。

以下示例显示了修改后的system-test-config.xml文件：

```xml
<beans>
    <!-- picks up and registers AppConfig as a bean definition -->
    <context:component-scan base-package="com.acme"/>
    <context:property-placeholder location="classpath:/com/acme/jdbc.properties"/>

    <bean class="org.springframework.jdbc.datasource.DriverManagerDataSource">
        <property name="url" value="${jdbc.url}"/>
        <property name="username" value="${jdbc.username}"/>
        <property name="password" value="${jdbc.password}"/>
    </bean>
</beans>
```

**@Configuration以类为中心的XML与@ImportResource的结合使用**

在@Configuration类是配置容器的主要机制的应用程序中，仍然有必要至少使用一些XML。在这些情况下，你可以使用@ImportResource并仅定义所需的XML。这样做实现了一种“以Java为中心”的方法来配置容器，并将XML保持在最低限度。以下示例（包括配置类，定义Bean的XML文件，属性文件和主类）显示了如何使用@ImportResource注解来实现按需使用XML的以Java为中心的配置：

```java
@Configuration
@ImportResource("classpath:/com/acme/properties-config.xml")
public class AppConfig {

    @Value("${jdbc.url}")
    private String url;

    @Value("${jdbc.username}")
    private String username;

    @Value("${jdbc.password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        return new DriverManagerDataSource(url, username, password);
    }
}
```

```xml
properties-config.xml
<beans>
    <context:property-placeholder location="classpath:/com/acme/jdbc.properties"/>
</beans>
```

```
jdbc.properties
jdbc.url=jdbc:hsqldb:hsql://localhost/xdb
jdbc.username=sa
jdbc.password=
```

```java
public static void main(String[] args) {
    ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
    TransferService transferService = ctx.getBean(TransferService.class);
    // ...
}
```

## 环境抽象

Environment接口是集成在容器中的抽象，它对应用程序环境的两个关键方面进行建模：[profiles](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-definition-profiles) 和 [properties](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-property-source-abstraction).

profiles是仅在给定profile处于活动状态时才向容器注册的Bean定义的命名逻辑组。可以将Bean分配给profile，无论是以XML定义还是带有注解。与配置文件相关的环境对象的作用是确定当前哪些配置文件（如果有）处于活动状态，以及默认情况下哪些配置文件（如果有）应处于活动状态。

属性在几乎所有应用程序中都起着重要作用，并且可能源自多种来源：属性文件，JVM系统属性，系统环境变量，JNDI，Servlet上下文参数，临时属性对象，Map对象等。环境对象与属性相关的作用是为用户提供方便的服务界面，用于配置属性源并从中解析属性。

### Bean定义配置文件

Bean定义配置文件在核心容器中提供了一种机制，该机制允许在不同环境中注册不同的Bean。 “环境”一词对不同的用户可能具有不同的含义，并且此功能可以帮助解决许多用例，包括：

- 在开发中针对内存中的数据源进行工作，而不是在进行QA或生产时从JNDI查找相同的数据源。
- 仅在将应用程序部署到性能环境中时注册监视基础结构。
- 为客户A和客户B部署注册bean的自定义实现。

在需要数据源的实际应用中考虑第一个用例。在测试环境中，配置可能类似于以下内容：

```java
@Bean
public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
        .setType(EmbeddedDatabaseType.HSQL)
        .addScript("my-schema.sql")
        .addScript("my-test-data.sql")
        .build();
}
```

现在，假设该应用程序的数据源已在生产应用程序服务器的JNDI目录中注册，请考虑如何将该应用程序部署到QA或生产环境中。现在，我们的dataSource bean看起来像下面的清单：

```java
@Bean(destroyMethod="")
public DataSource dataSource() throws Exception {
    Context ctx = new InitialContext();
    return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
}
```

问题是如何根据当前环境在使用这两种变体之间进行切换。随着时间的流逝，Spring用户已经设计出多种方法来完成此任务，通常依赖于系统环境变量和包含${placeholder}令牌的XML \<import />语句的组合，这些语句根据值解析为正确的配置文件路径环境变量。 Bean定义配置文件是一个核心容器功能，可提供此问题的解决方案。

如果我们概括前面特定于环境的Bean定义示例中所示的用例，那么最终需要在某些上下文中而不是在其他上下文中注册某些Bean定义。你可能会说你要在情况A中注册一个特定的bean定义配置文件，在情况B中注册一个不同的配置文件。我们首先更新配置以反映这种需求。

#### 使用@Profile

@Profile注解可让你指示一个或多个指定的配置文件处于活动状态时有资格注册的组件。使用前面的示例，我们可以如下重写dataSource配置：

```java
@Configuration
@Profile("development")
public class StandaloneDataConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("classpath:com/bank/config/sql/schema.sql")
            .addScript("classpath:com/bank/config/sql/test-data.sql")
            .build();
    }
}
```

```java
@Configuration
@Profile("production")
public class JndiDataConfig {

    @Bean(destroyMethod="")
    public DataSource dataSource() throws Exception {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
    }
}
```

> 如前所述，对于@Bean方法，通常选择使用程序化JNDI查找，方法是使用Spring的JndiTemplate/JndiLocatorDelegate帮助器或前面显示的直接JNDI InitialContext用法，而不是JndiObjectFactoryBean变体，这将迫使你将返回类型声明为FactoryBean类型。

概要文件字符串可以包含简单的概要文件名称（例如，production）或概要文件表达式。配置文件表达式允许表达更复杂的配置文件逻辑（例如，`production & us-east`）。概要文件表达式中支持以下运算符：

- `!`: A logical “not” of the profile
- `&`: A logical “and” of the profiles
- `|`: A logical “or” of the profiles

> 你不能混用&和|不使用括号的运算符。例如，`production & us-east | eu-central`不是有效的表达式。它必须表示为`production & (us-east | eu-central)`）。

你可以将@Profile用作元注解，以创建自定义的组合注解。以下示例定义了一个自定义@Production注解，你可以将其用作@Profile（“production”）的替代品：

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Profile("production")
public @interface Production {
}
```

>如果@Configuration类用@Profile标记，则除非该类中的一个或多个指定的配置文件处于活动状态，否则所有与该类关联的@Bean方法和@Import注解都会被绕过。如果@Component或@Configuration类标记有@Profile（{“p1”，“p2”}），则除非已激活概要文件'p1'或'p2'，否则不会注册或处理该类。如果给定的配置文件以NOT运算符(!)为前缀，则只有在该配置文件处于非活动状态时，才会注册带注解的元素。例如，给定@Profile（{“p1”，“!p2”}），如果配置文件'p1'处于活动状态或如果配置文件'p2'未处于活动状态，则会进行注册。

也可以在方法级别将@Profile声明为仅包含配置类的一个特定Bean（例如，特定Bean的替代变体），如以下示例所示：

```java
@Configuration
public class AppConfig {

    @Bean("dataSource")
    @Profile("development") 
    public DataSource standaloneDataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("classpath:com/bank/config/sql/schema.sql")
            .addScript("classpath:com/bank/config/sql/test-data.sql")
            .build();
    }

    @Bean("dataSource")
    @Profile("production") 
    public DataSource jndiDataSource() throws Exception {
        Context ctx = new InitialContext();
        return (DataSource) ctx.lookup("java:comp/env/jdbc/datasource");
    }
}
```

>standaloneDataSource方法仅在开发配置文件中可用。
>jndiDataSource方法仅在生产配置文件中可用。

> 在@Bean方法上使用@Profile时，可能会出现特殊情况：如果JavaBean具有相同的Java方法名称（类似于构造函数重载），则必须在所有重载方法上一致声明@Profile条件。如果条件不一致，则仅重载方法中第一个声明。因此，@Profile不能用于选择具有特定参数签名的重载方法。在创建时，同一bean的所有工厂方法之间的解析都遵循Spring的构造函数解析算法。
>
> 如果要使用不同的概要文件条件定义备用bean，请使用@Bean name属性使用不同的Java方法名称来指向相同的bean名称，如前面的示例所示。如果参数签名都相同（例如，所有变体都具有no-arg工厂方法），则这是首先在有效Java类中表示这种排列的唯一方法（因为只能有一个特定名称和参数签名的方法）。

#### XML Bean定义配置文件

XML对应项是\<beans>元素的profile属性。我们前面的示例配置可以用两个XML文件重写，如下所示：

```xml
<beans profile="development"
    xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    xsi:schemaLocation="...">

    <jdbc:embedded-database id="dataSource">
        <jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
        <jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
    </jdbc:embedded-database>
</beans>
```

```xml
<beans profile="production"
    xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jee="http://www.springframework.org/schema/jee"
    xsi:schemaLocation="...">

    <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
</beans>
```

也可以避免在同一文件中拆分和嵌套\<beans />元素，如以下示例所示：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:jdbc="http://www.springframework.org/schema/jdbc"
    xmlns:jee="http://www.springframework.org/schema/jee"
    xsi:schemaLocation="...">

    <!-- other bean definitions -->

    <beans profile="development">
        <jdbc:embedded-database id="dataSource">
            <jdbc:script location="classpath:com/bank/config/sql/schema.sql"/>
            <jdbc:script location="classpath:com/bank/config/sql/test-data.sql"/>
        </jdbc:embedded-database>
    </beans>

    <beans profile="production">
        <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
    </beans>
</beans>
```

spring-bean.xsd已被限制为仅允许这些元素作为文件中的最后一个元素。这应该有助于提供灵活性，而不会引起XML文件混乱。

> XML对应项不支持前面描述的配置文件表达式。但是，可以使用！取消配置文件。也可以通过嵌套配置文件来应用逻辑“和”，如以下示例所示：
>
> ```xml
> <beans xmlns="http://www.springframework.org/schema/beans"
>  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
>  xmlns:jdbc="http://www.springframework.org/schema/jdbc"
>  xmlns:jee="http://www.springframework.org/schema/jee"
>  xsi:schemaLocation="...">
> 
>  <!-- other bean definitions -->
> 
>  <beans profile="production">
>      <beans profile="us-east">
>          <jee:jndi-lookup id="dataSource" jndi-name="java:comp/env/jdbc/datasource"/>
>      </beans>
>  </beans>
> </beans>
> ```
>
> 在前面的示例中，如果生产和用户配置文件都处于活动状态，则将显示dataSource bean。

#### 激活配置文件(Profile)

现在我们已经更新了配置，我们仍然需要指示Spring哪个配置文件处于活动状态。如果立即启动示例应用程序，则会看到抛出NoSuchBeanDefinitionException的消息，因为容器找不到名为dataSource的Spring bean。

可以通过多种方式来激活配置文件，但最直接的方法是针对可通过ApplicationContext获得的Environment API以编程方式进行配置。以下示例显示了如何执行此操作：

```java
AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
ctx.getEnvironment().setActiveProfiles("development");
ctx.register(SomeConfig.class, StandaloneDataConfig.class, JndiDataConfig.class);
ctx.refresh();
```

此外，可以通过spring.profiles.active属性声明性地激活配置文件，该属性可以通过系统环境变量，JVM系统属性，web.xml中的servlet上下文参数或一个entry作为JNDI中的条目来指定（请参阅[`PropertySource` Abstraction](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-property-source-abstraction)）在集成测试中，可以使用spring-test模块中的@ActiveProfiles注解来声明活动配置文件（请参阅[context configuration with environment profiles](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/testing.html#testcontext-ctx-management-env-profiles)）。

请注意，配置文件不是“非此即彼”的命题。你可以一次激活多个配置文件。通过编程，你可以为setActiveProfiles()方法提供多个配置文件名称，该方法接受String ... varargs。以下示例激活多个配置文件：

```java
ctx.getEnvironment().setActiveProfiles("profile1", "profile2");
```

声明性地，spring.profiles.active可以接受以逗号分隔的配置文件名称列表，如以下示例所示：

```
    -Dspring.profiles.active="profile1,profile2"
```

#### 默认配置文件

默认配置文件表示默认情况下启用的配置文件。考虑以下示例：

```java
@Configuration
@Profile("default")
public class DefaultDataConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.HSQL)
            .addScript("classpath:com/bank/config/sql/schema.sql")
            .build();
    }
}
```

如果没有配置文件处于活动状态，那么将创建dataSource。你可以看到这是为一个或多个bean提供默认定义的一种方法。如果启用了任何配置文件，则默认配置文件将不适用。

可以通过在Environment上使用setDefaultProfiles()或声明性地通过使用spring.profiles.default属性来更改默认配置文件的名称。

### PropertySource抽象

Spring的环境抽象提供了可配置属性源层次结构上的搜索操作。考虑以下清单：

```java
ApplicationContext ctx = new GenericApplicationContext();
Environment env = ctx.getEnvironment();
boolean containsMyProperty = env.containsProperty("my-property");
System.out.println("Does my environment contain the 'my-property' property? " + containsMyProperty);
```

在前面的代码片段中，我们看到了一种高级方式来询问Spring是否为当前环境定义了my-property属性。为了回答这个问题，环境对象在一组PropertySource对象上执行搜索。
PropertySource是对任何键值对源的简单抽象，并且Spring的StandardEnvironment配置有两个PropertySource对象-一个代表JVM系统属性的集合（System.getProperties()）和一个代表系统环境变量的集合（System.getenv()）。

>这些默认属性源存在于StandardEnvironment中，可在独立应用程序中使用。 
>StandardServletEnvironment填充了其他默认属性源，包括servlet配置和servlet上下文参数。它可以选择启用JndiPropertySource。有关详细信息，请参见javadoc。

具体来说，当使用StandardEnvironment时，如果在运行时存在my-property系统属性或my-property环境变量，则对env.containsProperty（“my-property”）的调用将返回true。

> 执行的搜索是分层的。默认情况下，系统属性优先于环境变量。因此，如果在调用env.getProperty（“my-property”）时在两个地方都同时设置了my-property属性，则系统属性值“wins”并返回。请注意，属性值不会合并，而是会被前面的条目完全覆盖。
>
> 对于常见的StandardServletEnvironment，完整层次结构如下，最高优先级条目位于顶部：
>
> - ServletConfig参数（如果适用，例如在DispatcherServlet上下文中）
> - ServletContext参数（web.xml上下文参数条目）
> - JNDI环境变量（java：comp/env/条目）
> - JVM系统属性（-D命令行参数）
> - JVM系统环境（操作系统环境变量）

最重要的是，整个机制是可配置的。也许你有一个要集成到此搜索中的自定义属性源。为此，实现并实例化你自己的PropertySource并将其添加到当前环境的PropertySources集中。以下示例显示了如何执行此操作：

```java
ConfigurableApplicationContext ctx = new GenericApplicationContext();
MutablePropertySources sources = ctx.getEnvironment().getPropertySources();
sources.addFirst(new MyPropertySource());
```

在前面的代码中，在搜索中添加了具有最高优先级的MyPropertySource。如果它包含my-property属性，则将检测并返回该属性，以支持任何其他PropertySource中的my-property属性。[`MutablePropertySources`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/core/env/MutablePropertySources.html)API公开了许多方法，这些方法允许对属性源集进行精确操作。

### 使用@PropertySource

@PropertySource注解为将PropertySource添加到Spring的环境中提供了一种方便的声明性机制。

给定一个名为app.properties的文件，其中包含键-值对testbean.name = myTestBean，下面的@Configuration类使用@PropertySource，其方式是对testBean.getName()的调用返回myTestBean：

```java
@Configuration
@PropertySource("classpath:/com/myco/app.properties")
public class AppConfig {

    @Autowired
    Environment env;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setName(env.getProperty("testbean.name"));
        return testBean;
    }
}
```

@PropertySource资源位置中存在的任何$ {…}占位符都是根据已经针对该环境注册的一组属性源来解析的，如以下示例所示：

```java
@Configuration
@PropertySource("classpath:/com/${my.placeholder:default/path}/app.properties")
public class AppConfig {

    @Autowired
    Environment env;

    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setName(env.getProperty("testbean.name"));
        return testBean;
    }
}
```

假设my.placeholder存在于已注册的属性源之一（例如，系统属性或环境变量）中，则占位符将解析为相应的值。如果不是，则将default/path用作默认值。如果未指定默认值并且无法解析属性，则抛出IllegalArgumentException。

>根据Java 8约定，@PropertySource注解是可重复的。但是，所有此类@PropertySource注解都需要在同一级别上声明，可以直接在配置类上声明，也可以在同一自定义注解中声明为元注解。不建议将直接注解和元注解混合使用，因为直接注解会有效地覆盖元注解。

### 声明中的占位符解析

从历史上看，元素中占位符的值只能根据JVM系统属性或环境变量来解析。这已不再是这种情况。由于环境抽象是在整个容器中集成的，因此很容易通过它路由占位符的解析。这意味着你可以按照自己喜欢的任何方式配置解析过程。你可以更改搜索系统属性和环境变量的优先级，也可以完全删除它们。你还可以根据需要将自己的属性源添加到混合中。

具体而言，以下语句无论在何处定义customer属性都有效，只要该属性在环境中可用即可：

```xml
<beans>
    <import resource="com/bank/service/${customer}-config.xml"/>
</beans>
```

## 注册一个LoadTimeWeaver

Spring使用LoadTimeWeaver在将类加载到Java虚拟机（JVM）中时对其进行动态转换。

要启用加载时编织，可以将@EnableLoadTimeWeaving添加到你的@Configuration类之一，如以下示例所示：

```java
@Configuration
@EnableLoadTimeWeaving
public class AppConfig {
}
```

另外，对于XML配置，可以使用context：load-time-weaver元素：

```xml
<beans>
    <context:load-time-weaver/>
</beans>
```

为ApplicationContext配置后，该ApplicationContext中的任何bean都可以实现LoadTimeWeaverAware，从而接收对加载时weaver实例的引用。与[Spring’s JPA support](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/data-access.html#orm-jpa) 结合使用时，该功能特别有用，因为在JPA类转换中可能需要进行加载时编织。有关更多详细信息，请查阅[`LocalContainerEntityManagerFactoryBean`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/orm/jpa/LocalContainerEntityManagerFactoryBean.html) javadoc。有关AspectJ加载时编织的更多信息，请参见 [Load-time Weaving with AspectJ in the Spring Framework](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-aj-ltw).

## ApplicationContext的其他功能

如本章介绍中所讨论的，*org.springframework.beans.factory*包提供了用于管理和操纵bean的基本功能，包括以编程方式。*org.springframework.context*包添加了[`ApplicationContext`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/context/ApplicationContext.html)接口，该接口扩展了BeanFactory接口，此外还扩展了其他接口以提供更多面向应用程序框架的样式的附加功能。许多人以完全声明性的方式使用ApplicationContext，甚至没有以编程方式创建它，而是依靠诸如ContextLoader之类的支持类来自动实例化ApplicationContext作为Java EE Web应用程序正常启动过程的一部分。

为了以更加面向框架的方式增强BeanFactory的功能，上下文包还提供以下功能：

- 通过MessageSource界面访问i18n样式的消息。
- 通过ResourceLoader界面访问资源，例如URL和文件。
- 通过使用ApplicationEventPublisher接口，将事件发布到实现ApplicationListener接口的bean。
- 加载多个（分层）上下文，使每个上下文都通过HierarchicalBeanFactory接口集中在一个特定层上，例如应用程序的Web层。

### 使用MessageSource进行国际化

ApplicationContext接口扩展了一个称为MessageSource的接口，因此提供了国际化（“ i18n”）功能。Spring还提供了HierarchicalMessageSource接口，该接口可以分层解析消息。这些接口一起提供了Spring影响消息解析的基础。这些接口上定义的方法包括：

- *String getMessage(String code,Object [] args,String default,Locale loc)*：用于从MessageSource检索消息的基本方法。如果找不到针对指定语言环境的消息，则使用默认消息。使用标准库提供的MessageFormat功能，传入的所有参数都将成为替换值。
- *String getMessage(String code,Object [] args,Locale loc)*：与先前的方法基本相同，但有一个区别：无法指定默认消息。如果找不到该消息，则抛出NoSuchMessageException。
- *String getMessage(MessageSourceResolvable resolvable,Locale locale)*：前面方法中使用的所有属性也都包装在一个名为MessageSourceResolvable的类中，你可以在此方法中使用该类。

加载ApplicationContext时，它将自动搜索在上下文中定义的MessageSource bean。 
Bean必须具有名称messageSource。如果找到了这样的bean，则对先前方法的所有调用都将委派给消息源。如果找不到消息源，则ApplicationContext尝试查找包含同名bean的父级。如果是这样，它将使用该bean作为MessageSource。如果ApplicationContext找不到任何消息源，则将实例化一个空的DelegatingMessageSource，以便能够接受对上述方法的调用。

Spring提供了两个MessageSource实现，即ResourceBundleMessageSource和StaticMessageSource。两者都实现HierarchicalMessageSource以便进行嵌套消息传递。
StaticMessageSource很少使用，但是提供了将消息添加到源中的编程方式。下面的示例显示ResourceBundleMessageSource：

```xml
<beans>
    <bean id="messageSource"
            class="org.springframework.context.support.ResourceBundleMessageSource">
        <property name="basenames">
            <list>
                <value>format</value>
                <value>exceptions</value>
                <value>windows</value>
            </list>
        </property>
    </bean>
</beans>
```

该示例假设你在类路径中定义了三个资源包，分别称为`format`, `exceptions` 和 `windows`。解析消息的任何请求都通过JDK标准的通过ResourceBundle对象解析消息的方式来处理。就本示例而言，假定上述两个资源束文件的内容如下：

```
   # in format.properties
    message=Alligators rock!
```

```
    # in exceptions.properties
    argument.required=The {0} argument is required.
```

下一个示例显示了执行MessageSource功能的程序。请记住，所有ApplicationContext实现也是MessageSource实现，因此可以转换为MessageSource接口。

```java
public static void main(String[] args) {
    MessageSource resources = new ClassPathXmlApplicationContext("beans.xml");
    String message = resources.getMessage("message", null, "Default", Locale.ENGLISH);
    System.out.println(message);
}
```

The resulting output from the above program is as follows:

```
Alligators rock!
```

总而言之，MessageSource是在名为beans.xml的文件中定义的，该文件位于类路径的根目录下。 messageSource bean定义通过其basenames属性引用了许多资源包。列表中传递给basenames属性的三个文件在类路径的根目录下以文件形式存在，分别称为format.properties，exceptions.properties和windows.properties。

下一个示例显示了传递给消息查找的参数。这些参数将转换为String对象，并插入到查找消息中的占位符中。

```xml
<beans>

    <!-- this MessageSource is being used in a web application -->
    <bean id="messageSource" class="org.springframework.context.support.ResourceBundleMessageSource">
        <property name="basename" value="exceptions"/>
    </bean>

    <!-- lets inject the above MessageSource into this POJO -->
    <bean id="example" class="com.something.Example">
        <property name="messages" ref="messageSource"/>
    </bean>

</beans>
```

```java
public class Example {

    private MessageSource messages;

    public void setMessages(MessageSource messages) {
        this.messages = messages;
    }

    public void execute() {
        String message = this.messages.getMessage("argument.required",
            new Object [] {"userDao"}, "Required", Locale.ENGLISH);
        System.out.println(message);
    }
}
```

调用execute()方法的结果输出如下：

```
The userDao argument is required.
```

关于国际化（“ i18n”），Spring的各种MessageSource实现遵循与标准JDK 
ResourceBundle相同的语言环境解析和后备规则。简而言之，并继续前面定义的示例messageSource，如果要针对英国（en-GB）语言环境解析消息，则可以分别创建名为format_en_GB.properties，exceptions_en_GB.properties和windows_en_GB.properties的文件。

通常，语言环境解析由应用程序的周围环境管理。在以下示例中，手动指定了针对其解析（英国）消息的语言环境：

```
# in exceptions_en_GB.properties
argument.required=Ebagum lad, the ''{0}'' argument is required, I say, required.
```

```java
public static void main(final String[] args) {
    MessageSource resources = new ClassPathXmlApplicationContext("beans.xml");
    String message = resources.getMessage("argument.required",
        new Object [] {"userDao"}, "Required", Locale.UK);
    System.out.println(message);
}
```

运行上述程序的结果输出如下：

```
Ebagum lad, the 'userDao' argument is required, I say, required.
```

你还可以使用MessageSourceAware接口获取对已定义的任何MessageSource的引用。创建和配置bean时，在ApplicationContext中实现MessageSourceAware接口的所有bean都与应用程序上下文的MessageSource一起注入。

> 作为ResourceBundleMessageSource的替代，Spring提供了ReloadableResourceBundleMessageSource类。此变体支持相同的包文件格式，但比基于标准JDK的ResourceBundleMessageSource实现更灵活。特别是，它允许从任何Spring资源位置（不仅从类路径）读取文件，并且支持捆绑属性文件的热重载（同时在它们之间有效地进行缓存）。有关详细信息，请参见[`ReloadableResourceBundleMessageSource`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/context/support/ReloadableResourceBundleMessageSource.html) javadoc。

### 标准和自定义事件

通过ApplicationEvent类和ApplicationListener接口提供ApplicationContext中的事件处理。如果将实现ApplicationListener接口的bean部署到上下文中，则每次将ApplicationEvent发布到ApplicationContext时，都会通知该bean。本质上，这是标准的Observer设计模式。

> 从Spring 4.2开始，事件基础结构得到了显着改进，并提供了 [annotation-based model](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#context-functionality-events-annotation) 以及发布任意事件（即不一定从ApplicationEvent扩展的对象）的功能。发布此类对象后，我们会为你包装一个事件。

下表描述了Spring提供的标准事件：

| Event                        | Explanation                                                  |
| ---------------------------- | ------------------------------------------------------------ |
| `ContextRefreshedEvent`      | Published when the `ApplicationContext` is initialized or refreshed (for example, by using the `refresh()` method on the `ConfigurableApplicationContext` interface). Here, “initialized” means that all beans are loaded, post-processor beans are detected and activated, singletons are pre-instantiated, and the `ApplicationContext` object is ready for use. As long as the context has not been closed, a refresh can be triggered multiple times, provided that the chosen `ApplicationContext` actually supports such “hot” refreshes. For example, `XmlWebApplicationContext` supports hot refreshes, but `GenericApplicationContext` does not. |
| `ContextStartedEvent`        | Published when the `ApplicationContext` is started by using the `start()` method on the `ConfigurableApplicationContext` interface. Here, “started” means that all `Lifecycle` beans receive an explicit start signal. Typically, this signal is used to restart beans after an explicit stop, but it may also be used to start components that have not been configured for autostart (for example, components that have not already started on initialization). |
| `ContextStoppedEvent`        | Published when the `ApplicationContext` is stopped by using the `stop()` method on the `ConfigurableApplicationContext` interface. Here, “stopped” means that all `Lifecycle` beans receive an explicit stop signal. A stopped context may be restarted through a `start()` call. |
| `ContextClosedEvent`         | Published when the `ApplicationContext` is being closed by using the `close()` method on the `ConfigurableApplicationContext` interface or via a JVM shutdown hook. Here, "closed" means that all singleton beans will be destroyed. Once the context is closed, it reaches its end of life and cannot be refreshed or restarted. |
| `RequestHandledEvent`        | A web-specific event telling all beans that an HTTP request has been serviced. This event is published after the request is complete. This event is only applicable to web applications that use Spring’s `DispatcherServlet`. |
| `ServletRequestHandledEvent` | A subclass of `RequestHandledEvent` that adds Servlet-specific context information. |

可以创建和发布自己的自定义事件。以下示例显示了一个简单的类，该类扩展了Spring的ApplicationEvent基类：

```java
public class BlackListEvent extends ApplicationEvent {

    private final String address;
    private final String content;

    public BlackListEvent(Object source, String address, String content) {
        super(source);
        this.address = address;
        this.content = content;
    }

    // accessor and other methods...
}
```

若要发布自定义ApplicationEvent，请在ApplicationEventPublisher上调用publishEvent()方法。通常，这是通过创建一个实现ApplicationEventPublisherAware的类并将其注册为Spring Bean来完成的。以下示例显示了此类：

```java
public class EmailService implements ApplicationEventPublisherAware {

    private List<String> blackList;
    private ApplicationEventPublisher publisher;

    public void setBlackList(List<String> blackList) {
        this.blackList = blackList;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void sendEmail(String address, String content) {
        if (blackList.contains(address)) {
            publisher.publishEvent(new BlackListEvent(this, address, content));
            return;
        }
        // send email...
    }
}
```

在配置时，Spring容器检测到EmailService实现了ApplicationEventPublisherAware并自动调用setApplicationEventPublisher()。实际上，传入的参数是Spring容器本身。你正在通过其ApplicationEventPublisher接口与应用程序上下文进行交互。

要接收自定义ApplicationEvent，可以创建一个实现ApplicationListener的类并将其注册为Spring Bean。以下示例显示了此类：

```java
public class BlackListNotifier implements ApplicationListener<BlackListEvent> {

    private String notificationAddress;

    public void setNotificationAddress(String notificationAddress) {
        this.notificationAddress = notificationAddress;
    }

    public void onApplicationEvent(BlackListEvent event) {
        // notify appropriate parties via notificationAddress...
    }
}
```

请注意，ApplicationListener通常使用你的自定义事件的类型（上一示例中的BlackListEvent）进行参数化。这意味着onApplicationEvent()方法可以保持类型安全，从而避免了任何向下转换的需求。你可以根据需要注册任意数量的事件监听器，但是请注意，默认情况下，事件侦听器会同步接收事件。这意味着publishEvent()方法将阻塞，直到所有监听器都已完成对事件的处理为止。这种同步和单线程方法的一个优点是，当监听器接收到事件时，如果有可用的事务上下文，它将在发布者的事务上下文内部进行操作。如果有必要采用其他发布事件的策略，请参阅Spring的[`ApplicationEventMulticaster`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/context/event/ApplicationEventMulticaster.html)接口的javadoc和[`SimpleApplicationEventMulticaster`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/context/event/SimpleApplicationEventMulticaster.html)实现。

以下示例显示了用于注册和配置上述每个类的Bean定义：

```xml
<bean id="emailService" class="example.EmailService">
    <property name="blackList">
        <list>
            <value>known.spammer@example.org</value>
            <value>known.hacker@example.org</value>
            <value>john.doe@example.org</value>
        </list>
    </property>
</bean>

<bean id="blackListNotifier" class="example.BlackListNotifier">
    <property name="notificationAddress" value="blacklist@example.org"/>
</bean>
```

总而言之，当调用emailService bean的sendEmail()方法时，如果有任何电子邮件应列入黑名单，则将发布BlackListEvent类型的自定义事件。 blackListNotifier bean被注册为ApplicationListener并接收BlackListEvent，这时它可以通知适当的参与者。

>Spring的事件机制旨在在同一应用程序上下文内在Spring bean之间进行简单的通信。但是，对于更复杂的企业集成需求，单独维护的[Spring Integration](https://projects.spring.io/spring-integration/)项目为基于著名的Spring编程模型构建轻量级，[面向模式](https://www.enterpriseintegrationpatterns.com/)，事件驱动的架构提供了完整的支持。

#### 基于注解的事件监听器

从Spring 4.2开始，你可以使用@EventListener注解在托管Bean的任何公共方法上注册事件侦听器。 BlackListNotifier可以重写如下：

```java
public class BlackListNotifier {

    private String notificationAddress;

    public void setNotificationAddress(String notificationAddress) {
        this.notificationAddress = notificationAddress;
    }

    @EventListener
    public void processBlackListEvent(BlackListEvent event) {
        // notify appropriate parties via notificationAddress...
    }
}
```

方法签名再次声明其监听的事件类型，但是这次使用灵活的名称，并且没有实现特定的监听器接口。只要实际事件类型在其实现层次结构中解析你的通用参数，也可以通过通用类型来缩小事件类型。

如果你的方法应该监听多个事件，或者你要完全不使用任何参数来定义它，则事件类型也可以在注解本身上指定。以下示例显示了如何执行此操作：

```java
@EventListener({ContextStartedEvent.class, ContextRefreshedEvent.class})
public void handleContextStart() {
    // ...
}
```

还可以通过使用定义[`SpEL` expression](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#expressions)的注解的condition属性来添加其他运行时过滤，该注解应匹配以针对特定事件实际调用该方法。

以下示例显示了仅当事件的content属性等于my-event时，才可以重写我们的通知程序以进行调用：

```java
@EventListener(condition = "#blEvent.content == 'my-event'")
public void processBlackListEvent(BlackListEvent blEvent) {
    // notify appropriate parties via notificationAddress...
}
```

每个SpEL表达式都会根据专用上下文进行评估。下表列出了可用于上下文的项目，以便可以将它们用于条件事件处理：

| Name            | Location           | Description                                                  | Example                                                      |
| --------------- | ------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| Event           | root object        | The actual `ApplicationEvent`.                               | `#root.event` or `event`                                     |
| Arguments array | root object        | The arguments (as an object array) used to invoke the method. | `#root.args` or `args`; `args[0]` to access the first argument, etc. |
| *Argument name* | evaluation context | The name of any of the method arguments. If, for some reason, the names are not available (for example, because there is no debug information in the compiled byte code), individual arguments are also available using the `#a<#arg>` syntax where `<#arg>` stands for the argument index (starting from 0). | `#blEvent` or `#a0` (you can also use `#p0` or `#p<#arg>` parameter notation as an alias) |

如果由于处理另一个事件而需要发布一个事件，则可以更改方法签名以返回应发布的事件，如以下示例所示：

```java
@EventListener
public ListUpdateEvent handleBlackListEvent(BlackListEvent event) {
    // notify appropriate parties via notificationAddress and
    // then publish a ListUpdateEvent...
}
```

> [asynchronous listeners](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#context-functionality-events-async)不支持此功能。

此新方法为上述方法处理的每个BlackListEvent发布一个新的ListUpdateEvent。如果你需要发布多个事件，则可以返回事件的Collection。

#### 异步监听器

如果希望特定的侦听器异步处理事件，则可以重用[regular `@Async` support](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/integration.html#scheduling-annotation-support-async).。以下示例显示了如何执行此操作：

```java
@EventListener
@Async
public void processBlackListEvent(BlackListEvent event) {
    // BlackListEvent is processed in a separate thread
}
```

使用异步事件时，请注意以下限制：

- 如果异步事件监听器引发Exception，则不会将其传播到调用方。有关更多详细信息，请参见`AsyncUncaughtExceptionHandler`
- 异步事件监听器方法无法通过返回值来发布后续事件。如果你需要发布另一个事件作为处理的结果，请注入[`ApplicationEventPublisher`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/aop/interceptor/AsyncUncaughtExceptionHandler.html)以手动发布事件。

#### Ordering Listeners

如果需要先调用一个监听器，则可以将@Order注解添加到方法声明中，如以下示例所示：

```java
@EventListener
@Order(42)
public void processBlackListEvent(BlackListEvent event) {
    // notify appropriate parties via notificationAddress...
}
```

#### 泛型事件(Generic Events)

还可以使用泛型来进一步定义事件的结构。考虑使用EntityCreatedEvent \<T>，其中T是已创建的实际实体的类型。例如，你可以创建以下侦听器定义以仅接收Person的EntityCreatedEvent：

```java
@EventListener
public void onPersonCreated(EntityCreatedEvent<Person> event) {
    // ...
}
```

由于类型擦除，只有在触发的事件解析了事件监听器所依据的通用参数（即类似PersonCreatedEvent的类扩展EntityCreatedEvent \<Person> {…}）时，此方法才起作用。

在某些情况下，如果所有事件都遵循相同的结构，这可能会变得很乏味（就像前面示例中的事件一样）。在这种情况下，可以实现ResolvableTypeProvider来指导框架超出运行时环境提供的范围。以下事件显示了如何执行此操作：

```java
public class EntityCreatedEvent<T> extends ApplicationEvent implements ResolvableTypeProvider {

    public EntityCreatedEvent(T entity) {
        super(entity);
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(getSource()));
    }
}
```

> 这不仅适用于ApplicationEvent，而且适用于你作为事件发送的任何任意对象。

### 方便地访问低级资源

为了获得最佳用法和对应用程序上下文的理解，你应该熟悉Spring的Resource抽象，如 [Resources](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources)所述。

应用程序上下文是ResourceLoader，可用于加载Resource对象。 Resource本质上是JDK 
*java.net.URL*类的功能更丰富的版本。实际上，资源的实现在适当的地方包装了*java.net.URL*的实例。资源可以以透明的方式从几乎任何位置获取低级资源，包括从类路径，文件系统位置，可使用标准URL描述的任何位置以及一些其他变体。如果资源位置字符串是没有任何特殊前缀的简单路径，则这些资源的来源是特定的，并且适合于实际的应用程序上下文类型。

你可以配置部署到应用程序上下文中的bean，以实现特殊的回调接口ResourceLoaderAware，以便在初始化时自动调用，并将应用程序上下文本身作为ResourceLoader传入。还可以公开Resource类型的属性，以用于访问静态资源。它们像其他任何属性一样注入其中。你可以将那些Resource属性指定为简单的String路径，并在部署bean时依靠从这些文本字符串到实际Resource对象的自动转换。

提供给ApplicationContext构造函数的一个或多个位置路径实际上是资源字符串，并且根据特定的上下文实现以简单的形式对其进行适当处理。例如，ClassPathXmlApplicationContext将简单的位置路径视为类路径位置。你也可以使用带有特殊前缀的位置路径（资源字符串）来强制从类路径或URL中加载定义，而不管实际的上下文类型如何。

### Web应用程序的便捷ApplicationContext实例化

你可以使用例如ContextLoader声明性地创建ApplicationContext实例。当然，你还可以使用ApplicationContext实现之一以编程方式创建ApplicationContext实例。

你可以使用ContextLoaderListener注册ApplicationContext，如以下示例所示：

```xml
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>/WEB-INF/daoContext.xml /WEB-INF/applicationContext.xml</param-value>
</context-param>

<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
```

监听器检查contextConfigLocation参数。如果参数不存在，那么监听器将使用/WEB-INF/applicationContext.xml作为默认值。当参数确实存在时，监听器将使用预定义的定界符（逗号，分号和空格）来分隔String，并将这些值用作搜索应用程序上下文的位置。还支持Ant风格的路径模式。示例包括/WEB-INF/\*Context.xml（适用于所有名称以Context.xml结尾且位于WEB-INF目录中的文件）和/WEB-INF/**/\*Context.xml（适用于所有此类文件）文件在WEB-INF的任何子目录中）。

### 将Spring ApplicationContext部署为Java EE RAR文件

可以将Spring ApplicationContext部署为RAR文件，并将上下文及其所有必需的bean类和库JAR封装在Java EE RAR部署单元中。这等效于引导独立的ApplicationContext（仅托管在Java EE环境中）能够访问Java EE服务器功能。 RAR部署是部署无头WAR文件的方案的一种更自然的选择实际上，这种WAR文件没有任何HTTP入口点，仅用于在Java 
EE环境中引导Spring ApplicationContext。

对于不需要HTTP入口点而仅由消息端点和计划的任务组成的应用程序上下文，RAR部署是理想的选择。在这样的上下文中，Bean可以使用应用程序服务器资源，例如JTA事务管理器和JNDI绑定的JDBC DataSource实例以及JMS ConnectionFactory实例，并且还可以在平台的JMX服务器上注册全部通过Spring的标准事务管理以及JNDI和JMX支持工具。应用程序组件还可以通过Spring的TaskExecutor抽象与应用程序服务器的JCA
WorkManager进行交互。

有关RAR部署中涉及的配置详细信息，请参见[`SpringContextResourceAdapter`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/jca/context/SpringContextResourceAdapter.html)类的javadoc。

对于将Spring ApplicationContext作为Java EE RAR文件的简单部署：

- 将所有应用程序类打包到RAR文件（这是具有不同文件扩展名的标准JAR文件）中。将所有必需的库JAR添加到RAR归档文件的根目录中。添加一个META-INF/ra.xml部署描述符（如[`SpringContextResourceAdapter`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/jca/context/SpringContextResourceAdapter.html)的javadoc中所示）和相应的Spring XML 
  bean定义文件（通常为META-INF/applicationContext.xml）。
- 将生成的RAR文件拖放到应用程序服务器的部署目录中。

> 此类RAR部署单元通常是独立的。它们不会将组件暴露给外界，甚至不会暴露给同一应用程序的其他模块。与基于RAR的ApplicationContext的交互通常是通过与其他模块共享的JMS目标进行的。例如，基于RAR的ApplicationContext还可以安排一些任务(Jobs)或对文件系统（或类似文件）中的新文件做出反应。如果需要允许来自外部的同步访问，则可以（例如）导出RMI端点，该端点可以由同一台计算机上的其他应用程序模块使用。

## BeanFactory

BeanFactory API为Spring的IoC功能提供了基础。它的特定合同主要用于与Spring的其他部分以及相关的第三方框架集成，并且它的DefaultListableBeanFactory实现是更高级别的GenericApplicationContext容器中的关键委托。

BeanFactory和相关接口（例如BeanFactoryAware，InitializingBean，DisposableBean）是其他框架组件的重要集成点。不需要任何注解，甚至不需要反射，它们可以在容器及其组件之间进行非常有效的交互。应用程序级bean可以使用相同的回调接口，但通常更喜欢通过注解或通过编程配置进行声明式依赖注入。

请注意，核心BeanFactory API级别及其DefaultListableBeanFactory实现不对配置格式或要使用的任何组件注解进行假设。所有这些都是通过扩展（例如XmlBeanDefinitionReader和AutowiredAnnotationBeanPostProcessor）引入的，并以核心元数据表示形式对共享BeanDefinition对象进行操作。这就是使Spring的容器如此灵活和可扩展的本质。

### BeanFactory或ApplicationContext？

本节说明BeanFactory和ApplicationContext容器级别之间的区别以及对引导的影响。

除非有充分的理由，否则应使用ApplicationContext，将GenericApplicationContext及其子类AnnotationConfigApplicationContext作为自定义引导的常见实现。这些是用于所有常见目的的Spring核心容器的主要入口点：加载配置文件，触发类路径扫描，以编程方式注册bean定义和带注解的类，以及（从5.0版本开始）注册功能性bean定义。

因为ApplicationContext包含BeanFactory的所有功能，所以通常建议在普通BeanFactory上使用，除非需要完全控制bean处理的方案。在ApplicationContext（例如GenericApplicationContext实现）中，按照约定（即，按bean名称或按bean类型（尤其是后处理器））检测到几种bean，而普通的DefaultListableBeanFactory则与任何特殊bean无关。

对于许多扩展的容器功能，例如注解处理和AOP代理，BeanPostProcessor扩展点是必不可少的。如果仅使用普通的DefaultListableBeanFactory，则默认情况下不会检测到此类后处理器并将其激活。这种情况可能会造成混淆，因为你的bean配置实际上并没有错。而是在这种情况下，需要通过其他设置完全引导容器。

下表列出了BeanFactory和ApplicationContext接口和实现所提供的功能。

| Feature                                                 | `BeanFactory` | `ApplicationContext` |
| ------------------------------------------------------- | ------------- | -------------------- |
| Bean instantiation/wiring                               | Yes           | Yes                  |
| Integrated lifecycle management                         | No            | Yes                  |
| Automatic `BeanPostProcessor` registration              | No            | Yes                  |
| Automatic `BeanFactoryPostProcessor` registration       | No            | Yes                  |
| Convenient `MessageSource` access (for internalization) | No            | Yes                  |
| Built-in `ApplicationEvent` publication mechanism       | No            | Yes                  |

要向DefaultListableBeanFactory显式注册Bean后处理器，需要以编程方式调用addBeanPostProcessor，如以下示例所示：

```java
DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
// populate the factory with bean definitions

// now register any needed BeanPostProcessor instances
factory.addBeanPostProcessor(new AutowiredAnnotationBeanPostProcessor());
factory.addBeanPostProcessor(new MyBeanPostProcessor());

// now start using the factory
```

要将BeanFactoryPostProcessor应用于普通的DefaultListableBeanFactory，你需要调用其postProcessBeanFactory方法，如以下示例所示：

```java
DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(factory);
reader.loadBeanDefinitions(new FileSystemResource("beans.xml"));

// bring in some property values from a Properties file
PropertySourcesPlaceholderConfigurer cfg = new PropertySourcesPlaceholderConfigurer();
cfg.setLocation(new FileSystemResource("jdbc.properties"));

// now actually do the replacement
cfg.postProcessBeanFactory(factory);
```

在这两种情况下，显式的注册步骤都是不方便的，这就是为什么在Spring支持的应用程序中，各种ApplicationContext变量比普通的DefaultListableBeanFactory更为可取的原因，尤其是在典型企业设置中依赖BeanFactoryPostProcessor和BeanPostProcessor实例来扩展容器功能时。

> AnnotationConfigApplicationContext已注册了所有常见的注解后处理器，并且可以通过配置注解（例如@EnableTransactionManagement）在幕后引入其他处理器。在Spring基于注解的配置模型的抽象级别上，bean后处理器的概念仅是内部容器详细信息。

> 翻译：翻译软件
>
> 校正：靓仔Q
>
> 时间：2020.5.11-2020.5.15