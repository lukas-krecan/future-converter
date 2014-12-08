/**
 * Copyright 2009-2013 the original author or authors.
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
import net.javacrumbs.futureconverter.common.test.AbstractConverterTest;
import org.junit.After;
import org.mockito.ArgumentCaptor;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.javacrumbs.futureconverter.springjava.FutureConverter.toCompletableFuture;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ToCompletableFutureConverterTest extends AbstractConverterTest<
        ListenableFuture<String>,
        CompletableFuture<String>,
        Consumer<String>
        > {


    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Consumer<String> callback = mock(Consumer.class);

    private final Function<Throwable, Void> exceptionHandler = mock(Function.class);

    // latch to wait for callback to be called
    private final CountDownLatch callbackLatch = new CountDownLatch(1);

    @After
    public void shutdown() {
        executorService.shutdown();
    }

    @Override
    protected CompletableFuture<String> convert(ListenableFuture<String> originalFuture) {
        return toCompletableFuture(originalFuture);
    }

    @Override
    protected ListenableFuture<String> createFinishedOriginal() {
        ListenableFutureTask<String> listenable = new ListenableFutureTask<>(() -> VALUE);
        executorService.execute(listenable);
        return listenable;
    }

    @Override
    protected void addCallbackTo(CompletableFuture<String> convertedFuture) {
        convertedFuture.thenAccept(callback).exceptionally(exceptionHandler).thenRun(callbackLatch::countDown);
    }

    @Override
    protected ListenableFuture<String> createRunningFuture() {
        ListenableFutureTask<String> listenableFuture = new ListenableFutureTask<>(() -> {
            waitForSignal();
            return VALUE;
        });
        executorService.execute(listenableFuture);
        return listenableFuture;
    }

    @Override
    protected void verifyCallbackCalledWithCorrectValue() throws InterruptedException {
        callbackLatch.await();
        verify(callback).accept(VALUE);
    }

    @Override
    protected void waitForCalculationToFinish(CompletableFuture<String> convertedFuture) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        convertedFuture.thenRun(latch::countDown);
        latch.await(1, TimeUnit.SECONDS);
    }

    @Override
    protected void verifyCallbackCalledWithException(Exception exception) throws InterruptedException {
        callbackLatch.await();
        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(exceptionHandler).apply(captor.capture());
        assertEquals(CompletionException.class, captor.getValue().getClass());
        assertEquals(exception, captor.getValue().getCause());

    }

    @Override
    protected void verifyCallbackCalledWithException(Class<? extends Exception> exceptionClass) throws InterruptedException {
        callbackLatch.await();
        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(exceptionHandler).apply(captor.capture());
        assertEquals(CompletionException.class, captor.getValue().getClass());
        assertEquals(exceptionClass, captor.getValue().getCause().getClass());
    }

    @Override
    protected ListenableFuture<String> createExceptionalFuture(Exception exception) {
        ListenableFutureTask<String> listenable = new ListenableFutureTask<>(() -> {
            throw exception;
        });
        executorService.execute(listenable);
        return listenable;
    }
}
