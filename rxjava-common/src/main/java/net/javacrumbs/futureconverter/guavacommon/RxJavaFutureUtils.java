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
import net.javacrumbs.futureconverter.common.internal.OriginSource;
import net.javacrumbs.futureconverter.common.internal.ValueConsumer;
import rx.Single;
import rx.Subscription;

import java.util.concurrent.CompletableFuture;

public class RxJavaFutureUtils {

    public static <T> SingleWrappingValueConsumer<T> createSingleWrappingValueConsumer(Object origin) {
        return new SingleWrappingValueConsumer<>(origin);
    }

    public static <T> CancellationCallback registerListeners(Single<T> single, ValueConsumer<T> valueConsumer) {
        Subscription subscription = single.subscribe(valueConsumer::success, valueConsumer::failure);
        return ignore -> subscription.unsubscribe();
    }


    public static class SingleWrappingValueConsumer<T> implements ValueConsumer<T> {
        private final Single<T> single;
        //FIXME: There has to be a better way
        private final CompletableFuture<T> valueHolder = new CompletableFuture<>();

        private SingleWrappingValueConsumer(Object origin) {
            single = new OriginHoldingSingle<>(
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
                ),
                origin
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

    private static class OriginHoldingSingle<T> extends Single<T> implements OriginSource {
        private final Object origin;

        private OriginHoldingSingle(OnSubscribe<T> f, Object origin) {
            super(f);
            this.origin = origin;
        }

        @Override
        public Object getOrigin() {
            return origin;
        }
    }
}
