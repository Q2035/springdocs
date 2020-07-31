[Spring AOP APIs](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop-api)

---

上一章通过@AspectJ和基于架构的切面定义描述了Spring对AOP的支持。在本章中，我们讨论了较低级别的Spring AOP API。对于常见的应用程序，我们建议将Spring AOP与AspectJ切入点一起使用，如上一章所述。

## Spring中的Pointcut API

本节描述了Spring如何处理关键切入点概念。

### 概念

Spring的切入点模型使切入点重用不受通知类型的影响。你可以使用相同的切入点来定位不同的通知。

*org.springframework.aop.Pointcut*接口是中央接口，用于将通知定向到特定的类和方法。完整的界面如下：

```java
public interface Pointcut {

    ClassFilter getClassFilter();

    MethodMatcher getMethodMatcher();

}
```

将Pointcut接口分为两部分，可以重用类和方法匹配的部分以及细粒度的合成操作(例如与另一个方法匹配器执行“联合”)。

ClassFilter接口用于将切入点限制为给定的一组目标类。如果matches()方法始终返回true，则将匹配所有目标类。以下清单显示了ClassFilter接口定义：

```java
public interface ClassFilter {

    boolean matches(Class clazz);
}
```

MethodMatcher接口通常更重要。完整的接口如下：

```java
public interface MethodMatcher {

    boolean matches(Method m, Class targetClass);

    boolean isRuntime();

    boolean matches(Method m, Class targetClass, Object[] args);
}
```

matchs(Method，Class)方法用于测试此切入点是否与目标类上的给定方法匹配。创建AOP代理时可以执行此评估，以避免需要对每个方法调用进行测试。如果两个参数的match方法对于给定的方法返回true，并且MethodMatcher的isRuntime()方法返回true，则在每次方法调用时都将调用三个参数的match方法。这样，切入点就可以在执行目标通知之前立即查看传递给方法调用的参数。

大多数MethodMatcher实现都是静态的，这意味着它们的isRuntime()方法返回false。在这种情况下，永远不会调用三参数match方法。

> 如果可能，请尝试使切入点成为静态，从而允许AOP框架在创建AOP代理时缓存切入点评估的结果。

### 切入点的操作

Spring支持切入点上的操作(特别是联合和相交)。

联合表示两个切入点之一匹配的方法。交集是指两个切入点都匹配的方法。联合通常更有用。你可以通过使用*org.springframework.aop.support.Pointcuts*类中的静态方法或在同一包中使用ComposablePointcut类来编写切入点。但是，使用AspectJ切入点表达式通常是一种更简单的方法。

### AspectJ表达切入点

从2.0开始，Spring使用的最重要的切入点类型是*org.springframework.aop.aspectj.AspectJExpressionPointcut*。这是一个切入点，该切入点使用AspectJ提供的库来解析AspectJ切入点表达式字符串。

