---
[Resources](https://docs.spring.io/spring/docs/5.2.6.RELEASE/spring-framework-reference/core.html#resources )(Version 5.2.6.RELEASE)
---

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

- getInputStream()：找到并打开资源，返回一个InputStream以便从资源中读取。预期每次调用都返回一个新的InputStream。调用者有责任关闭流
- exist()：返回一个布尔值，指示此资源是否实际以物理形式存在。
- isOpen()：返回一个布尔值，指示此资源是否表示具有打开流的句柄。如果为true，则不能多次读取InputStream，必须只读取一次，然后将其关闭以避免资源泄漏。对于所有常规资源实现，返回false，但InputStreamResource除外。
- getDescription()：返回对此资源的描述，用于在处理资源时用于错误输出。这通常是标准文件名或资源的实际URL。

其他方法可让你获取代表资源的实际URL或File对象（如果基础实现兼容并且支持该功能）。

当需要资源时，Spring本身广泛使用Resource抽象作为许多方法签名中的参数类型。一些Spring API中的其他方法（例如，各种ApplicationContext实现的构造函数）采用String形式，该字符串以未经修饰或简单的形式用于创建适合于该上下文实现的Resource，或者通过String路径上的特殊前缀，让调用者指定必须创建并使用特定的资源实现。

尽管Spring经常使用Resource接口，但实际上，在你自己的代码中单独用作通用实用工具类来访问资源也非常有用，即使你的代码不了解或不关心其他任何Spring的一部分。虽然这将你的代码耦合到Spring，但实际上仅将其耦合到这套实用程序类，它们充当URL的更强大替代，并且可以被视为等同于你将用于此目的的任何其他库。

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

与其他Resource实现相反，这是一个已经打开的资源的描述符。因此，它从isOpen()返回true。如果需要将资源描述符保留在某个地方，或者需要多次读取流，请不要使用它。

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

当你在特定的应用程序上下文中调用getResource()，并且指定的位置路径没有特定的前缀时，你将获得适合该特定应用程序上下文的Resource类型。例如，假设针对ClassPathXmlApplicationContext实例执行了以下代码片段：

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

本节介绍如何使用资源创建应用程序上下文，包括使用XML的快捷方式，如何使用通配符以及其他详细信息。

### 构造应用程序上下文

应用程序上下文构造函数（针对特定的应用程序上下文类型）通常采用字符串或字符串数组作为资源的位置路径，例如构成上下文定义的XML文件。

当位置路径没有前缀时，从该路径构建并用于加载Bean定义的特定Resource类型取决于特定应用程序上下文，并且适用于该特定应用程序上下文。例如，考虑以下示例，该示例创建一个ClassPathXmlApplicationContext：

```java
ApplicationContext ctx = new ClassPathXmlApplicationContext("conf/appContext.xml");
```

由于使用了ClassPathResource，因此从类路径中加载了Bean定义。但是，请考虑以下示例，该示例创建一个FileSystemXmlApplicationContext：

```java
ApplicationContext ctx =
    new FileSystemXmlApplicationContext("conf/appContext.xml");
```

现在，bean定义是从文件系统位置（在这种情况下，是相对于当前工作目录）加载的。

请注意，在位置路径上使用特殊的类路径前缀或标准URL前缀会覆盖为加载定义而创建的默认资源类型。考虑以下示例：

```java
ApplicationContext ctx =
    new FileSystemXmlApplicationContext("classpath:conf/appContext.xml");
```

使用FileSystemXmlApplicationContext从类路径加载bean定义。但是，它仍然是FileSystemXmlApplicationContext。如果随后将其用作ResourceLoader，则任何无前缀的路径仍将视为文件系统路径。

#### 构造ClassPathXmlApplicationContext实例-快捷方式

ClassPathXmlApplicationContext公开了许多构造函数以启用方便的实例化。基本思想是，你只能提供一个字符串数组，该字符串数组仅包含XML文件本身的文件名（不包含前导路径信息），并且还提供一个Class。然后，ClassPathXmlApplicationContext从提供的类中派生路径信息。

请考虑以下目录布局：

```
com/
  foo/
    services.xml
    daos.xml
    MessengerService.class
```

以下示例显示如何实例化由在名为service.xml和daos.xml（位于类路径中）的文件中定义的bean组成的ClassPathXmlApplicationContext实例：

```java
ApplicationContext ctx = new ClassPathXmlApplicationContext(
    new String[] {"services.xml", "daos.xml"}, MessengerService.class);
```

有关各种构造函数的详细信息，请参见[`ClassPathXmlApplicationContext`](https://docs.spring.io/spring-framework/docs/5.2.6.RELEASE/javadoc-api/org/springframework/jca/context/SpringContextResourceAdapter.html)javadoc。

### 应用程序上下文构造函数资源路径中的通配符

应用程序上下文构造函数值中的资源路径可以是简单路径（如先前所示），每个路径都具有到目标资源的一对一映射，或者可以包含特殊的“ classpath*：”前缀或内部Ant样式的常规表达（通过使用Spring的PathMatcher实用程序进行匹配）。后者也是有效的通配符。

这种机制的一种用途是当你需要进行组件样式的应用程序组装时。所有组件都可以将上下文定义片段“发布”到一个众所周知的位置路径，并且当使用前缀为classpath *：的相同路径创建最终应用程序上下文时，所有组件片段都会被自动拾取。

请注意，此通配符特定于在应用程序上下文构造函数中使用资源路径（或当你直接使用PathMatcher实用工具类层次结构时），并且在构造时已解析。它与Resource类型本身无关。你不能使用classpath*前缀来构造实际的Resource，因为资源仅指向一个资源。

#### Ant风格模式

路径位置可以包含Ant样式的模式，如以下示例所示：

```
/WEB-INF/*-context.xml
com/mycompany/**/applicationContext.xml
file:C:/some/path/*-context.xml
classpath:com/mycompany/**/applicationContext.xml
```

当路径位置包含Ant样式的模式时，解析程序将遵循更复杂的过程来尝试解析通配符。它为到达最后一个非通配符段的路径生成资源，并从中获取URL。如果此URL不是jar：URL或特定于容器的变体（例如WebLogic中的zip：，WebSphere中的wsjar等），则从中获取j*ava.io.File*并将其用于遍历文件系统。对于jar URL，解析器可以从中获取*java.net.JarURLConnection*，也可以手动解析jar URL，然后遍历jar文件的内容以解析通配符。

**对可移植性的影响**

如果指定的路径已经是一个文件URL（由于基本ResourceLoader是一个文件系统，所以它是隐式的，或者是显式的），则保证通配符可以完全可移植的方式工作。

如果指定的路径是类路径位置，则解析器必须通过调用Classloader.getResource()获得最后的非通配符路径段URL。由于这只是路径的一个节点（而不是末尾的文件），因此实际上（在ClassLoader javadoc中）未定义确切返回的是哪种URL。实际上，它始终是代表目录的*java.io.File*（类路径资源在其中解析到文件系统位置）或某种jar URL（类路径资源在jar上解析）。尽管如此，此操作仍存在可移植性问题。

如果为最后一个非通配符段获取了jar URL，则解析程序必须能够从中获取*java.net.JarURLConnection*或手动解析jar URL，以便能够遍历jar的内容并解析通配符。这在大多数环境中确实有效，但在其他环境中则无效，因此我们强烈建议你在依赖特定环境之前，对来自jars的资源的通配符解析进行彻底测试。

#### classpath*:前缀

在构造基于XML的应用程序上下文时，位置字符串可以使用特殊的classpath *:前缀，如以下示例所示：

```java
ApplicationContext ctx =
    new ClassPathXmlApplicationContext("classpath*:conf/appContext.xml");
```

这个特殊的前缀指定必须获取与给定名称匹配的所有类路径资源（内部，这实际上是通过调用ClassLoader.getResources（…）发生的），然后合并形成最终的应用程序上下文定义。

> 通配符类路径依赖于基础类加载器的getResources()方法。由于当今大多数应用程序服务器都提供自己的类加载器实现，因此行为可能有所不同，尤其是在处理jar文件时。检查classpath *是否有效的一个简单测试是使用classloader从classpath的jar中加载文件：
>
> ```java
> getClass().getClassLoader().getResources("<someFileInsideTheJar>")
> ```
>
> 尝试对具有相同名称但位于两个不同位置的文件进行此测试。如果返回了不合适的结果，请检查应用程序服务器文档中可能会影响类加载器行为的设置。

你还可以在其余位置路径中将classpath *：前缀与PathMatcher模式结合使用（例如，classpath *：META-INF/ *-beans.xml）。在这种情况下，解析策略非常简单：在最后一个非通配符路径段上使用ClassLoader.getResources()调用，以获取类加载器层次结构中的所有匹配资源，然后从每个资源获取相同的PathMatcher解析前面描述的策略用于通配符子路径。

#### 有关通配符的其他说明

请注意，当classpath \*：与Ant样式的模式结合使用时，除非模式文件实际驻留在文件系统中，否则在模式启动之前，它只能与至少一个根目录可靠地一起工作。这意味着诸如`classpath \*:\*.xml`之类的模式可能不会从jar文件的根目录检索文件，而只会从扩展目录的根目录检索文件。

Spring检索类路径条目的能力源自JDK的ClassLoader.getResources()方法，该方法仅返回文件系统中的空字符串位置（指示可能要搜索的根目录）。Spring还会评估jar文件中的URLClassLoader运行时配置和*java.class.path*清单，但这不能保证会导致可移植行为。

> 扫描类路径包需要在类路径中存在相应的目录条目。使用Ant构建JAR时，请勿激活JAR任务的仅文件开关。此外，在某些环境中，基于安全策略，可能不会公开类路径目录-例如，在JDK1.7.0_45及更高版本上的独立应用程序（要求在清单中设置“受信任的库”。请参阅https://stackoverflow.com/questions/19394570/java-jre-7u45-breaks-classloader-getresources）。
>
> 在JDK 9的模块路径（Jigsaw）上，Spring的类路径扫描通常可以正常进行。强烈建议在此处将资源放入专用目录，以避免在搜索jar文件根目录级别时出现上述可移植性问题。

具有Ant样式的classpath：如果要搜索的根包在多个类路径位置可用，则不能保证资源找到匹配的资源。考虑以下资源位置示例：

```
com/mycompany/package1/service-context.xml
```

现在考虑某人可能用来尝试找到该文件的Ant样式的路径：

```
classpath:com/mycompany/**/service-context.xml
```

这样的资源可能只在一个位置，但是当使用诸如上一示例的路径尝试对其进行解析时，解析器将处理`getResource("com/mycompany")`返回的（第一个）URL。如果此基本包节点存在于多个类加载器位置，则实际的最终资源可能不存在。因此，在这种情况下，你应该首选使用具有相同Ant样式模式的classpath*：，该模式将搜索包含根包的所有类路径位置。

### `FileSystemResource` 注意事项

未附加到FileSystemApplicationContext的FileSystemResource（即，当FileSystemApplicationContext不是实际的ResourceLoader时）将按你期望的方式处理绝对路径和相对路径。相对路径是相对于当前工作目录的，而绝对路径是相对于文件系统的根的。

但是，出于向后兼容性（历史）的原因，当FileSystemApplicationContext是ResourceLoader时，此情况会更改。 FileSystemApplicationContext强制所有附加的FileSystemResource实例将所有位置路径都视为相对位置，无论它们是否以前斜杠开头。实际上，这意味着以下示例是等效的：

```java
ApplicationContext ctx =
    new FileSystemXmlApplicationContext("conf/context.xml");
```

```java
ApplicationContext ctx =
    new FileSystemXmlApplicationContext("/conf/context.xml");
```

以下示例也是等效的（尽管使它们有所不同是有意义的，因为一种情况是相对的，另一种情况是绝对的）：

```java
FileSystemXmlApplicationContext ctx = ...;
ctx.getResource("some/resource/path/myTemplate.txt");
```

```java
FileSystemXmlApplicationContext ctx = ...;
ctx.getResource("/some/resource/path/myTemplate.txt");
```

在实践中，如果需要真正的绝对文件系统路径，则应避免将绝对路径与FileSystemResource或FileSystemXmlApplicationContext一起使用，并通过使用file：URL前缀来强制使用UrlResource。以下示例显示了如何执行此操作：

```java
// actual context type doesn't matter, the Resource will always be UrlResource
ctx.getResource("file:///some/resource/path/myTemplate.txt");
```

```java
// force this FileSystemXmlApplicationContext to load its definition via a UrlResource
ApplicationContext ctx =
    new FileSystemXmlApplicationContext("file:///conf/context.xml");
```

> 翻译：侧边翻译
>
> 校正：靓仔Q
>
> 时间：2020.5.16