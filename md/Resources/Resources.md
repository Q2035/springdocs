Resources(Version 5.2.6.RELEASE)

------

本章介绍了Spring如何处理4 ··资源以及如何在Spring中使用资源。它包括以下主题：

- [Introduction](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-introduction)
- [The Resource Interface](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-resource)
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

其他方法可让你获取代表资源的实际URL或File对象（如果基础实现兼容并且支持该功能）。

当需要资源时，Spring本身广泛使用Resource抽象作为许多方法签名中的参数类型。一些Spring API中的其他方法（例如，各种ApplicationContext实现的构造函数）采用String形式，该字符串以未经修饰或简单的形式用于创建适合于该上下文实现的Resource，或者通过String路径上的特殊前缀，让调用者指定必须创建并使用特定的资源实现。

尽管Spring经常使用Resource接口，但实际上，在你自己的代码中单独用作通用实用工具类来访问资源也非常有用，即使你的代码不了解或不关心其他任何东西春天的一部分。虽然这将你的代码耦合到Spring，但实际上仅将其耦合到这套实用程序类，它们充当URL的更强大替代，并且可以被视为等同于你将用于此目的的任何其他库。

> Resources抽象不能替代功能(The Resource abstraction does not replace functionality)。它尽可能地包装它。例如，UrlResource包装一个URL，然后使用包装的URL进行工作。

## 内置Resource实现

Spring包含以下Resource实现：

- [`UrlResource`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-implementations-urlresource)
- [`ClassPathResource`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-implementations-classpathresource)
- [`FileSystemResource`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-implementations-filesystemresource)
- [`ServletContextResource`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-implementations-servletcontextresource)
- [`InputStreamResource`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-implementations-inputstreamresource)
- [`ByteArrayResource`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-implementations-bytearrayresource)

### `UrlResource`

UrlResource包装了*java.net.URL*，可用于访问通常可以通过URL访问的任何对象，例如文件，HTTP目标，FTP目标等。所有URL都具有标准化的String表示形式，因此使用适当的标准化前缀来指示另一种URL类型。其中包括file：用于访问文件系统路径，http：用于通过HTTP协议访问资源，ftp：用于通过FTP访问资源等。

UrlResource是由Java代码通过显式使用UrlResource构造函数创建的，但通常在调用带有String参数表示路径的API方法时隐式创建。对于后一种情况，JavaBeans PropertyEditor最终决定要创建哪种类型的资源。如果路径字符串包含众所周知的前缀（例如，classpath :），则它将为该前缀创建适当的专用资源。但是，如果它不能识别前缀，则假定该字符串是标准URL字符串并创建一个UrlResource。

### `ClassPathResource`

此类表示应从类路径获取的资源。它使用线程上下文类加载器，给定的类加载器或给定的类来加载资源。

如果类路径资源驻留在文件系统中，而不是驻留在jar中并且尚未（通过servlet引擎或任何环境将其扩展到）文件系统的类路径资源驻留在文件系统中，则此Resource实现以*java.io.File*支持解析。为了解决这个问题，各种Resource实现始终支持将解析作为*java.net.URL*。

Java代码通过显式使用ClassPathResource构造函数来创建ClassPathResource，但通常在调用带有String参数表示路径的API方法时隐式创建ClassPathResource。对于后一种情况，JavaBeans PropertyEditor可以识别字符串路径上的特殊前缀classpath：，并在这种情况下创建ClassPathResource。

### `FileSystemResource`

这是*java.io.File*和*java.nio.file.Path*句柄的Resource实现。它支持解析为文件和URL。

### `ServletContextResource`

这是ServletContext资源的Resource实现，它解释相关Web应用程序根目录中的相对路径。

它始终支持流访问和URL访问，但仅在扩展Web应用程序档案且资源实际位于文件系统上时才允许*java.io.File*访问。它是在文件系统上扩展还是直接扩展，或者直接从JAR或其他类似数据库（可以想到的）中访问，实际上取决于Servlet容器。

### `InputStreamResource`

InputStreamResource是给定InputStream的Resource实现。仅当没有特定的资源实现适用时才应使用它。特别是，在可能的情况下，最好选择ByteArrayResource或任何基于文件的Resource实现。

与其他Resource实现相反，这是一个已经打开的资源的描述符。因此，它从isOpen（）返回true。如果需要将资源描述符保留在某个地方，或者需要多次读取流，请不要使用它。

### `ByteArrayResource`

这是给定字节数组的Resource实现。它为给定的字节数组创建一个ByteArrayInputStream。这对于从任何给定的字节数组加载内容很有用，而不必求助于一次性InputStreamResource。

## `ResourceLoader`

ResourceLoader接口旨在由可以返回（即加载）Resource实例的对象实现。以下清单显示了ResourceLoader接口定义：

```java
public interface ResourceLoader {

    Resource getResource(String location);
}
```

所有应用程序上下文均实现ResourceLoader接口。因此，所有应用程序上下文都可用于获取资源实例。

