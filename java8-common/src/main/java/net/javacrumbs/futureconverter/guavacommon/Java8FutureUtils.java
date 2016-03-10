/**
 * Copyright 2009-2016 the original author or authors.
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
package net.javacrumbs.futureconverter.guavacommon;

import net.javacrumbs.futureconverter.common.internal.AbstractCommonListenableFutureWrapper;
import net.javacrumbs.futureconverter.common.internal.CommonCallback;
import net.javacrumbs.futureconverter.common.internal.CommonListenable;
import net.javacrumbs.futureconverter.common.internal.SettableFuture;

import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

public class Java8FutureUtils {
    public static <T> CompletableFuture<T> createCompletableFuture(CommonListenable<T> commonListenable) {
        if (commonListenable instanceof ListenableCompletableFutureWrapper) {
            return ((ListenableCompletableFutureWrapper<T>) commonListenable).getWrappedFuture();
        } else {
            return new CompletableFutureListenableWrapper<T>(commonListenable);
        }
    }

    public static <T> AbstractCommonListenableFutureWrapper<T> createCommonListenable(CompletableFuture<T> completableFuture) {
        if (completableFuture instanceof CompletableFutureListenableWrapper &&
            ((CompletableFutureListenableWrapper<T>) completableFuture).getCommonListenable() instanceof AbstractCommonListenableFutureWrapper) {
            return (AbstractCommonListenableFutureWrapper<T>) ((CompletableFutureListenableWrapper<T>) completableFuture).getCommonListenable();
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
            if (cancellationCallback !=null){
                throw new IllegalStateException("Cancellation callback can be set only once.");
            };
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
        private final CommonListenable<T> commonListenable;

        public CompletableFutureListenableWrapper(CommonListenable<T> commonListenable) {
            this.commonListenable = commonListenable;
            commonListenable.addSuccessCallback(this::complete);
            commonListenable.addFailureCallback(this::completeExceptionally);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean result = commonListenable.cancel(mayInterruptIfRunning);
            super.cancel(mayInterruptIfRunning);
            return result;
        }

        public CommonListenable<T> getCommonListenable() {
            return commonListenable;
        }
    }

    private static final class ListenableCompletableFutureWrapper<T> extends AbstractCommonListenableFutureWrapper<T> {
        private ListenableCompletableFutureWrapper(CompletableFuture<T> completableFuture) {
            super(completableFuture);
        }

        @Override
        public void addSuccessCallback(CommonCallback<T> successCallback) {
            getWrappedFuture().thenAccept(successCallback::process);
        }

        @Override
        public void addFailureCallback(CommonCallback<Throwable> failureCallback) {
            getWrappedFuture().exceptionally(e -> {
                failureCallback.process(e);
                return null;
            });
        }

        @Override
        protected CompletableFuture<T> getWrappedFuture() {
            return (CompletableFuture<T>) super.getWrappedFuture();
        }
    }
}
