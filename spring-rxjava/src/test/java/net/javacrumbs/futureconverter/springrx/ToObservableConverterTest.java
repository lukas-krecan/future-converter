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

import org.junit.After;
import org.junit.Test;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.util.concurrent.ListenableFutureTask;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.javacrumbs.futureconverter.springrx.FutureConverter.toListenableFuture;
import static net.javacrumbs.futureconverter.springrx.FutureConverter.toObservable;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ToObservableConverterTest {

    public static final String VALUE = "test";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final CountDownLatch latch = new CountDownLatch(1);

    private final CountDownLatch waitLatch = new CountDownLatch(1);

    @After
    public void shutdown() {
        executorService.shutdown();
    }

    @Test
    public void testConvertToObservableCompleted() throws ExecutionException, InterruptedException {
        ListenableFutureTask<String> listenable = new ListenableFutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return VALUE;
            }
        });
        executorService.execute(listenable);

        Observable<String> observable = toObservable(listenable);
        Action1<String> onNext = mock(Action1.class);
        Action1<Throwable> onError = mock(Action1.class);
        final Action0 onComplete = mock(Action0.class);

        observable.subscribe(onNext, onError, new Action0() {
            @Override
            public void call() {
                onComplete.call();
                latch.countDown();
            }
        });

        latch.await();

        verify(onNext).call(VALUE);
        verifyZeroInteractions(onError);
        verify(onComplete).call();

        assertSame(listenable, toListenableFuture(observable));
    }

    @Test
    public void testRun() throws ExecutionException, InterruptedException {

        ListenableFutureTask<String> listenable = createAsyncListenableFuture();

        Observable<String> observable = toObservable(listenable);
        Action1<String> onNext = mock(Action1.class);
        Action1<Throwable> onError = mock(Action1.class);
        final Action0 onComplete = mock(Action0.class);

        observable.subscribe(onNext, onError, new Action0() {
            @Override
            public void call() {
                onComplete.call();
                latch.countDown();
            }
        });
        verifyZeroInteractions(onNext);
        verifyZeroInteractions(onError);
        verifyZeroInteractions(onComplete);

        waitLatch.countDown();
        latch.await();

        //wait for the result
        verify(onNext).call(VALUE);
        verifyZeroInteractions(onError);
        verify(onComplete).call();
    }

    private ListenableFutureTask<String> createAsyncListenableFuture() throws InterruptedException {
        ListenableFutureTask<String> listenable = new ListenableFutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                waitLatch.await();
                return VALUE;
            }
        });
        executorService.execute(listenable);
        return listenable;
    }


    @Test
    public void testCancelOriginal() throws ExecutionException, InterruptedException {
        ListenableFutureTask<String> listenable = createAsyncListenableFuture();

        Observable<String> observable = toObservable(listenable);
        Action1<String> onNext = mock(Action1.class);
        final Action1<Throwable> onError = mock(Action1.class);
        Action0 onComplete = mock(Action0.class);


        observable.subscribe(
                onNext,
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable t) {
                        onError.call(t);
                        latch.countDown();
                    }
                }
        );
        listenable.cancel(true);

        latch.await();

        verify(onError).call(any(Throwable.class));
        verifyZeroInteractions(onNext);
        verifyZeroInteractions(onComplete);
    }

    @Test
    public void testUnsubscribe() throws ExecutionException, InterruptedException {
        ListenableFutureTask<String> listenable = createAsyncListenableFuture();

        Observable<String> observable = toObservable(listenable);
        Action1<String> onNext = mock(Action1.class);
        Action1<Throwable> onError = mock(Action1.class);
        Action0 onComplete = mock(Action0.class);

        Subscription subscription = observable.subscribe(
                onNext,
                onError,
                onComplete
        );

        subscription.unsubscribe();


        listenable.addCallback(new ListenableFutureCallback<String>() {
            @Override
            public void onSuccess(String s) {
                latch.countDown();
            }

            @Override
            public void onFailure(Throwable throwable) {
                latch.countDown();
            }
        });

        latch.await();
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

    private void doTestException(final Exception exception) throws InterruptedException {
        ListenableFutureTask<String> listenable = new ListenableFutureTask<>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                throw exception;
            }
        });
        executorService.execute(listenable);

        Observable<String> observable = toObservable(listenable);
        Action1<String> onNext = mock(Action1.class);
        final Action1<Throwable> onError = mock(Action1.class);
        Action0 onComplete = mock(Action0.class);

        observable.subscribe(
                onNext,
                new Action1<Throwable>() {
                    @Override
                    public void call(Throwable t) {
                        onError.call(t);
                        latch.countDown();
                    }
                },
                onComplete
        );
        latch.await();

        //wait for the result
        verifyZeroInteractions(onNext);
        verify(onError).call(exception);
        verifyZeroInteractions(onComplete);
    }

}
