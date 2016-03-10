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
package net.javacrumbs.futureconverter.guavarx;

import com.google.common.util.concurrent.ListenableFuture;
import net.javacrumbs.futureconverter.common.internal.OriginSource;
import net.javacrumbs.futureconverter.common.internal.SettableFuture;
import net.javacrumbs.futureconverter.guavacommon.GuavaFutureUtils;
import net.javacrumbs.futureconverter.guavacommon.RxJavaFutureUtils;
import rx.Observable;

public class FutureConverter {

    /**
     * Converts {@link com.google.common.util.concurrent.ListenableFuture} to  {@link rx.Observable}.
     * The original future is NOT canceled upon unsubscribe.
     *
     * @param listenableFuture
     * @param <T>
     * @return
     */
    public static <T> Observable<T> toObservable(ListenableFuture<T> listenableFuture) {
        if (listenableFuture instanceof OriginSource && ((OriginSource) listenableFuture).getOrigin() instanceof Observable) {
            return (Observable<T>) ((OriginSource) listenableFuture).getOrigin();
        } else {
            return RxJavaFutureUtils.createObservable(GuavaFutureUtils.createCommonListenable(listenableFuture));
        }
    }

    /**
     * Converts  {@link rx.Observable} to {@link com.google.common.util.concurrent.ListenableFuture}.
     * Modifies the original Observable and takes only the first value.
     *
     * @param observable
     * @param <T>
     * @return
     */
    public static <T> ListenableFuture<T> toListenableFuture(Observable<T> observable) {
        if (observable instanceof OriginSource && ((OriginSource) observable).getOrigin() instanceof ListenableFuture) {
            return (ListenableFuture<T>) ((OriginSource) observable).getOrigin();
        } else {
            SettableFuture<T> settableFuture = GuavaFutureUtils.createSettableFuture(observable);
            RxJavaFutureUtils.waitForResults(observable, settableFuture);
            return (ListenableFuture<T>) settableFuture;
        }
    }

}
