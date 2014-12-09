Future Converter
================

Converts between various future types, [RxJava](https://github.com/Netflix/RxJava) Observables,
Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html),
Java 8 [CompletableFuture](http://download.java.net/lambda/b88/docs/api/java/util/concurrent/CompletableFuture.html) and
Guava [ListenableFuture](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/util/concurrent/ListenableFuture.html).

Please note that the conversion is not always straightforward. Especially RxObservables are completely different concept than
Futures. Nevertheless, the conversion is more or less possible.

I am aware of the following quirks:

* Observable can produce multiple values. When converting to Future, we take the first value
* It is not [possible to cancel ObservableFuture](http://stackoverflow.com/questions/23320407/how-to-cancel-java-8-completable-future) if it's blocked.

The project has pretty good test coverage, but testing asynchronous stuff is tricky. if you find any bug, please let me know.

# spring-rxjava
Converts between [RxJava](https://github.com/Netflix/RxJava) Observables and Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html)

Import the dependency

    <dependency>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>spring-rxjava</artifactId>
        <version>0.0.4</version>
    </dependency>

And then use

    import static net.javacrumbs.futureconverter.springrx.FutureConverter.*;

    ...
    ListenableFuture<String> listenable = toListenableFuture(observable);
    ...
    Observable<String> observable = toObservable(listenable);




# spring-java8
Converts between Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html) and Java 8 [CompletableFuture](http://download.java.net/lambda/b88/docs/api/java/util/concurrent/CompletableFuture.html)

Import the dependency

    <dependency>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>spring-java8</artifactId>
        <version>0.0.4</version>
    </dependency>

And then use

    import static net.javacrumbs.futureconverter.springjava.FutureConverter.*;

    ...
    CompletableFuture<String> completable = toCompletableFuture(listenable);
    ...
    ListenableFuture<String> listenable = toListenableFuture(completable);

# rxjava-java8
Converts between [RxJava](https://github.com/Netflix/RxJava) Observables and Java 8 [CompletableFuture](http://download.java.net/lambda/b88/docs/api/java/util/concurrent/CompletableFuture.html)

Import the dependency

    <dependency>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>rxjava-java8</artifactId>
        <version>0.0.4</version>
    </dependency>

And then use

    import static net.javacrumbs.futureconverter.java8rx.FutureConverter.*;

    ...
    CompletableFuture<String> completable = toCompletableFuture(observable);
    ...
    Observable<String> observable = toObservable(completable);

# spring-guava
Converts between Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html)
and Guava [ListenableFuture](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/util/concurrent/ListenableFuture.html)


Import the dependency

    <dependency>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>spring-guava</artifactId>
        <version>0.0.4</version>
    </dependency>

And then use

    import static net.javacrumbs.futureconverter.springguava.FutureConverter.*;

    ...
    com.google.common.util.concurrent.ListenableFuture<String> guavaListenableFuture
            = toGuavaListenableFuture(springListenableFuture);
    ...
    org.springframework.util.concurrent.ListenableFuture<String> springListenableFuture
            = toSpringListenableFuture(guavaListenableFuture);

# java8-guava
Converts between Java 8 [CompletableFuture](http://download.java.net/lambda/b88/docs/api/java/util/concurrent/CompletableFuture.html)
and Guava [ListenableFuture](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/util/concurrent/ListenableFuture.html)


Import the dependency

    <dependency>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>java8-guava</artifactId>
        <version>0.0.4</version>
    </dependency>

And then use

    import static net.javacrumbs.futureconverter.java8guava.FutureConverter.*;

    ...
    ListenableFuture<String> guavaListenableFuture = toListenableFuture(completable);
    ...
    CompletableFuture<String> completable = toCompletableFuture(listenable);;
