---
[Validation, Data Binding, and Type Conversion](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#validation )(Version 5.2.6.RELEASE)
---
考虑将验证作为业务逻辑有利有弊，Spring提供了一种验证（和数据绑定）设计，但并不排除其中任何一个。具体来说，验证不应与Web层绑定，应该易于本地化，并且应该可以插入任何可用的验证器。考虑到这些问题，Spring提供了一个`Validator`合同，该合同既基本又可以在应用程序的每个层中使用。

数据绑定对于使用户输入动态绑定到应用程序的域模型（或用于处理用户输入的任何对象）非常有用。 Spring提供了恰当地命名为DataBinder的功能。 Validator和DataBinder组成了`validation` 包，该`validation` 包主要用于但不限于Web层。

BeanWrapper是Spring框架中的基本概念，并在很多地方使用。但是，你可能不需要直接使用BeanWrapper。但是，因为这是参考文档，所以我们认为可能需要进行一些解释。我们将在本章中解释BeanWrapper，因为如果你要使用它，那么在尝试将数据绑定到对象时最有可能使用它。

Spring的DataBinder和较低级别的BeanWrapper都使用PropertyEditorSupport实现来解析和格式化属性值。 PropertyEditor和PropertyEditorSupport类型是JavaBeans规范的一部分，本章还将对此进行说明。 Spring 3引入了core.convert包，该包提供了常规的类型转换工具，以及用于格式化UI字段值的高级"format"包。你可以将这些包用作PropertyEditorSupport实现的更简单替代方案。本章还将对它们进行讨论。

Spring通过设置基础结构和Spring自己的Validator合同的适配器来支持[Java Bean Validation](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#validation-beanvalidation )。应用程序可以全局启用一次Bean验证，如Java Bean验证中所述，并将其专用于所有验证需求。在Web层中，应用程序可以每个DataBinder进一步注册控制器本地的Spring Validator实例，如[Configuring a `DataBinder`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#validation-binder ),中所述，这对于插入自定义验证逻辑很有用。

## 使用Spring的Validator界面进行验证