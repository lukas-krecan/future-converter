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

import java.util.concurrent.CompletableFuture;

/**
 * Converts between {@link java.util.concurrent.CompletableFuture} and Spring 4 {@link org.springframework.util.concurrent.ListenableFuture}.
 */
public class FutureConverter {

    /**
     * Converts {@link java.util.concurrent.CompletableFuture} to {@link org.springframework.util.concurrent.ListenableFuture}.
     *
     * @param completableFuture
     * @param <T>
     * @return
     */
    public static <T> ListenableFuture<T> toListenableFuture(CompletableFuture<T> completableFuture) {
        if (completableFuture instanceof CompletableListenableFuture) {
            return ((CompletableListenableFuture<T>) completableFuture).getListenableFuture();
        } else {
            return new ListenableCompletableFutureWrapper<>(completableFuture);
        }
    }

    /**
     * Converts  {@link org.springframework.util.concurrent.ListenableFuture} to {@link java.util.concurrent.CompletableFuture}.
     *
     * @param listenableFuture
     * @param <T>
     * @return
     */
    public static <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenableFuture) {
        if (listenableFuture instanceof ListenableCompletableFutureWrapper) {
            return ((ListenableCompletableFutureWrapper<T>) listenableFuture).getWrappedFuture();
        } else {
            return buildCompletableFutureFromListenableFuture(listenableFuture);
        }
    }

    private static <T> CompletableFuture<T> buildCompletableFutureFromListenableFuture(final ListenableFuture<T> listenableFuture) {
        CompletableFuture<T> completable = new CompletableListenableFuture<T>(listenableFuture);
        listenableFuture.addCallback(new ListenableFutureCallback<T>() {
            @Override
            public void onSuccess(T result) {
                completable.complete(result);
            }

            @Override
            public void onFailure(Throwable t) {
                completable.completeExceptionally(t);
            }
        });
        return completable;
    }

    private static final class CompletableListenableFuture<T> extends CompletableFuture<T> {
        private final ListenableFuture<T> listenableFuture;

        public CompletableListenableFuture(ListenableFuture<T> listenableFuture) {
            this.listenableFuture = listenableFuture;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean result = listenableFuture.cancel(mayInterruptIfRunning);
            super.cancel(mayInterruptIfRunning);
            return result;
        }

        public ListenableFuture<T> getListenableFuture() {
            return listenableFuture;
        }
    }
}
