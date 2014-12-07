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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.javacrumbs.futureconverter.springguava.FutureConverter.toSpringListenableFuture;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ToSpringListenableFutureConverterTest extends AbstractConverterTest<
        com.google.common.util.concurrent.ListenableFuture<String>,
        ListenableFuture<String>,
        ListenableFutureCallback<String>
        > {

    public final ListenableFutureCallback<String> callback = mock(ListenableFutureCallback.class);
    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    @Override
    protected ListenableFuture<String> convert(com.google.common.util.concurrent.ListenableFuture<String> originalFuture) {
        return toSpringListenableFuture(originalFuture);
    }

    @Override
    protected com.google.common.util.concurrent.ListenableFuture<String> createFinishedOriginal() {
        return Futures.immediateFuture(VALUE);
    }

    @Override
    protected void addCallbackTo(ListenableFuture<String> convertedFuture) {
        convertedFuture.addCallback(callback);
    }

    @Override
    protected com.google.common.util.concurrent.ListenableFuture<String> createRunningFuture() {
        return executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    waitForSignal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return VALUE;
            }
        });
    }

    @Override
    protected void verifyCallbackCalledWithCorrectValue() {
        verify(callback).onSuccess(VALUE);
    }

    @Override
    protected void waitForCalculationToFinish(ListenableFuture<String> convertedFuture) throws InterruptedException {
        //has to wait for the other thread
        final CountDownLatch latch = new CountDownLatch(1);
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
    protected com.google.common.util.concurrent.ListenableFuture<String> createExceptionalFuture(Exception exception) {
        return Futures.immediateFailedFuture(exception);
    }
}
