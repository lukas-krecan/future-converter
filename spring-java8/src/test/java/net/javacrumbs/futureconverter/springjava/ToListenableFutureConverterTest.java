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
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static net.javacrumbs.futureconverter.springjava.FutureConverter.toListenableFuture;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ToListenableFutureConverterTest extends AbstractConverterTest<
        CompletableFuture<String>,
        ListenableFuture<String>,
        ListenableFutureCallback<String>> {

    public final ListenableFutureCallback<String> callback = mock(ListenableFutureCallback.class);


    @Override
    protected ListenableFuture<String> convert(CompletableFuture<String> originalFuture) {
        return toListenableFuture(originalFuture);
    }

    @Override
    protected CompletableFuture<String> createFinishedOriginal() {
        return CompletableFuture.completedFuture(VALUE);
    }

    @Override
    protected void addCallbackTo(ListenableFuture<String> convertedFuture) {
        convertedFuture.addCallback(callback);
    }

    @Override
    protected CompletableFuture<String> createRunningFuture() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                waitForSignal();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return VALUE;
        });
    }

    @Override
    protected void verifyCallbackCalledWithCorrectValue() {
        verify(callback).onSuccess(VALUE);
    }

    @Override
    protected void waitForCalculationToFinish(ListenableFuture<String> convertedFuture) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        convertedFuture.addCallback(new ListenableFutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                latch.countDown();
            }
        });
        latch.await(1, TimeUnit.SECONDS);
    }

    @Override
    protected void verifyCallbackCalledWithException(Exception exception) {
        verify(callback).onFailure(exception);
    }

    @Override
    protected void verifyCallbackCalledWithException(Class<? extends Exception> exceptionClass) {
        verify(callback).onFailure(any(exceptionClass));
    }

    @Override
    protected CompletableFuture<String> createExceptionalFuture(Exception exception) {
        CompletableFuture<String> completable = new CompletableFuture<>();
        completable.completeExceptionally(exception);
        return completable;
    }

    @Override
    @Ignore
    public void testCancelBeforeConversion() throws ExecutionException, InterruptedException {
        // CompletableFuture can not be canceled
    }
}
