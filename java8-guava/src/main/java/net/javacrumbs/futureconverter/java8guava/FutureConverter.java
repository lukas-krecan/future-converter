/**
 * Copyright 2009-2015 the original author or authors.
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
package net.javacrumbs.futureconverter.java8guava;


import com.google.common.util.concurrent.ListenableFuture;
import net.javacrumbs.futureconverter.common.internal.CancellationCallback;
import net.javacrumbs.futureconverter.common.internal.OriginSource;
import net.javacrumbs.futureconverter.guavacommon.GuavaFutureUtils;
import net.javacrumbs.futureconverter.guavacommon.GuavaFutureUtils.ListenableFutureValueConsumer;
import net.javacrumbs.futureconverter.guavacommon.Java8FutureUtils;
import net.javacrumbs.futureconverter.guavacommon.Java8FutureUtils.CompletableFutureValueConsumer;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Converts between {@link java.util.concurrent.CompletableFuture} and Guava {@link com.google.common.util.concurrent.ListenableFuture}.
 */
public class FutureConverter {

    /**
     * Converts {@link java.util.concurrent.CompletableFuture} to {@link com.google.common.util.concurrent.ListenableFuture}.
     */
    public static <T> ListenableFuture<T> toListenableFuture(CompletableFuture<T> completableFuture) {
        Optional<ListenableFuture<T>> originalValue = (Optional<ListenableFuture<T>>) OriginSource.extractOriginalValue(completableFuture, ListenableFuture.class);
        return originalValue.orElseGet(() -> {
            ListenableFutureValueConsumer<T> listenableFuture = GuavaFutureUtils.createListenableFuture(completableFuture);
            CancellationCallback cancellationCallback = Java8FutureUtils.registerListeners(completableFuture, listenableFuture);
            return GuavaFutureUtils.registerCancellationCallback(listenableFuture, cancellationCallback);
        });
    }

    /**
     * Converts  {@link com.google.common.util.concurrent.ListenableFuture} to {@link java.util.concurrent.CompletableFuture}.
     */
    public static <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenableFuture) {
        Optional<CompletableFuture<T>> originalValue = (Optional<CompletableFuture<T>>) OriginSource.extractOriginalValue(listenableFuture, CompletableFuture.class);
        return originalValue.orElseGet(() -> {
            CompletableFutureValueConsumer<T> completableFuture = Java8FutureUtils.createCompletableFuture(listenableFuture);
            CancellationCallback cancellationCallback = GuavaFutureUtils.registerListeners(listenableFuture, completableFuture);
            return Java8FutureUtils.registerCancellationCallback(completableFuture, cancellationCallback);
        });
    }
}
