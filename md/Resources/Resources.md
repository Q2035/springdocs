Resources(Version 5.2.6.RELEASE)

------

本章介绍了Spring如何处理资源以及如何在Spring中使用资源。它包括以下主题：

- [Introduction](##简介)
- [The Resource Interface](##资源接口)
- [Built-in Resource Implementations](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-implementations)
- [The `ResourceLoader`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-resourceloader)
- [The `ResourceLoaderAware` interface](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-resourceloaderaware)
- [Resources as Dependencies](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-as-dependencies)
- [Application Contexts and Resource Paths](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-app-ctx)

## 简介

不幸的是，Java的标准*java.net.URL*类和用于各种URL前缀的标准处理程序不足以满足所有对低级资源的访问。例如，没有标准化的URL实现可用于访问需要从类路径或相对于ServletContext获得的资源。虽然可以注册用于特殊URL前缀的新处理程序（类似于用于诸如http：的现有前缀的处理程序），但这通常相当复杂，并且URL接口仍然缺少某些理想的功能，例如用于检查是否存在的方法指向的资源。

## 资源接口

Spring的Resource接口旨在成为一种功能更强大的接口，用于抽象化对低级资源的访问。以下清单显示了Resource接口定义：

```java
public interface Resource extends InputStreamSource {

    boolean exists();

    boolean isOpen();

    URL getURL() throws IOException;

    File getFile() throws IOException;

    Resource createRelative(String relativePath) throws IOException;

    String getFilename();

    String getDescription();
}
```

如Resource接口的定义所示，它扩展了InputStreamSource接口。以下清单显示了InputStreamSource接口的定义：

```java
public interface InputStreamSource {

    InputStream getInputStream() throws IOException;
}
```

Resource接口中一些最重要的方法是：

- getInputStream（）：找到并打开资源，返回一个InputStream以便从资源中读取。预期每次调用都返回一个新的InputStream。调用者有责任关闭流
- exist（）：返回一个布尔值，指示此资源是否实际以物理形式存在。
- isOpen（）：返回一个布尔值，指示此资源是否表示具有打开流的句柄。如果为true，则不能多次读取InputStream，必须只读取一次，然后将其关闭以避免资源泄漏。对于所有常规资源实现，返回false，但InputStreamResource除外。
- getDescription（）：返回对此资源的描述，用于在处理资源时用于错误输出。这通常是标准文件名或资源的实际URL。

其他方法可让您获取代表资源的实际URL或File对象（如果基础实现兼容并且支持该功能）。