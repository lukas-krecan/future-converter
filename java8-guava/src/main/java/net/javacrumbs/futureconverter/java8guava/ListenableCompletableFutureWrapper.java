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
package net.javacrumbs.futureconverter.java8guava;

import com.google.common.util.concurrent.ExecutionList;
import com.google.common.util.concurrent.ListenableFuture;
import net.javacrumbs.futureconverter.common.FutureWrapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Wraps {@link java.util.concurrent.CompletableFuture} and provides {@link com.google.common.util.concurrent.ListenableFuture} interface.
 *
 * @param <T>
 */
class ListenableCompletableFutureWrapper<T> extends FutureWrapper<T> implements ListenableFuture<T> {
    private final ExecutionList executionList = new ExecutionList();

    ListenableCompletableFutureWrapper(CompletableFuture<T> wrapped) {
        super(wrapped);
        wrapped.whenComplete((result, ex) -> executionList.execute());
    }

    @Override
    public void addListener(Runnable listener, Executor executor) {
        executionList.add(listener, executor);
    }

    @Override
    public CompletableFuture<T> getWrappedFuture() {
        return (CompletableFuture<T>) super.getWrappedFuture();
    }

}
