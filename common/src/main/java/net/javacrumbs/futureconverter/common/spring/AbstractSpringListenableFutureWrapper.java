/**
 * Copyright 2009-2014 the original author or authors.
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
package net.javacrumbs.futureconverter.common.spring;

import net.javacrumbs.futureconverter.common.FutureWrapper;
import org.springframework.util.concurrent.FailureCallback;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.ListenableFutureCallbackRegistry;
import org.springframework.util.concurrent.SuccessCallback;

import java.util.concurrent.Future;

/**
 * Common superclass for Spring ListenableFuture.
 */
public abstract class AbstractSpringListenableFutureWrapper<T> extends FutureWrapper<T> implements ListenableFuture<T> {
    private final ListenableFutureCallbackRegistry<T> callbackRegistry;

    protected AbstractSpringListenableFutureWrapper(Future<T> wrappedFuture) {
        super(wrappedFuture);
        this.callbackRegistry = new ListenableFutureCallbackRegistry<>();
    }

    @Override
    public void addCallback(ListenableFutureCallback<? super T> callback) {
        this.addCallback(callback, callback);
    }

    @Override
    public void addCallback(SuccessCallback<? super T> successCallback, FailureCallback failureCallback) {
        callbackRegistry.addSuccessCallback(successCallback);
        callbackRegistry.addFailureCallback(failureCallback);
    }

    public void success(T value) {
        callbackRegistry.success(value);
    }

    public void failure(Throwable exception) {
        callbackRegistry.failure(exception);
    }
}
