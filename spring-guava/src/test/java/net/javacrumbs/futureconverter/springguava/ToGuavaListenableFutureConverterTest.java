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
package net.javacrumbs.futureconverter.springguava;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Test;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.util.concurrent.ListenableFuture;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.javacrumbs.futureconverter.springguava.FutureConverter.toGuavaListenableFuture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ToGuavaListenableFutureConverterTest extends AbstractConverterTest<
        ListenableFuture<String>,
        com.google.common.util.concurrent.ListenableFuture<String>,
        FutureCallback<String>> {

    public final FutureCallback<String> callback = mock(FutureCallback.class);
    private final AsyncListenableTaskExecutor executor = new TaskExecutorAdapter(Executors.newCachedThreadPool());

    @Override
    protected void waitForCalculationToFinish(com.google.common.util.concurrent.ListenableFuture<String> convertedFuture) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        Futures.addCallback(convertedFuture, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable t) {
                latch.countDown();
            }
        }, MoreExecutors.directExecutor());

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
    protected ListenableFuture<String> createExceptionalFuture(final Exception exception) {
        return executor.submitListenable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                throw exception;
            }
        });
    }


    @Override
    protected com.google.common.util.concurrent.ListenableFuture<String> convert(ListenableFuture<String> originalFuture) {
        return toGuavaListenableFuture(originalFuture);
    }

    @Override
    protected ListenableFuture<String> createFinishedOriginal() {
        return executor.submitListenable(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return VALUE;
                    }
                });
    }

    @Override
    protected void addCallbackTo(com.google.common.util.concurrent.ListenableFuture<String> convertedFuture) {
        Futures.addCallback(convertedFuture, callback, MoreExecutors.directExecutor());
    }

    @Override
    protected ListenableFuture<String> createRunningFuture() {
        return executor.submitListenable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                waitForSignal();
                return VALUE;
            }
        });
    }

    @Override
    protected void verifyCallbackCalledWithCorrectValue() {
        verify(callback).onSuccess(VALUE);
    }
}
