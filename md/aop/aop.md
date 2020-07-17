---
[Aspect Oriented Programming with Spring](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop)
---
---
Aspect Oriented Programming with Spring(Version 5.2.6.RELEASE)

---

面向切面的编程(AOP)通过提供另一种思考程序结构的方式来补充面向对象的编程(OOP)。 OOP中模块化的关键单元是类，而在AOP中模块化是切面。切面使关注点(例如事务管理)的模块化可以跨越多种类型和对象。 (这种关注在AOP文献中通常被称为“跨领域”关注。)

Spring的关键组件之一是AOP框架。尽管Spring IoC容器不依赖于AOP(这意味着你不需要的话可以不使用AOP)，但AOP是对Spring IoC的补充，可以提供非常强大的中间件解决方案。

> Spring AOP 和 AspectJ的切入点
>
> Spring提供了使用基于[schema-based approach](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-schema)或[@AspectJ annotation style](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj)来编写自定义切面的简单而强大的方法。这两种样式都提供了完全类型化的建议，并使用了AspectJ切入点语言，同时仍使用Spring AOP进行编织。
>
> 本章讨论基于架构和基于@AspectJ的AOP支持。[下一章](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-api)将讨论较低级别的AOP支持。

AOP在Spring框架中用于:

- 提供声明式企业服务。此类服务中最重要的是[declarative transaction management](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/data-access.html#transaction-declarative).
- 让用户实现自定义切面，并用AOP补充其对OOP的使用。

> 如果你只对通用声明性服务或其他预包装的声明性中间件服务(例如池)感兴趣，则无需直接使用Spring AOP，并且可以跳过本章的大部分内容。

## AOP概念

首先让我们定义一些主要的AOP概念和术语。这些术语不是特定于Spring的。不幸的是，AOP术语并不是特别直观。但是，如果使用Spring自己的术语，将会更加令人困惑。

- Aspect:涉及多个类别的关注点的模块化。事务管理是企业Java应用程序中横切关注的一个很好的例子。在Spring AOP中，切面是通过使用常规类([schema-based approach](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-schema))或使用@Aspect注解( [@AspectJ style](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj))注解的常规类来实现的。
- Join point:在程序执行过程中的一点，例如方法的执行或异常的处理。在Spring AOP中，连接点始终代表方法的执行。
- Advice:切面在特定的连接点处采取的操作。不同类型的通知包括“around”，“before”和“after”通知。 (Advice类型将在后面讨论。)包括Spring在内的许多AOP框架都将通知建模为拦截器，并在切入点周围维护一系列拦截器。
- Pointcut:与join point匹配的谓词。Advice与Pointcut表达式关联，并在与该切入点匹配的任何连接点处运行(例如，执行具有特定名称的方法)。切入点表达式匹配的切入点的概念是AOP的核心，默认情况下，Spring使用AspectJ作为切入点表达语言。
- Intruduction:代表类型声明其他方法或字段。 Spring AOP允许你向任何通知对象引入新的接口(和相应的实现)。例如，你可以使用Intruduction使Bean实现IsModified接口，以简化缓存。 (在AspectJ社区中，介绍被称为类型间声明。)
- Target object:一个或多个切面通知的对象。也称为“通知对象”。由于Spring AOP是使用运行时代理实现的，因此该对象始终是代理对象。
- AOP proxy:由AOP框架创建的一个对象，用于实现切面合同(建议方法执行等)。在Spring Framework中，AOP代理是JDK动态代理或CGLIB代理。
- Weaving:将切面与其他应用程序类型或对象链接以创建通知的对象。这可以在编译时(例如，使用AspectJ编译器)，加载时或在运行时完成。像其他纯Java AOP框架一样，Spring AOP在运行时执行植入。

Spring AOP包括以下类型的Advice:

- Before advice:在连接点之前运行的通知，但是它不能阻止执行流程继续进行到连接点(除非它引发异常)。
- After returning advice:在连接点正常完成后要运行的通知(例如，如果方法返回而没有引发异常)。
- After throwing advice:如果方法因抛出异常而退出，则执行通知。
- After (finally) advice:无论连接点退出的方式如何(正常或特殊返回)，均应执行通知。
- Around advice:围绕连接点的通知，例如方法调用。这是最有力的建议。环绕通知可以在方法调用之前和之后执行自定义行为。它还负责选择是返回连接点还是通过返回其自身的返回值或引发异常走捷径通知的方法执行。

环绕通知(Around advice)是最通用的通知。由于Spring AOP与AspectJ一样，提供了各种通知类型，因此我们建议你使用功能最弱的通知类型，以实现所需的行为。例如，如果你只需要使用方法的返回值更新缓存，则最好使用最终通知(After returing advice)而不是环绕通知(Around advice)，尽管环绕通知可以完成相同的事情。使用最具体的通知类型可提供更简单的编程模型，并减少出错的可能性。例如，你不需要在用于环绕通知的JoinPoint上调用proce()方法，因此，你不会失败。

所有通知参数都是静态类型的，因此你可以使用适当类型(例如，从方法执行返回的值的类型)而不是对象数组的通知参数。

切入点匹配的连接点的概念是AOP的关键，它与仅提供拦截功能的旧技术有所不同。切入点使通知的目标独立于面向对象的层次结构。例如，你可以将提供声明性事务管理的环绕通知应用于跨越多个对象(例如服务层中的所有业务操作)的一组方法。

## Spring AOP能力和目标

Spring AOP是用纯Java实现的。不需要特殊的编译过程。 Spring AOP不需要控制类加载器的层次结构，因此适合在Servlet容器或应用程序服务器中使用。

Spring AOP当前仅支持方法执行连接点(建议在Spring Bean上执行方法)。尽管可以在不破坏核心Spring AOP API的情况下添加对字段拦截的支持，但并未实现字段拦截。如果需要字段访问和更新连接点通知，请考虑使用诸如AspectJ之类的语言。

Spring AOP的AOP方法不同于大多数其他AOP框架。目的不是提供最完整的AOP实现(尽管Spring AOP相当强大)。相反，其目的是在AOP实现和Spring IoC之间提供紧密的集成，以帮助解决企业应用程序中的常见问题。

因此，例如，通常将Spring 框架的AOP功能与Spring IoC容器结合使用。通过使用常规bean定义语法来配置切面(尽管这允许强大的“自动代理”功能)。这是与其他AOP实现的关键区别。使用Spring AOP不能轻松或高效地完成某些事情，例如通知非常细粒度的对象(通常是域对象)。在这种情况下，AspectJ是最佳选择。但是，我们的经验是，Spring AOP可以为企业Java应用程序中的大多数问题提供出色的解决方案。

Spring AOP从未努力与AspectJ竞争以提供全面的AOP解决方案。我们认为，基于代理的框架(如Spring AOP)和成熟的框架(如AspectJ)都是有价值的，它们是互补的，而不是竞争。 Spring无缝地将Spring AOP和IoC与AspectJ集成在一起，以在基于Spring的一致应用程序架构中支持AOP的所有使用。这种集成不会影响Spring AOP API或AOP Alliance API。 Spring AOP仍然向后兼容。请参阅[下一章](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-api )，以讨论Spring AOP API。

> Spring框架的宗旨之一是非侵入性。这是一个想法，你不应被迫将特定于框架的类和接口引入业务或域模型。但是，在某些地方，Spring框架确实为你提供了将特定于Spring 的依赖项引入代码库的选项。提供此类选项的理由是，在某些情况下，以这种方式阅读或编码某些特定功能可能会变得更加容易。但是，Spring框架(几乎)总是为你提供选择:你可以自由地就哪个选项最适合你的特定用例或场景做出明智的决定。
>
> 与本章相关的一种选择是选择哪种AOP框架(以及哪种AOP样式)。你可以选择AspectJ或Spring AOP。你也可以选择@AspectJ注解样式方法或Spring XML配置样式方法。本章选择首先介绍@AspectJ风格的方法不应被视为Spring团队比起Spring XML配置风格更喜欢@AspectJ注解风格。
>
> 有关每种样式的“为什么”的更完整讨论，请参见[Choosing which AOP Declaration Style to Use](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-choosing)

## AOP代理

Spring AOP默认将标准JDK动态代理用于AOP代理。这使得可以代理任何接口(或一组接口)。

Spring AOP也可以使用CGLIB代理。这对于代理类而不是接口是必需的。默认情况下，如果业务对象未实现接口，则使用CGLIB。由于对接口而不是对类进行编程是一种好习惯，因此业务类通常实现一个或多个业务接口。在那些需要通知未在接口上声明的方法或需要将代理对象作为具体类型传递给方法的情况下(极少数情况)，可以[强制使用CGLIB](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-proxying )。

掌握Spring AOP是基于代理的这一事实很重要。请参阅[Understanding AOP Proxies](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-understanding-aop-proxies)以全面了解此实现细节的实际含义。

## @AspectJ 支持

@AspectJ是一种将切面声明为带有注解的常规Java类的样式。 @AspectJ样式是[AspectJ project](https://www.eclipse.org/aspectj)在AspectJ 5版本中引入的。 Spring使用AspectJ提供的用于切入点解析和匹配的库来解释与AspectJ 5相同的注解。但是，AOP运行时仍然是纯Spring AOP，并且不依赖于AspectJ编译器或编织器。

> 使用AspectJ编译器和编织器可以使用完整的AspectJ语言，有关内容在[Using AspectJ with Spring Applications](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-using-aspectj)进行了讨论。

### 启用@AspectJ支持

要在Spring配置中使用@AspectJ切面，你需要启用Spring基于@AspectJ切面配置Spring AOP的支持，并基于这些切面通知是否对Bean进行自动代理。通过自动代理，我们的意思是，如果Spring确定一个或多个切面通知一个bean，它会自动为该bean生成一个代理来拦截方法调用并确保按需执行通知。

可以使用XML或Java样式的配置来启用@AspectJ支持。无论哪种情况，你都需要确保AspectJ的Aspectjweaver.jar库位于应用程序的类路径(版本1.8或更高版本)上。该库在AspectJ发行版的lib目录或Maven中央仓库中可用。

#### 通过Java配置启用@AspectJ支持

要通过Java @Configuration启用@AspectJ支持，请添加@EnableAspectJAutoProxy注解，如以下示例所示:

```java
@Configuration
@EnableAspectJAutoProxy
public class AppConfig {

}
```

#### 通过XML配置启用@AspectJ支持

要通过基于XML的配置启用@AspectJ支持，请使用aop:aspectj-autoproxy元素，如以下示例所示:

```xml
<aop:aspectj-autoproxy/>
```

假定你使用[XML Schema-based configuration](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#xsd-schemas)中所述的架构支持。有关如何在aop名称空间中导入标签的信息，请参见 [the AOP schema](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#xsd-schemas-aop)。

### 声明一个切面

启用@AspectJ支持后，Spring会自动检测应用程序上下文中使用@AspectJ声明(具有@Aspect注解)的任何bean，并用于配置Spring AOP。接下来的两个示例显示了一个不太有用的切面所需的最小定义。

两个示例中的第一个示例显示了应用程序上下文中的常规bean定义，该定义指向具有@Aspect注解的bean类:

```xml
<bean id="myAspect" class="org.xyz.NotVeryUsefulAspect">
    <!-- configure properties of the aspect here -->
</bean>
```

这两个示例中的第二个示例显示了NotVeryUsefulAspect类定义，该类定义使用*org.aspectj.lang.annotation.Aspect*注解进行注解；

```java
package org.xyz;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class NotVeryUsefulAspect {

}
```

切面(使用@Aspect注解的类)可以具有方法和字段，与任何其他类相同。它们还可以包含切入点，通知和介绍(类型间)声明。

> 通过组件扫描自动检测切面
>
> 你可以将切面类注册为Spring XML配置中的常规bean，也可以通过类路径扫描自动检测它们——与其他任何Spring管理的bean一样。但是，请注意，@Aspect注解不足以在类路径中进行自动检测。为此，你需要添加一个单独的@Component注解(或者，按照Spring的组件扫描程序的规则，有条件的自定义构造型注解)。

> 向其他切面提供通知？
>
> 在Spring AOP中，切面本身不能成为其他切面的建议目标。类上的@Aspect注解将其标记为一个切面，因此将其从自动代理中排除。

### 声明切入点

切入点确定了感兴趣的连接点，从而使我们能够控制何时执行通知。 Spring AOP仅支持Spring Bean的方法执行连接点，因此你可以将切入点视为与Spring Bean上的方法执行匹配。切入点声明由两部分组成:一个包含名称和任何参数的签名，以及一个切入点表达式，该切入点表达式精确确定我们感兴趣的方法执行。在AOP的@AspectJ注解样式中，常规方法定义提供了切入点签名。使用@Pointcut注解指示切入点表达式(用作切入点签名的方法必须具有void返回类型)。

一个示例可能有助于使切入点签名和切入点表达式之间的区别变得清晰。下面的示例定义一个名为anyOldTransfer的切入点，该切入点与任何名为transfer的方法的执行相匹配:

```java
@Pointcut("execution(* transfer(..))") // the pointcut expression
private void anyOldTransfer() {} // the pointcut signature
```

形成@Pointcut注解的值的切入点表达式是一个常规的AspectJ 5切入点表达式。有关AspectJ的切入点语言的完整讨论，请参见 [AspectJ Programming Guide](https://www.eclipse.org/aspectj/doc/released/progguide/index.html)(以及扩展，包括[AspectJ 5 Developer’s Notebook](https://www.eclipse.org/aspectj/doc/released/adk15notebook/index.html))或有关AspectJ的书籍之一(如Colyer等人的《Eclipse AspectJ》，或由Ramnivas Laddad撰写的《 AspectJ in Action》)。

#### 支持的切入点指示符

Spring AOP支持以下在切入点表达式中使用的AspectJ切入点指示符(PCD):

- execution:用于匹配方法执行的连接点。这是使用Spring AOP时要使用的主要切入点指示符。
- within:将匹配限制为某些类型内的连接点(使用Spring AOP时，在匹配类型内声明的方法的执行)。
- this:限制匹配到连接点(使用Spring AOP时方法的执行)，其中bean引用(Spring AOP代理)是给定类型的实例。
- target:限制匹配到连接点(使用Spring AOP时方法的执行)，其中目标对象(代理的应用程序对象)是给定类型的实例。
- args:将匹配限制为连接点(使用Spring AOP时方法的执行)，其中参数是给定类型的实例。
- @target:限制匹配到连接点(使用Spring AOP时方法的执行)的匹配，其中执行对象的类具有给定类型的注解。
- @args:限制匹配的连接点(使用Spring AOP时方法的执行)，其中传递的实际参数的运行时类型具有给定类型的注解。
- @within:限制匹配到具有给定注解的类型内的连接点(使用Spring AOP时，使用给定注解的类型中声明的方法的执行)。
- @annotation:将匹配限制为连接点的主题(在Spring AOP中正在执行的方法)具有给定注解的连接点。

> 其他切入点类型
>
> 完整的AspectJ切入点语言支持Spring不支持的其他切入点指示符:call, get, set, preinitialization, staticinitialization, initialization, handler, adviceexecution, withincode, cflow, cflowbelow, if, @this, and @withincode。在Spring AOP解释的切入点表达式中使用这些切入点指示符会导致抛出IllegalArgumentException。
>
> Spring AOP支持的切入点指示符集合可能会在将来的版本中扩展，以支持更多的AspectJ切入点指示符。

由于Spring AOP仅将匹配限制为方法执行连接点，因此前面对切入点指示符的讨论所给出的定义比在AspectJ编程指南中所能找到的要窄。此外，AspectJ本身具有基于类型的语义，并且在执行连接点处，此对象和目标都引用同一个对象:执行该方法的对象。
Spring AOP是基于代理的系统，可区分代理对象本身(绑定到此对象)和代理后面的目标对象(绑定到目标)。

> 由于Spring的AOP框架基于代理的性质，因此根据定义，不会拦截目标对象内的调用。对于JDK代理，只能拦截代理上的公共接口方法调用。使用CGLIB，将拦截代理上的公共方法和受保护的方法调用(必要时甚至包可见的方法)。但是，通常应通过公共签名设计通过代理进行的常见交互。
>
> 请注意，切入点定义通常与任何拦截方法匹配。如果严格地将切入点设置为仅公开使用，即使在CGLIB代理方案中通过代理可能存在非公开交互，也需要相应地进行定义。
>
> 如果你的拦截需要在目标类中包括方法调用甚至构造函数，请考虑使用Spring驱动的 [native AspectJ weaving](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-aj-ltw)，而不是Spring的基于代理的AOP框架。这构成了具有不同特征的AOP使用模式，因此请确保在做出决定之前先熟悉编织。

Spring AOP还支持其他名为bean的PCD。使用PCD，可以将连接点的匹配限制为特定的命名Spring Bean或一组命名Spring Bean(使用通配符时)。 Bean PCD具有以下形式:

```java
bean(idOrNameOfBean)
```

idOrNameOfBean令牌可以是任何Spring bean的名称。提供了使用\*字符的有限通配符支持，因此，如果为Spring bean建立了一些命名约定，则可以编写bean PCD表达式来选择它们。与其他切入点指示符一样，bean PCD可以与&&(和)，||(或)，和！ (否定)运算符一起使用。

> Bean PCD仅在Spring AOP中受支持，而在本机AspectJ编织中不受支持。它是AspectJ定义的标准PCD的特定于Spring的扩展，因此不适用于@Aspect模型中声明的切面。
>
> Bean PCD在实例级别(基于Spring bean名称概念构建)上运行，而不是仅在类型级别(基于编织的AOP受其限制)上运行。基于实例的切入点指示符是Spring基于代理的AOP框架的特殊功能，并且与Spring bean工厂紧密集成，因此可以自然而直接地通过名称识别特定bean。

#### 组合切入点表达式

你可以使用&&、||组合切入点表达式和！，你也可以按名称引用切入点表达式。以下示例显示了三个切入点表达式:

```java
@Pointcut("execution(public * *(..))")
private void anyPublicOperation() {} 

@Pointcut("within(com.xyz.someapp.trading..*)")
private void inTrading() {} 

@Pointcut("anyPublicOperation() && inTrading()")
private void tradingOperation() {} 
```

> `anyPublicOperation` 如果方法执行连接点表示执行，则匹配任何公共方法。
> 
>`inTrading` 如果交易模块中有方法执行，则匹配。
> 
>`tradingOperation` 如果一个方法执行代表该方法中的任何公共方法，则匹配交易模块。

最佳实践是从较小的命名组件中构建更复杂的切入点表达式，如先前所示。按名称引用切入点时，将应用常规的Java可见性规则(你可以看到相同类型的私有切入点，层次结构中受保护的切入点，任何位置的公共切入点，等等)。可见性不影响切入点匹配。

#### 共享通用切入点定义

在使用企业应用程序时，开发人员通常希望从多个切面引用应用程序的模块和特定的操作集。我们建议为此定义一个“ SystemArchitecture”切面，以捕获常见的切入点表达式。这样的切面通常类似于以下示例:

```java
package com.xyz.someapp;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class SystemArchitecture {

    /**
     * A join point is in the web layer if the method is defined
     * in a type in the com.xyz.someapp.web package or any sub-package
     * under that.
     */
    @Pointcut("within(com.xyz.someapp.web..*)")
    public void inWebLayer() {}

    /**
     * A join point is in the service layer if the method is defined
     * in a type in the com.xyz.someapp.service package or any sub-package
     * under that.
     */
    @Pointcut("within(com.xyz.someapp.service..*)")
    public void inServiceLayer() {}

    /**
     * A join point is in the data access layer if the method is defined
     * in a type in the com.xyz.someapp.dao package or any sub-package
     * under that.
     */
    @Pointcut("within(com.xyz.someapp.dao..*)")
    public void inDataAccessLayer() {}

    /**
     * A business service is the execution of any method defined on a service
     * interface. This definition assumes that interfaces are placed in the
     * "service" package, and that implementation types are in sub-packages.
     *
     * If you group service interfaces by functional area (for example,
     * in packages com.xyz.someapp.abc.service and com.xyz.someapp.def.service) then
     * the pointcut expression "execution(* com.xyz.someapp..service.*.*(..))"
     * could be used instead.
     *
     * Alternatively, you can write the expression using the 'bean'
     * PCD, like so "bean(*Service)". (This assumes that you have
     * named your Spring service beans in a consistent fashion.)
     */
    @Pointcut("execution(* com.xyz.someapp..service.*.*(..))")
    public void businessService() {}

    /**
     * A data access operation is the execution of any method defined on a
     * dao interface. This definition assumes that interfaces are placed in the
     * "dao" package, and that implementation types are in sub-packages.
     */
    @Pointcut("execution(* com.xyz.someapp.dao.*.*(..))")
    public void dataAccessOperation() {}

}
```

你可以在需要切入点表达式的任何地方引用在此切面定义的切入点。例如，要使服务层具有事务性，你可以编写以下内容:

```xml
<aop:config>
    <aop:advisor
        pointcut="com.xyz.someapp.SystemArchitecture.businessService()"
        advice-ref="tx-advice"/>
</aop:config>

<tx:advice id="tx-advice">
    <tx:attributes>
        <tx:method name="*" propagation="REQUIRED"/>
    </tx:attributes>
</tx:advice>
```

在[Schema-based AOP Support](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-schema)中讨论了\<aop:config>和\<aop:advisor>元素。 [Transaction Management](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/data-access.html#transaction)中讨论事务元素。

#### 例子

Spring AOP用户可能最常使用执行切入点指示符。执行表达式的格式如下:

```
execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern)
                throws-pattern?)
```

除了返回类型模式(前面的代码片段中的ret-type-pattern)，名称模式和参数模式以外的所有部分都是可选的。返回类型模式确定该方法的返回类型必须是什么才能使连接点匹配。\*最常用作返回类型模式。它匹配任何返回类型。仅当方法返回给定类型时，标准类型名称才匹配。名称模式与方法名称匹配。你可以将\*通配符用作名称模式的全部或一部分。如果你指定了声明类型模式，请在其后加上。将其加入名称模式组件。参数模式稍微复杂一些:()匹配不带参数的方法，而(..)匹配任意数量(零个或多个)的参数。(\*)模式与采用任何类型的一个参数的方法匹配。 (\*，String)与采用两个参数的方法匹配。第一个可以是任何类型，而第二个必须是String。有关更多信息，请查阅AspectJ编程指南的“[Language Semantics](https://www.eclipse.org/aspectj/doc/released/progguide/semantics-pointcuts.html)”部分。

以下示例显示了一些常用的切入点表达式:

- 任何公共方法的执行:

  ```
  execution(public * *(..))
  ```

- 名称以set开头的任何方法的执行:

  ```
  execution(* set*(..))
  ```

- AccountService接口定义的任何方法的执行:

  ```
  execution(* com.xyz.service.AccountService.*(..))
  ```

- service包中定义的任何方法的执行:

  ```
  execution(* com.xyz.service.*.*(..))
  ```

- service包或其子包之一中定义的任何方法的执行:

  ```
  execution(* com.xyz.service..*.*(..))
  ```

- service包中的任何连接点(Join Point)(仅在Spring AOP中执行方法):

  ```
  within(com.xyz.service..*)
  ```

- 实现AccountService代理接口的任何连接点(仅在Spring AOP中方法执行):

  ```
   this(com.xyz.service.AccountService)
  ```
  
  > “this”通常以绑定形式使用。有关如何在通知正文中使代理对象可用的信息，请参阅“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分。
  
- 目标对象实现AccountService接口的任何连接点(仅在Spring AOP中执行方法):

  ```
  target(com.xyz.service.AccountService)
  ```

  > “target”通常以绑定形式使用。有关如何使目标对象在通知正文中可用的信息，请参见“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分。

- 任何采用单个参数并且在运行时传递的参数为Serializable的连接点(仅在Spring AOP中方法执行):

  ```
  args(java.io.Serializable)
  ```

  > “ args”通常以绑定形式使用。有关如何使方法参数在通知正文中可用的信息，请参见“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分。

  请注意，此示例中给出的切入点与execution(* *(java.io.Serializable))不同。如果在运行时传递的参数为Serializable，则args版本匹配；如果方法签名声明单个类型为Serializable的参数，则与execution版本匹配。

- 目标对象具有@Transactional注解的任何连接点(仅在Spring AOP中执行方法):

  ```
  @target(org.springframework.transaction.annotation.Transactional)
  ```

  > 你也可以在绑定形式中使用“ @target”。有关如何使注解对象在通知正文中可用的信息，请参见“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分。

- 目标对象的声明类型具有@Transactional注解的任何连接点(仅在Spring AOP中方法执行):

  ```
  @within(org.springframework.transaction.annotation.Transactional)
  ```

  > 你也可以在绑定形式中使用“ @within”。有关如何使注解对象在通知正文中可用的信息，请参见“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分。

- 任何执行方法带有@Transactional注解的联接点(仅在Spring AOP中执行方法):

  ```
  @annotation(org.springframework.transaction.annotation.Transactional)
  ```

  > 你也可以在绑定形式中使用“ @annotation”。有关如何使注解对象在通知正文中可用的信息，请参见“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分。

- 任何采用单个参数的连接点(仅在Spring AOP中是方法执行)，并且传递的参数的运行时类型具有@Classified注解:

  ```
  @args(com.xyz.security.Classified)
  ```

  > 你也可以在绑定形式中使用“@args”。请参阅“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分，如何使通知对象中的注解对象可用。

- 名为tradeService的Spring bean上的任何连接点(仅在Spring AOP中执行方法):

  ```
  bean(tradeService)
  ```

- Spring Bean上具有与通配符表达式\*Service匹配的名称的任何连接点(仅在Spring AOP中才执行方法):

  ```
  bean(*Service)
  ```

#### 写好切入点

在编译期间，AspectJ处理切入点优化匹配性能。检查代码并确定每个连接点是否(静态或动态)匹配给定的切入点是一个耗费资源的过程。 (动态匹配意味着无法从静态分析中完全确定匹配，并且在代码中进行测试以确定在运行代码时是否存在实际匹配)。首次遇到切入点声明时，AspectJ将其重写为匹配过程的最佳形式。这是什么意思？基本上，切入点以DNF(析取范式)重写，并且对切入点的组件进行排序，以便首先检查那些资源消耗较低的组件。这意味着你不必担心理解各种切入点指示符的性能，并且可以在切入点声明中以任何顺序提供它们。

但是，AspectJ只能使用所告诉的内容。为了获得最佳的匹配性能，你应该考虑他们试图达到的目标，并在定义中尽可能缩小匹配的搜索空间。现有的指示符自然分为三类之一:同类，作用域和上下文:

- 同类指示者选择一种特殊的连接点:`execution`, `get`, `set`, `call`, and `handler`
- 作用域指定者选择一组感兴趣的连接点(可能有多种):`within` and `withincode`
- 上下文指示符根据上下文匹配(并可选地绑定):`this`, `target`, and `@annotation`

编写正确的切入点至少应包括前两种类型(种类和作用域)。你可以包括上下文指示符以根据连接点上下文进行匹配，也可以绑定该上下文以在通知中使用。仅提供同类的标识符或仅提供上下文的标识符是可行的，但是由于额外的处理和分析，可能会影响编织性能(使用的时间和内存)。范围指定符的匹配非常快，使用它们的使用意味着AspectJ可以非常迅速地消除不应进一步处理的连接点组。一个好的切入点应尽可能包括一个切入点。

### 声明Advice

建议与切入点表达式关联，并且在切入点匹配的方法执行之前，之后或周围运行。切入点表达式可以是对命名切入点的简单引用，也可以是就地声明的切入点表达式。

#### Before Advice

你可以使用@Before注解在一个切面中声明前置通知:

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class BeforeExample {

    @Before("com.xyz.myapp.SystemArchitecture.dataAccessOperation()")
    public void doAccessCheck() {
        // ...
    }

}
```

你可以使用@Before注解在一个切面中声明前置通知(before advice):

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class BeforeExample {

    @Before("com.xyz.myapp.SystemArchitecture.dataAccessOperation()")
    public void doAccessCheck() {
        // ...
    }

}
```

如果使用切入点表达式，则可以将前面的示例重写为以下示例:

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class BeforeExample {

    @Before("execution(* com.xyz.myapp.dao.*.*(..))")
    public void doAccessCheck() {
        // ...
    }

}
```

#### After Returning Advice

返回值后，当匹配的方法执行正常返回时运行后置通知。你可以使用@AfterReturning注解进行声明:

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterReturning;

@Aspect
public class AfterReturningExample {
 @AfterReturning("com.xyz.myapp.SystemArchitecture.dataAccessOperation()")
    public void doAccessCheck() {
        // ...
    }

}
```

> 你可以在同一切面内拥有多个通知声明(以及其他成员)。在这些示例中，我们仅显示单个通知声明，以集中每个通知的效果。

有时，你需要在通知正文中访问返回的实际值。你可以使用@AfterReturning的形式绑定返回值以获取该访问，如以下示例所示:

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterReturning;

@Aspect
public class AfterReturningExample {

    @AfterReturning(
        pointcut="com.xyz.myapp.SystemArchitecture.dataAccessOperation()",
        returning="retVal")
    public void doAccessCheck(Object retVal) {
        // ...
    }

}
```

返回属性中使用的名称必须与advice方法中的参数名称相对应。当方法执行返回时，返回值将作为相应的参数值传递到通知方法。返回子句也将匹配限制为仅返回指定类型值的方法执行(在这种情况下为Object，它匹配任何返回值)。

请注意，返回后通知使用时，不可能返回完全不同的引用。

#### After Throwing Advice

抛出异常后通知，当匹配的方法执行抛出异常退出时运行。你可以使用@AfterThrowing注解进行声明，如以下示例所示:

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterThrowing;

@Aspect
public class AfterThrowingExample {

    @AfterThrowing("com.xyz.myapp.SystemArchitecture.dataAccessOperation()")
    public void doRecoveryActions() {
        // ...
    }

}
```

通常，你希望通知仅在引发给定类型的异常时才运行，并且你通常还需要访问通知正文中的引发异常。你可以使用throwing属性来限制匹配(如果需要)(否则，请使用Throwable作为异常类型)，并将抛出的异常绑定到advice参数。以下示例显示了如何执行此操作:

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.AfterThrowing;

@Aspect
public class AfterThrowingExample {

    @AfterThrowing(
        pointcut="com.xyz.myapp.SystemArchitecture.dataAccessOperation()",
        throwing="ex")
    public void doRecoveryActions(DataAccessException ex) {
        // ...
    }

}
```

throwing属性中使用的名称必须与advice方法中的参数名称相对应。当通过抛出异常退出方法执行时，该异常将作为相应的参数值传递给通知方法。throwing子句还将匹配仅限制为抛出指定类型的异常(在这种情况下为DataAccessException)的方法执行。

#### After (Finally) Advice

当匹配的方法执行退出时，后置通知运行。通过使用@After注解声明它。之后必须准备处理正常和异常返回条件的通知。它通常用于释放资源类似的目的。以下示例显示了后置通知的用法:

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.After;

@Aspect
public class AfterFinallyExample {

    @After("com.xyz.myapp.SystemArchitecture.dataAccessOperation()")
    public void doReleaseLock() {
        // ...
    }

}
```

#### Around Advice

最后一种是环绕通知。环绕通知在匹配方法的执行过程中“围绕”运行。它有机会在方法执行之前和之后进行工作，并确定何时，如何或者根本不执行该方法。如果需要以线程安全的方式(例如，启动和停止计时器)在方法执行之前和之后共享状态，则通常使用环绕通知。始终使用最贴合要求的通知形式来满足你(也就是说，可以使用Before Advice时，请勿使用环绕通知)。

通过使用@Around注解来声明环绕通知。咨询方法的第一个参数必须是ProceedingJoinPoint类型。在通知的正文中，在ProceedingJoinPoint上调用proceed()会使基础方法执行。前进方法也可以传入Object[]。数组中的值用作方法执行时的参数。

> 当用Object []进行调用proceed时，proceed的行为与AspectJ编译器所编译的环绕通知的行为略有不同。对于使用传统AspectJ语言编写的环绕通知，传递给procced的参数数量必须与传递给环绕通知的参数数量(而不是基础连接点采用的参数数量)相匹配，并且传递给给定的参数位置会取代该值绑定到的实体的连接点处的原始值。
> Spring采取的方法更简单，并且更适合其基于代理的，仅执行的语义。如果你编译为Spring编写的@AspectJ切面，并在AspectJ编译器和weaver中使用参数进行处理，则只需要意识到这种区别。有一种方法可以在Spring AOP和AspectJ之间100％兼容，并且在[下面有关建议参数的部分](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj-advice-params)中对此进行了讨论。

以下示例显示了如何使用环绕通知:

```java
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.ProceedingJoinPoint;

@Aspect
public class AroundExample {

    @Around("com.xyz.myapp.SystemArchitecture.businessService()")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
        // start stopwatch
        Object retVal = pjp.proceed();
        // stop stopwatch
        return retVal;
    }

}
```

环绕建议返回的值是该方法的调用者看到的返回值。例如，如果一个简单的缓存切面有一个值，则它可以从缓存中返回一个值，如果没有，则调用proceed()。请注意，在环绕通知的正文中，procced可能被调用一次，多次或完全不被调用。所有这些都是合法的。

#### Advice Parameters

Spring提供了完全类型化的通知，这意味着你可以在建议签名中声明所需的参数(如我们先前在返回和抛出示例中所看到的)，而不是一直使用Object[]数组。我们将在本节的后面部分介绍如何使参数和其他上下文值可用于建议主体。首先，我们看一下如何编写通用建议，以了解该建议当前建议的方法。

##### 访问当前的JoinPoint

任何通知方法都可以将*org.aspectj.lang.JoinPoint*类型的参数声明为它的第一个参数(请注意，需要环绕通知以声明ProceedingJoinPoint类型的第一个参数，该类型是JoinPoint的子类。JoinPoint接口提供了一个几种有用的方法:

- getArgs():返回方法参数。
- getThis():返回代理对象。
- getTarget():返回目标对象。
- getSignature():返回建议方法的描述。
- toString():打印有关建议方法的有用描述。

有关更多详细信息，请参见 [javadoc](https://www.eclipse.org/aspectj/doc/released/runtime-api/org/aspectj/lang/JoinPoint.html)。

##### 将参数传递给通知

我们已经看到了如何绑定返回的值或异常值(在返回建议和抛出异常建议之后使用)。要使参数值可用于通知正文，可以使用args的绑定形式。如果在args表达式中使用参数名称代替类型名称，则在调用通知时会将相应参数的值作为参数值传递。一个例子应该使这一点更清楚。假设你要通知以Account对象作为第一个参数的DAO操作的执行，并且你需要在通知正文中访问该帐户。可以编写以下内容:

```java
@Before("com.xyz.myapp.SystemArchitecture.dataAccessOperation() && args(account,..)")
public void validateAccount(Account account) {
    // ...
}
```

切入点表达式的args(account，..)部分有两个用途。首先，它将匹配限制为方法采用至少一个参数且传递给该参数的参数为Account实例的那些方法执行。其次，它通过account参数使通知的实际Account对象可用。

有关更多详细信息，请参见AspectJ编程指南。

代理对象(this)，目标对象(target)和注解(@within，@target，@annotation和@args)都可以以类似的方式绑定。接下来的两个示例显示如何匹配使用@Auditable注解的方法的执行并提取审计代码:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {
    AuditCode value();
}
```

这两个示例中的第二个示例显示了与@Auditable方法的执行相匹配的建议:

```java
@Before("com.xyz.lib.Pointcuts.anyPublicMethod() && @annotation(auditable)")
public void audit(Auditable auditable) {
    AuditCode code = auditable.value();
    // ...
}
```

##### 通知参数和泛型

Spring AOP可以处理类声明和方法参数中使用的泛型。假设具有如下通用类型:

```java
public interface Sample<T> {
    void sampleGenericMethod(T param);
    void sampleGenericCollectionMethod(Collection<T> param);
}
```

你可以通过在要拦截方法的参数类型中键入通知参数，将方法类型的拦截限制为某些参数类型:

```java
@Before("execution(* ..Sample+.sampleGenericMethod(*)) && args(param)")
public void beforeSampleMethod(MyType param) {
    // Advice implementation
}
```

这种方法不适用于通用集合。因此，不能按以下方式定义切入点:

```java
@Before("execution(* ..Sample+.sampleGenericCollectionMethod(*)) && args(param)")
public void beforeSampleMethod(Collection<MyType> param) {
    // Advice implementation
}

```

为了使这项工作有效，我们将不得不检查集合的每个元素，这是不合理的，因为我们也无法决定通常如何处理空值。要实现类似的目的，必须将参数键入Collection <？>并手动检查元素的类型。

##### 确定参数名称

通知调用中的参数绑定依赖于切入点表达式中使用的名称与切入点方法签名中声明的参数名称的匹配。通过Java反射无法获得参数名称，因此Spring AOP使用以下策略来确定参数名称:

- 如果用户已明确指定参数名称，则使用指定的参数名称。通知和切入点注解均具有可选的argNames属性，你可以使用该属性来指定带注解的方法的参数名称。这些参数名称在运行时可用。以下示例显示如何使用argNames属性:

  ```java
  @Before(value="com.xyz.lib.Pointcuts.anyPublicMethod() && target(bean) && @annotation(auditable)",
          argNames="bean,auditable")
  public void audit(Object bean, Auditable auditable) {
      AuditCode code = auditable.value();
      // ... use code and bean
  }
  
  ```

  对JoinPoint，ProceedingJoinPoint和JoinPoint.StaticPart类型的第一个参数给予的特殊处理对于不收集任何其他联接点上下文的建议实例特别方便。在这种情况下，你可以省略argNames属性。例如，以下建议无需声明argNames属性:

  ```java
  @Before("com.xyz.lib.Pointcuts.anyPublicMethod()")
  public void audit(JoinPoint jp) {
      // ... use jp
  }
  ```

  使用'argNames'属性有点笨拙，因此，如果未指定'argNames'属性，Spring AOP将查看该类的调试信息，并尝试从局部变量表中确定参数名称。只要已使用调试信息(至少是“-g:vars”)编译了类，此信息就会存在。启用此标志时进行编译的结果是:(1)你的代码更易于理解(逆向工程)，(2)类文件的大小略大(通常无关紧要)，(3)删除未使用的本地代码的优化变量不适用于你的编译器。换句话说，通过启用该标志，你应该不会遇到任何困难。

  > 如果即使没有调试信息，但是使用了AspectJ(ajc)编译文件，则无需添加argNames属性，因为编译器会保留所需的信息。

- 如果在没有必要调试信息的情况下编译了代码，Spring AOP将尝试推断绑定变量与参数的配对(例如，如果切入点表达式中仅绑定了一个变量，并且建议方法仅接受一个参数，则配对很明显)。如果在给定可用信息的情况下变量的绑定不明确，则抛出AmbiguousBindingException。

- 如果以上所有策略均失败，则抛出IllegalArgumentException。

##### Proceeding with Arguments

前面我们提到过，我们将描述如何编写一个在Spring AOP和AspectJ中始终有效的参数的proceed调用。解决方案是确保通知签名按顺序绑定每个方法参数。以下示例显示了如何执行此操作:

```java
@Around("execution(List<Account> find*(..)) && " +
        "com.xyz.myapp.SystemArchitecture.inDataAccessLayer() && " +
        "args(accountHolderNamePattern)")
public Object preProcessQueryPattern(ProceedingJoinPoint pjp,
        String accountHolderNamePattern) throws Throwable {
    String newPattern = preProcess(accountHolderNamePattern);
    return pjp.proceed(new Object[] {newPattern});
}

```

在许多情况下，无论如何都要进行此绑定(如上例所示)。

#### Advice Ordering

当多条通知都希望在同一连接点上运行时会发生什么？ Spring AOP遵循与AspectJ相同的优先级规则来确定建议执行的顺序。优先级最高的建议首先“在途中”运行(因此，给定两条优先(before)建议，则优先级最高的首先运行)。从连接点“出路”时，优先级最高的通知将最后运行(因此，给定两条后置通知，优先级最高的通知将之后运行)。

当在不同切面定义的两条通知都需要在同一连接点上运行时，除非另行指定，否则执行顺序是不确定的。你可以通过指定优先级来控制执行顺序。通过在切面类中实现*org.springframework.core.Ordered*接口或使用Order注解对其进行注解，可以通过常规的Spring方法来完成。给定两个切面，从Ordered.getValue()(或注解值)返回较低值的切面具有较高的优先级。

当在相同切面定义的两条建议都需要在同一连接点上运行时，其顺序是未定义的(因为无法通过反射为javac编译的类检索声明顺序)。考虑将这些投资方法折叠为每个切面类中每个连接点的一个通知方法，或将建议重构为单独的切面类。

### Introductions

Introductions(在AspectJ中称为类型间声明)使切面可以声明通知对象实现给定的接口，并代表那些对象提供该接口的实现。

你可以使用@DeclareParents注解进行介绍。此注解用于声明匹配类型具有新的父代(名称由来)。例如，给定一个名为UsageTracked的接口和该接口名为DefaultUsageTracked的实现，以下切面声明服务接口的所有实现者也都实现了UsageTracked接口(例如，通过JMX公开统计信息):

```java
@Aspect
public class UsageTracking {

    @DeclareParents(value="com.xzy.myapp.service.*+", defaultImpl=DefaultUsageTracked.class)
    public static UsageTracked mixin;

    @Before("com.xyz.myapp.SystemArchitecture.businessService() && this(usageTracked)")
    public void recordUsage(UsageTracked usageTracked) {
        usageTracked.incrementUseCount();
    }

}
```

要实现的接口由带注解的字段的类型确定。 @DeclareParents注解的value属性是AspectJ类型的模式。匹配类型的任何bean都实现UsageTracked接口。请注意，在前面示例的前置通知中，服务Bean可以直接用作UsageTracked接口的实现。如果以编程方式访问bean，则应编写以下内容:

```java
UsageTracked usageTracked = (UsageTracked) context.getBean("myService");
```

### 切面实例化模型

> 这是一个高级主题。如果刚开始使用AOP，则可以放心地跳过它，直到以后需要的时候再回头看。

默认情况下，应用程序上下文中每个切面都有一个实例。 AspectJ将此称为单例实例化模型。可以使用备用生命周期来定义切面。 
Spring支持AspectJ的perthis和pertarget实例化模型(当前不支持percflow，percflowbelow和pertypewithin)。

你可以通过在@Aspect注解中指定perthis子句来声明perthis切面。考虑以下示例:

```java
@Aspect("perthis(com.xyz.myapp.SystemArchitecture.businessService())")
public class MyAspect {

    private int someState;

    @Before(com.xyz.myapp.SystemArchitecture.businessService())
    public void recordServiceUsage() {
        // ...
    }

}
```

在前面的示例中，“perthis”子句的作用是为每个执行业务服务的唯一服务对象(每个与切入点表达式匹配的联接点绑定到“this”的唯一对象)创建一个切面实例。切面实例是在服务对象上首次调用方法时创建的。当服务对象超出范围时，切面将超出范围。在创建切面实例之前，其中的任何通知都不会执行。创建切面实例后，在其中声明的建议将在匹配的连接点处执行，但是仅当服务对象是与此切面相关联的对象时才执行。有关每个子句的更多信息，请参见AspectJ编程指南。

pertarget实例化模型的工作方式与perthis完全相同，但是它在匹配的连接点为每个唯一目标对象创建一个切面实例。

### 一个AOP示例

既然你已经了解了所有组成部分是如何工作的，那么我们可以将它们放在一起做一些有用的事情。

有时由于并发问题(例如，死锁)，业务服务的执行可能会失败。如果重试该操作，则很可能在下一次尝试中成功。对于适合在这种情况下重试的业务(不需要为解决冲突而需要返回给用户的幂等操作)，我们希望透明地重试该操作，以避免客户端看到PessimisticLockingFailureException。这项要求明确地跨越了服务层中的多个服务，因此非常适合通过一个切面实施。

因为我们想重试该操作，所以我们需要使用环绕通知，以便可以多次调用proceed。以下清单显示了基本切面的实现:

```java
@Aspect
public class ConcurrentOperationExecutor implements Ordered {

    private static final int DEFAULT_MAX_RETRIES = 2;

    private int maxRetries = DEFAULT_MAX_RETRIES;
    private int order = 1;

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Around("com.xyz.myapp.SystemArchitecture.businessService()")
    public Object doConcurrentOperation(ProceedingJoinPoint pjp) throws Throwable {
        int numAttempts = 0;
        PessimisticLockingFailureException lockFailureException;
        do {
            numAttempts++;
            try {
                return pjp.proceed();
            }
            catch(PessimisticLockingFailureException ex) {
                lockFailureException = ex;
            }
        } while(numAttempts <= this.maxRetries);
        throw lockFailureException;
    }

}
```

请注意，切面实现了Ordered接口，因此我们可以将切面的优先级设置为高于事务通知(每次重试时都希望有新的事务)。 maxRetries和order属性均由Spring配置。建议的主要动作发生在doConcurrentOperation中。请注意，目前，我们将重试逻辑应用于每个businessService()。我们尝试继续，如果失败并出现PessimisticLockingFailureException，则我们将再次尝试，除非我们用尽了所有重试尝试。

相应的Spring配置如下:

```xml
<aop:aspectj-autoproxy/>

<bean id="concurrentOperationExecutor" class="com.xyz.myapp.service.impl.ConcurrentOperationExecutor">
    <property name="maxRetries" value="3"/>
    <property name="order" value="100"/>
</bean>
```

为了完善切面，使其仅重试幂等运算，我们可以定义以下幂等注解:

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    // marker annotation
}
```

然后，我们可以使用注解来注解服务操作的实现。切面更改为仅重试幂等操作涉及更改切入点表达式，以便只有@Idempotent操作匹配，如下所示:

```java
@Around("com.xyz.myapp.SystemArchitecture.businessService() && " +
        "@annotation(com.xyz.myapp.service.Idempotent)")
public Object doConcurrentOperation(ProceedingJoinPoint pjp) throws Throwable {
    // ...
}
```

## 基于模式的AOP支持

如果你更喜欢基于XML的格式，Spring还提供了使用新的aop名称空间标签定义切面的支持。支持与使用@AspectJ样式完全相同的切入点表达式和建议类型。因此，在本节中，我们将重点放在新语法上，并使读者参考上一节中的讨论([@AspectJ support](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj))，以了解编写切入点表达式和建议参数的绑定。

要使用本节中描述的aop名称空间标签，需要导入spring-aop模式，如[XML Schema-based configuration](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#xsd-schemas).所述。有关如何在aop名称空间中导入标签的信息，请参见 [the AOP schema](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#xsd-schemas-aop).

在Spring配置中，所有切面和挑战程序元素都必须放在\<aop:config>元素内(在应用程序上下文配置中可以有多个\<aop:config>元素)。\<aop:config>元素可以包含切入点，顾问程序和aspect元素(请注意，必须按此顺序声明它们)。

> \<aop:config>的配置样式大量使用了Spring的自动代理机制。如果你已经通过使用BeanNameAutoProxyCreator或类似方法使用显式自动代理，则可能会导致问题(例如，未编织通知)。推荐的用法模式是仅使用\<aop:config>样式或仅使用AutoProxyCreator样式，并且不要混合使用。

### 声明切面

使用模式支持时，切面是在Spring应用程序上下文中定义为Bean的常规Java对象。状态和行为在对象的字段和方法中捕获，切入点和通知信息在XML中捕获。

你可以使用\<aop:aspect>元素声明一个切面，并使用ref属性引用该bean，如以下示例所示:

```xml
<aop:config>
    <aop:aspect id="myAspect" ref="aBean">
        ...
    </aop:aspect>
</aop:config>

<bean id="aBean" class="...">
    ...
</bean>
```

支持切面的bean(在本例中为aBean)当然可以像配置其他Spring Bean一样进行配置并注入依赖项。

### 声明切入点

你可以在\<aop:config>元素内声明一个命名的切入点，让切入点定义在多个切面和通知之间共享。

可以定义代表服务层中任何业务服务的执行的切入点:

```xml
<aop:config>

    <aop:pointcut id="businessService"
        expression="execution(* com.xyz.myapp.service.*.*(..))"/>

</aop:config>
```

请注意，切入点表达式本身使用的[@AspectJ support](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj)中所述的AspectJ切入点表达式语言。如果使用基于XML的声明样式，则可以引用在切入点表达式中的类型(@Aspects)中定义的命名切入点。定义上述切入点的另一种方法如下：

```xml
<aop:config>

    <aop:pointcut id="businessService"
        expression="com.xyz.myapp.SystemArchitecture.businessService()"/>

</aop:config>
```

假定你具有“[Sharing Common Pointcut Definitions](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-common-pointcuts)”中所述的SystemArchitecture切面。

然后，在切面中声明切入点与声明顶级切入点非常相似，如以下示例所示：

```xml
<aop:config>

    <aop:aspect id="myAspect" ref="aBean">

        <aop:pointcut id="businessService"
            expression="execution(* com.xyz.myapp.service.*.*(..))"/>

        ...

    </aop:aspect>

</aop:config>
```

与@AspectJ切面几乎相同，使用基于架构的定义样式声明的切入点可以收集连接点上下文。例如，以下切入点收集this对象作为连接点上下文，并将其传递给通知：

```xml
<aop:config>

    <aop:aspect id="myAspect" ref="aBean">

        <aop:pointcut id="businessService"
            expression="execution(* com.xyz.myapp.service.*.*(..)) && this(service)"/>

        <aop:before pointcut-ref="businessService" method="monitor"/>

        ...

    </aop:aspect>

</aop:config>
```

必须声明该通知以通过包含匹配名称的参数来接收收集的连接点上下文，如下所示：

```java
public void monitor(Object service) {
    // ...
}
```

组合切入点子表达式时，&&在XML文档中很尴尬，因此你可以分别使用＆，or或not关键字代替&&，||和！。例如，上一个切入点可以更好地编写：

```xml
<aop:config>

    <aop:aspect id="myAspect" ref="aBean">

        <aop:pointcut id="businessService"
            expression="execution(* com.xyz.myapp.service.*.*(..)) and this(service)"/>

        <aop:before pointcut-ref="businessService" method="monitor"/>

        ...
    </aop:aspect>
</aop:config>
```

请注意，以这种方式定义的切入点由其XML ID引用，并且不能用作命名切入点以形成复合切入点。因此，基于架构的定义样式中的命名切入点支持比@AspectJ样式所提供的更受限制。

### 声明通知

基于模式的AOP支持使用与@AspectJ样式相同的五种通知，并且它们具有完全相同的语义。

#### 前置通知

在运行匹配的方法运行之前运行。使用\<aop:before>元素在<aop：aspect>中声明它，如以下示例所示：

```xml
<aop:aspect id="beforeExample" ref="aBean">

    <aop:before
        pointcut-ref="dataAccessOperation"
        method="doAccessCheck"/>

    ...

</aop:aspect>
```

这里，dataAccessOperation是在最高(<aop：config>)级别定义的切入点的ID。要定义内联切入点，请使用以下方法将pointcut-ref属性替换为pointcut属性：

```xml
<aop:aspect id="beforeExample" ref="aBean">

    <aop:before
        pointcut="execution(* com.xyz.myapp.dao.*.*(..))"
        method="doAccessCheck"/>

    ...

</aop:aspect>
```

正如我们在@AspectJ样式的讨论中所指出的那样，使用命名的切入点可以显著提高代码的可读性。

method属性标识提供通知正文的方法(doAccessCheck)。必须为包含通知的Aspect元素所引用的bean上定义此方法。在执行数据访问操作(与切入点表达式匹配的方法执行联接点)之前，将调用Aspect Bean上的doAccessCheck方法。

#### 后置通知

在匹配的方法执行正常完成时运行。它在<aop：aspect>内以与之前通知相同的方式声明。以下示例显示了如何声明它：

```xml
<aop:aspect id="afterReturningExample" ref="aBean">

    <aop:after-returning
        pointcut-ref="dataAccessOperation"
        method="doAccessCheck"/>

    ...

</aop:aspect>
```

与@AspectJ样式一样，你可以在通知正文中获取返回值。为此，使用returning属性指定返回值应传递到的参数的名称，如以下示例所示：

```xml
<aop:aspect id="afterReturningExample" ref="aBean">

    <aop:after-returning
        pointcut-ref="dataAccessOperation"
        returning="retVal"
        method="doAccessCheck"/>

    ...

</aop:aspect>
```

doAccessCheck方法必须声明一个名为retVal的参数。该参数的类型以与@AfterReturning中所述相同的方式约束匹配。例如，你可以声明如下方法签名：

```java
public void doAccessCheck(Object retVal) {...
```

#### 异常通知

当匹配的方法执行通过抛出异常退出时执行。∂在<aop：aspect>中声明它，如以下示例所示：

```xml
<aop:aspect id="afterThrowingExample" ref="aBean">

    <aop:after-throwing
        pointcut-ref="dataAccessOperation"
        method="doRecoveryActions"/>

    ...

</aop:aspect>
```

与@AspectJ样式一样，你可以在通知正文中获取引发的异常。为此请使用throwing属性指定异常应传递到的参数的名称，如以下示例所示：

```xml
<aop:aspect id="afterThrowingExample" ref="aBean">

    <aop:after-throwing
        pointcut-ref="dataAccessOperation"
        throwing="dataAccessEx"
        method="doRecoveryActions"/>

    ...

</aop:aspect>
```

doRecoveryActions方法必须声明一个名为dataAccessEx的参数。该参数的类型以与@AfterThrowing中所述相同的方式约束匹配。例如，方法签名可以声明如下：

```java
public void doRecoveryActions(DataAccessException dataAccessEx) {...
```

#### 最终通知

无论最终如何执行匹配的方法，通知(最终)都会运行。你可以使用after元素对其进行声明，如以下示例所示：

```xml
<aop:aspect id="afterFinallyExample" ref="aBean">

    <aop:after
        pointcut-ref="dataAccessOperation"
        method="doReleaseLock"/>

    ...

</aop:aspect>
```

#### 环绕通知

环绕通知在匹配方法“周围”运行。它有机会在方法执行之前和之后进行工作，并确定何时，如何以及甚至是否执行该方法。环绕通知通常用于以线程安全的方式(例如，启动和停止计时器)在方法执行之前和之后共享状态。我们建议始终使用最不强大的通知形式，以满足你的要求。如果前置通知可以完成工作，请不要使用环绕通知。

你可以使用aop：around元素声明环绕通知。咨询方法的第一个参数必须是ProceedingJoinPoint类型。在通知的正文中，在ProceedingJoinPoint上调用proce()会使底层方法执行。还可以借助Object []调用proce方法。数组中的值在方法执行时用作方法执行的参数。有关调用Object  []的注意事项，请参见“[Around Advice](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj-around-advice)”。以下示例显示了在XML中声明环绕通知：

```xml
<aop:aspect id="aroundExample" ref="aBean">

    <aop:around
        pointcut-ref="businessService"
        method="doBasicProfiling"/>

    ...

</aop:aspect>
```

doBasicProfiling通知的实现可以与@AspectJ示例完全相同(当然要减去注解)，如以下示例所示：

```java
public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
    // start stopwatch
    Object retVal = pjp.proceed();
    // stop stopwatch
    return retVal;
}
```

#### 通知参数

基于XML声明样式以与@AspectJ样式相同的方式支持完全类型，即通过名称与通知方法参数匹配切入点参数。有关详细信息，请参见[Advice Parameters](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj-advice-params)。如果你希望显式指定通知方法的参数名称(不依赖于先前描述的检测策略)，则可以通过使用advice元素的arg-names属性来实现，该属性的处理方式与注解中的argNames属性相同(如 [Determining Argument Names](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj-advice-params-names)所述)。以下示例显示如何在XML中指定参数名称：

```xml
<aop:before
    pointcut="com.xyz.lib.Pointcuts.anyPublicMethod() and @annotation(auditable)"
    method="audit"
    arg-names="auditable"/>
```

arg-names属性接受以逗号分隔的参数名称列表。

以下基于XSD的方法中涉及程度稍高的示例以及一些与强类型参数结合使用的通知：

```java
package x.y.service;

public interface PersonService {

    Person getPerson(String personName, int age);
}

public class DefaultFooService implements FooService {

    public Person getPerson(String name, int age) {
        return new Person(name, age);
    }
}
```

接下来是切面。请注意profile(..)方法接受许多强类型参数，其中第一个恰好是用于进行方法调用的连接点。此参数的存在表明profile(..)将用作通知，如以下示例所示：

```java
package x.y;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.util.StopWatch;

public class SimpleProfiler {

    public Object profile(ProceedingJoinPoint call, String name, int age) throws Throwable {
        StopWatch clock = new StopWatch("Profiling for '" + name + "' and '" + age + "'");
        try {
            clock.start(call.toShortString());
            return call.proceed();
        } finally {
            clock.stop();
            System.out.println(clock.prettyPrint());
        }
    }
}
```

最后，以下示例XML配置影响特定连接点的上述通知的执行：

```xml
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:aop="http://www.springframework.org/schema/aop"
    xsi:schemaLocation="
        http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop https://www.springframework.org/schema/aop/spring-aop.xsd">

    <!-- this is the object that will be proxied by Spring's AOP infrastructure -->
    <bean id="personService" class="x.y.service.DefaultPersonService"/>

    <!-- this is the actual advice itself -->
    <bean id="profiler" class="x.y.SimpleProfiler"/>

    <aop:config>
        <aop:aspect ref="profiler">

            <aop:pointcut id="theExecutionOfSomePersonServiceMethod"
                expression="execution(* x.y.service.PersonService.getPerson(String,int))
                and args(name, age)"/>

            <aop:around pointcut-ref="theExecutionOfSomePersonServiceMethod"
                method="profile"/>

        </aop:aspect>
    </aop:config>

</beans>
```

考虑以下驱动程序脚本：

```java
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import x.y.service.PersonService;

public final class Boot {

    public static void main(final String[] args) throws Exception {
        BeanFactory ctx = new ClassPathXmlApplicationContext("x/y/plain.xml");
        PersonService person = (PersonService) ctx.getBean("personService");
        person.getPerson("Pengo", 12);
    }
}
```

有了这样的启动类，我们将在标准输出上获得类似于以下内容的输出：

```
StopWatch 'Profiling for 'Pengo' and '12'': running time (millis) = 0
-----------------------------------------
ms     %     Task name
-----------------------------------------
00000  ?  execution(getFoo)
```

#### 通知顺序

当需要在同一连接点(执行方法)上执行多个通知时，排序规则如“[Advice Ordering](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj-advice-ordering)”中所述。切面之间的优先级是通过将Order注解添加到支持切面的Bean或通过使Bean实现Ordered接口来确定的。

### Introductions

引介(在AspectJ中称为类型间声明)使切面可以声明通知的对象实现给定的接口，并代表那些对象提供该接口的实现(注：之前的通知都是在目标方法范围内织入，而引介则是直接在类级别上添加目标未实现的接口方法)。

你可以通过在aop：aspect中使用aop：declare-parents声明引介。你可以使用aop：declare-parents元素声明匹配类型具有新的父代(因此而得名)。例如，给定一个名为UsageTracked的接口和该接口名为DefaultUsageTracked的实现，下面的切面声明了服务接口的所有实现者也都实现了UsageTracked接口。 (例如，为了通过JMX公开统计信息。)

```xml
<aop:aspect id="usageTrackerAspect" ref="usageTracking">

    <aop:declare-parents
        types-matching="com.xzy.myapp.service.*+"
        implement-interface="com.xyz.myapp.service.tracking.UsageTracked"
        default-impl="com.xyz.myapp.service.tracking.DefaultUsageTracked"/>

    <aop:before
        pointcut="com.xyz.myapp.SystemArchitecture.businessService()
            and this(usageTracked)"
            method="recordUsage"/>

</aop:aspect>
```

支持usageTracking bean的类将包含以下方法：

```java
public void recordUsage(UsageTracked usageTracked) {
    usageTracked.incrementUseCount();
}
```

要实现的接口由Implement-interface属性确定。类型匹配属性的值是AspectJ类型模式。匹配类型的任何Bean均实现UsageTracked接口。请注意，在前面示例的通知中，服务Bean可以直接用作UsageTracked接口的实现。要以编程方式访问bean，可以编写以下代码：

```java
UsageTracked usageTracked = (UsageTracked) context.getBean("myService");
```

> 此处可参考[循序渐进之Spring AOP(4) - Introduction](https://blog.csdn.net/u010599762/article/details/80182178)

### 切面实例化模型

模式定义切面唯一受支持的实例化模型是单例模型。在将来的版本中可能会支持其他实例化模型。

“advisors”的概念来自Spring中定义的AOP支持，并且在AspectJ中没有直接等效的概念。advisors就像一个独立的小型切面，只有一条通知。通知本身由bean表示，并且必须实现[Advice Types in Spring](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-api-advice-types)中描述的通知接口之一。advisors可以利用AspectJ切入点表达式。

Spring通过<aop：advisor>元素支持advisors程序概念。你最常看到它与事务通知结合使用，事务通知在Spring中也有其自己的名称空间支持。以下示例显示advisors程序：

```xml
<aop:config>

    <aop:pointcut id="businessService"
        expression="execution(* com.xyz.myapp.service.*.*(..))"/>

    <aop:advisor
        pointcut-ref="businessService"
        advice-ref="tx-advice"/>

</aop:config>

<tx:advice id="tx-advice">
    <tx:attributes>
        <tx:method name="*" propagation="REQUIRED"/>
    </tx:attributes>
</tx:advice>
```

除了在前面的示例中使用的pointcut-ref属性，你还可以使用pointcut属性内联定义一个pointcut表达式。

要定义advisor程序的优先级，请使用order属性定义advisor程序的Ordered值。

### AOP模式示例

本节将展示[An AOP Example](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj-example)中的并发锁定失败重试示例在使用模式支持重写时的样子。

有时由于并发问题(例如，死锁)，业务服务的执行可能会失败。如果重试该操作，则很可能在下一次尝试中成功。对于适合在这种情况下重试的业务(不需要为解决冲突而需要返回给用户的幂等操作)，我们希望透明地重试该操作，以避免客户端看到PessimisticLockingFailureException。这项要求明确地跨越了服务层中的多个服务，因此非常适合通过一个切面实施。

因为我们想重试该操作，所以我们需要使用环绕通知，以便可以多次调用proced。以下清单显示了基本切面的实现(这是使用模式支持的常规Java类)：

```java
public class ConcurrentOperationExecutor implements Ordered {

    private static final int DEFAULT_MAX_RETRIES = 2;

    private int maxRetries = DEFAULT_MAX_RETRIES;
    private int order = 1;

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Object doConcurrentOperation(ProceedingJoinPoint pjp) throws Throwable {
        int numAttempts = 0;
        PessimisticLockingFailureException lockFailureException;
        do {
            numAttempts++;
            try {
                return pjp.proceed();
            }
            catch(PessimisticLockingFailureException ex) {
                lockFailureException = ex;
            }
        } while(numAttempts <= this.maxRetries);
        throw lockFailureException;
    }

}
```

请注意，切面实现了Ordered接口，因此我们可以将切面的优先级设置为高于事务通州(每次重试时都希望有新的事务)。  maxRetries和order属性均由Spring配置。主要操作发生在通知方法周围的doConcurrentOperation中。我们尝试继续。如果由于PessimisticLockingFailureException失败，则将重试，直到我们用尽了所有重试尝试。

> 该类与@AspectJ示例中使用的类相同，但是除去了注解。

相应的Spring配置如下：

```xml
<aop:config>

    <aop:aspect id="concurrentOperationRetry" ref="concurrentOperationExecutor">

        <aop:pointcut id="idempotentOperation"
            expression="execution(* com.xyz.myapp.service.*.*(..))"/>

        <aop:around
            pointcut-ref="idempotentOperation"
            method="doConcurrentOperation"/>

    </aop:aspect>

</aop:config>

<bean id="concurrentOperationExecutor"
    class="com.xyz.myapp.service.impl.ConcurrentOperationExecutor">
        <property name="maxRetries" value="3"/>
        <property name="order" value="100"/>
</bean>
```

请注意，目前我们假设所有业务服务都是幂等的。如果不是这种情况，我们可以改进切面，以便通过引入等幂注解并使用该注解来做到服务操作的实现，使其仅重试真正的幂等操作，如以下示例所示：

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    // marker annotation
}
```

切面更改为仅重试幂等操作涉及更改切入点表达式，以便仅与@Idempotent操作匹配，如下所示：

```xml
<aop:pointcut id="idempotentOperation"
        expression="execution(* com.xyz.myapp.service.*.*(..)) and
        @annotation(com.xyz.myapp.service.Idempotent)"/>
```

## 选择要使用的AOP声明样式

一旦确定切面是实现给定需求的最佳方法，你如何在使用Spring AOP或AspectJ以及在Aspect语言(代码)样式，@  AspectJ注解样式或Spring XML样式之间做出选择？这些决定受许多因素影响，包括应用程序需求，开发工具和团队对AOP的熟悉程度。

### Spring AOP还是完整AspectJ？

使用最简单的方法即可。 Spring  AOP比使用完整的AspectJ更简单，因为不需要在开发和构建过程中引入AspectJ编译器/编织器。如果你只需要通知在Spring  bean上执行操作，则Spring  AOP是正确的选择。如果你需要通知不受Spring容器管理的对象(通常是域对象)，则需要使用AspectJ。如果你希望通知除简单方法执行之外的连接点(例如，字段get或设置连接点等)，则还需要使用AspectJ。

使用AspectJ时，可以选择AspectJ语言语法(也称为“代码样式”)或@AspectJ注解样式。显然，如果你不使用Java  5+，则别无选择：使用代码样式。如果切面在你的设计中起着重要作用，并且你能够将AspectJ开发工具(AJDT)插件用于Eclipse，则AspectJ语言语法是首选。它更干净，更简单，因为该语言是专为编写切面而设计的。如果你不使用Eclipse或只有少数几个切面在你的应用程序并且不起主要作用，那么你可能需要考虑使用@AspectJ样式，在IDE中坚持常规Java编译，并向其中添加切面编织阶段你的构建脚本。

### @AspectJ或Spring AOP的XML？

如果选择使用Spring AOP，则可以选择@AspectJ或XML样式。有各种折衷考虑。

XML样式可能是现有Spring用户最熟悉的，并且得到了真正的POJO的支持。当使用AOP作为配置企业服务的工具时，XML是一个不错的选择(一个很好的测试是你是否将切入点表达式视为配置的一部分，而你可能希望独立更改)。使用XML样式，可以说从你的配置中可以更清楚地了解系统中存在哪些切面。

XML样式有两个缺点。首先，它没有完全将要解决的需求的实现封装在一个地方。  DRY原则说，系统中的任何知识都应该有单一，明确，权威的表示。在使用XML样式时，关于如何实现需求的部分会在配置文件中的后备bean类的声明和XML中分散。当你使用@AspectJ样式时，此信息将封装在一个模块中：切面。其次，与@AspectJ样式相比，XML样式在表达能力上有更多限制：仅支持“单例”切面实例化模型，并且无法组合以XML声明的命名切入点。例如，使用@AspectJ样式，你可以编写如下内容：

```java
@Pointcut("execution(* get*())")
public void propertyAccess() {}

@Pointcut("execution(org.xyz.Account+ *(..))")
public void operationReturningAnAccount() {}

@Pointcut("propertyAccess() && operationReturningAnAccount()")
public void accountPropertyAccess() {}
```

在XML样式中，你可以声明前两个切入点：

```xml
<aop:pointcut id="propertyAccess"
        expression="execution(* get*())"/>

<aop:pointcut id="operationReturningAnAccount"
        expression="execution(org.xyz.Account+ *(..))"/>
```

XML方法的缺点是你无法通过组合这些定义来定义accountPropertyAccess切入点。

@AspectJ样式支持其他实例化模型和更丰富的切入点组合。它具有将切面保持为模块化单元的优势。它还可以被Spring  AOP和AspectJ同时理解。因此，如果你以后决定需要AspectJ的功能来实现其他要求，则可以轻松地迁移到经典的AspectJ设置。总而言之，Spring团队在自定义切面更喜欢@AspectJ样式，而不是简单地配置企业服务。

## 混合切面类型

通过使用自动代理支持，模式定义的<aop：aspect>切面，<aop：advisor>声明的advisor程序，甚至是同一配置中其他样式的代理和拦截器，完全可以混合@AspectJ样式的切面。所有这些都是通过使用相同的基础支持机制实现的，并且可以毫无困难地共存。

## 代理机制

Spring AOP使用JDK动态代理或CGLIB创建给定目标对象的代理。 JDK内置了JDK动态代理，而CGLIB是常见的开源类定义库(重新包装到spring-core中)。

如果要代理的目标对象实现至少一个接口，则使用JDK动态代理。代理了由目标类型实现的所有接口。如果目标对象未实现任何接口，则将创建CGLIB代理。

如果要强制使用CGLIB代理(例如，代理为目标对象定义的每个方法，而不仅是由其接口实现的方法)，都可以这样做。但是，你应该考虑以下问题：

- 使用CGLIB，不能代理final方法，因为它们不能在运行时生成的子类中被覆盖。
- 从Spring 4.0开始，由于CGLIB代理实例是通过Objenesis创建的，因此不会调用代理对象的构造函数两次。仅当你的JVM不允许绕过构造函数时，你才可能从Spring的AOP支持中看到两次调用和相应的调试日志条目。

要强制使用CGLIB代理，请将<aop：config>元素的proxy-target-class属性的值设置为true，如下所示：

```xml
<aop:config proxy-target-class="true">
    <!-- other beans defined here... -->
</aop:config>
```

要在使用@AspectJ自动代理支持时强制CGLIB代理，请将<aop：aspectj-autoproxy>元素的proxy-target-class属性设置为true，如下所示：

```xml
<aop:aspectj-autoproxy proxy-target-class="true"/>
```

> 多个<aop：config />部分在运行时折叠到一个统一的自动代理创建器中，该创建器将应用任何<aop：config  />部分(通常来自不同的XML bean定义文件)指定的最强的代理设置。这也适用于<tx：annotation-driven  />和<aop：aspectj-autoproxy />元素。  为了清楚起见，在<tx：annotation-driven  />，<aop：aspectj-autoproxy />或<aop：config  />元素上使用proxy-target-class =“ true”会强制对所有三个元素使用CGLIB代理其中。

### 理解AOP代理

Spring AOP是基于代理的。在编写自己的切面或使用Spring框架提供的任何基于Spring AOP的切面之前，掌握最后一条语句实际含义的语义至关重要。

首先考虑以下情况：你有一个普通的，未经代理的，没有特殊要求的，直接的对象引用，如以下代码片段所示：

```java
public class SimplePojo implements Pojo {

    public void foo() {
        // this next method invocation is a direct call on the 'this' reference
        this.bar();
    }

    public void bar() {
        // some logic...
    }
}
```

如果在对象引用上调用方法，则直接在该对象引用上调用该方法，如下图和清单所示：

![](https://cdn.hellooooo.top/image/blog/2020/07/aop/aop-proxy-plain-pojo-call.png)

```java
public class Main {

    public static void main(String[] args) {
        Pojo pojo = new SimplePojo();
        // this is a direct method call on the 'pojo' reference
        pojo.foo();
    }
}
```

当客户端代码具有的引用是代理时，情况会稍有变化。考虑以下图表和代码片段：

![](https://cdn.hellooooo.top/image/blog/2020/07/aop/aop-proxy-call.png)

```java
public class Main {

    public static void main(String[] args) {
        ProxyFactory factory = new ProxyFactory(new SimplePojo());
        factory.addInterface(Pojo.class);
        factory.addAdvice(new RetryAdvice());

        Pojo pojo = (Pojo) factory.getProxy();
        // this is a method call on the proxy!
        pojo.foo();
    }
}
```

此处要理解的关键是Main类的main(..)方法中的客户端代码具有对代理的引用。这意味着该对象引用上的方法调用是代理上的调用。结果，代理可以委派给与该特定方法调用相关的所有拦截器(通知)。但是，一旦调用最终到达目标对象(在本例中为SimplePojo，则为引用)，它可能对其自身进行的任何方法调用(例如this.bar()或this.foo())都会被调用。但不是通过代理。这具有重要的意义。这意味着自调用不会导致与方法调用相关的通知得到执行的机会。

好吧，那该怎么办？最佳方法(在这里宽松地使用术语“最佳”)是重构代码，以免发生自调用。这确实需要你做一些工作，但这是最好的，侵入性最小的方法。下一种方法绝对可怕，我们正要指出这一点，恰恰是因为它是如此可怕。你可以(对我们来说是痛苦的)完全将类中的逻辑绑定到Spring AOP，如以下示例所示：

```java
public class SimplePojo implements Pojo {

    public void foo() {
        // this works, but... gah!
        ((Pojo) AopContext.currentProxy()).bar();
    }

    public void bar() {
        // some logic...
    }
}
```

这将你的代码完全耦合到Spring AOP，并且使类本身意识到在AOP上下文中使用它的事实。创建代理时，它还需要一些其他配置，如以下示例所示：

```java
public class Main {

    public static void main(String[] args) {
        ProxyFactory factory = new ProxyFactory(new SimplePojo());
        factory.addInterface(Pojo.class);
        factory.addAdvice(new RetryAdvice());
        factory.setExposeProxy(true);

        Pojo pojo = (Pojo) factory.getProxy();
        // this is a method call on the proxy!
        pojo.foo();
    }
}
```

最后，必须指出，AspectJ没有此自调用问题，因为它不是基于代理的AOP框架。

## 以编程方式创建@AspectJ代理

除了使用<aop：config>或<aop：aspectj-autoproxy>声明配置中的各个切面外，还可以通过编程方式创建通知目标对象的代理。有关Spring的AOP API的完整详细信息，请参阅[next chapter](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-api)。在这里，我们要重点介绍通过使用@AspectJ切面自动创建代理的功能。

你可以使用*org.springframework.aop.aspectj.annotation.AspectJProxyFactory*类为一个或多个@AspectJ切面通知的目标对象创建代理。此类的基本用法非常简单，如以下示例所示：

```java
// create a factory that can generate a proxy for the given target object
AspectJProxyFactory factory = new AspectJProxyFactory(targetObject);

// add an aspect, the class must be an @AspectJ aspect
// you can call this as many times as you need with different aspects
factory.addAspect(SecurityManager.class);

// you can also add existing aspect instances, the type of the object supplied must be an @AspectJ aspect
factory.addAspect(usageTracker);

// now get the proxy object...
MyInterfaceType proxy = factory.getProxy();
```

有关更多信息，请参见[javadoc](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/aop/aspectj/annotation/AspectJProxyFactory.html)。

## 在Spring应用中使用AspectJ

到目前为止，本章介绍的所有内容都是纯Spring AOP。在本节中，我们将研究如果你的需求超出了Spring AOP本身提供的功能，那么如何使用AspectJ编译器或weaver代替Spring AOP或除Spring AOP之外使用。

Spring附带了一个小的AspectJ切面库，该库在你的发行版中可以作为spring-aspects.jar独立使用。你需要将其添加到类路径中才能使用其中的切面。[Using AspectJ to Dependency Inject Domain Objects with Spring](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-atconfigurable)以及 [Other Spring aspects for AspectJ](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ajlib-other)讨论了该库的内容以及如何使用它。[Configuring AspectJ Aspects by Using Spring IoC](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-aj-configure) 讨论了如何依赖注入使用AspectJ编译器编织的AspectJ切面。最后，[Load-time Weaving with AspectJ in the Spring Framework](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-aj-ltw)提供了加载时编织的介绍。

### Using AspectJ to Dependency Inject Domain Objects with Spring

Spring容器实例化并配置在你的应用程序上下文中定义的bean。给定包含要应用的配置的Bean定义的名称，也可以要求Bean工厂配置预先存在的对象。  spring-aspects.jar包含注解驱动的切面，该切面利用此功能允许依赖项注入任何对象。其旨在用于在任何容器的控制范围之外创建的对象。域对象通常属于此类，因为它们通常是通过数据库查询的结果由new操作符或ORM工具以编程方式创建的。

@Configurable注解将一个类标记为符合Spring驱动的配置。在最简单的情况下，你可以将其纯粹用作标记注解，如以下示例所示：

```java
package com.xyz.myapp.domain;

import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class Account {
    // ...
}
```

当以这种方式用作标记接口时，Spring通过使用具有与完全限定类型名称相同名称的bean定义(通常为原型作用域)来配置带注解类型的新实例(在这种情况下为Account)。  (*com.xyz.myapp.domain.Account*)。由于Bean的默认名称是其类型的完全限定名称，因此声明原型定义的便捷方法是省略id属性，如以下示例所示：

```xml
<bean class="com.xyz.myapp.domain.Account" scope="prototype">
    <property name="fundsTransferService" ref="fundsTransferService"/>
</bean>
```

如果要显式指定要使用的原型bean定义的名称，则可以直接在注解中这样做，如以下示例所示：

```java
package com.xyz.myapp.domain;

import org.springframework.beans.factory.annotation.Configurable;

@Configurable("account")
public class Account {
    // ...
}
```

Spring现在查找名为account的bean定义，并将其用作配置新Account实例的定义。

你也可以使用自动装配来避免完全指定专用的bean定义。要让Spring应用自动装配，请使用@Configurable注解的autowire属性。你可以指定@Configurable(autowire = Autowire.BY_TYPE)或@Configurable(autowire =  Autowire.BY_NAME)分别按类型或名称进行自动装配。作为替代方案，最好为你的对象指定显式的，注解驱动的依赖项注入。通过@Autowired或@Inject在字段或方法级别进行@Configurable Bean(有关更多详细信息，请参见[Annotation-based Container Configuration](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-annotation-config))。

最后，你可以使用dependencyCheck属性(例如，@Configurable(autowire =  Autowire.BY_NAME，dependencyCheck =  true))为新创建和配置的对象中的对象引用启用Spring依赖检查。如果此属性设置为true，则Spring在配置后验证是否已设置了所有属性(不是基元或集合)。

请注意，单独使用注解不会执行任何操作。  spring-aspects.jar中的AnnotationBeanConfigurerAspect对注解的存在起作用。从本质上讲，切面说，“从初始化带有@Configurable注解的类型的新对象返回之后，使用Spring根据注解的属性配置新创建的对象”。在这种情况下，“初始化”是指新实例化的对象(例如，使用new运算符实例化的对象)以及正在进行反序列化(例如，通过[readResolve()](https://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html))的Serializable对象。

> 上段中的关键短语之一是“本质上”。在大多数情况下，“从新对象的初始化返回后”的确切语义是可以的。在这种情况下，“初始化后”是指在构造对象之后注入依赖项。这意味着该依赖项不可在类的构造函数体中使用。如果你希望在构造函数主体执行之前注入依赖项，从而可以在构造函数主体中使用这些依赖项，则需要在@Configurable声明中对此进行定义，如下所示：
>
> ```java
> @Configurable(preConstruction = true)
> ```
>
> 你可以在[in this appendix](https://www.eclipse.org/aspectj/doc/next/progguide/semantics-joinPoints.html) of the [AspectJ Programming Guide](https://www.eclipse.org/aspectj/doc/next/progguide/index.html)中找到有关AspectJ中各种切入点类型的语言语义的更多信息。

为此，必须将带注解的类型与AspectJ编织器编织在一起。你可以使用构建时的Ant或Maven任务来执行此操作(例如，参见[AspectJ Development Environment Guide](https://www.eclipse.org/aspectj/doc/released/devguide/antTasks.html))，也可以使用加载时编织(请参阅 [Load-time Weaving with AspectJ in the Spring Framework](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-aj-ltw))。Spring需要配置AnnotationBeanConfigurerAspect本身(以便获得对将用于配置新对象的bean工厂的引用)。如果使用基于Java的配置，则可以将@EnableSpringConfigured添加到任何@Configuration类中，如下所示：

```java
@Configuration
@EnableSpringConfigured
public class AppConfig {
}
```

如果你更喜欢基于XML的配置，则Spring上下文名称空间定义了一个方便的context：spring-configured元素，你可以按以下方式使用它：

```xml
<context:spring-configured/>
```

在配置切面之前创建的@Configurable对象实例会导致向调试日志发出消息，并且未进行对象配置。一个示例可能是Spring配置中的bean，当它由Spring初始化时会创建域对象。在这种情况下，你可以使用depends-on bean属性来手动指定bean取决于配置切面。下面的示例显示如何使用depends-on属性：

```xml
<bean id="myService"
        class="com.xzy.myapp.service.MyService"
        depends-on="org.springframework.beans.factory.aspectj.AnnotationBeanConfigurerAspect">

    <!-- ... -->

</bean>
```

> 除非你真的想在运行时依赖它的语义，否则不要通过bean配置器切面激活@Configurable处理。特别是，请确保不要在通过容器注册为常规Spring bean的bean类上使用@Configurable。这样做将导致两次初始化，一次是通过容器，一次是通过切面。

**单元测试@Configurable对象**

@Configurable支持的目标之一是实现域对象的独立单元测试，而不会遇到与硬编码查找相关的困难。如果AspectJ尚未编织@Configurable类型，则注解在单元测试期间不起作用。你可以在被测对象中设置模拟或存根属性引用，然后照常进行。如果AspectJ编织了@Configurable类型，你仍然可以像往常一样在容器外部进行单元测试，但是每次构造@Configurable对象时，你都会看到一条警告消息，指示该对象尚未由Spring配置。

**使用多个应用程序上下文**

> 这个我看的迷迷糊糊的，都是机翻的

用于实现@Configurable支持的AnnotationBeanConfigurerAspect是AspectJ单例切面。单例切面的范围与静态成员的范围相同：每个类加载器都有一个切面实例来定义类型。这意味着，如果你在同一个类加载器层次结构中定义多个应用程序上下文，则需要考虑在何处定义@EnableSpringConfigured bean，以及在何处将spring-aspects.jar放在类路径上。

考虑一个典型的Spring  Web应用程序配置，该配置具有一个共享的父应用程序上下文，该上下文定义了通用的业务服务，支持那些服务所需的一切，以及每个Servlet的一个子应用程序上下文(其中包含该Servlet的特定定义)。所有这些上下文共存于同一类加载器层次结构中，因此AnnotationBeanConfigurerAspect只能保存对其中一个的引用。在这种情况下，我们建议在共享(父)应用程序上下文中定义@EnableSpringConfigured  bean。这定义了你可能想注入域对象的服务。结果是，你无法使用@Configurable机制来配置域对象，该域对象引用的是在子(特定于servlet的)上下文中定义的bean的引用(无论如何，这可能不是你想做的事情)。

在同一容器中部署多个Web应用程序时，请确保每个Web应用程序通过使用其自己的类加载器(例如，将spring-aspects.jar放置在“  WEB-INF/lib”中)在spring-aspects.jar中加载类型。如果将spring-aspects.jar仅添加到容器级的类路径中(并因此由共享的父类加载器加载)，则所有Web应用程序都共享相同的切面实例(这可能不是你想要的)。

### AspectJ的其他Spring切面

除了@Configurable切面之外，spring-aspects.jar还包含一个AspectJ切面，你可以使用该切面来驱动Spring的事务管理，以使用@Transactional注解对类型和方法进行注解。这主要是针对希望在Spring容器之外使用Spring框架的事务支持的用户。

解释@Transactional注解的切面是AnnotationTransactionAspect。使用此切面时，必须注解实现类(或该类中的方法，或两者)，而不是注解该类所实现的接口(如果有)。 AspectJ遵循Java的规则，即不继承接口上的注释。

类上的@Transactional注解指定用于执行该类中任何公共操作的默认事务语义。

类中方法上的@Transactional注解会覆盖类注解(如果存在)给出的默认事务语义。可以注解任何可见性的方法，包括私有方法。直接注解非公共方法是执行此类方法而获得事务划分的唯一方法。

> 从Spring Framework  4.2开始，spring-aspects提供了一个相似的切面，为标准*javax.transaction.Transactional*注解提供了完全相同的功能。查看JtaAnnotationTransactionAspect了解更多详细信息。

对于希望使用Spring配置和事务管理支持但又不想(或不能)使用注解的AspectJ程序员，spring-aspects.jar还包含抽象切面，你可以扩展它们以提供自己的切入点定义。有关更多信息，请参见AbstractBeanConfigurerAspect和AbstractTransactionAspect的来源。例如，以下摘录显示了如何编写切面来使用与完全限定的类名匹配的原型bean定义来配置域模型中定义的对象的所有实例：

```java
public aspect DomainObjectConfiguration extends AbstractBeanConfigurerAspect {

    public DomainObjectConfiguration() {
        setBeanWiringInfoResolver(new ClassNameBeanWiringInfoResolver());
    }

    // the creation of a new bean (any object in the domain model)
    protected pointcut beanCreation(Object beanInstance) :
        initialization(new(..)) &&
        SystemArchitecture.inDomainModel() &&
        this(beanInstance);
}
```

> 不翻了，跳过
>
> ![image-20200717203911553](https://cdn.hellooooo.top/image/blog/2020/07/aop/image-20200717203911553.png)

## 更多资源

可以在[AspectJ website](https://www.eclipse.org/aspectj)上找到有关AspectJ的更多信息。

Eclipse AspectJ，作者：Adrian Colyer等(Addison-Wesley，2005年)为AspectJ语言提供了全面的介绍和参考。

强烈推荐Ramnivas Laddad撰写的《 AspectJ in Action》第二版(Manning，2009年)。本书的重点是AspectJ，但在一定程度上探讨了许多通用的AOP主题。

> 翻译：侧边翻译
>
> 校正：靓仔Q
>
> 时间：2020.5.20~2020.7.17