当你在特定的应用程序上下文中调用getResource（），并且指定的位置路径没有特定的前缀时，你将获得适合该特定应用程序上下文的Resource类型。例如，假设针对ClassPathXmlApplicationContext实例执行了以下代码片段：

```java
Resource template = ctx.getResource("some/resource/path/myTemplate.txt");
```

针对ClassPathXmlApplicationContext，该代码返回ClassPathResource。如果对FileSystemXmlApplicationContext实例执行了相同的方法，则它将返回FileSystemResource。对于WebApplicationContext，它将返回ServletContextResource。类似地，它将为每个上下文返回适当的对象。

结果，你可以以适合特定应用程序上下文的方式加载资源。

另一方面，你也可以通过指定特殊的classpath：前缀来强制使用ClassPathResource，而与应用程序上下文类型无关，如下例所示：

```java
Resource template = ctx.getResource("classpath:some/resource/path/myTemplate.txt");
```

同样，你可以通过指定任何标准*java.net.URL*前缀来强制使用UrlResource。以下两个示例使用file和http前缀：

```java
Resource template = ctx.getResource("file:///some/resource/path/myTemplate.txt");
```

```java
Resource template = ctx.getResource("https://myhost.com/resource/path/myTemplate.txt");
```

下表总结了将String对象转换为Resource对象的策略：

| Prefix     | Example                          | Explanation                                                  |
| ---------- | -------------------------------- | ------------------------------------------------------------ |
| classpath: | `classpath:com/myapp/config.xml` | Loaded from the classpath.                                   |
| file:      | `file:///data/config.xml`        | Loaded as a `URL` from the filesystem. See also [`FileSystemResource` Caveats](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources-filesystemresource-caveats). |
| http:      | `https://myserver/logo.png`      | Loaded as a `URL`.                                           |
| (none)     | `/data/config.xml`               | Depends on the underlying `ApplicationContext`.              |

## `ResourceLoaderAware`

ResourceLoaderAware接口是一个特殊的回调接口，用于标识期望随ResourceLoader参考一起提供的组件。以下清单显示了ResourceLoaderAware接口的定义：

```java
public interface ResourceLoaderAware {

    void setResourceLoader(ResourceLoader resourceLoader);
}
```

当一个类实现ResourceLoaderAware并部署到应用程序上下文中（作为Spring托管的bean）时，该类被应用程序上下文识别为ResourceLoaderAware。然后，应用程序上下文调用setResourceLoader（ResourceLoader），将自身作为参数提供（请记住，Spring中的所有应用程序上下文均实现ResourceLoader接口）。

由于ApplicationContext是ResourceLoader，因此bean也可以实现ApplicationContextAware接口，并直接使用提供的应用程序上下文来加载资源。但是，通常，如果需要的话，最好使用专用的ResourceLoader接口。该代码将仅耦合到资源加载接口（可以视为实用程序接口），而不耦合到整个Spring ApplicationContext接口。

在应用程序组件中，你还可以依靠自动装配ResourceLoader来实现ResourceLoaderAware接口。 “传统”构造函数和byType自动装配模式（如“[Autowiring Collaborators](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-factory-autowire)”中所述）能够分别为构造函数参数或setter方法参数提供ResourceLoader。为了获得更大的灵活性（包括自动装配字段和多个参数方法的能力），请考虑使用基于注解的自动装配功能。在这种情况下，只要有问题的字段，构造函数或方法带有@Autowired注解，ResourceLoader就会自动连接到需要ResourceLoader类型的字段，构造函数参数或方法参数中。有关更多信息，请参见[Using `@Autowired`](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#beans-autowired-annotation).

## 资源依赖

如果Bean本身将通过某种动态过程来确定和提供资源路径，那么对于Bean来说，使用ResourceLoader接口加载资源可能是有意义的。例如，考虑加载某种模板，其中所需的特定资源取决于用户的角色。如果资源是静态的，则有必要完全消除对ResourceLoader接口的使用，让Bean公开所需的Resource属性，并期望将其注入其中。

然后注入这些属性的琐事是，所有应用程序上下文都注册并使用了特殊的JavaBeans 
PropertyEditor，它可以将String路径转换为Resource对象。因此，如果myBean具有资源类型的模板属性，则可以为该资源配置一个简单的字符串，如以下示例所示：

```xml
<bean id="myBean" class="...">
    <property name="template" value="some/resource/path/myTemplate.txt"/>
</bean>
```

请注意，资源路径没有前缀。因此，由于应用程序上下文本身将用作ResourceLoader，因此根据上下文的确切类型，通过ClassPathResource，FileSystemResource或ServletContextResource加载资源本身。

如果需要强制使用特定的资源类型，则可以使用前缀。以下两个示例显示了如何强制ClassPathResource和UrlResource（后者用于访问文件系统文件）：

```xml
<property name="template" value="classpath:some/resource/path/myTemplate.txt">  
```

```xml
<property name="template" value="file:///some/resource/path/myTemplate.txt"/>
```

## 应用程序上下文和资源路径

