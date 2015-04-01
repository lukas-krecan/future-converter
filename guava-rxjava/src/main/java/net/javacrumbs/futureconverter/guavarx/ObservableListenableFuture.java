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
package net.javacrumbs.futureconverter.guavarx;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import net.javacrumbs.futureconverter.common.FutureWrapper;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

class ObservableListenableFuture<T> extends FutureWrapper<T> implements ListenableFuture<T> {
    private final Observable<T> observable;
    private final Subscription subscription;

    @SuppressWarnings("unchecked")
    ObservableListenableFuture(Observable<T> observable) {
        super((Future<T>) SettableFuture.create());
        this.observable = observable;
        subscription = observable.single().subscribe(new Action1<T>() {
            @Override
            public void call(T t) {
                getWrappedFuture().set(t);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                getWrappedFuture().setException(throwable);
            }
        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        subscription.unsubscribe();
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public SettableFuture<T> getWrappedFuture() {
        return (SettableFuture<T>) super.getWrappedFuture();
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        getWrappedFuture().addListener(listener, executor);
    }

    public Observable<T> getObservable() {
        return observable;
    }
}
