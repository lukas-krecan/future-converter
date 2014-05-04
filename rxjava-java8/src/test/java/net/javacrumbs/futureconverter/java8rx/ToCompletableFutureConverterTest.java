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
package net.javacrumbs.futureconverter.java8rx;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static net.javacrumbs.futureconverter.java8rx.FutureConverter.toCompletableFuture;
import static net.javacrumbs.futureconverter.java8rx.FutureConverter.toObservable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ToCompletableFutureConverterTest {

    public static final String VALUE = "test";

    private final CountDownLatch waitLatch = new CountDownLatch(1);
    private final CountDownLatch taskStartedLatch = new CountDownLatch(1);

    private AtomicInteger subscribed = new AtomicInteger(0);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private AtomicReference<Future> futureTaskRef = new AtomicReference<>();

    @After
    public void cleanup() {
        waitLatch.countDown();
        executorService.shutdown();
    }


    @Test
    public void testConvertToCompletableCompleted() throws ExecutionException, InterruptedException {
        Observable<String> observable = Observable.from(VALUE);
        CompletableFuture<String> completable = toCompletableFuture(observable);
        Consumer<String> consumer = mockConsumer();

        CountDownLatch latch = new CountDownLatch(1);
        completable.thenAccept(consumer).thenRun(latch::countDown);

        assertEquals(VALUE, completable.get());
        assertEquals(true, completable.isDone());
        assertEquals(false, completable.isCancelled());
        latch.await();
        verify(consumer).accept(VALUE);
        assertSame(observable, toObservable(completable));

    }

    @SuppressWarnings("unchecked")
    private Consumer<String> mockConsumer() {
        return mock(Consumer.class);
    }

    @Test
    public void testRun() throws ExecutionException, InterruptedException {
        Observable<String> observable = createAsyncObservable();
        CompletableFuture<String> completable = toCompletableFuture(observable);

        Consumer<String> consumer = mockConsumer();
        assertEquals(false, completable.isDone());
        assertEquals(false, completable.isCancelled());

        CountDownLatch latch = new CountDownLatch(1);
        completable.thenAccept(consumer).thenRun(latch::countDown);
        waitLatch.countDown();

        //wait for the result
        assertEquals(VALUE, completable.get());
        assertEquals(true, completable.isDone());
        assertEquals(false, completable.isCancelled());

        latch.await();
        verify(consumer).accept(VALUE);
        assertEquals(1, subscribed.get());
    }

    @Test
    public void testCancelOriginal() throws ExecutionException, InterruptedException {
        Observable<String> observable = createAsyncObservable();

        CompletableFuture<String> completable = toCompletableFuture(observable);
        System.out.println("Future cancel:" + futureTaskRef.get());

        taskStartedLatch.await(); //wait for the task to start
        futureTaskRef.get().cancel(true);
        assertTrue(futureTaskRef.get().isCancelled());

        try {
            completable.get();
            fail("Exception expected");
        } catch (ExecutionException e) {
            //ok
        }
        assertEquals(true, completable.isDone());
        assertEquals(false, completable.isCancelled());

        assertEquals(1, subscribed.get());
    }

    @Test
    public void testUnsubscribe() throws ExecutionException, InterruptedException {
        Observable<String> observable = createAsyncObservable();

        CompletableFuture<String> completable = toCompletableFuture(observable);
        ((ObservableCompletableFuture) completable).getSubscription().unsubscribe();

        try {
            completable.get();
            fail("Exception expected");
        } catch (ExecutionException e) {
            //ok
        }
        assertEquals(true, completable.isDone());
        assertEquals(false, completable.isCancelled());

        assertEquals(1, subscribed.get());
    }

    @Test
    public void testCancelNew() throws ExecutionException, InterruptedException {
        Observable<String> observable = createAsyncObservable();

        CompletableFuture<String> completable = toCompletableFuture(observable);
        assertTrue(completable.cancel(true));

        try {
            completable.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, completable.isDone());
        assertEquals(true, completable.isCancelled());

        assertTrue(((ObservableCompletableFuture) completable).getSubscription().isUnsubscribed());
        assertEquals(1, subscribed.get());
    }


    @Test
    public void testCancelCompleted() throws ExecutionException, InterruptedException {
        Observable<String> observable = Observable.from(VALUE);

        CompletableFuture<String> completable = toCompletableFuture(observable);
        assertFalse(completable.cancel(true));

        assertEquals(VALUE, completable.get());

        assertTrue(completable.isDone());
        assertFalse(completable.isCancelled());
    }

    //
    @Test
    public void testRuntimeException() throws ExecutionException, InterruptedException {
        doTestException(new RuntimeException("test"));
    }

    @Test
    public void testIOException() throws ExecutionException, InterruptedException {
        doTestException(new IOException("test"));
    }


    private void doTestException(final Exception exception) throws ExecutionException, InterruptedException {
        Observable<String> observable = Observable.create((Subscriber<? super String> subscriber) -> {
            subscriber.onError(exception);
        });

        CompletableFuture<String> completableFuture = toCompletableFuture(observable);
        try {
            completableFuture.get();
        } catch (ExecutionException e) {
            assertSame(exception, e.getCause());
        }
    }


    private Observable<String> createAsyncObservable() {
        return Observable.create((Subscriber<? super String> subscriber) -> {
            subscribed.incrementAndGet();
            Future<?> future = executorService.submit(() -> {
                try {
                    taskStartedLatch.countDown();
                    waitLatch.await();
                    subscriber.onNext(VALUE);
                    subscriber.onCompleted();
                } catch (InterruptedException e) {
                    subscriber.onError(e);
                    throw new RuntimeException(e);
                }
            });
            subscriber.add(Subscriptions.from(future));
            assertTrue(this.futureTaskRef.compareAndSet(null, future));
        });
    }
}
