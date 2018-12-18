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
package net.javacrumbs.futureconverter.guavarx2;

import com.google.common.util.concurrent.ListenableFuture;
import io.reactivex.Single;
import net.javacrumbs.futureconverter.common.test.guava.GuavaOriginalFutureTestHelper;
import net.javacrumbs.futureconverter.common.test.rxjava2.AbstractFutureToSingleConverterTest;

public class ToSingleConverterTest extends AbstractFutureToSingleConverterTest<ListenableFuture<String>> {

    public ToSingleConverterTest() {
        super(new GuavaOriginalFutureTestHelper());
    }

    @Override
    protected Single<String> toSingle(ListenableFuture<String> future) {
        return FutureConverter.toSingle(future);
    }

    @Override
    protected ListenableFuture<String> toFuture(Single<String> single) {
        return FutureConverter.toListenableFuture(single);
    }
}
