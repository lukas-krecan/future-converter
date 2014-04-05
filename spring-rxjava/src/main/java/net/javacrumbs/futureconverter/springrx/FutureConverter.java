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
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.Future;

public class FutureConverter {

    public static <T> Observable<T> toObservable(ListenableFuture<T> listenableFuture) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                listenableFuture.addCallback(new ListenableFutureCallback<T>() {
                    @Override
                    public void onSuccess(T t) {
                        subscriber.onNext(t);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        subscriber.onError(throwable);
                    }
                });
                subscriber.add(Subscriptions.from(listenableFuture));
            }
        });
    }

    public static <T> Future<T> toListenableFuture(Observable<T> observable) {
        return observable.toBlockingObservable().toFuture();
    }
}
