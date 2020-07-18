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

