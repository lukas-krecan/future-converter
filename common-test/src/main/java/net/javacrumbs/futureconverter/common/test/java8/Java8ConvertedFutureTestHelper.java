/**
 * Copyright 2009-2014 the original author or authors.
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
import org.mockito.ArgumentCaptor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class Java8ConvertedFutureTestHelper implements ConvertedFutureTestHelper<CompletableFuture<String>> {

    private final Consumer<String> callback = mock(Consumer.class);

    private final Function<Throwable, Void> exceptionHandler = mock(Function.class);

    // latch to wait for callback to be called
    private final CountDownLatch callbackLatch = new CountDownLatch(1);

    @Override
    public void addCallbackTo(CompletableFuture<String> convertedFuture) {
        convertedFuture.thenAccept(callback).exceptionally(exceptionHandler).thenRun(callbackLatch::countDown);
    }


    @Override
    public void verifyCallbackCalledWithCorrectValue() throws InterruptedException {
        callbackLatch.await();
        verify(callback).accept(AbstractConverterTest.VALUE);
    }

    @Override
    public void waitForCalculationToFinish(CompletableFuture<String> convertedFuture) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        convertedFuture.thenRun(latch::countDown);
        latch.await(1, TimeUnit.SECONDS);
    }

    @Override
    public void verifyCallbackCalledWithException(Exception exception) throws InterruptedException {
        callbackLatch.await();
        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(exceptionHandler).apply(captor.capture());
        assertEquals(CompletionException.class, captor.getValue().getClass());
        assertEquals(exception, captor.getValue().getCause());

    }

    @Override
    public void verifyCallbackCalledWithException(Class<? extends Exception> exceptionClass) throws InterruptedException {
        callbackLatch.await();
        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(exceptionHandler).apply(captor.capture());
        assertEquals(CompletionException.class, captor.getValue().getClass());
        assertEquals(exceptionClass, captor.getValue().getCause().getClass());
    }

}
