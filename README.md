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
* It is not [possible to cancel CompletableFuture](http://stackoverflow.com/questions/23320407/how-to-cancel-java-8-completable-future) if it's blocked.

The project has pretty good test coverage, but testing asynchronous stuff is tricky. if you find any bug, please let me know.

# spring-java8
Converts between Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html) and Java 8 [CompletableFuture](http://download.java.net/lambda/b88/docs/api/java/util/concurrent/CompletableFuture.html)

Import the dependency

    <dependency>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>future-converter-spring-java8</artifactId>
        <version>0.2.2</version>
    </dependency>

And then use

    import static net.javacrumbs.futureconverter.springjava.FutureConverter.*;

    ...
    CompletableFuture<String> completable = toCompletableFuture(listenable);
    ...
    ListenableFuture<String> listenable = toListenableFuture(completable);

# spring-guava
Converts between Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html)
and Guava [ListenableFuture](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/util/concurrent/ListenableFuture.html)


Import the dependency

    <dependency>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>future-converter-spring-guava</artifactId>
        <version>0.2.2</version>
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
        <artifactId>future-converter-java8-guava</artifactId>
        <version>0.2.2</version>
    </dependency>

And then use

    import static net.javacrumbs.futureconverter.java8guava.FutureConverter.*;

    ...
    ListenableFuture<String> guavaListenableFuture = toListenableFuture(completable);
    ...
    CompletableFuture<String> completable = toCompletableFuture(listenable);;


#RxJava
Please note that conversion from/to RxJava Observables is not straightforward.

* When converting Observable to a Future, only one element can be produced by the Observable. If your observable produces
multiple values, please limit it using `observable.take(1)`.
* When converting a Future to an Observable, it's not clear what should happen upon unsubscribe. Since version 0.2.2 RxJava support does
not cancel the Future, since there is no good place to keep track of the subscriptions (there may be multiple subscriptions for any given Future).
* Converting Observable to a Future registers exactly one subscription which is unsubscribed upon Future cancellation.

# rxjava-java8
Converts between [RxJava](https://github.com/Netflix/RxJava) Observables and Java 8 [CompletableFuture](http://download.java.net/lambda/b88/docs/api/java/util/concurrent/CompletableFuture.html)

Import the dependency

    <dependency>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>future-converter-rxjava-java8</artifactId>
        <version>0.2.2</version>
    </dependency>

And then use

    import static net.javacrumbs.futureconverter.java8rx.FutureConverter.*;

    ...
    CompletableFuture<String> completable = toCompletableFuture(observable);
    ...
    Observable<String> observable = toObservable(completable);

# spring-rxjava
Converts between [RxJava](https://github.com/Netflix/RxJava) Observables and Spring 4 [ListenableFuture](http://docs.spring.io/spring/docs/4.0.0.BUILD-SNAPSHOT/javadoc-api/org/springframework/util/concurrent/ListenableFuture.html)

Import the dependency

    <dependency>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>future-converter-spring-rxjava</artifactId>
        <version>0.2.2</version>
    </dependency>

And then use

    import static net.javacrumbs.futureconverter.springrx.FutureConverter.*;

    ...
    ListenableFuture<String> listenable = toListenableFuture(observable);
    ...
    Observable<String> observable = toObservable(listenable);

# guava-rxjava
Converts between [RxJava](https://github.com/Netflix/RxJava) Observables and Guava [ListenableFuture](http://docs.guava-libraries.googlecode.com/git-history/release/javadoc/com/google/common/util/concurrent/ListenableFuture.html)

Import the dependency

    <dependency>
        <groupId>net.javacrumbs.future-converter</groupId>
        <artifactId>future-converter-guava-rxjava</artifactId>
        <version>0.2.2</version>
    </dependency>

And then use

    import static net.javacrumbs.futureconverter.guavarx.FutureConverter.*;

    ...
    ListenableFuture<String> listenable = toListenableFuture(observable);
    ...
    Observable<String> observable = toObservable(listenable);
