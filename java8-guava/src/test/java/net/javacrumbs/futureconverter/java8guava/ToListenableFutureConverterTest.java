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
package net.javacrumbs.futureconverter.java8guava;

import com.google.common.util.concurrent.ListenableFuture;
import jdk.nashorn.internal.ir.annotations.Ignore;
import net.javacrumbs.futureconverter.common.test.AbstractConverterHelperBasedTest;
import net.javacrumbs.futureconverter.common.test.guava.GuavaConvertedFutureTestHelper;
import net.javacrumbs.futureconverter.common.test.java8.Java8OriginalFutureTestHelper;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static net.javacrumbs.futureconverter.java8guava.FutureConverter.toCompletableFuture;
import static net.javacrumbs.futureconverter.java8guava.FutureConverter.toListenableFuture;


public class ToListenableFutureConverterTest extends AbstractConverterHelperBasedTest<
        CompletableFuture<String>,
        ListenableFuture<String>> {


    public ToListenableFutureConverterTest() {
        super(new Java8OriginalFutureTestHelper(), new GuavaConvertedFutureTestHelper());
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