有关支持的AspectJ切入点原语的讨论，请参见 [previous chapter](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/spring-framework-reference/core.html#aop)。

### 便捷切入点实现

Spring提供了几种方便的切入点实现。你可以直接使用其中一些。其他的则应归入特定于应用程序的切入点中。

**静态切入点**

静态切入点基于方法和目标类，不能考虑方法的参数。静态切入点足以满足大多数用途，并且大部分情况下是最好的。首次调用方法时，Spring只能评估一次静态切入点。之后，无需在每次方法调用时再次评估切入点。

本节的其余部分描述了Spring附带的一些静态切入点实现。

**正则表达式切入点**

指定静态切入点的一种明显方法是正则表达式。除了Spring之外，还有几个AOP框架使之成为可能。*org.springframework.aop.support.JdkRegexpMethodPointcut*是一个通用的正则表达式切入点，它使用JDK中的正则表达式支持。

使用JdkRegexpMethodPointcut类，可以提供模式字符串的列表。如果其中任何一个匹配，则切入点的评估结果为true。(因此，结果实际上是这些切入点的并集)

以下示例显示如何使用JdkRegexpMethodPointcut：

```xml
<bean id="settersAndAbsquatulatePointcut"
        class="org.springframework.aop.support.JdkRegexpMethodPointcut">
    <property name="patterns">
        <list>
            <value>.*set.*</value>
            <value>.*absquatulate</value>
        </list>
    </property>
</bean>
```

Spring提供了一个名为RegexpMethodPointcutAdvisor的便捷类，该类使我们还可以引用一个Advice(请记住，Advice可以是拦截器，前置通知，异常通知等)。在幕后，Spring使用了JdkRegexpMethodPointcut。使用RegexpMethodPointcutAdvisor可以简化织入，因为一个bean封装了切入点和通知，如以下示例所示：

```xml
<bean id="settersAndAbsquatulateAdvisor"
        class="org.springframework.aop.support.RegexpMethodPointcutAdvisor">
    <property name="advice">
        <ref bean="beanNameOfAopAllianceInterceptor"/>
    </property>
    <property name="patterns">
        <list>
            <value>.*set.*</value>
            <value>.*absquatulate</value>
        </list>
    </property>
</bean>
```

你可以将RegexpMethodPointcutAdvisor与任何Advice类型一起使用。

**属性驱动的切入点**

静态切入点的一种重要类型是元数据驱动的切入点。这使用元数据属性的值(通常是源码级别的元数据)。

**动态切入点**

动态切入点比静态切入点代价更高。它们考虑了方法参数以及静态信息。这意味着必须在每次方法调用时对它们进行评估，并且由于参数会有所不同，因此无法缓存结果。

主要示例是control flow切入点。

**Control Flow Pointcut**

弹簧控制流切入点在概念上类似于AspectJ cflow切入点，尽管功能不那么强大。  (当前无法指定一个切入点在与另一个切入点匹配的连接点下执行)控制流切入点与当前调用堆栈匹配。例如，如果连接点是由*com.mycompany.web*包中的方法或SomeCaller类调用的，则可能会触发。通过使用*org.springframework.aop.support.ControlFlowPointcut*类指定控制流切入点。

> 与其他动态切入点相比，控制流切入点在运行时进行评估要昂贵得多。在Java 1.4中，成本大约是其他动态切入点的五倍。

### 切入点超类

Spring提供了有用的切入点超类，以帮助你实现自己的切入点。

因为静态切入点最有用，所以你可能应该子类化StaticMethodMatcherPointcut。这仅需要实现一个抽象方法(尽管你可以覆盖其他方法以自定义行为)。下面的示例演示如何对StaticMethodMatcherPointcut进行子类化：

```java
class TestStaticPointcut extends StaticMethodMatcherPointcut {

    public boolean matches(Method m, Class targetClass) {
        // return true if custom criteria match
    }
}
```

动态切入点也有超类。你可以将自定义切入点与任何通知类型一起使用。

### 自定义切入点

由于Spring AOP中的切入点是Java类，而不是语言功能(如AspectJ)，因此可以声明自定义切入点，无论是静态还是动态。 Spring中的自定义切入点可以任意复杂。但是，如果可以的话，我们建议使用AspectJ切入点表达语言。

> Spring的后续版本可能会提供JAC提供的“语义切入点”的支持-例如，“all methods that change instance variables in the target object”。

## Spring中的Advice API

现在，我们可以检查Spring AOP如何处理通知。

### 通知生命周期

每个通知都是一个Spring bean。通知实例可以在所有通知对象之间共享，或者对于每个通知对象都是唯一的。这对应于每个类或每个实例的通知。

类通知最常用。适用于一般通知，例如交易通知。这些不依赖于代理对象的状态或添加新状态。它们仅作用于方法和参数。

实例通知都适合引入，以支持混合。在这种情况下，通知将状态添加到代理对象。

你可以在同一AOP代理中混合使用共享通知和基于实例的通知。

### Spring中的通知类型

Spring提供了几种通知类型，并且可以扩展以支持任意通知类型。本节介绍基本概念和标准通知类型。

#### 拦截环绕通知

Spring中最基本的通知类型是环绕通知的拦截。

对于使用方法拦截的通知，Spring符合AOP Alliance接口。实现MethodInterceptor和环绕通知的类也应该实现以下接口：

```java
public interface MethodInterceptor extends Interceptor {

    Object invoke(MethodInvocation invocation) throws Throwable;
}
```

invoke()方法的MethodInvocation参数公开了正在调用的方法，目标连接点，AOP代理以及该方法的参数。 invoke()方法应返回调用的结果：连接点的返回值。

以下示例显示了一个简单的MethodInterceptor实现：

```java
public class DebugInterceptor implements MethodInterceptor {

    public Object invoke(MethodInvocation invocation) throws Throwable {
        System.out.println("Before: invocation=[" + invocation + "]");
        Object rval = invocation.proceed();
        System.out.println("Invocation returned");
        return rval;
    }
}
```

请注意对MethodInvocation的proceed()方法的调用。这沿着拦截器链向下到达连接点。大多数拦截器都调用此方法并返回其返回值。但是，MethodInterceptor像任何环绕通知一样，可以返回不同的值或引发异常，而不是调用proceed方法。但是，你不希望在没有充分理由的情况下执行此操作。

> MethodInterceptor实现可与其他符合AOP  Alliance的AOP实现互操作性。本节其余部分讨论的其他通知类型将实现常见的AOP概念，但以特定于Spring的方式。尽管使用最特定的通知类型有一个优势，但是如果你可能想在另一个AOP框架中运行方面，则在通知周围使用MethodInterceptor。请注意，切入点当前无法在框架之间互操作，并且AOP Alliance当前未定义切入点接口。

#### 前置通知

一种更简单的通知类型是前置通知。这不需要MethodInvocation对象，因为它仅在进入方法之前被调用。

前置通知的主要优点在于，无需调用proce()方法，因此，不会因疏忽而未能沿拦截器链继续行进。

以下清单显示了MethodBeforeAdvice接口：

```java
public interface MethodBeforeAdvice extends BeforeAdvice {

    void before(Method m, Object[] args, Object target) throws Throwable;
}
```

(尽管通常的对象适用于字段拦截，并且Spring不太可能实现，Spring的API设计允许先于字段前置通知？[Spring’s API design would allow for field before advice, although the usual objects apply to field interception and it is unlikely for Spring to ever implement it.])

请注意，返回类型为void。通知可以在联接点执行之前插入自定义行为，但不能更改返回值。如果前置通知引发异常，它将中止拦截器链的进一步执行。异常会传播回拦截链。如果未检查或在调用的方法的签名上，则将其直接传递给客户端。否则，它将被AOP代理包装在未经检查的异常中。

以下示例显示了Spring中的前置通知，该通知计算所有方法调用：

```java
public class CountingBeforeAdvice implements MethodBeforeAdvice {

    private int count;

    public void before(Method m, Object[] args, Object target) throws Throwable {
        ++count;
    }

    public int getCount() {
        return count;
    }
}
```

> 前置通知可以被任何切入点使用。

#### 异常通知

如果联接点引发异常，则在联接点返回之后调用异常通知。  Spring提供类型化的异常通知(typed throws advice)。请注意，这意味着*org.springframework.aop.ThrowsAdvice*接口不包含任何方法。它是一个标记接口，用于标识给定的对象实现了一个或多个类型的异常通知方法。这些应采用以下形式：

```java
afterThrowing([Method, args, target], subclassOfThrowable)
```

仅最后一个参数是必需的。方法签名可以具有一个或四个变量，具体取决于通知方法是否对该方法和变量感兴趣。接下来的两个清单显示了作为引发通知示例的类。

如果引发RemoteException(包括从子类)，则调用以下通知：

```java
public class RemoteThrowsAdvice implements ThrowsAdvice {

    public void afterThrowing(RemoteException ex) throws Throwable {
        // Do something with remote exception
    }
}
```

与前面的通知不同，下一个示例声明了四个参数，以便可以访问被调用的方法，方法参数和目标对象。如果抛出ServletException，则调用以下通知：

```java
public class ServletThrowsAdviceWithArguments implements ThrowsAdvice {

    public void afterThrowing(Method m, Object[] args, Object target, ServletException ex) {
        // Do something with all arguments
    }
}
```

最后一个示例说明如何在处理RemoteException和ServletException的单个类中使用这两种方法。可以在单个类中组合任意数量的引发通知方法。以下清单显示了最后一个示例：

```java
public static class CombinedThrowsAdvice implements ThrowsAdvice {

    public void afterThrowing(RemoteException ex) throws Throwable {
        // Do something with remote exception
    }

    public void afterThrowing(Method m, Object[] args, Object target, ServletException ex) {
        // Do something with all arguments
    }
}
```

如果异常通知方法本身引发异常，则它将覆盖原始异常(也就是说，它将更改引发给用户的异常)。重写异常通常是RuntimeException，它与任何方法签名都兼容。但是，如果异常通知方法抛出一个已检查的异常，则它必须与目标方法的已声明异常匹配，因此在某种程度上与特定的目标方法签名耦合。不要抛出与目标方法签名不兼容的未声明的检查异常！

> 异常通知可以与任何切入点一起使用。

#### 最终通知

Spring中的最终通知必须实现*org.springframework.aop.AfterReturningAdvice*接口，以下清单显示：

```java
public interface AfterReturningAdvice extends Advice {

    void afterReturning(Object returnValue, Method m, Object[] args, Object target)
            throws Throwable;
}
```

最终通知可以访问返回值(无法修改)，调用的方法，方法的参数和目标。

最终通知后的以下内容将计数所有未引发异常的成功方法调用：

```java
public class CountingAfterReturningAdvice implements AfterReturningAdvice {

    private int count;

    public void afterReturning(Object returnValue, Method m, Object[] args, Object target)
            throws Throwable {
        ++count;
    }

    public int getCount() {
        return count;
    }
}
```

此通知不会更改执行路径。如果抛出异常，则会将其抛出拦截器链，而不是返回值。

> 最终通知可以与任何切入点一起使用。

#### 引入增强

Spring将引入增强(Intruduction Advice)视为一种特殊的拦截通知。

引入需要实现了以下接口的IntroductionAdvisor和IntroductionInterceptor：

```java
public interface IntroductionInterceptor extends MethodInterceptor {

    boolean implementsInterface(Class intf);
}
```

从AOP Alliance MethodInterceptor接口继承的invoke()方法必须实现引入。也就是说，如果被调用的方法在引入的接口上，则引入拦截器负责处理方法调用，不能调用proceed()。

引入增强不能与任何切入点一起使用，因为它仅适用于类，而不适用于方法级别。你只能通过IntroductionAdvisor使用引入增强，它具有以下方法：

```java
public interface IntroductionAdvisor extends Advisor, IntroductionInfo {

    ClassFilter getClassFilter();

    void validateInterfaces() throws IllegalArgumentException;
}

public interface IntroductionInfo {

    Class<?>[] getInterfaces();
}
```

没有MethodMatcher，因此没有与引入增强相关的Pointcut。只有类过滤是合乎逻辑的。

getInterfaces()方法返回此advisor引入的接口。

在内部使用validateInterfaces()方法来查看引入的接口是否可以由配置的IntroductionInterceptor实现。

考虑一下Spring测试套件中的一个示例，并假设我们想为一个或多个对象引入以下接口：

```java
public interface Lockable {
    void lock();
    void unlock();
    boolean locked();
}
```

这说明了混合。我们希望能够将通知对象强制转换为Lockable，无论它们的类型如何，并调用lock和unlock方法。如果我们调用lock()方法，我们希望所有的setter方法都抛出一个LockedException。因此，我们可以添加一个切面，使对象在不了解对象的情况下不可变：AOP的一个很好的例子。

首先，我们需要一个IntroductionInterceptor来完成繁重的工作。在这种情况下，我们扩展了*org.springframework.aop.support.DelegatingIntroductionInterceptor*便利类。我们可以直接实现IntroductionInterceptor，但是在大多数情况下，最好使用DelegatingIntroductionInterceptor。

DelegatingIntroductionInterceptor旨在将引入的接口的实际实现委派给委派，从而隐藏使用侦听的方式。你可以使用构造函数参数将委托设置为任何对象。默认委托(使用无参数构造函数时)是这个。因此，在下一个示例中，委托是DelegatingIntroductionInterceptor的LockMixin子类。给定一个委托(默认情况下为本身)，DelegatingIntroductionInterceptor实例将查找由委托实现的所有接口(IntroductionInterceptor除外)，并支持针对其中任何一个的介绍。诸如LockMixin的子类可以调用preventInterface(Class  intf)方法来禁止不应公开的接口。但是，无论IntroductionInterceptor准备支持多少个接口，IntroductionAdvisor都会使用控件来实际公开哪些接口。引入的接口隐藏了目标对同一接口的任何实现。

因此，LockMixin扩展了DelegatingIntroductionInterceptor并实现了Lockable本身。超类自动选择可支持Lockable的引入，因此我们不需要指定它。我们可以通过这种方式引入任意数量的接口。

注意锁定实例变量的使用。这有效地将附加状态添加到目标对象中保存的状态。

下面的示例显示示例LockMixin类：

```java
public class LockMixin extends DelegatingIntroductionInterceptor implements Lockable {

    private boolean locked;

    public void lock() {
        this.locked = true;
    }

    public void unlock() {
        this.locked = false;
    }

    public boolean locked() {
        return this.locked;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (locked() && invocation.getMethod().getName().indexOf("set") == 0) {
            throw new LockedException();
        }
        return super.invoke(invocation);
    }

}
```

通常，你无需重写invoke()方法。通常足以满足DelegatingIntroductionInterceptor实现(如果引入了方法，则调用委托方法，否则进行到连接点)。在当前情况下，我们需要添加一个检查：如果处于锁定模式，则不能调用任何setter方法。

所需的引入仅需要保存一个独特的LockMixin实例并指定所引入的接口(在这种情况下，仅是Lockable)。一个更复杂的示例可能引用了引入拦截器(将被定义为原型)。在这种情况下，没有与LockMixin相关的配置，因此我们使用new创建它。以下示例显示了我们的LockMixinAdvisor类：

```java
public class LockMixinAdvisor extends DefaultIntroductionAdvisor {

    public LockMixinAdvisor() {
        super(new LockMixin(), Lockable.class);
    }
}
```

我们可以非常简单地应用此advisor程序，因为它不需要配置。  (但是，如果没有IntroductionAdvisor，则无法使用IntroductionInterceptor。)与Introduction一样，advisor必须是按实例的，因为它是有状态的。对于每个通知对象，我们需要一个LockMixinAdvisor实例，因此需要一个LockMixin实例。advisor程序包含通知对象状态的一部分。

我们可以像其他任何advisor一样，通过使用Advised.addAdvisor()方法或XML配置中的(推荐方式)以编程方式应用此advisor。下面讨论的所有代理创建选择(包括“自动代理创建器”)都可以正确处理介绍和有状态的混合。

## Spring中的顾问(Advisor)API

在Spring中，顾问程序是一个切面，仅包含与切入点表达式关联的单个通知对象。

除了引介的特殊情况外，任何顾问都可以与任何通知一起使用。  *org.springframework.aop.support.DefaultPointcutAdvisor*是最常用的顾问类。它可以与MethodInterceptor，BeforeAdvice或ThrowsAdvice一起使用。

可以在同一AOP代理中的Spring中混合使用顾问和通知类型。例如，你可以在一个代理配置中使用对环绕通知，异常通知以及前置通知的拦截。 Spring自动创建必要的拦截器链。

## 使用ProxyFactoryBean创建AOP代理

如果你将Spring  IoC容器(ApplicationContext或BeanFactory)用于你的业务对象(应该如此！)，则要使用Spring的AOP  FactoryBean实现之一。 (请记住，工厂bean引入了一个间接层，允许它创建其他类型的对象。)

> 看到这里完全不知道这是什么意思，翻下去估计我都会蒙了，这里就先停一下。