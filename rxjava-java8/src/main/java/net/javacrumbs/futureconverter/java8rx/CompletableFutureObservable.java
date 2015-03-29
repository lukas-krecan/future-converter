/**
 * Copyright 2009-2014 the original author or authors.
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
import rx.subscriptions.Subscriptions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Wraps  {@link CompletableFuture} as {@link rx.Observable}.
 * The  original future is NOT canceled upon unsubscribe.
 *
 * @param <T>
 */
class CompletableFutureObservable<T> extends Observable<T> {
    private final CompletableFuture<T> completableFuture;

    CompletableFutureObservable(CompletableFuture<T> completableFuture) {
        super(onSubscribe(completableFuture));
        this.completableFuture = completableFuture;
    }

    private static <T> OnSubscribe<T> onSubscribe(final CompletableFuture<T> completableFuture) {
        return subscriber -> {
            completableFuture.thenAccept(value -> {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onNext(value);
                    subscriber.onCompleted();
                }
                }).exceptionally(throwable -> {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(throwable);
                }
                return null;
            });
        };
    }

    public CompletableFuture<T> getCompletableFuture() {
        return completableFuture;
    }
}
