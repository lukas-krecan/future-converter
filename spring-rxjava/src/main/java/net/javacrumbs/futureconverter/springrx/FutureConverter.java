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
package net.javacrumbs.futureconverter.springrx;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.ListenableFutureCallbackRegistry;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureConverter {

    public static <T> Observable<T> toObservable(ListenableFuture<T> listenableFuture) {
        return ListenableFutureObservable.create(listenableFuture);
    }

    public static <T> ListenableFuture<T> toListenableFuture(Observable<T> observable) {
        if (observable instanceof ListenableFutureObservable) {
            return ((ListenableFutureObservable) observable).getListenableFuture();
        } else {
            return new ListenableFutureObservableWrapper(observable);
        }
    }

    private static class ListenableFutureObservableWrapper<T> implements ListenableFuture<T> {
        private final Observable<T> observable;
        private final Future<T> futureFromObservable;
        private final ListenableFutureCallbackRegistry<T> callbackRegistry = new ListenableFutureCallbackRegistry<>();

        private ListenableFutureObservableWrapper(Observable<T> wrapped) {
            this.observable = wrapped;
            this.futureFromObservable = wrapped.toBlockingObservable().toFuture();
            wrapped.take(1).subscribe(new Subscriber<T>() {
                @Override
                public void onCompleted() {
                }

                @Override
                public void onError(Throwable e) {
                    callbackRegistry.failure(e);
                }

                @Override
                public void onNext(T t) {
                    callbackRegistry.success(t);
                }
            });
        }

        @Override
        public void addCallback(ListenableFutureCallback<? super T> callback) {
            callbackRegistry.addCallback(callback);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return futureFromObservable.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return futureFromObservable.isCancelled();
        }

        @Override
        public boolean isDone() {
            return futureFromObservable.isDone();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return futureFromObservable.get();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return futureFromObservable.get(timeout, unit);
        }

        public Observable<T> getObservable() {
            return observable;
        }
    }
}
