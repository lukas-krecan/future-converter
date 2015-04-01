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
package net.javacrumbs.futureconverter.java8rx;

import net.javacrumbs.futureconverter.common.test.java8.Java8ConvertedFutureTestHelper;
import net.javacrumbs.futureconverter.common.test.rxjava.AbstractObservableToFutureConverterTest;
import rx.Observable;

import java.util.concurrent.CompletableFuture;

public class ToCompletableFutureConverterTest extends AbstractObservableToFutureConverterTest<CompletableFuture<String>> {
    public ToCompletableFutureConverterTest() {
        super(new Java8ConvertedFutureTestHelper());
    }

    @Override
    protected CompletableFuture<String> toFuture(Observable<String> observable) {
        return FutureConverter.toCompletableFuture(observable);
    }

    @Override
    protected Observable<String> toObservable(CompletableFuture<String> future) {
        return FutureConverter.toObservable(future);
    }
}
