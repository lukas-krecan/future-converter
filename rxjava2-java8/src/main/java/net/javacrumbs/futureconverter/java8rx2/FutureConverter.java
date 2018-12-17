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
package net.javacrumbs.futureconverter.java8rx2;

import io.reactivex.Single;
import net.javacrumbs.futureconverter.java8common.Java8FutureUtils;
import net.javacrumbs.futureconverter.rxjava2common.RxJava2FutureUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Converts between Java 8 {@link java.util.concurrent.CompletableFuture} and RxJava {@link io.reactivex.Single}
 */
public class FutureConverter {

    /**
     * Converts {@link io.reactivex.Single} to {@link java.util.concurrent.CompletableFuture}.
     */
    public static <T> CompletableFuture<T> toCompletableFuture(Single<T> single) {
        return Java8FutureUtils.createCompletableFuture(RxJava2FutureUtils.createValueSource(single));
    }

    /**
     * Converts {@link java.util.concurrent.CompletableFuture} to {@link io.reactivex.Single}.
     * The original future is canceled upon unsubscribe.
     */
    public static <T> Single<T> toSingle(CompletableFuture<T> completableFuture) {
        return RxJava2FutureUtils.createSingle(Java8FutureUtils.createValueSource(completableFuture));
    }
}

