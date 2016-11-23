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
package net.javacrumbs.futureconverter.springcommon;

import net.javacrumbs.futureconverter.common.internal.FutureWrapper;
import net.javacrumbs.futureconverter.common.internal.ValueSource;
import net.javacrumbs.futureconverter.common.internal.ValueSourceFuture;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.SettableListenableFuture;
import org.springframework.util.concurrent.SuccessCallback;

import java.util.function.Consumer;


public class SpringFutureUtils {
    // *************************************** Converting to ListenableFuture ******************************************

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

    /**
     * ListenableFuture that delegates all the work to ValueSourceFuture.
     */
    private static class ValueSourceFutureBackedListenableFuture<T> extends FutureWrapper<T> implements ListenableFuture<T> {
        private ValueSourceFutureBackedListenableFuture(ValueSourceFuture<T> valueSourceFuture) {
            super(valueSourceFuture);
        }

        @Override
        public void addCallback(ListenableFutureCallback<? super T> callback) {
            getWrappedFuture().addCallbacks(callback::onSuccess, callback::onFailure);
        }

        @Override
        public void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback) {
            getWrappedFuture().addCallbacks(successCallback::onSuccess, failureCallback::onFailure);
        }

        @Override
        protected ValueSourceFuture<T> getWrappedFuture() {
            return (ValueSourceFuture<T>) super.getWrappedFuture();
        }
    }


    /**
     * If we only get ValueSource we have to create a ValueSourceFuture. Here we wrap Spring SettableListenableFuture
     * and use it for listener handling and value storage.
     */
    private static class ValueSourceBackedListenableFuture<T> extends FutureWrapper<T> implements ListenableFuture<T> {
        private final ValueSource<T> valueSource;

        private ValueSourceBackedListenableFuture(ValueSource<T> valueSource) {
            super(new SettableListenableFuture<>());
            this.valueSource = valueSource;
            valueSource.addCallbacks(value -> getWrappedFuture().set(value), ex -> getWrappedFuture().setException(ex));
        }


        @Override
        protected SettableListenableFuture<T> getWrappedFuture() {
            return (SettableListenableFuture<T>) super.getWrappedFuture();
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            valueSource.cancel(mayInterruptIfRunning);
            return super.cancel(mayInterruptIfRunning);
        }

        private ValueSource<T> getValueSource() {
            return valueSource;
        }

        @Override
        public void addCallback(ListenableFutureCallback<? super T> callback) {
            getWrappedFuture().addCallback(callback);
        }

        @Override
        public void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback) {
            getWrappedFuture().addCallback(successCallback, failureCallback);
        }
    }

    // *************************************** Converting from ListenableFuture ******************************************

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


    private static class ListenableFutureBackedValueSourceFuture<T> extends ValueSourceFuture<T> {
        private ListenableFutureBackedValueSourceFuture(ListenableFuture<T> wrappedFuture) {
            super(wrappedFuture);
        }

        @Override
        public void addCallbacks(Consumer<T> successCallback, Consumer<Throwable> failureCallback) {
            getWrappedFuture().addCallback(successCallback::accept, failureCallback::accept);
        }

        @Override
        protected ListenableFuture<T> getWrappedFuture() {
            return (ListenableFuture<T>) super.getWrappedFuture();
        }
    }
}
