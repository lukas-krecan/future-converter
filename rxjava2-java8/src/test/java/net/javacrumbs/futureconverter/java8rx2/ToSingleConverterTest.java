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
import net.javacrumbs.futureconverter.common.test.java8.Java8OriginalFutureTestHelper;
import net.javacrumbs.futureconverter.common.test.rxjava2.AbstractFutureToSingleConverterTest;

import java.util.concurrent.CompletableFuture;

public class ToSingleConverterTest extends AbstractFutureToSingleConverterTest<CompletableFuture<String>> {
    public ToSingleConverterTest() {
        super(new Java8OriginalFutureTestHelper());
    }

    @Override
    protected Single<String> toSingle(CompletableFuture<String> future) {
        return FutureConverter.toSingle(future);
    }

    @Override
    protected CompletableFuture<String> toFuture(Single<String> single) {
        return FutureConverter.toCompletableFuture(single);
    }
}
