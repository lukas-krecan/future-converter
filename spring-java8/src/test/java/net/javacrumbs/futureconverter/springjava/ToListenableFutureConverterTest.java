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
package net.javacrumbs.futureconverter.springjava;

import jdk.nashorn.internal.ir.annotations.Ignore;
import net.javacrumbs.futureconverter.common.test.AbstractConverterHelperBasedTest;
import net.javacrumbs.futureconverter.common.test.java8.Java8OriginalFutureTestHelper;
import net.javacrumbs.futureconverter.common.test.spring.SpringConvertedFutureTestHelper;
import org.junit.Test;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.javacrumbs.futureconverter.springjava.FutureConverter.toCompletableFuture;
import static net.javacrumbs.futureconverter.springjava.FutureConverter.toListenableFuture;

public class ToListenableFutureConverterTest extends AbstractConverterHelperBasedTest<
        CompletableFuture<String>,
        ListenableFuture<String>> {


    public ToListenableFutureConverterTest() {
        super(new Java8OriginalFutureTestHelper(), new SpringConvertedFutureTestHelper());
    }

    @Override
    protected ListenableFuture<String> convert(CompletableFuture<String> originalFuture) {
        return toListenableFuture(originalFuture);
    }

    @Override
    protected CompletableFuture<String> convertBack(ListenableFuture<String> converted) {
        return toCompletableFuture(converted);
    }

    @Test
    @Ignore
    public void testCancelBeforeConversion() throws ExecutionException, InterruptedException {
        // completable futures can not be canceled
    }

}
