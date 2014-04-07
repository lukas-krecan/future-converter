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
package net.javacrumbs.futureconverter.springrx;

import com.google.common.util.concurrent.ForwardingExecutorService;
import org.junit.After;
import org.junit.Test;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static net.javacrumbs.futureconverter.springrx.FutureConverter.toListenableFuture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ToListenableFutureConverterTest {

    public static final String VALUE = "test";

    private final CountDownLatch waitLatch = new CountDownLatch(1);

    private final ListenableFutureCallback<String> callback = mock(ListenableFutureCallback.class);

    private final ExecutorService wrappedExecutorService = Executors.newSingleThreadExecutor();

    private Future<?> futureTask;

    private final ExecutorService executorService = new ForwardingExecutorService() {
        @Override
        protected ExecutorService delegate() {
            return wrappedExecutorService;
        }

        @Override
        public Future<?> submit(Runnable task) {
            Future<?> future = super.submit(task);
            futureTask = future;
            return future;
        }
    };
    private AtomicInteger subscribed = new AtomicInteger(0);

    @After
    public void shutdown() {
        executorService.shutdown();
    }

    @Test
    public void testConvertToObservableCompleted() throws ExecutionException, InterruptedException {
        Observable<String> observable = Observable.from(VALUE);

        ListenableFuture<String> listenable = toListenableFuture(observable);
        listenable.addCallback(callback);

        verify(callback).onSuccess(VALUE);
        assertEquals(VALUE, listenable.get());
        assertEquals(true, listenable.isDone());
    }

    @Test
    public void testRun() throws ExecutionException, InterruptedException {
        Observable<String> observable = createAsyncObservable();

        ListenableFuture<String> listenable = toListenableFuture(observable);
        listenable.addCallback(callback);
        verifyZeroInteractions(callback);
        assertEquals(false, listenable.isDone());
        waitLatch.countDown();

        assertEquals(VALUE, listenable.get());
        verify(callback).onSuccess(VALUE);
        assertEquals(true, listenable.isDone());
        assertEquals(1, subscribed.get());
    }


    @Test
    public void testCancelOriginal() throws ExecutionException, InterruptedException {
        Observable<String> observable = createAsyncObservable();

        ListenableFuture<String> listenable = toListenableFuture(observable);
        listenable.addCallback(callback);


        futureTask.cancel(true);

        try {
            listenable.get();
        } catch (ExecutionException e) {
            assertEquals(InterruptedException.class, e.getCause().getClass());
        }
        verify(callback).onFailure(any(InterruptedException.class));
    }

    @Test
    public void testCancelOuter() throws ExecutionException, InterruptedException {
        Observable<String> observable = createAsyncObservable();

        ListenableFuture<String> listenable = toListenableFuture(observable);
        listenable.addCallback(callback);

        listenable.cancel(true);

        try {
            futureTask.get();
        } catch (CancellationException e) {
            //ok
        }
        assertTrue(futureTask.isCancelled());
        assertTrue(listenable.isCancelled());
    }


    @Test
    public void testConvertToCompletableException() throws ExecutionException, InterruptedException {
        doTestException(new RuntimeException("test"));
    }

    @Test
    public void testIOException() throws ExecutionException, InterruptedException {
        doTestException(new IOException("test"));
    }

    private void doTestException(final Exception exception) throws ExecutionException, InterruptedException {
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onError(exception);
            }
        });

        ListenableFuture<String> listenableFuture = toListenableFuture(observable);
        try {
            listenableFuture.get();
        } catch (ExecutionException e) {
            assertSame(exception, e.getCause());
        }
    }


    private Observable<String> createAsyncObservable() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                subscribed.incrementAndGet();
                Future<?> future = executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            System.out.println("Started");
                            waitLatch.await();
                            subscriber.onNext(VALUE);
                            subscriber.onCompleted();
                        } catch (InterruptedException e) {
                            subscriber.onError(e);
                            throw new RuntimeException(e);
                        }
                    }
                });
                subscriber.add(Subscriptions.from(future));
            }
        });
    }
}
