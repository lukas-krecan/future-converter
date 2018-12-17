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
package net.javacrumbs.futureconverter.springguava;

import net.javacrumbs.futureconverter.guavacommon.GuavaFutureUtils;
import net.javacrumbs.futureconverter.springcommon.SpringFutureUtils;
import org.springframework.util.concurrent.ListenableFuture;

/**
 * Converts between Guava {@link com.google.common.util.concurrent.ListenableFuture} and Spring 4 {@link org.springframework.util.concurrent.ListenableFuture}.
 */
public class FutureConverter {

    /**
     * Converts Guava {@link com.google.common.util.concurrent.ListenableFuture} to Spring 4 {@link org.springframework.util.concurrent.ListenableFuture}
     */
    public static <T> ListenableFuture<T> toSpringListenableFuture(com.google.common.util.concurrent.ListenableFuture<T> guavaListenableFuture) {
        return SpringFutureUtils.createListenableFuture(GuavaFutureUtils.createValueSourceFuture(guavaListenableFuture));
    }

    /**
     * Converts Spring 4 {@link org.springframework.util.concurrent.ListenableFuture}
     * to Guava {@link com.google.common.util.concurrent.ListenableFuture}.
     */
    public static <T> com.google.common.util.concurrent.ListenableFuture<T> toGuavaListenableFuture(ListenableFuture<T> springListenableFuture) {
            return GuavaFutureUtils.createListenableFuture(SpringFutureUtils.createValueSourceFuture(springListenableFuture));
    }
}
