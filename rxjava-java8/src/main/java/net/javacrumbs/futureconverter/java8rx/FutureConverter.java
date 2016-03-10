/**
 * Copyright 2009-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.futureconverter.java8rx;

import net.javacrumbs.futureconverter.common.internal.OriginSource;
import net.javacrumbs.futureconverter.common.internal.SettableFuture;
import net.javacrumbs.futureconverter.guavacommon.Java8FutureUtils;
import net.javacrumbs.futureconverter.guavacommon.RxJavaFutureUtils;
import rx.Observable;

import java.util.concurrent.CompletableFuture;

/**
 * Converts between Java 8 {@link java.util.concurrent.CompletableFuture} and RxJava {@link rx.Observable}
 */
public class FutureConverter {

    /**
     * Converts {@link rx.Observable} to {@link java.util.concurrent.CompletableFuture}. Takes
     * only the first value produced by observable.
     */
    public static <T> CompletableFuture<T> toCompletableFuture(Observable<T> observable) {
        if (observable instanceof OriginSource && ((OriginSource) observable).getOrigin() instanceof CompletableFuture) {
            return (CompletableFuture<T>) ((OriginSource) observable).getOrigin();
        } else {
            SettableFuture<T> completableFuture = Java8FutureUtils.createSettableFuture(observable);
            RxJavaFutureUtils.waitForResults(observable, completableFuture);
            return (CompletableFuture<T>) completableFuture;
        }
    }

    /**
     * Converts {@link java.util.concurrent.CompletableFuture} to {@link rx.Observable}.
     * The original future is NOT canceled upon unsubscribe.
     */
    public static <T> Observable<T> toObservable(CompletableFuture<T> completableFuture) {
        if (completableFuture instanceof OriginSource && ((OriginSource) completableFuture).getOrigin() instanceof Observable) {
            return (Observable<T>) ((OriginSource) completableFuture).getOrigin();
        } else {
            return RxJavaFutureUtils.createObservable(Java8FutureUtils.createCommonListenable(completableFuture));
        }
    }
}

