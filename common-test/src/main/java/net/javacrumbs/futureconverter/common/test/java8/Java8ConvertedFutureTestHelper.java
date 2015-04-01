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
package net.javacrumbs.futureconverter.common.test.java8;

import net.javacrumbs.futureconverter.common.test.AbstractConverterTest;
import net.javacrumbs.futureconverter.common.test.ConvertedFutureTestHelper;
import net.javacrumbs.futureconverter.common.test.common.CommonConvertedFutureTestHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class Java8ConvertedFutureTestHelper extends CommonConvertedFutureTestHelper implements ConvertedFutureTestHelper<CompletableFuture<String>> {

    private final Consumer<String> callback = mock(Consumer.class);

    private final Function<Throwable, String> exceptionHandler = mock(Function.class);

    @Override
    public void addCallbackTo(CompletableFuture<String> convertedFuture) {
        convertedFuture.exceptionally(exceptionHandler).thenAccept(callback).thenRun(this::callbackCalled);
    }

    @Override
    public void verifyCallbackCalledWithCorrectValue() throws InterruptedException {
        waitForCallback();
        verify(callback).accept(AbstractConverterTest.VALUE);
    }

    @Override
    public void waitForCalculationToFinish(CompletableFuture<String> convertedFuture) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        convertedFuture.thenRun(latch::countDown);
        latch.await(1, TimeUnit.SECONDS);
    }

    @Override
    public void verifyCallbackCalledWithException(Exception exception) {
        waitForCallback();
        verify(exceptionHandler).apply(exception);

    }

    @Override
    public void verifyCallbackCalledWithException(Class<? extends Exception> exceptionClass) {
        waitForCallback();
        verify(exceptionHandler).apply(any(exceptionClass));
    }

}
