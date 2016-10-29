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
package net.javacrumbs.futureconverter.springcommon;

import net.javacrumbs.futureconverter.common.FutureWrapper;
import net.javacrumbs.futureconverter.common.internal.CommonCallback;
import net.javacrumbs.futureconverter.common.internal.ValueSourceFuture;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.ListenableFutureCallbackRegistry;
import org.springframework.util.concurrent.SuccessCallback;


public class SpringFutureUtils {

    public static <T> ListenableFuture<T> createListenableFuture(ValueSourceFuture<T> valueSource) {
        if (valueSource instanceof ListenableFutureBackedValueSourceFuture) {
            return ((ListenableFutureBackedValueSourceFuture<T>) valueSource).getWrappedFuture();
        } else {
            return new ValueSourceFutureBackedListenableFuture<>(valueSource);
        }
    }

    public static <T> ValueSourceFuture<T> createValueSourceFuture(ListenableFuture<T> listenableFuture) {
        if (listenableFuture instanceof ValueSourceFutureBackedListenableFuture) {
            return ((ValueSourceFutureBackedListenableFuture<T>) listenableFuture).getWrappedFuture();
        } else {
            return new ListenableFutureBackedValueSourceFuture<>(listenableFuture);
        }
    }

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



    private static class ListenableFutureBackedValueSourceFuture<T> extends ValueSourceFuture<T> {
        private ListenableFutureBackedValueSourceFuture(ListenableFuture<T> wrappedFuture) {
            super(wrappedFuture);
        }

        @Override
        public void addCallbacks(CommonCallback<T> successCallback, CommonCallback<Throwable> failureCallback) {
            getWrappedFuture().addCallback(successCallback::process, failureCallback::process);
        }

        @Override
        protected ListenableFuture<T> getWrappedFuture() {
            return (ListenableFuture<T>) super.getWrappedFuture();
        }
    }
}
