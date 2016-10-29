/**
 * Copyright 2009-2016 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.futureconverter.guavacommon;

import net.javacrumbs.futureconverter.common.internal.CancellationCallback;
import net.javacrumbs.futureconverter.common.internal.CommonCallback;
import net.javacrumbs.futureconverter.common.internal.ValueConsumer;
import net.javacrumbs.futureconverter.common.internal.ValueSource;
import rx.Single;
import rx.Subscription;

import java.util.concurrent.CompletableFuture;

public class RxJavaFutureUtils {

    public static <T> SingleWrappingValueConsumer<T> createSingleWrappingValueConsumer() {
        return new SingleWrappingValueConsumer<>();
    }

    public static <T> CancellationCallback registerListeners(Single<T> single, ValueConsumer<T> valueConsumer) {
        Subscription subscription = single.subscribe(valueConsumer::success, valueConsumer::failure);
        return ignore -> subscription.unsubscribe();
    }


    public static class SingleWrappingValueConsumer<T> implements ValueConsumer<T> {
        private final Single<T> single;
        //FIXME: There has to be a better way
        private final CompletableFuture<T> valueHolder = new CompletableFuture<>();

        private SingleWrappingValueConsumer() {
            single = Single.create(
                singleSubscriber -> valueHolder.whenComplete(
                    (v, t) -> {
                        if (!singleSubscriber.isUnsubscribed()) {
                            if (t == null) {
                                try {
                                    singleSubscriber.onSuccess(v);
                                } catch (Throwable ex) {
                                    singleSubscriber.onError(ex);
                                }
                            } else {
                                singleSubscriber.onError(t);
                            }
                        }
                    }
                )
            );
        }

        @Override
        public void success(T value) {
            valueHolder.complete(value);
        }

        @Override
        public void failure(Throwable ex) {
            valueHolder.completeExceptionally(ex);
        }

        public Single<T> getSingle() {
            return single;
        }
    }


    public static <T> Single<T> createSingle(ValueSource<T> valueSource) {
        if (valueSource instanceof SingleBackedValueSource) {
            return ((SingleBackedValueSource<T>) valueSource).getSingle();
        }
        return new ValueSourceBackedSingle<>(valueSource);
    }

    public static <T> ValueSource<T> createValueSource(Single<T> single) {
        if (single instanceof ValueSourceBackedSingle) {
            return ((ValueSourceBackedSingle<T>) single).getValueSource();
        } else {
            return new SingleBackedValueSource<>(single);
        }
    }

    private static class SingleBackedValueSource<T> implements ValueSource<T> {
        private final Single<T> single;

        private SingleBackedValueSource(Single<T> single) {
            this.single = single;
        }

        @Override
        public void addCallbacks(CommonCallback<T> successCallback, CommonCallback<Throwable> failureCallback) {
            single.subscribe(successCallback::process, failureCallback::process);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        public Single<T> getSingle() {
            return single;
        }
    }

    private static class ValueSourceBackedSingle<T> extends Single<T> {
        private final ValueSource<T> valueSource;

        ValueSourceBackedSingle(ValueSource<T> valueSource) {
            super(onSubscribe(valueSource));
            this.valueSource = valueSource;
        }

        private static <T> OnSubscribe<T> onSubscribe(final ValueSource<T> valueSource) {
            return subscriber -> {
                valueSource.addCallbacks(value -> {
                        if (!subscriber.isUnsubscribed()) {
                            try {
                                subscriber.onSuccess(value);
                            } catch (Throwable e) {
                                subscriber.onError(e);
                            }
                        }
                    },
                    throwable -> {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onError(throwable);
                        }
                    });
            };
        }

        private ValueSource<T> getValueSource() {
            return valueSource;
        }
    }
}
