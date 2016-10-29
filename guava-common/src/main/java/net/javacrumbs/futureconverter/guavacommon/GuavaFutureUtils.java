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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import net.javacrumbs.futureconverter.common.FutureWrapper;
import net.javacrumbs.futureconverter.common.internal.CancellationCallback;
import net.javacrumbs.futureconverter.common.internal.OriginSource;
import net.javacrumbs.futureconverter.common.internal.ValueConsumer;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;


public class GuavaFutureUtils {
    public static <T> ListenableFutureValueConsumer<T> createListenableFuture(Object origin) {
        return new ListenableFutureValueConsumer<>(origin);
    }

    public static <T> ListenableFuture<T> registerCancellationCallback(ListenableFuture<T> listenableFuture, CancellationCallback cancellationCallback) {
        return new ListenableFutureCancellationWrapper<>(listenableFuture, cancellationCallback);
    }

    public static <T> CancellationCallback registerListeners(ListenableFuture<T> listenableFuture, ValueConsumer<T> valueConsumer) {
        listenableFuture.addListener(() -> {
                try {
                    valueConsumer.success(listenableFuture.get());
                } catch (ExecutionException e) {
                    valueConsumer.failure(e.getCause());
                } catch (Throwable e) {
                    valueConsumer.failure(e);
                }
            },
            MoreExecutors.directExecutor()
        );
        return listenableFuture::cancel;
    }

    public static class ListenableFutureValueConsumer<T> extends FutureWrapper<T> implements ListenableFuture<T>, ValueConsumer<T>, OriginSource {
        private final Object origin;

        private ListenableFutureValueConsumer(Object origin) {
            super(SettableFuture.create());
            this.origin = origin;
        }

        @Override
        public void success(T value) {
            getWrappedFuture().set(value);
        }

        @Override
        public void failure(Throwable ex) {
            if (ex instanceof CancellationException) {
                getWrappedFuture().cancel(true);
            } else {
                getWrappedFuture().setException(ex);
            }
        }

        @Override
        public void addListener(Runnable listener, Executor executor) {
            getWrappedFuture().addListener(listener, executor);
        }

        @Override
        protected SettableFuture<T> getWrappedFuture() {
            return (SettableFuture<T>) super.getWrappedFuture();
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

    private static class ListenableFutureCancellationWrapper<T> extends FutureWrapper<T> implements ListenableFuture<T>, OriginSource {

        private final CancellationCallback cancellationCallback;

        private ListenableFutureCancellationWrapper(ListenableFuture<T> wrappedFuture, CancellationCallback cancellationCallback) {
            super(wrappedFuture);
            this.cancellationCallback = cancellationCallback;
        }

        @Override
        public void addListener(Runnable listener, Executor executor) {
            getWrappedFuture().addListener(listener, executor);
        }

        @Override
        protected ListenableFuture<T> getWrappedFuture() {
            return (ListenableFuture<T>) super.getWrappedFuture();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancellationCallback.cancel(mayInterruptIfRunning);
            return super.cancel(mayInterruptIfRunning);
        }

        @Override
        public Object getOrigin() {
            //FIMXE: types
            if (getWrappedFuture() instanceof OriginSource) {
                return ((OriginSource)getWrappedFuture()).getOrigin();
            } else {
                throw new IllegalStateException("Wrapped future has to be origin source");
            }
        }
    }
}
