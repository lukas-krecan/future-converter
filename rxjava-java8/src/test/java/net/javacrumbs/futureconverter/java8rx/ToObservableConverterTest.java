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
import org.junit.Test;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.javacrumbs.futureconverter.java8rx.FutureConverter.toCompletableFuture;
import static net.javacrumbs.futureconverter.java8rx.FutureConverter.toObservable;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ToObservableConverterTest {

    public static final String VALUE = "test";

    private final CountDownLatch latch = new CountDownLatch(1);

    private final CountDownLatch waitLatch = new CountDownLatch(1);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @After
    public void cleanup() {
        waitLatch.countDown();
        executorService.shutdown();
    }

    @Test
    public void testConvertToObservableCompleted() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completable = CompletableFuture.completedFuture(VALUE);

        Observable<String> observable = toObservable(completable);
        Action1<String> onNext = mockAction();
        Action1<Throwable> onError = mockAction();
        final Action0 onComplete = mock(Action0.class);

        observable.subscribe(onNext, onError, () -> {
            onComplete.call();
            latch.countDown();
        });

        latch.await();

        verify(onNext).call(VALUE);
        verifyZeroInteractions(onError);
        verify(onComplete).call();

        assertSame(completable, toCompletableFuture(observable));
    }

    @Test
    public void testRun() throws ExecutionException, InterruptedException {

        CompletableFuture<String> completable = createAsyncCompletable();

        Observable<String> observable = toObservable(completable);
        Action1<String> onNext = mockAction();
        Action1<Throwable> onError = mockAction();
        final Action0 onComplete = mock(Action0.class);

        observable.subscribe(onNext, onError, () -> {
            onComplete.call();
            latch.countDown();
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

    @Test
    public void testCancelOriginal() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completable = createAsyncCompletable();

        Observable<String> observable = toObservable(completable);
        Action1<String> onNext = mockAction();
        final Action1<Throwable> onError = mockAction();
        Action0 onComplete = mock(Action0.class);


        observable.subscribe(
                onNext,
                t -> {
                    onError.call(t);
                    latch.countDown();
                }
        );
        completable.cancel(true);

        latch.await();

        verify(onError).call(any(Throwable.class));
        verifyZeroInteractions(onNext);
        verifyZeroInteractions(onComplete);
    }

    @SuppressWarnings("unchecked")
    private <T> Action1<T> mockAction() {
        return mock(Action1.class);
    }

    @Test
    public void testUnsubscribe() throws ExecutionException, InterruptedException {
        CompletableFuture<String> completable = createAsyncCompletable();

        Observable<String> observable = toObservable(completable);
        Action1<String> onNext = mockAction();
        Action1<Throwable> onError = mockAction();
        Action0 onComplete = mock(Action0.class);

        Subscription subscription = observable.subscribe(
                onNext,
                onError,
                onComplete
        );

        subscription.unsubscribe();


        completable.exceptionally(t -> {
            latch.countDown();
            return null;
        });

        latch.await();
        assertTrue(completable.isCancelled());
    }


    @Test
    public void testConvertToCompletableException() throws ExecutionException, InterruptedException {
        doTestException(new RuntimeException("test"));
    }

    private void doTestException(final RuntimeException exception) throws InterruptedException {
        CompletableFuture<String> completable = CompletableFuture.supplyAsync(() -> {
            throw exception;
        }, executorService);

        Observable<String> observable = toObservable(completable);
        Action1<String> onNext = mockAction();
        final Action1<Throwable> onError = mockAction();
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
        verify(onError).call(any(CompletionException.class));
        verifyZeroInteractions(onComplete);
    }

    private CompletableFuture<String> createAsyncCompletable() throws InterruptedException {
        return CompletableFuture.supplyAsync(() -> {
            try {
                waitLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return VALUE;
        }, executorService);
    }

}
