/*
 * Copyright © 2014-2019 Lukas Krecan (lukas@krecan.net)
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
package net.javacrumbs.futureconverter.springrx;

import net.javacrumbs.futureconverter.rxjavacommon.RxJavaFutureUtils;
import net.javacrumbs.futureconverter.springcommon.SpringFutureUtils;
import org.springframework.util.concurrent.ListenableFuture;
import rx.Single;

public class FutureConverter {

    /**
     * Converts {@link ListenableFuture} to  {@link rx.Single}.
     * The original future is canceled upon unsubscribe.
     */
    public static <T> Single<T> toSingle(ListenableFuture<T> listenableFuture) {
        return RxJavaFutureUtils.createSingle(SpringFutureUtils.createValueSource(listenableFuture));
    }

    /**
     * Converts  {@link rx.Single} to {@link ListenableFuture}.
     */
    public static <T> ListenableFuture<T> toListenableFuture(Single<T> single) {
        return SpringFutureUtils.createListenableFuture(RxJavaFutureUtils.createValueSource(single));
    }

}
