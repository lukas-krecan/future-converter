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
package net.javacrumbs.futureconverter.springrx2;

import io.reactivex.Single;
import net.javacrumbs.futureconverter.rxjava2common.RxJava2FutureUtils;
import net.javacrumbs.futureconverter.springcommon.SpringFutureUtils;
import org.springframework.util.concurrent.ListenableFuture;

public class FutureConverter {

    /**
     * Converts {@link ListenableFuture} to  {@link io.reactivex.Single}.
     * The original future is canceled upon unsubscribe.
     */
    public static <T> Single<T> toSingle(ListenableFuture<T> listenableFuture) {
        return RxJava2FutureUtils.createSingle(SpringFutureUtils.createValueSource(listenableFuture));
    }

    /**
     * Converts  {@link io.reactivex.Single} to {@link ListenableFuture}.
     */
    public static <T> ListenableFuture<T> toListenableFuture(Single<T> single) {
        return SpringFutureUtils.createListenableFuture(RxJava2FutureUtils.createValueSource(single));
    }

}
