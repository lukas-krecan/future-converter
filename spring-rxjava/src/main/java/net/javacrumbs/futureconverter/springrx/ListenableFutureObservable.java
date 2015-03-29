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
package net.javacrumbs.futureconverter.springrx;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

/**
 * Wraps  {@link org.springframework.util.concurrent.ListenableFuture} as {@link rx.Observable}.
 * The  original future is NOT canceled upon unsubscribe.
 *
 * @param <T>
 */
class ListenableFutureObservable<T> extends Observable<T> {
    private final ListenableFuture<T> listenableFuture;

    ListenableFutureObservable(ListenableFuture<T> listenableFuture) {
        super(onSubscribe(listenableFuture));
        this.listenableFuture = listenableFuture;
    }

    private static <T> OnSubscribe<T> onSubscribe(final ListenableFuture<T> listenableFuture) {
        return new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                listenableFuture.addCallback(new ListenableFutureCallback<T>() {
                    @Override
                    public void onSuccess(T t) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(t);
                            subscriber.onCompleted();
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(throwable);
                        }
                    }
                });
            }
        };
    }

    public ListenableFuture<T> getListenableFuture() {
        return listenableFuture;
    }
}
