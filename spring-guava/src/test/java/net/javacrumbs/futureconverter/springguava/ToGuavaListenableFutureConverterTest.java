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

public class ToGuavaListenableFutureConverterTest {

    public static final String VALUE = "test";
    public final FutureCallback<String> callback = mock(FutureCallback.class);
    private final CountDownLatch waitLatch = new CountDownLatch(1);
    private final AsyncListenableTaskExecutor executor = new TaskExecutorAdapter(Executors.newCachedThreadPool());

    @Test
    public void testConvertToGuavaListenableCompleted() throws ExecutionException, InterruptedException {
        ListenableFuture<String> originalFuture = executor.submitListenable(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return VALUE;
                    }
                });

        com.google.common.util.concurrent.ListenableFuture<String> newFuture = toGuavaListenableFuture(originalFuture);
        assertEquals(VALUE, newFuture.get());
        assertEquals(true, newFuture.isDone());
        assertEquals(false, newFuture.isCancelled());
        addCallback(newFuture, callback);
        verify(callback).onSuccess(VALUE);
    }

    @Test
    public void testRun() throws ExecutionException, InterruptedException {
        ListenableFuture<String> originalFuture = createRunningFuture();
        com.google.common.util.concurrent.ListenableFuture<String> convertedFuture = toGuavaListenableFuture(originalFuture);
        addCallback(convertedFuture, callback);
        assertEquals(false, convertedFuture.isDone());
        assertEquals(false, convertedFuture.isCancelled());
        waitLatch.countDown();

        //wait for the result
        assertEquals(VALUE, convertedFuture.get());
        assertEquals(true, convertedFuture.isDone());
        assertEquals(false, convertedFuture.isCancelled());

        //has to wait for the other thread
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
        verify(callback).onSuccess(VALUE);
    }

    @Test
    public void testCancelOriginal() throws ExecutionException, InterruptedException {
        ListenableFuture<String> originalFuture = createRunningFuture();
        originalFuture.cancel(true);

        com.google.common.util.concurrent.ListenableFuture<String> convertedFuture = toGuavaListenableFuture(originalFuture);

        try {
            convertedFuture.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, convertedFuture.isDone());
        assertEquals(true, convertedFuture.isCancelled());
        addCallback(convertedFuture, callback);
        verify(callback).onFailure(any(RuntimeException.class));
    }

    private ListenableFuture<String> createRunningFuture() {
        return executor.submitListenable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                waitLatch.await();
                return VALUE;
            }
        });
    }

    @Test
    public void testCancelNew() throws ExecutionException, InterruptedException {
        ListenableFuture<String> originalFuture = createRunningFuture();
        com.google.common.util.concurrent.ListenableFuture<String> convertedFuture = toGuavaListenableFuture(originalFuture);
        convertedFuture.cancel(true);

        try {
            convertedFuture.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, convertedFuture.isDone());
        assertEquals(true, convertedFuture.isCancelled());
        assertEquals(true, originalFuture.isDone());
        assertEquals(true, originalFuture.isCancelled());
        addCallback(convertedFuture, callback);
        verify(callback).onFailure(any(RuntimeException.class));
    }

    private void addCallback(com.google.common.util.concurrent.ListenableFuture<String> convertedFuture, FutureCallback<String> callback) {
        Futures.addCallback(convertedFuture, callback, MoreExecutors.directExecutor());
    }

    @Test
    public void testConvertToListenableException() throws ExecutionException, InterruptedException {
        Exception exception = new RuntimeException("test");
        doTestException(exception);
    }

    @Test
    public void testConvertToListenableIOException() throws ExecutionException, InterruptedException {
        Exception exception = new IOException("test");
        doTestException(exception);
    }


    private void doTestException(final Exception exception) throws InterruptedException {
        ListenableFuture<String> originalFuture = executor.submitListenable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                throw exception;
            }
        });
        com.google.common.util.concurrent.ListenableFuture<String> convertedFuture = toGuavaListenableFuture(originalFuture);
        try {
            convertedFuture.get();
            fail("Exception expected");
        } catch (ExecutionException e) {
            assertEquals(exception, e.getCause());
        }
        assertEquals(true, convertedFuture.isDone());
        assertEquals(false, convertedFuture.isCancelled());

        addCallback(convertedFuture, callback);
        verify(callback).onFailure(exception);
    }
}
