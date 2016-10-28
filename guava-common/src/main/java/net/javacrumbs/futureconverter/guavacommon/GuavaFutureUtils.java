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

import com.google.common.util.concurrent.ExecutionList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import net.javacrumbs.futureconverter.common.FutureWrapper;
import net.javacrumbs.futureconverter.common.internal.CommonCallback;
import net.javacrumbs.futureconverter.common.internal.ValueSource;
import net.javacrumbs.futureconverter.common.internal.ValueSourceFuture;

import java.util.concurrent.Executor;


public class GuavaFutureUtils {
    /**
     * Creates listenable future from ValueSourceFuture. We have to send all Future API calls to ValueSourceFuture.
     */
    public static <T> ListenableFuture<T> createListenableFuture(ValueSourceFuture<T> valueSource) {
        if (valueSource instanceof ListenableFutureBackedValueSourceFuture) {
            return ((ListenableFutureBackedValueSourceFuture<T>) valueSource).getWrappedFuture();
        } else {
            return new ValueSourceFutureBackedListenableFuture<>(valueSource);
        }
    }

    public static <T> ListenableFuture<T> createListenableFuture(ValueSource<T> valueSource) {
        if (valueSource instanceof ListenableFutureBackedValueSourceFuture) {
            return ((ListenableFutureBackedValueSourceFuture<T>) valueSource).getWrappedFuture();
        } else {
            return new ValueSourceBackedListenableFuture<>(valueSource);
        }
    }


    public static <T> ValueSourceFuture<T> createValueSourceFuture(ListenableFuture<T> listenableFuture) {
        if (listenableFuture instanceof ValueSourceFutureBackedListenableFuture) {
            return ((ValueSourceFutureBackedListenableFuture<T>) listenableFuture).getWrappedFuture();
        } else {
            return new ListenableFutureBackedValueSourceFuture<>(listenableFuture);
        }
    }

    public static <T> ValueSource<T> createValueSource(ListenableFuture<T> listenableFuture) {
        if (listenableFuture instanceof ValueSourceBackedListenableFuture) {
            return ((ValueSourceBackedListenableFuture<T>) listenableFuture).getValueSource();
        } else {
            return new ListenableFutureBackedValueSourceFuture<>(listenableFuture);
        }
    }

    /**
     * If we only get ValueSource we have to create a ValueSourceFuture. //FXIME: remove
     */
    private static class ValueSourceBackedListenableFuture<T> extends FutureWrapper<T> implements ListenableFuture<T> {
        private final ValueSource<T> valueSource;

        private ValueSourceBackedListenableFuture(ValueSource<T> valueSource) {
            super(com.google.common.util.concurrent.SettableFuture.create());
            this.valueSource = valueSource;
            valueSource.addCallbacks(value -> getWrappedFuture().set(value), ex -> getWrappedFuture().setException(ex));
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
        public boolean cancel(boolean mayInterruptIfRunning) {
            valueSource.cancel(mayInterruptIfRunning);
            return super.cancel(mayInterruptIfRunning);
        }

        private ValueSource<T> getValueSource() {
            return valueSource;
        }
    }


    private static class ValueSourceFutureBackedListenableFuture<T> extends FutureWrapper<T> implements ListenableFuture<T> {
        private final ExecutionList executionList = new ExecutionList();

        ValueSourceFutureBackedListenableFuture(ValueSourceFuture<T> valueSourceFuture) {
            super(valueSourceFuture);
            valueSourceFuture.addCallbacks(value -> executionList.execute(), ex -> executionList.execute());
        }

        @Override
        protected ValueSourceFuture<T> getWrappedFuture() {
            return (ValueSourceFuture<T>) super.getWrappedFuture();
        }

        @Override
        public void addListener(Runnable listener, Executor executor) {
            executionList.add(listener, executor);
        }
    }

    /**
     * Wraps ListenableFuture and exposes it as ValueSourceFuture.
     */
    private static class ListenableFutureBackedValueSourceFuture<T> extends ValueSourceFuture<T> {
        private ListenableFutureBackedValueSourceFuture(ListenableFuture<T> wrappedFuture) {
            super(wrappedFuture);
        }

        @Override
        public void addCallbacks(CommonCallback<T> successCallback, CommonCallback<Throwable> failureCallback) {
            Futures.addCallback(getWrappedFuture(), new FutureCallback<T>() {
                @Override
                public void onSuccess(T result) {
                    successCallback.process(result);
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
    }
}
