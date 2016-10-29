/**
 * Copyright 2009-2015 the original author or authors.
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
package net.javacrumbs.futureconverter.common.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Wraps future and delegates to wrapped.
 */
public class FutureWrapper<T> implements Future<T> {
    private final Future<T> wrappedFuture;

    protected FutureWrapper(Future<T> wrappedFuture) {
        if (wrappedFuture == null) {
            throw new NullPointerException("Wrapped future can not be null");
        }
        this.wrappedFuture = wrappedFuture;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return wrappedFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return wrappedFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return wrappedFuture.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return wrappedFuture.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return wrappedFuture.get(timeout, unit);
    }

    protected Future<T> getWrappedFuture() {
        return wrappedFuture;
    }
}
