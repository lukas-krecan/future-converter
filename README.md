Future Converter [![Build Status](https://travis-ci.org/lukas-krecan/future-converter.png?branch=master)](https://travis-ci.org/lukas-krecan/future-converter) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.javacrumbs.future-converter/future-converter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.javacrumbs.future-converter/future-converter)
================

Converts between various future types, [RxJava](https://github.com/Netflix/RxJava) Single, [RxJava 2](https://github.com/Netflix/RxJava) Single,
Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html),
Java 8 [CompletableFuture](http://download.java.net/lambda/b88/docs/api/java/util/concurrent/CompletableFuture.html) and
Guava [ListenableFuture](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/util/concurrent/ListenableFuture.html).

I am aware of the following quirks:
* It is not [possible to cancel CompletableFuture](http://stackoverflow.com/questions/23320407/how-to-cancel-java-8-completable-future) if it's blocked.

The project has pretty good test coverage, but testing asynchronous stuff is tricky. If you find any bug, please let me know.

# General usage

Import the dependency

```xml
<dependency>
    <groupId>net.javacrumbs.future-converter</groupId>
    <artifactId>future-converter-FROM-TO</artifactId>
    <version>1.1.0</version>
</dependency>
```
where you replace FROM and TO by library you want to use. For example `future-converter-spring-guava`. 
Then you just use static methods from `net.javacrumbs.futureconverter.FROMTO.FutureConverter` class.

## spring-java8
Converts between Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html) and Java 8 [CompletableFuture](http://download.java.net/lambda/b88/docs/api/java/util/concurrent/CompletableFuture.html)

Import the dependency

```xml
<dependency>
    <groupId>net.javacrumbs.future-converter</groupId>
    <artifactId>future-converter-spring-java8</artifactId>
    <version>1.1.0</version>
</dependency>
```

And then use

```java
import static net.javacrumbs.futureconverter.springjava.FutureConverter.*;

...
CompletableFuture<String> completable = toCompletableFuture(listenable);
...
ListenableFuture<String> listenable = toListenableFuture(completable);
```

## spring-guava
Converts between Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html)
and Guava [ListenableFuture](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/util/concurrent/ListenableFuture.html)


Import the dependency

```xml
<dependency>
    <groupId>net.javacrumbs.future-converter</groupId>
    <artifactId>future-converter-spring-guava</artifactId>
    <version>1.1.0</version>
</dependency>
```

And then use

```java
import static net.javacrumbs.futureconverter.springguava.FutureConverter.*;

...
com.google.common.util.concurrent.ListenableFuture<String> guavaListenableFuture
        = toGuavaListenableFuture(springListenableFuture);
...
org.springframework.util.concurrent.ListenableFuture<String> springListenableFuture
        = toSpringListenableFuture(guavaListenableFuture);
```

## java8-guava
Converts between Java 8 [CompletableFuture](http://download.java.net/lambda/b88/docs/api/java/util/concurrent/CompletableFuture.html)
and Guava [ListenableFuture](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/util/concurrent/ListenableFuture.html)


Import the dependency

```xml
<dependency>
    <groupId>net.javacrumbs.future-converter</groupId>
    <artifactId>future-converter-java8-guava</artifactId>
    <version>1.1.0</version>
</dependency>

```

And then use

```java
import static net.javacrumbs.futureconverter.java8guava.FutureConverter.*;

...
ListenableFuture<String> guavaListenableFuture = toListenableFuture(completable);
...
CompletableFuture<String> completable = toCompletableFuture(listenable);;
```

##RxJava
Since version 1.1.0 we are using rx.Single for integration with RxJava

Please note that
* When converting a Future to a Single, we cancle the original future on unsubscribe. If tou need the feature to continue running, use something like `single.toObservable().publish().refCount().toSingle()`
* Converting Single to a Future registers exactly one subscription which is unsubscribed upon Future cancellation.

## rxjava-java8
Converts between [RxJava](https://github.com/Netflix/RxJava) Single and Java 8 [CompletableFuture](http://download.java.net/lambda/b88/docs/api/java/util/concurrent/CompletableFuture.html)

Import the dependency

```xml
<dependency>
    <groupId>net.javacrumbs.future-converter</groupId>
    <artifactId>future-converter-rxjava-java8</artifactId>
    <version>1.1.0</version>
</dependency>
```

And then use

```java
import static net.javacrumbs.futureconverter.java8rx.FutureConverter.*;

...
CompletableFuture<String> completable = toCompletableFuture(single);
...
Single<String> single = toSingle(completable);
```

## spring-rxjava
Converts between [RxJava](https://github.com/Netflix/RxJava) Observables and Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html)

Import the dependency

```xml
<dependency>
    <groupId>net.javacrumbs.future-converter</groupId>
    <artifactId>future-converter-spring-rxjava</artifactId>
    <version>1.1.0</version>
</dependency>
```

And then use

```java
import static net.javacrumbs.futureconverter.springrx.FutureConverter.*;

...
ListenableFuture<String> listenable = toListenableFuture(single);
...
Single<String> single = toSingle(listenable);
```

## guava-rxjava
Converts between [RxJava](https://github.com/Netflix/RxJava) Observables and Guava [ListenableFuture](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/util/concurrent/ListenableFuture.html)

Import the dependency

```xml
<dependency>
    <groupId>net.javacrumbs.future-converter</groupId>
    <artifactId>future-converter-guava-rxjava</artifactId>
    <version>1.1.0</version>
</dependency>
```

And then use

```java
import static net.javacrumbs.futureconverter.guavarx.FutureConverter.*;

...
ListenableFuture<String> listenable = toListenableFuture(single);
...
Single<String> single = toSingle(listenable);
```