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
package net.javacrumbs.futureconverter.guavarx;

import com.google.common.util.concurrent.ListenableFuture;
import net.javacrumbs.futureconverter.common.internal.CancellationCallback;
import net.javacrumbs.futureconverter.common.internal.OriginSource;
import net.javacrumbs.futureconverter.guavacommon.GuavaFutureUtils;
import net.javacrumbs.futureconverter.guavacommon.GuavaFutureUtils.ListenableFutureValueConsumer;
import net.javacrumbs.futureconverter.guavacommon.RxJavaFutureUtils;
import net.javacrumbs.futureconverter.guavacommon.RxJavaFutureUtils.SingleWrappingValueConsumer;
import rx.Single;

import java.util.Optional;

public class FutureConverter {

    /**
     * Converts {@link com.google.common.util.concurrent.ListenableFuture} to  {@link rx.Single}.
     * The original future is NOT canceled upon unsubscribe.
     */
    public static <T> Single<T> toSingle(ListenableFuture<T> listenableFuture) {
        Optional<Single<T>> originalValue = (Optional<Single<T>>) OriginSource.extractOriginalValue(listenableFuture, Single.class);
        return originalValue.orElseGet(() -> {
            SingleWrappingValueConsumer<T> valueConsumer = RxJavaFutureUtils.createSingleWrappingValueConsumer(listenableFuture);
            GuavaFutureUtils.registerListeners(listenableFuture, valueConsumer);
            return valueConsumer.getSingle();
        });
    }

    /**
     * Converts  {@link rx.Observable} to {@link com.google.common.util.concurrent.ListenableFuture}.
     * Modifies the original Observable and takes only the first value.
     */
    public static <T> ListenableFuture<T> toListenableFuture(Single<T> single) {
        Optional<ListenableFuture<T>> originalValue = (Optional<ListenableFuture<T>>) OriginSource.extractOriginalValue(single, ListenableFuture.class);
        return originalValue.orElseGet(() -> {
            ListenableFutureValueConsumer<T> listenableFuture = GuavaFutureUtils.createListenableFuture(single);
            CancellationCallback cancellationCallback = RxJavaFutureUtils.registerListeners(single, listenableFuture);
            return GuavaFutureUtils.registerCancellationCallback(listenableFuture, cancellationCallback);
        });
    }
}
