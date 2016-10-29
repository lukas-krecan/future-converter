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

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class Java8FutureUtils {
    public static <T> CompletableFutureValueConsumer<T> createCompletableFuture(Object origin) {
        return new CompletableFutureValueConsumer<>(origin);
    }

    public static <T> CancellationCallback registerListeners(CompletableFuture<T> completableFuture, ValueConsumer<T> valueConsumer) {
        completableFuture.whenComplete((v, t) -> {
           if (t == null) {
               valueConsumer.success(v);
           } else {
               valueConsumer.failure(t);
           }
        });
        return completableFuture::cancel;
    }

    public static <T> CompletableFuture<T> registerCancellationCallback(CompletableFutureValueConsumer<T> completableFuture, CancellationCallback cancellationCallback) {
        // Creates new CompletableFuture, we want to use the original one
        completableFuture.whenComplete((v, t) -> {
            if (t instanceof CancellationException) {
                cancellationCallback.cancel(true);
            }
        });
        return completableFuture;
    }

    public static class CompletableFutureValueConsumer<T> extends CompletableFuture<T> implements ValueConsumer<T>, OriginSource {
        private final Object origin;

        public CompletableFutureValueConsumer(Object origin) {
            this.origin = origin;
        }

        @Override
        public void success(T value) {
            complete(value);
        }

        @Override
        public void failure(Throwable ex) {
            if (ex instanceof CancellationException) {
                cancel(true);
            } else {
                completeExceptionally(ex);
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return super.cancel(mayInterruptIfRunning);
        }

        @Override
        public Object getOrigin() {
            return origin;
        }
    }
}
