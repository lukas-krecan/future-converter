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
import org.junit.Test;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.javacrumbs.futureconverter.springguava.FutureConverter.toSpringListenableFuture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ToSpringListenableFutureConverterTest {

    public static final String VALUE = "test";
    public final ListenableFutureCallback<String> callback = mock(ListenableFutureCallback.class);
    private final CountDownLatch waitLatch = new CountDownLatch(1);
    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    @Test
    public void testConvertToSpringListenableCompleted() throws ExecutionException, InterruptedException {
        com.google.common.util.concurrent.ListenableFuture<String> completed = Futures.immediateFuture(VALUE);
        ListenableFuture<String> listenable = toSpringListenableFuture(completed);
        assertEquals(VALUE, listenable.get());
        assertEquals(true, listenable.isDone());
        assertEquals(false, listenable.isCancelled());
        listenable.addCallback(callback);
        verify(callback).onSuccess(VALUE);
    }

    @Test
    public void testRun() throws ExecutionException, InterruptedException {
        com.google.common.util.concurrent.ListenableFuture<String> guavaFuture = createRunningFuture();
        ListenableFuture<String> listenable = toSpringListenableFuture(guavaFuture);
        listenable.addCallback(callback);
        assertEquals(false, listenable.isDone());
        assertEquals(false, listenable.isCancelled());
        waitLatch.countDown();

        //wait for the result
        assertEquals(VALUE, listenable.get());
        assertEquals(true, listenable.isDone());
        assertEquals(false, listenable.isCancelled());

        //has to wait for the other thread
        final CountDownLatch latch = new CountDownLatch(1);
        listenable.addCallback(new ListenableFutureCallback<String>() {
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
        verify(callback).onSuccess(VALUE);
    }

    @Test
    public void testCancelOriginal() throws ExecutionException, InterruptedException {
        com.google.common.util.concurrent.ListenableFuture<String> guavaFuture = createRunningFuture();
        guavaFuture.cancel(true);

        ListenableFuture<String> listenable = toSpringListenableFuture(guavaFuture);

        try {
            listenable.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, listenable.isDone());
        assertEquals(true, listenable.isCancelled());
        listenable.addCallback(callback);
        verify(callback).onFailure(any(RuntimeException.class));
    }

    private com.google.common.util.concurrent.ListenableFuture<String> createRunningFuture() {
        return executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    waitLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return VALUE;
            }
        });
    }

    @Test
    public void testCancelNew() throws ExecutionException, InterruptedException {
        com.google.common.util.concurrent.ListenableFuture<String> guavaFuture = createRunningFuture();
        ListenableFuture<String> listenable = toSpringListenableFuture(guavaFuture);
        listenable.cancel(true);

        try {
            listenable.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, listenable.isDone());
        assertEquals(true, listenable.isCancelled());
        assertEquals(true, guavaFuture.isDone());
        assertEquals(true, guavaFuture.isCancelled());
        listenable.addCallback(callback);
        verify(callback).onFailure(any(RuntimeException.class));
    }

    @Test
    public void testConvertToListenableException() throws ExecutionException, InterruptedException {
        Throwable exception = new RuntimeException("test");
        doTestException(exception);
    }

    @Test
    public void testConvertToListenableIOException() throws ExecutionException, InterruptedException {
        Throwable exception = new IOException("test");
        doTestException(exception);
    }


    private void doTestException(Throwable exception) throws InterruptedException {
        com.google.common.util.concurrent.ListenableFuture<String> guavaFuture = Futures.immediateFailedFuture(exception);
        ListenableFuture<String> listenable = toSpringListenableFuture(guavaFuture);
        try {
            listenable.get();
            fail("Exception expected");
        } catch (ExecutionException e) {
            assertEquals(exception, e.getCause());
        }
        assertEquals(true, listenable.isDone());
        assertEquals(false, listenable.isCancelled());

        listenable.addCallback(callback);
        verify(callback).onFailure(exception);
    }
}
