---
[Aspect Oriented Programming with Spring](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop)
------
   Aspect Oriented Programming with Spring(Version 5.2.6.RELEASE)
   
   ---
   
   面向切面的编程（AOP）通过提供另一种思考程序结构的方式来补充面向对象的编程（OOP）。 OOP中模块化的关键单元是类，而在AOP中模块化是切面。切面使关注点（例如事务管理）的模块化可以跨越多种类型和对象。 （这种关注在AOP文献中通常被称为“跨领域”关注。）
   
   Spring的关键组件之一是AOP框架。尽管Spring IoC容器不依赖于AOP（这意味着你不需要的话可以不使用AOP），但AOP是对Spring IoC的补充，以提供功能非常强大的中间件解决方案。
   
   > Spring AOP 和 AspectJ的切入点
   >
   > Spring提供了使用基于[schema-based approach](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-schema)或[@AspectJ annotation style](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj)来编写自定义切面的简单而强大的方法。这两种样式都提供了完全类型化的建议，并使用了AspectJ切入点语言，同时仍使用Spring AOP进行编织。
   >
   > 本章讨论基于架构和基于@AspectJ的AOP支持。[下一章](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-api)将讨论较低级别的AOP支持。
   
   AOP在Spring框架中用于:
   
   - 提供声明式企业服务。此类服务中最重要的是[declarative transaction management](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/data-access.html#transaction-declarative).
   - 让用户实现自定义切面，并用AOP补充其对OOP的使用。
   
   > 如果你只对通用声明性服务或其他预包装的声明性中间件服务（例如池）感兴趣，则无需直接使用Spring AOP，并且可以跳过本章的大部分内容。
   
   ## AOP概念
   
   首先让我们定义一些主要的AOP概念和术语。这些术语不是特定于Spring的。不幸的是，AOP术语并不是特别直观。但是，如果使用Spring自己的术语，将会更加令人困惑。
   
   - Aspect:涉及多个类别的关注点的模块化。事务管理是企业Java应用程序中横切关注的一个很好的例子。在Spring AOP中，切面是通过使用常规类（[schema-based approach](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-schema)）或使用@Aspect注解（ [@AspectJ style](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj)）注解的常规类来实现的。
   - Join point:在程序执行过程中的一点，例如方法的执行或异常的处理。在Spring AOP中，连接点始终代表方法的执行。
   - Advice:切面在特定的连接点处采取的操作。不同类型的建议包括“around”，“before”和“after”建议。 （Advice类型将在后面讨论。）包括Spring在内的许多AOP框架都将建议建模为拦截器，并在连接点周围维护一系列拦截器。
   - Pointcut:与join point匹配的谓词。Advice与Pointcut表达式关联，并在与该切入点匹配的任何连接点处运行（例如，执行具有特定名称的方法）。切入点表达式匹配的连接点的概念是AOP的核心，默认情况下，Spring使用AspectJ切入点表达语言。
   - Intruduction:代表类型声明其他方法或字段。 Spring AOP允许你向任何建议对象引入新的接口（和相应的实现）。例如，你可以使用Intruduction使Bean实现IsModified接口，以简化缓存。 （在AspectJ社区中，介绍被称为类型间声明。）
   - Target object:一个或多个切面建议的对象。也称为“建议对象”。由于Spring AOP是使用运行时代理实现的，因此该对象始终是代理对象。
   - AOP proxy:由AOP框架创建的一个对象，用于实现切面合同（建议方法执行等）。在Spring Framework中，AOP代理是JDK动态代理或CGLIB代理。
   - Weaving:将切面与其他应用程序类型或对象链接以创建建议的对象。这可以在编译时（例如，使用AspectJ编译器），加载时或在运行时完成。像其他纯Java AOP框架一样，Spring AOP在运行时执行编织。
   
   Spring AOP包括以下类型的Advice:
   
   - Before advice:在连接点之前运行的建议，但是它不能阻止执行流程继续进行到连接点（除非它引发异常）。
   - After returning advice:在连接点正常完成后要运行的建议（例如，如果方法返回而没有引发异常）。
   - After throwing advice:如果方法因抛出异常而退出，则执行建议。
   - After (finally) advice:无论连接点退出的方式如何（正常或特殊返回），均应执行建议。
   - Around advice:围绕连接点的建议，例如方法调用。这是最有力的建议。环绕建议可以在方法调用之前和之后执行自定义行为。它还负责选择是返回连接点还是通过返回其自身的返回值或引发异常走捷径建议的方法执行。
   
   环绕建议(Around advice)是最通用的建议。由于Spring AOP与AspectJ一样，提供了各种建议类型，因此我们建议你使用功能最弱的建议类型，以实现所需的行为。例如，如果你只需要使用方法的返回值更新缓存，则最好使用返回后的建议(After returing advice)而不是周围的建议(Around advice)，尽管周围的建议可以完成相同的事情。使用最具体的建议类型可提供更简单的编程模型，并减少出错的可能性。例如，你不需要在用于环绕建议的JoinPoint上调用proce()方法，因此，你不会失败。
   
   所有建议参数都是静态类型的，因此你可以使用适当类型（例如，从方法执行返回的值的类型）而不是对象数组的建议参数。
   
   切入点匹配的连接点的概念是AOP的关键，它与仅提供拦截功能的旧技术有所不同。切入点使建议的目标独立于面向对象的层次结构。例如，你可以将提供声明性事务管理的环绕建议应用于跨越多个对象（例如服务层中的所有业务操作）的一组方法。
   
   ## Spring AOP能力和目标
   
   Spring AOP是用纯Java实现的。不需要特殊的编译过程。 Spring AOP不需要控制类加载器的层次结构，因此适合在Servlet容器或应用程序服务器中使用。
   
   Spring AOP当前仅支持方法执行连接点（建议在Spring Bean上执行方法）。尽管可以在不破坏核心Spring AOP API的情况下添加对字段拦截的支持，但并未实现字段拦截。如果需要建议字段访问和更新连接点，请考虑使用诸如AspectJ之类的语言。
   
   Spring AOP的AOP方法不同于大多数其他AOP框架。目的不是提供最完整的AOP实现（尽管Spring AOP相当强大）。相反，其目的是在AOP实现和Spring IoC之间提供紧密的集成，以帮助解决企业应用程序中的常见问题。
   
   因此，例如，通常将Spring Framework的AOP功能与Spring IoC容器结合使用。通过使用常规bean定义语法来配置切面（尽管这允许强大的“自动代理”功能）。这是与其他AOP实现的关键区别。使用Spring AOP不能轻松或高效地完成某些事情，例如建议非常细粒度的对象（通常是域对象）。在这种情况下，AspectJ是最佳选择。但是，我们的经验是，Spring AOP可以为企业Java应用程序中的大多数问题提供出色的解决方案。
   
   Spring AOP从未努力与AspectJ竞争以提供全面的AOP解决方案。我们认为，基于代理的框架（如Spring AOP）和成熟的框架（如AspectJ）都是有价值的，它们是互补的，而不是竞争。 Spring无缝地将Spring AOP和IoC与AspectJ集成在一起，以在基于Spring的一致应用程序架构中支持AOP的所有使用。这种集成不会影响Spring AOP API或AOP Alliance API。 Spring AOP仍然向后兼容。请参阅[下一章](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-api )，以讨论Spring AOP API。
   
   > Spring框架的宗旨之一是非侵入性。这是一个想法，你不应被迫将特定于框架的类和接口引入业务或域模型。但是，在某些地方，Spring Framework确实为你提供了将特定于Spring Framework的依赖项引入代码库的选项。提供此类选项的理由是，在某些情况下，以这种方式阅读或编码某些特定功能可能会变得更加容易。但是，Spring框架（几乎）总是为你提供选择:你可以自由地就哪个选项最适合你的特定用例或场景做出明智的决定。
   >
   > 与本章相关的一种选择是选择哪种AOP框架（以及哪种AOP样式）。你可以选择AspectJ或Spring AOP。你也可以选择@AspectJ注解样式方法或Spring XML配置样式方法。本章选择首先介绍@AspectJ风格的方法不应被视为Spring团队比Spring XML配置风格更喜欢@AspectJ注解风格的方法。
   >
   > 有关每种样式的“为什么和为什么”的更完整讨论，请参见[Choosing which AOP Declaration Style to Use](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-choosing)
   
   ## AOP代理
   
   Spring AOP默认将标准JDK动态代理用于AOP代理。这使得可以代理任何接口（或一组接口）。
   
   Spring AOP也可以使用CGLIB代理。这对于代理类而不是接口是必需的。默认情况下，如果业务对象未实现接口，则使用CGLIB。由于对接口而不是对类进行编程是一种好习惯，因此业务类通常实现一个或多个业务接口。在那些需要建议在接口上未声明的方法或需要将代理对象作为具体类型传递给方法的情况下（在极少数情况下），可以[强制使用CGLIB](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-proxying )。
   
   掌握Spring AOP是基于代理的这一事实很重要。请参阅[Understanding AOP Proxies](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-understanding-aop-proxies)以全面了解此实现细节的实际含义。
   
   ## @AspectJ 支持
   
   @AspectJ是一种将切面声明为带有注解的常规Java类的样式。 @AspectJ样式是[AspectJ project](https://www.eclipse.org/aspectj)在AspectJ 5版本中引入的。 Spring使用AspectJ提供的用于切入点解析和匹配的库来解释与AspectJ 5相同的注解。但是，AOP运行时仍然是纯Spring 
   AOP，并且不依赖于AspectJ编译器或编织器。
   
   > 使用AspectJ编译器和weaver可以使用完整的AspectJ语言，有关在[Using AspectJ with Spring Applications](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-using-aspectj).进行了讨论。
   
   ### 启用@AspectJ支持
   
   要在Spring配置中使用@AspectJ切面，你需要启用Spring支持以基于@AspectJ切面配置Spring AOP，并基于这些切面是否建议对Bean进行自动代理。通过自动代理，我们的意思是，如果Spring确定一个或多个切面建议一个bean，它会自动为该bean生成一个代理来拦截方法调用并确保按需执行建议。
   
   可以使用XML或Java样式的配置来启用@AspectJ支持。无论哪种情况，你都需要确保AspectJ的Aspectjweaver.jar库位于应用程序的类路径（版本1.8或更高版本）上。该库在AspectJ发行版的lib目录或Maven中央仓库中可用。
   
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
   
   启用@AspectJ支持后，Spring会自动检测在应用程序上下文中使用@AspectJ切面（具有@Aspect注解）的类定义的任何bean，并用于配置Spring AOP。接下来的两个示例显示了一个不太有用的切面所需的最小定义。
   
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
   
   切面（使用@Aspect注解的类）可以具有方法和字段，与任何其他类相同。它们还可以包含切入点，建议和介绍（类型间）声明。
   
   > 通过组件扫描自动检测切面
   >
   > 你可以将切面类注册为Spring XML配置中的常规bean，也可以通过类路径扫描自动检测它们——与其他任何Spring管理的bean一样。但是，请注意，@Aspect注解不足以在类路径中进行自动检测。为此，你需要添加一个单独的@Component注解（或者，按照Spring的组件扫描程序的规则，有条件的自定义构造型注解）。
   
   > 向其他切面提供建议？
   >
   > 在Spring AOP中，切面本身不能成为其他切面的建议目标。类上的@Aspect注解将其标记为一个切面，因此将其从自动代理中排除。
   
   ### 声明切入点
   
   切入点确定了感兴趣的连接点，从而使我们能够控制何时执行建议。 Spring AOP仅支持Spring Bean的方法执行连接点，因此你可以将切入点视为与Spring Bean上的方法执行匹配。切入点声明由两部分组成:一个包含名称和任何参数的签名，以及一个切入点表达式，该切入点表达式精确确定我们感兴趣的方法执行。在AOP的@AspectJ注解样式中，常规方法定义提供了切入点签名。使用@Pointcut注解指示切入点表达式（用作切入点签名的方法必须具有void返回类型）。
   
   一个示例可能有助于使切入点签名和切入点表达式之间的区别变得清晰。下面的示例定义一个名为anyOldTransfer的切入点，该切入点与任何名为transfer的方法的执行相匹配:
   
   ```java
   @Pointcut("execution(* transfer(..))") // the pointcut expression
   private void anyOldTransfer() {} // the pointcut signature
   ```
   
   形成@Pointcut注解的值的切入点表达式是一个常规的AspectJ 5切入点表达式。有关AspectJ的切入点语言的完整讨论，请参见 [AspectJ Programming Guide](https://www.eclipse.org/aspectj/doc/released/progguide/index.html)（以及扩展，包括[AspectJ 5 Developer’s Notebook](https://www.eclipse.org/aspectj/doc/released/adk15notebook/index.html)）或有关AspectJ的书籍之一（如Colyer等人的《Eclipse AspectJ》，或由Ramnivas Laddad撰写的《 AspectJ in Action》）。
   
   #### 支持的切入点指示符
   
   Spring AOP支持以下在切入点表达式中使用的AspectJ切入点指示符（PCD）:
   
   - execution:用于匹配方法执行的连接点。这是使用Spring AOP时要使用的主要切入点指示符。
   - within:将匹配限制为某些类型内的连接点（使用Spring AOP时，在匹配类型内声明的方法的执行）。
   - this:限制匹配到连接点（使用Spring AOP时方法的执行），其中bean引用（Spring AOP代理）是给定类型的实例。
   - target:限制匹配到连接点（使用Spring AOP时方法的执行），其中目标对象（代理的应用程序对象）是给定类型的实例。
   - args:将匹配限制为连接点（使用Spring AOP时方法的执行），其中参数是给定类型的实例。
   - @target:限制匹配到连接点（使用Spring AOP时方法的执行）的匹配，其中执行对象的类具有给定类型的注解。
   - @args:限制匹配的连接点（使用Spring AOP时方法的执行），其中传递的实际参数的运行时类型具有给定类型的注解。
   - @within:限制匹配到具有给定注解的类型内的连接点（使用Spring AOP时，使用给定注解的类型中声明的方法的执行）。
   - @annotation:将匹配限制为连接点的主题（在Spring AOP中正在执行的方法）具有给定注解的连接点。
   
   > 其他切入点类型
   >
   > 完整的AspectJ切入点语言支持Spring不支持的其他切入点指示符:调用，获取，设置，预初始化，静态初始化，初始化，处理程序，建议执行，内部代码，cflow，cflowbelow，if，@this和@withincode。在Spring AOP解释的切入点表达式中使用这些切入点指示符会导致抛出IllegalArgumentException。
   >
   > Spring AOP支持的切入点指示符集合可能会在将来的版本中扩展，以支持更多的AspectJ切入点指示符。
   
   由于Spring AOP仅将匹配限制为仅方法执行连接点，因此前面对切入点指示符的讨论所给出的定义比在AspectJ编程指南中所能找到的要窄。此外，AspectJ本身具有基于类型的语义，并且在执行连接点处，此对象和目标都引用同一个对象:执行该方法的对象。
   Spring AOP是基于代理的系统，可区分代理对象本身（绑定到此对象）和代理后面的目标对象（绑定到目标）。
   
   > 由于Spring的AOP框架基于代理的性质，因此根据定义，不会拦截目标对象内的调用。对于JDK代理，只能拦截代理上的公共接口方法调用。使用CGLIB，将拦截代理上的公共方法和受保护的方法调用（必要时甚至包可见的方法）。但是，通常应通过公共签名设计通过代理进行的常见交互。
   >
   > 请注意，切入点定义通常与任何拦截方法匹配。如果严格地将切入点设置为仅公开使用，即使在CGLIB代理方案中通过代理可能存在非公开交互，也需要相应地进行定义。
   >
   > 如果你的拦截需要在目标类中包括方法调用甚至构造函数，请考虑使用Spring驱动的 [native AspectJ weaving](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-aj-ltw)，而不是Spring的基于代理的AOP框架。这构成了具有不同特征的AOP使用模式，因此请确保在做出决定之前先熟悉编织。
   
   Spring AOP还支持其他名为bean的PCD。使用PCD，可以将连接点的匹配限制为特定的命名Spring Bean或一组命名Spring Bean（使用通配符时）。 Bean PCD具有以下形式:
   
   ```java
   bean(idOrNameOfBean)
   ```
   
   idOrNameOfBean令牌可以是任何Spring bean的名称。提供了使用\*字符的有限通配符支持，因此，如果为Spring bean建立了一些命名约定，则可以编写bean PCD表达式来选择它们。与其他切入点指示符一样，bean PCD可以与&&（和），||（或），和！ （否定）运算符一起使用。
   
   > Bean PCD仅在Spring AOP中受支持，而在本机AspectJ编织中不受支持。它是AspectJ定义的标准PCD的特定于Spring的扩展，因此不适用于@Aspect模型中声明的切面。
   >
   > Bean PCD在实例级别（基于Spring bean名称概念构建）上运行，而不是仅在类型级别（基于编织的AOP受其限制）上运行。基于实例的切入点指示符是Spring基于代理的AOP框架的特殊功能，并且与Spring bean工厂紧密集成，因此可以自然而直接地通过名称识别特定bean。
   
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
   
   > `anyPublicOperation` 如果方法执行联接点表示执行，则匹配
   > 任何公共方法。
   >
   > `inTrading` 如果交易模块中有方法执行，则匹配。
   >
   > `tradingOperation` 如果一个方法执行代表该方法中的任何公共方法，则匹配
   > 交易模块。
   
   最佳实践是从较小的命名组件中构建更复杂的切入点表达式，如先前所示。按名称引用切入点时，将应用常规的Java可见性规则（你可以看到相同类型的私有切入点，层次结构中受保护的切入点，任何位置的公共切入点，等等）。可见性不影响切入点匹配。
   
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
   
   Spring AOP用户可能最常使用执行切入点指示符。执行表达式的格式如下：
   
   ```
   execution(modifiers-pattern? ret-type-pattern declaring-type-pattern?name-pattern(param-pattern)
                   throws-pattern?)
   ```
   
   除了返回类型模式（前面的代码片段中的ret-type-pattern），名称模式和参数模式以外的所有部分都是可选的。返回类型模式确定该方法的返回类型必须是什么才能使连接点匹配。\*最常用作返回类型模式。它匹配任何返回类型。仅当方法返回给定类型时，标准类型名称才匹配。名称模式与方法名称匹配。你可以将\*通配符用作名称模式的全部或一部分。如果你指定了声明类型模式，请在其后加上。将其加入名称模式组件。参数模式稍微复杂一些：()匹配不带参数的方法，而（..）匹配任意数量（零个或多个）的参数。（\*）模式与采用任何类型的一个参数的方法匹配。 （\*，String）与采用两个参数的方法匹配。第一个可以是任何类型，而第二个必须是String。有关更多信息，请查阅AspectJ编程指南的“[Language Semantics](https://www.eclipse.org/aspectj/doc/released/progguide/semantics-pointcuts.html)”部分。
   
   以下示例显示了一些常用的切入点表达式：
   
   - 任何公共方法的执行：
   
     ```
     execution(public * *(..))
     ```
   
   - 名称以set开头的任何方法的执行：
   
     ```
     execution(* set*(..))
     ```
   
   - AccountService接口定义的任何方法的执行：
   
     ```
     execution(* com.xyz.service.AccountService.*(..))
     ```
   
   - service包中定义的任何方法的执行：
   
     ```
     execution(* com.xyz.service.*.*(..))
     ```
   
   - service包或其子包之一中定义的任何方法的执行：
   
     ```
     execution(* com.xyz.service..*.*(..))
     ```
   
   - service包中的任何连接点（仅在Spring AOP中执行方法）：
   
     ```
     within(com.xyz.service..*)
     ```
   
   - 代理实现AccountService接口的任何连接点（仅在Spring AOP中方法执行）：
   
     ```
      this(com.xyz.service.AccountService)
     ```
   
   > “this”通常以绑定形式使用。有关如何在建议正文中使代理对象可用的信息，请参阅“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分。
   
   - 目标对象实现AccountService接口的任何连接点（仅在Spring AOP中执行方法）：
   
     ```
     target(com.xyz.service.AccountService)
     ```
   
     > “目标”通常以绑定形式使用。有关如何使目标对象在建议正文中可用的信息，请参见“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分。
   
   - 任何采用单个参数并且在运行时传递的参数为Serializable的连接点（仅在Spring AOP中方法执行）：
   
     ```
     args(java.io.Serializable)
     ```
   
     > “ args”通常以绑定形式使用。有关如何使方法参数在建议正文中可用的信息，请参见“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分。
   
     请注意，此示例中给出的切入点与execution（* *（java.io.Serializable））不同。如果在运行时传递的参数为Serializable，则args版本匹配；如果方法签名声明单个类型为Serializable的参数，则执行版本匹配。
   
   - 目标对象具有@Transactional注解的任何连接点（仅在Spring AOP中执行方法）：
   
     ```
     @target(org.springframework.transaction.annotation.Transactional)
     ```
   
     > 你也可以在绑定形式中使用“ @target”。有关如何使注解对象在建议正文中可用的信息，请参见“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分。
   
   - 目标对象的声明类型具有@Transactional注解的任何连接点（仅在Spring AOP中方法执行）：
   
     ```
     @within(org.springframework.transaction.annotation.Transactional)
     ```
   
     > 你也可以在绑定形式中使用“ @within”。有关如何使注解对象在建议正文中可用的信息，请参见“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分。
   
   - 任何执行方法带有@Transactional注解的联接点（仅在Spring AOP中执行方法）：
   
     ```
     @annotation(org.springframework.transaction.annotation.Transactional)
     ```
   
     > 你也可以在绑定形式中使用“ @annotation”。有关如何使注解对象在建议正文中可用的信息，请参见“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分。
   
   - 任何采用单个参数的联接点（仅在Spring AOP中是方法执行），并且传递的参数的运行时类型具有@Classified注解：
   
     ```
     @args(com.xyz.security.Classified)
     ```
   
     > 你也可以在绑定形式中使用“@args”。请参阅“[Declaring Advice](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-advice)”部分，如何使建议对象中的注解对象可用。
   
   - 名为tradeService的Spring bean上的任何连接点（仅在Spring AOP中执行方法）：
   
     ```
     bean(tradeService)
     ```
   
   - Spring Bean上具有与通配符表达式\* Service匹配的名称的任何连接点（仅在Spring AOP中才执行方法）：
   
     ```
     bean(*Service)
     ```
   
   #### 写好切入点
   
   在编译期间，AspectJ处理切入点优化匹配性能。检查代码并确定每个连接点是否（静态或动态）匹配给定的切入点是一个耗费资源的过程。 （动态匹配意味着无法从静态分析中完全确定匹配，并且在代码中进行测试以确定在运行代码时是否存在实际匹配）。首次遇到切入点声明时，AspectJ将其重写为匹配过程的最佳形式。这是什么意思？基本上，切入点以DNF（析取范式）重写，并且对切入点的组件进行排序，以便首先检查那些资源消耗较低的组件。这意味着你不必担心理解各种切入点指示符的性能，并且可以在切入点声明中以任何顺序提供它们。
   
   但是，AspectJ只能使用所告诉的内容。为了获得最佳的匹配性能，你应该考虑他们试图达到的目标，并在定义中尽可能缩小匹配的搜索空间。现有的指示符自然分为三类之一：同类，作用域和上下文：
   
   - 同类指示者选择一种特殊的连接点：`execution`, `get`, `set`, `call`, and `handler`
   - 作用域指定者选择一组感兴趣的连接点（可能有多种）：`within` and `withincode`
   - 上下文指示符根据上下文匹配（并可选地绑定）：`this`, `target`, and `@annotation`
   
   编写正确的切入点至少应包括前两种类型（种类和作用域）。你可以包括上下文指示符以根据连接点上下文进行匹配，也可以绑定该上下文以在建议中使用。仅提供同类的标识符或仅提供上下文的标识符是可行的，但是由于额外的处理和分析，可能会影响编织性能（使用的时间和内存）。范围指定符的匹配非常快，使用它们的使用意味着AspectJ可以非常迅速地消除不应进一步处理的连接点组。一个好的切入点应尽可能包括一个切入点。
   
   ### 声明Advice
   
   建议与切入点表达式关联，并且在切入点匹配的方法执行之前，之后或周围运行。切入点表达式可以是对命名切入点的简单引用，也可以是就地声明的切入点表达式。
   
   #### Before Advice
   
   你可以使用@Before注解在一个切面中声明先建议：
   
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
   
   你可以使用@Before注解在一个切面中声明先建议(before advice)：
   
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
   
   如果使用就地切入点表达式，则可以将前面的示例重写为以下示例：
   
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
   
   返回建议后，当匹配的方法执行正常返回时运行建议。你可以使用@AfterReturning注解进行声明：
   
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
   
   > 你可以在同一切面内拥有多个建议声明（以及其他成员）。在这些示例中，我们仅显示单个建议声明，以集中每个建议的效果。
   
   有时，你需要在建议正文中访问返回的实际值。你可以使用@AfterReturning的形式绑定返回值以获取该访问，如以下示例所示：
   
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
   
   返回属性中使用的名称必须与advice方法中的参数名称相对应。当方法执行返回时，返回值将作为相应的参数值传递到通知方法。返回子句也将匹配限制为仅返回指定类型值的方法执行（在这种情况下为Object，它匹配任何返回值）。
   
   请注意，返回建议后使用时，不可能返回完全不同的引用。
   
   #### After Throwing Advice
   
   抛出建议后，当匹配的方法执行通过抛出异常退出时运行。你可以使用@AfterThrowing注解进行声明，如以下示例所示：
   
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
   
   通常，你希望通知仅在引发给定类型的异常时才运行，并且你通常还需要访问通知正文中的引发异常。你可以使用throwing属性来限制匹配（如果需要）（否则，请使用Throwable作为异常类型），并将抛出的异常绑定到advice参数。以下示例显示了如何执行此操作：
   
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
   
   throwing属性中使用的名称必须与advice方法中的参数名称相对应。当通过抛出异常退出方法执行时，该异常将作为相应的参数值传递给通知方法。throwing子句还将匹配仅限制为抛出指定类型的异常（在这种情况下为DataAccessException）的方法执行。
   
   #### After (Finally) Advice
   
   当匹配的方法执行退出时，通知（最终）运行。通过使用@After注释声明它。之后必须准备处理正常和异常返回条件的建议。它通常用于释放资源和类似目的。以下示例显示了最终建议后的用法：
   
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
   
   最后一种建议是环绕建议。环绕建议在匹配方法的执行过程中“围绕”运行。它有机会在方法执行之前和之后进行工作，并确定何时，如何或者根本不执行该方法。如果需要以线程安全的方式（例如，启动和停止计时器）在方法执行之前和之后共享状态，则通常使用环绕建议。始终使用最贴合要求的建议形式来满足你（也就是说，可以使用Before Advice，请勿使用环绕建议）。
   
   通过使用@Around批注来声明环绕建议。咨询方法的第一个参数必须是ProceedingJoinPoint类型。在建议的正文中，在ProceedingJoinPoint上调用proceed()会使基础方法执行。前进方法也可以传入Object[]。数组中的值用作方法执行时的参数。
   
   > 当用Object []进行调用proceed时，proceed的行为与AspectJ编译器所编译的环绕通知的行为略有不同。对于使用传统AspectJ语言编写的环绕通知，传递给procced的参数数量必须与传递给环绕通知的参数数量（而不是基础连接点采用的参数数量）相匹配，并且传递给给定的参数位置会取代该值绑定到的实体的连接点处的原始值。
   > Spring采取的方法更简单，并且更适合其基于代理的，仅执行的语义。如果你编译为Spring编写的@AspectJ切面，并在AspectJ编译器和weaver中使用参数进行处理，则只需要意识到这种区别。有一种方法可以在Spring
   > AOP和AspectJ之间100％兼容，并且在[下面有关建议参数的部分](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-ataspectj-advice-params)中对此进行了讨论。
   
   以下示例显示了如何使用环绕建议：
   
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
   
   环绕建议返回的值是该方法的调用者看到的返回值。例如，如果一个简单的缓存切面有一个值，则它可以从缓存中返回一个值，如果没有，则调用proceed()。请注意，在环绕建议的正文中，procced可能被调用一次，多次或完全不被调用。所有这些都是合法的。
   
   #### Advice Parameters
   
   Spring提供了完全类型化的建议，这意味着你可以在建议签名中声明所需的参数（如我们先前在返回和抛出示例中所看到的），而不是一直使用Object[]数组。我们将在本节的后面部分介绍如何使参数和其他上下文值可用于建议主体。首先，我们看一下如何编写通用建议，以了解该建议当前建议的方法。
   
   ##### 访问当前的JoinPoint
   
   任何建议方法都可以将*org.aspectj.lang.JoinPoint*类型的参数声明为它的第一个参数（请注意，需要环绕建议以声明ProceedingJoinPoint类型的第一个参数，该类型是JoinPoint的子类。JoinPoint接口提供了一个几种有用的方法：
   
   - getArgs()：返回方法参数。
   - getThis()：返回代理对象。
   - getTarget()：返回目标对象。
   - getSignature()：返回建议方法的描述。
   - toString()：打印有关建议方法的有用描述。
   
   有关更多详细信息，请参见 [javadoc](https://www.eclipse.org/aspectj/doc/released/runtime-api/org/aspectj/lang/JoinPoint.html)。
   
   ##### 将参数传递给建议
   
   我们已经看到了如何绑定返回的值或异常值（在返回建议和抛出异常建议之后使用）。要使参数值可用于建议正文，可以使用args的绑定形式。如果在args表达式中使用参数名称代替类型名称，则在调用建议时会将相应参数的值作为参数值传递。一个例子应该使这一点更清楚。假设你要建议以Account对象作为第一个参数的DAO操作的执行，并且你需要在建议正文中访问该帐户。可以编写以下内容：
   
   ```java
   @Before("com.xyz.myapp.SystemArchitecture.dataAccessOperation() && args(account,..)")
   public void validateAccount(Account account) {
       // ...
   }
   ```
   
   切入点表达式的args（account，..）部分有两个用途。首先，它将匹配限制为方法采用至少一个参数且传递给该参数的参数为Account实例的那些方法执行。其次，它通过account参数使建议的实际Account对象可用。
   
   有关更多详细信息，请参见AspectJ编程指南。
   
   代理对象（this），目标对象（target）和注解（@ within，@ target，@ annotation和@args）都可以以类似的方式绑定。接下来的两个示例显示如何匹配使用@Auditable注解的方法的执行并提取审计代码：
   
   ```java
   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.METHOD)
   public @interface Auditable {
       AuditCode value();
   }
   ```
   
   这两个示例中的第二个示例显示了与@Auditable方法的执行相匹配的建议：
   
   ```java
   @Before("com.xyz.lib.Pointcuts.anyPublicMethod() && @annotation(auditable)")
   public void audit(Auditable auditable) {
       AuditCode code = auditable.value();
       // ...
   }
   ```
   
   ##### 建议参数和泛型
   
   Spring AOP可以处理类声明和方法参数中使用的泛型。假设具有如下通用类型：
   
   ```java
   public interface Sample<T> {
       void sampleGenericMethod(T param);
       void sampleGenericCollectionMethod(Collection<T> param);
   }
   ```
   
   你可以通过在要拦截方法的参数类型中键入advice参数，将方法类型的拦截限制为某些参数类型：
   
   ```java
   @Before("execution(* ..Sample+.sampleGenericMethod(*)) && args(param)")
   public void beforeSampleMethod(MyType param) {
       // Advice implementation
   }
   ```
   
   这种方法不适用于通用集合。因此，不能按以下方式定义切入点：
   
   ```java
   @Before("execution(* ..Sample+.sampleGenericCollectionMethod(*)) && args(param)")
   public void beforeSampleMethod(Collection<MyType> param) {
       // Advice implementation
   }
   ```
   
   为了使这项工作有效，我们将不得不检查集合的每个元素，这是不合理的，因为我们也无法决定通常如何处理空值。要实现类似的目的，必须将参数键入Collection <？>并手动检查元素的类型。
   
   ##### 确定参数名称
   
   通知调用中的参数绑定依赖于切入点表达式中使用的名称与切入点方法签名中声明的参数名称的匹配。通过Java反射无法获得参数名称，因此Spring AOP使用以下策略来确定参数名称：
   
   - 如果用户已明确指定参数名称，则使用指定的参数名称。建议和切入点注解均具有可选的argNames属性，你可以使用该属性来指定带注解的方法的参数名称。这些参数名称在运行时可用。以下示例显示如何使用argNames属性：
   
     ```java
     @Before(value="com.xyz.lib.Pointcuts.anyPublicMethod() && target(bean) && @annotation(auditable)",
             argNames="bean,auditable")
     public void audit(Object bean, Auditable auditable) {
         AuditCode code = auditable.value();
         // ... use code and bean
     }
     ```
   
     对JoinPoint，ProceedingJoinPoint和JoinPoint.StaticPart类型的第一个参数给予的特殊处理对于不收集任何其他联接点上下文的建议实例特别方便。在这种情况下，你可以省略argNames属性。例如，以下建议无需声明argNames属性：
   
     ```java
     @Before("com.xyz.lib.Pointcuts.anyPublicMethod()")
     public void audit(JoinPoint jp) {
         // ... use jp
     }
     ```
   
     使用'argNames'属性有点笨拙，因此，如果未指定'argNames'属性，Spring AOP将查看该类的调试信息，并尝试从局部变量表中确定参数名称。只要已使用调试信息（至少是“-g：vars”）编译了类，此信息就会存在。启用此标志时进行编译的结果是：（1）你的代码更易于理解（逆向工程），（2）类文件的大小略大（通常无关紧要），（3）删除未使用的本地代码的优化变量不适用于你的编译器。换句话说，通过启用该标志，你应该不会遇到任何困难。
   
     > 如果即使没有调试信息，但是使用了AspectJ(ajc)编译文件，则无需添加argNames属性，因为编译器会保留所需的信息。
   
   - 如果在没有必要调试信息的情况下编译了代码，Spring AOP将尝试推断绑定变量与参数的配对（例如，如果切入点表达式中仅绑定了一个变量，并且建议方法仅接受一个参数，则配对很明显）。如果在给定可用信息的情况下变量的绑定不明确，则抛出AmbiguousBindingException。
   
   - 如果以上所有策略均失败，则抛出IllegalArgumentException。
   
   ##### Proceeding with Arguments
   
   前面我们提到过，我们将描述如何编写一个在Spring AOP和AspectJ中始终有效的参数的proceed调用。解决方案是确保建议签名按顺序绑定每个方法参数。以下示例显示了如何执行此操作：
   
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
   
   在许多情况下，无论如何都要进行此绑定（如上例所示）。
   
   #### Advice Ordering
   
   当多条建议都希望在同一连接点上运行时会发生什么？ Spring AOP遵循与AspectJ相同的优先级规则来确定建议执行的顺序。优先级最高的建议首先“在途中”运行（因此，给定两条优先(before)建议，则优先级最高的建议首先运行）。从连接点“出路”时，优先级最高的建议将最后运行（因此，给定两条后置通知，优先级最高的建议将之后运行）。
   
   当在不同切面定义的两条建议都需要在同一连接点上运行时，除非另行指定，否则执行顺序是不确定的。你可以通过指定优先级来控制执行顺序。通过在方面类中实现*org.springframework.core.Ordered*接口或使用Order注解对其进行注解，可以通过常规的Spring方法来完成。给定两个切面，从Ordered.getValue（）（或注解值）返回较低值的切面具有较高的优先级。
   
   当在相同切面定义的两条建议都需要在同一连接点上运行时，其顺序是未定义的（因为无法通过反射为javac编译的类检索声明顺序）。考虑将这些建议方法折叠为每个方面类中每个连接点的一个建议方法，或将建议重构为单独的方面类。
   
   ### Introductions
   
   Introductions（在AspectJ中称为类型间声明）使切面可以声明建议对象实现给定的接口，并代表那些对象提供该接口的实现。
   
   你可以使用@DeclareParents注解进行介绍。此注解用于声明匹配类型具有新的父代（名称由来）。例如，给定一个名为UsageTracked的接口和该接口名为DefaultUsageTracked的实现，以下切面声明服务接口的所有实现者也都实现了UsageTracked接口（例如，通过JMX公开统计信息）：
   
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
   
   要实现的接口由带注解的字段的类型确定。 @DeclareParents注解的value属性是AspectJ类型的模式。匹配类型的任何bean都实现UsageTracked接口。请注意，在前面示例的前置通知中，服务Bean可以直接用作UsageTracked接口的实现。如果以编程方式访问bean，则应编写以下内容：
   
   ```java
   UsageTracked usageTracked = (UsageTracked) context.getBean("myService");
   ```
   
