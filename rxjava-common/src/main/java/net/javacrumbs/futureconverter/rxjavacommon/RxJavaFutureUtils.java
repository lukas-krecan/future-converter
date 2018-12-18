/*
 * Copyright © 2014-2019 the original author or authors.
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
package net.javacrumbs.futureconverter.rxjavacommon;

import net.javacrumbs.futureconverter.common.internal.ValueSource;
import rx.Single;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

import java.util.function.Consumer;

public class RxJavaFutureUtils {
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
        private Subscription subscription;

        private SingleBackedValueSource(Single<T> single) {
            this.single = single;
        }

        @Override
        public void addCallbacks(Consumer<T> successCallback, Consumer<Throwable> failureCallback) {
            if (subscription == null) {
                subscription = single.subscribe(successCallback::accept, failureCallback::accept);
            } else {
                throw new IllegalStateException("add callbacks can be called only once");
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            subscription.unsubscribe();
            return true;
        }

        private Single<T> getSingle() {
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
                subscriber.add(Subscriptions.create(() -> valueSource.cancel(true)));
            };
        }

        private ValueSource<T> getValueSource() {
            return valueSource;
        }
    }
}
