/*
 * Copyright © 2014-2019 the original author or authors.
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
package net.javacrumbs.futureconverter.java8apifuture;


import com.google.api.core.ApiFuture;
import net.javacrumbs.futureconverter.apifuturecommon.ApiFutureUtils;
import net.javacrumbs.futureconverter.java8common.Java8FutureUtils;

import java.util.concurrent.CompletableFuture;

/**
 * Converts between {@link java.util.concurrent.CompletableFuture} and Google {@link com.google.api.core.ApiFuture}.
 */
public class FutureConverter {

    /**
     * Converts {@link java.util.concurrent.CompletableFuture} to {@link com.google.api.core.ApiFuture}.
     */
    public static <T> ApiFuture<T> toApiFuture(CompletableFuture<T> completableFuture) {
        return ApiFutureUtils.createApiFuture(Java8FutureUtils.createValueSourceFuture(completableFuture));
    }

    /**
     * Converts  {@link com.google.api.core.ApiFuture} to {@link java.util.concurrent.CompletableFuture}.
     */
    public static <T> CompletableFuture<T> toCompletableFuture(ApiFuture<T> apiFuture) {
        return Java8FutureUtils.createCompletableFuture(ApiFutureUtils.createValueSourceFuture(apiFuture));
    }
}
