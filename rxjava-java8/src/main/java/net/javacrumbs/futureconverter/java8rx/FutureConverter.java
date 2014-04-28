/**
 * Copyright 2009-2013 the original author or authors.
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

import rx.Observable;

import java.util.concurrent.CompletableFuture;

/**
 * Converts between Java 8 {@link java.util.concurrent.CompletableFuture} and RxJava {@link rx.Observable}
 */
public class FutureConverter {

    /**
     * Converts {@link rx.Observable} to {@link java.util.concurrent.CompletableFuture}. Takes
     * only the first value produced by observable.
     *
     * @param observable
     * @param <T>
     * @return
     */
    public static <T> CompletableFuture<T> toCompletableFuture(Observable<T> observable) {
        if (observable instanceof CompletableFutureObservable) {
            return ((CompletableFutureObservable<T>) observable).getCompletableFuture();
        } else {
            return new ObservableCompletableFuture<>(observable);
        }
    }

    /**
     * Converts {@link java.util.concurrent.CompletableFuture} to {@link rx.Observable}.
     *
     * @param completableFuture
     * @param <T>
     * @return
     */
    public static <T> Observable<T> toObservable(CompletableFuture<T> completableFuture) {
        if (completableFuture instanceof ObservableCompletableFuture) {
            return ((ObservableCompletableFuture<T>) completableFuture).getObservable();
        } else {
            return new CompletableFutureObservable<>(completableFuture);
        }
    }
}

