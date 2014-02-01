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
package net.javacrumbs.futureconverter.springjava;

import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.ListenableFutureCallbackRegistry;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FutureConverter {

    public static <T> ListenableFuture<T> toListenableFuture(CompletableFuture<T> completableFuture) {
        return new ListenableCompletableFutureWrapper<>(completableFuture);
    }

    private static class ListenableCompletableFutureWrapper<T> implements ListenableFuture<T> {
        private final CompletableFuture<T> wrapped;
        private final ListenableFutureCallbackRegistry<T> callbackRegistry = new ListenableFutureCallbackRegistry<>();

        private ListenableCompletableFutureWrapper(CompletableFuture<T> wrapped) {
            Objects.requireNonNull(wrapped, "Completable future has to be set");
            this.wrapped = wrapped;
            wrapped.whenComplete((result, ex) -> {
                if (ex != null) {
                    if (ex instanceof CompletionException && ex.getCause() != null) {
                        callbackRegistry.failure(ex.getCause());
                    } else {
                        callbackRegistry.failure(ex);
                    }
                } else {
                    callbackRegistry.success(result);
                }
            });
        }

        @Override
        public void addCallback(ListenableFutureCallback<? super T> callback) {
            callbackRegistry.addCallback(callback);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return wrapped.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return wrapped.isCancelled();
        }

        @Override
        public boolean isDone() {
            return wrapped.isDone();
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return wrapped.get();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return wrapped.get(timeout, unit);
        }
    }
}
