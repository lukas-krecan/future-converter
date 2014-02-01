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

import org.junit.Test;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static net.javacrumbs.futureconverter.springjava.FutureConverter.toListenableFuture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FutureConverterTest {

    public static final String VALUE = "test";
    public final ListenableFutureCallback<String> callback = mock(ListenableFutureCallback.class);

    @Test
    public void testConvertToListenableCompleted() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completable = CompletableFuture.completedFuture(VALUE);
        ListenableFuture<String> listenable = toListenableFuture(completable);
        assertEquals(VALUE, listenable.get());
        assertEquals(true, listenable.isDone());
        assertEquals(false, listenable.isCancelled());
        listenable.addCallback(callback);
        verify(callback).onSuccess(VALUE);
    }

    @Test
    public void testRun() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completable = CompletableFuture.supplyAsync(() -> {
            sleep(50);
            return VALUE;
        });
        ListenableFuture<String> listenable = toListenableFuture(completable);
        listenable.addCallback(callback);
        assertEquals(false, listenable.isDone());
        assertEquals(false, listenable.isCancelled());

        //wait for the result
        assertEquals(VALUE, listenable.get());
        assertEquals(true, listenable.isDone());
        assertEquals(false, listenable.isCancelled());

        //has to wait for the other thread
        CountDownLatch latch = new CountDownLatch(1);
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
    public void testCancel() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completable = CompletableFuture.supplyAsync(() -> {
            sleep(100);
            return "Hi";
        });
        completable.cancel(true);

        ListenableFuture<String> listenable = toListenableFuture(completable);

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
        CompletableFuture<String> completable = new CompletableFuture<>();
        completable.completeExceptionally(exception);
        ListenableFuture<String> listenable = toListenableFuture(completable);
        try {
            listenable.get();
            fail("Exception expected");
        } catch (ExecutionException e) {
            assertEquals(exception, e.getCause());
        }
        assertEquals(true, listenable.isDone());
        assertEquals(false, listenable.isCancelled());
        ;
        listenable.addCallback(callback);
        verify(callback).onFailure(exception);
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
