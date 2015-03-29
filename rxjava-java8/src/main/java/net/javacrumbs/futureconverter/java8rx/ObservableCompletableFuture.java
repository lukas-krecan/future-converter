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
import rx.Subscription;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ObservableCompletableFuture<T> extends CompletableFuture<T> {
    private final Subscription subscription;
    private final Observable<T> observable;

    public ObservableCompletableFuture(Observable<T> observable) {
        subscription = observable.single().subscribe(
                this::complete,
                this::completeExceptionally
        );
        this.observable = observable;
    }

    public Observable<T> getObservable() {
        return observable;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean result = super.cancel(mayInterruptIfRunning);
        subscription.unsubscribe();
        return result;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        checkSubscription();
        return super.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        checkSubscription();
        return super.get(timeout, unit);
    }

    @Override
    public boolean isCancelled() {
        checkSubscription();
        return super.isCancelled();
    }

    @Override
    public boolean isDone() {
        checkSubscription();
        return super.isDone();
    }

    /**
     * Sometimes the underlying task fails sooner than the subscription starts working. This
     * should guard against such situation.
     */
    private void checkSubscription() {
        if (subscription.isUnsubscribed() && !super.isDone()) {
            completeExceptionally(new ExecutionException("Observable unsubscribed", null));
        }
    }
}
