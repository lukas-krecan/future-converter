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

import net.javacrumbs.futureconverter.common.internal.CommonCallback;
import net.javacrumbs.futureconverter.common.internal.SettableFuture;
import net.javacrumbs.futureconverter.common.internal.ValueSource;
import net.javacrumbs.futureconverter.common.internal.ValueSourceFuture;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public class Java8FutureUtils {
    public static <T> CompletableFuture<T> createCompletableFuture(ValueSource<T> valueSource) {
        if (valueSource instanceof ListenableCompletableFutureWrapper) {
            return ((ListenableCompletableFutureWrapper<T>) valueSource).getWrappedFuture();
        } else {
            return new CompletableFutureListenableWrapper<T>(valueSource);
        }
    }

    public static <T> ValueSourceFuture<T> createValueSourceFuture(CompletableFuture<T> completableFuture) {
        if (completableFuture instanceof CompletableFutureListenableWrapper &&
            ((CompletableFutureListenableWrapper<T>) completableFuture).getValueSource() instanceof ValueSourceFuture) {
            return (ValueSourceFuture<T>) ((CompletableFutureListenableWrapper<T>) completableFuture).getValueSource();
        } else {
            return new ListenableCompletableFutureWrapper<>(completableFuture);
        }
    }

    public static <T> ValueSource<T> createValueSource(CompletableFuture<T> completableFuture) {
        if (completableFuture instanceof CompletableFutureListenableWrapper) {
            return ((CompletableFutureListenableWrapper<T>) completableFuture).getValueSource();
        } else {
            return new ListenableCompletableFutureWrapper<>(completableFuture);
        }
    }

    public static <T> SettableFuture<T> createSettableFuture(Object origin) {
        return new SettableCompletableFuture<>(origin);
    }

    private static final class SettableCompletableFuture<T> extends CompletableFuture<T> implements SettableFuture<T> {
        private final Object origin;
        private Runnable cancellationCallback;

        public SettableCompletableFuture(Object origin) {
            this.origin = origin;
        }

        @Override
        public void setResult(T value) {
            complete(value);
        }

        @Override
        public void setException(Throwable exception) {
            completeExceptionally(exception);
        }

        @Override
        public void setCancellationCallback(Runnable callback) {
            requireNonNull(callback);
            if (cancellationCallback != null) {
                throw new IllegalStateException("Cancellation callback can be set only once.");
            }
            ;
            cancellationCallback = callback;
        }

        public Object getOrigin() {
            return origin;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancellationCallback.run();
            return super.cancel(mayInterruptIfRunning);
        }
    }

    private static final class CompletableFutureListenableWrapper<T> extends CompletableFuture<T> {
        private final ValueSource<T> valueSource;

        public CompletableFutureListenableWrapper(ValueSource<T> valueSource) {
            this.valueSource = valueSource;
            valueSource.addCallbacks(this::complete, this::completeExceptionally);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean result = valueSource.cancel(mayInterruptIfRunning);
            super.cancel(mayInterruptIfRunning);
            return result;
        }

        public ValueSource<T> getValueSource() {
            return valueSource;
        }
    }

    private static final class ListenableCompletableFutureWrapper<T> extends ValueSourceFuture<T> {
        private ListenableCompletableFutureWrapper(CompletableFuture<T> completableFuture) {
            super(completableFuture);
        }


        @Override
        public void addCallbacks(CommonCallback<T> successCallback, CommonCallback<Throwable> failureCallback) {
            getWrappedFuture().whenComplete((v, t) -> {
                if (t == null) {
                    successCallback.process(v);
                } else {
                    failureCallback.process(t);
                }
            });
        }

        @Override
        protected CompletableFuture<T> getWrappedFuture() {
            return (CompletableFuture<T>) super.getWrappedFuture();
        }
    }
}
