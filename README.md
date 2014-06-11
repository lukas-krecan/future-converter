Future Converter
================

Converts between various future types, [RxJava](https://github.com/Netflix/RxJava) Observables, Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html)
and Java 8 [CompletableFuture](http://download.java.net/lambda/b88/docs/api/java/util/concurrent/CompletableFuture.html).

Please note that the conversion is not always straightforward. Especially RxObservables are completely different concept than
Futures. Nevertheless, the conversion is more or less possible.

I am aware of the following quirks:

* Observable can produce multiple values. When converting to Future, we take the first value
* It is not [possible to cancel ObservableFuture](http://stackoverflow.com/questions/23320407/how-to-cancel-java-8-completable-future) if it's blocked.

I think that the project has pretty good code coverage, but testing asynchronous stuff is hard. if you find any bug, please let me know.

# spring-rxjava
Converts between [RxJava](https://github.com/Netflix/RxJava) Observables and Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html)

Import the dependency

    <dependency>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>spring-rxjava</artifactId>
        <version>0.0.2</version>
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

    <parent>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>spring-java8</artifactId>
        <version>0.0.2</version>
    </parent>

And then use

    import static net.javacrumbs.futureconverter.springjava.FutureConverter.*;

    ...
    CompletableFuture<String> completable = toCompletableFuture(listenable);
    ...
    ListenableFuture<String> listenable = toListenableFuture(completable);

# rxjava-java8
Converts between [RxJava](https://github.com/Netflix/RxJava) Observables and Java 8 [CompletableFuture](http://download.java.net/lambda/b88/docs/api/java/util/concurrent/CompletableFuture.html)

Import the dependency

    <parent>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>rxjava-java8</artifactId>
        <version>0.0.2</version>
    </parent>

And then use

    import static net.javacrumbs.futureconverter.java8rx.FutureConverter.*;

    ...
    CompletableFuture<String> completable = toCompletableFuture(observable);
    ...
    Observable<String> observable = toObservable(completable);
