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
package net.javacrumbs.futureconverter.springguava;

import net.javacrumbs.futureconverter.common.test.AbstractConverterHelperBasedTest;
import net.javacrumbs.futureconverter.common.test.guava.GuavaConvertedFutureTestHelper;
import net.javacrumbs.futureconverter.common.test.spring.SpringOriginalFutureTestHelper;
import org.springframework.util.concurrent.ListenableFuture;

import static net.javacrumbs.futureconverter.springguava.FutureConverter.toGuavaListenableFuture;
import static net.javacrumbs.futureconverter.springguava.FutureConverter.toSpringListenableFuture;

public class ToGuavaListenableFutureConverterTest extends AbstractConverterHelperBasedTest<
        ListenableFuture<String>,
        com.google.common.util.concurrent.ListenableFuture<String>> {

    public ToGuavaListenableFutureConverterTest() {
        super(new SpringOriginalFutureTestHelper(), new GuavaConvertedFutureTestHelper());
    }

    @Override
    protected com.google.common.util.concurrent.ListenableFuture<String> convert(ListenableFuture<String> originalFuture) {
        return toGuavaListenableFuture(originalFuture);
    }

    @Override
    protected ListenableFuture<String> convertBack(com.google.common.util.concurrent.ListenableFuture<String> converted) {
        return toSpringListenableFuture(converted);
    }
}
