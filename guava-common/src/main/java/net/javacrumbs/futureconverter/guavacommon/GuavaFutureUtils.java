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

import com.google.common.util.concurrent.ExecutionList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import net.javacrumbs.futureconverter.common.FutureWrapper;
import net.javacrumbs.futureconverter.common.internal.AbstractCommonListenableFutureWrapper;
import net.javacrumbs.futureconverter.common.internal.CommonCallback;
import net.javacrumbs.futureconverter.common.internal.SettableFuture;

import java.util.concurrent.Executor;

import static java.util.Objects.requireNonNull;


public class GuavaFutureUtils {
    public static <T> ListenableFuture<T> createListenableFuture(AbstractCommonListenableFutureWrapper<T> listenable) {
        if (listenable instanceof CommonListenableListenableFutureWrapper) {
            return ((CommonListenableListenableFutureWrapper<T>) listenable).getWrappedFuture();
        } else {
            return new ListenableFutureCommonListenableWrapper<T>(listenable);
        }
    }

    public static <T> AbstractCommonListenableFutureWrapper<T> createCommonListenable(ListenableFuture<T> listenableFuture) {
        if (listenableFuture instanceof ListenableFutureCommonListenableWrapper) {
            return ((ListenableFutureCommonListenableWrapper<T>) listenableFuture).getWrappedFuture();
        } else {
            return new CommonListenableListenableFutureWrapper<T>(listenableFuture);
        }
    }

    public static <T> SettableFuture<T> createSettableFuture(Object origin) {
        return new SettableListenableFuture<T>(origin);
    }

    private static class SettableListenableFuture<T> extends FutureWrapper<T> implements ListenableFuture<T>, SettableFuture<T> {
        private final Object origin;
        private Runnable cancellationCallback;

        private SettableListenableFuture(Object origin) {
            super(com.google.common.util.concurrent.SettableFuture.<T>create());
            this.origin = origin;
        }

        @Override
        public void addListener(Runnable listener, Executor executor) {
            getWrappedFuture().addListener(listener, executor);
        }

        @Override
        protected com.google.common.util.concurrent.SettableFuture<T> getWrappedFuture() {
            return (com.google.common.util.concurrent.SettableFuture<T>) super.getWrappedFuture();
        }

        @Override
        public void setResult(T value) {
            getWrappedFuture().set(value);
        }

        @Override
        public void setException(Throwable exception) {
            getWrappedFuture().setException(exception);
        }

        @Override
        public void setCancellationCallback(Runnable callback) {
            requireNonNull(callback);
            if (cancellationCallback !=null){
                throw new IllegalStateException("Cancellation callback can be set only once.");
            };
            cancellationCallback = callback;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancellationCallback.run();
            return super.cancel(mayInterruptIfRunning);
        }

        @Override
        public Object getOrigin() {
            return origin;
        }
    }


    private static class ListenableFutureCommonListenableWrapper<T> extends FutureWrapper<T> implements ListenableFuture<T> {
        private final ExecutionList executionList = new ExecutionList();
        ListenableFutureCommonListenableWrapper(AbstractCommonListenableFutureWrapper<T> wrapped) {
            super(wrapped);
            wrapped.addSuccessCallback(new CommonCallback<T>() {
                @Override
                public void process(T value) {
                    executionList.execute();
                }
            });
            wrapped.addFailureCallback(new CommonCallback<Throwable>() {
                @Override
                public void process(Throwable value) {
                    executionList.execute();
                }
            });
        }

        @Override
        protected AbstractCommonListenableFutureWrapper<T> getWrappedFuture() {
            return (AbstractCommonListenableFutureWrapper<T>) super.getWrappedFuture();
        }

        @Override
        public void addListener(Runnable listener, Executor executor) {
            executionList.add(listener, executor);
        }
    }

    private static class CommonListenableListenableFutureWrapper<T> extends AbstractCommonListenableFutureWrapper<T> {
        protected CommonListenableListenableFutureWrapper(ListenableFuture<T> wrappedFuture) {
            super(wrappedFuture);
        }

        @Override
        public void addSuccessCallback(final CommonCallback<T> successCallback) {
            Futures.addCallback(getWrappedFuture(), new FutureCallback<T>() {
                @Override
                public void onSuccess(T result) {
                    successCallback.process(result);
                }

                @Override
                public void onFailure(Throwable t) {

                }
            }, MoreExecutors.directExecutor());
        }

        @Override
        public void addFailureCallback(final CommonCallback<Throwable> failureCallback) {
            Futures.addCallback(getWrappedFuture(), new FutureCallback<T>() {
                @Override
                public void onSuccess(T result) {

                }

                @Override
                public void onFailure(Throwable t) {
                    failureCallback.process(t);
                }
            }, MoreExecutors.directExecutor());
        }

        @Override
        protected ListenableFuture<T> getWrappedFuture() {
            return (ListenableFuture<T>) super.getWrappedFuture();
        }
    };
}
