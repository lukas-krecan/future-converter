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
package net.javacrumbs.futureconverter.common.test.rxjava;

import net.javacrumbs.futureconverter.common.test.OriginalFutureTestHelper;
import org.junit.After;
import org.junit.Test;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static net.javacrumbs.futureconverter.common.test.AbstractConverterTest.VALUE;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public abstract class AbstractFutureToObservableConverterTest<T extends Future<String>> {


    private final CountDownLatch latch = new CountDownLatch(1);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final OriginalFutureTestHelper<T> originalFutureTestHelper;

    protected AbstractFutureToObservableConverterTest(OriginalFutureTestHelper<T> originalFutureTestHelper) {
        this.originalFutureTestHelper = originalFutureTestHelper;
    }

    protected abstract Observable<String> toObservable(T future);

    protected abstract T toFuture(Observable<String> observable);

    @After
    public void cleanup() {
        executorService.shutdown();
    }

    @Test
    public void testConvertToObservableFinished() throws ExecutionException, InterruptedException {
        T completable = originalFutureTestHelper.createFinishedFuture();

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

        assertSame(completable, toFuture(observable));
    }

    @Test
    public void testRun() throws ExecutionException, InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Observable<String> observable = toObservable(future);
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

        originalFutureTestHelper.finishRunningFuture();
        latch.await();

        //wait for the result
        verify(onNext).call(VALUE);
        verifyZeroInteractions(onError);
        verify(onComplete).call();
    }

    @Test
    public void testMultipleSubscribers() throws ExecutionException, InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Observable<String> observable = toObservable(future);
        CountDownLatch latch = new CountDownLatch(2);

        Action1<String> onNext1 = mockAction();
        Action1<Throwable> onError1 = mockAction();
        final Action0 onComplete1 = mock(Action0.class);

        // first subscription
        observable.subscribe(onNext1, onError1, () -> {
            onComplete1.call();
            latch.countDown();
        });
        verifyZeroInteractions(onNext1);
        verifyZeroInteractions(onError1);
        verifyZeroInteractions(onComplete1);

        // second subscription
        Action1<String> onNext2 = mockAction();
        Action1<Throwable> onError2 = mockAction();
        final Action0 onComplete2 = mock(Action0.class);

        observable.subscribe(onNext2, onError2, () -> {
            onComplete2.call();
            latch.countDown();
        });
        verifyZeroInteractions(onNext2);
        verifyZeroInteractions(onError2);
        verifyZeroInteractions(onComplete2);


        originalFutureTestHelper.finishRunningFuture();

        //wait for the result
        latch.await();

        verify(onNext1).call(VALUE);
        verifyZeroInteractions(onError1);
        verify(onComplete1).call();

        verify(onNext2).call(VALUE);
        verifyZeroInteractions(onError2);
        verify(onComplete2).call();
    }

    @Test
    public void oneSubscriptionShouldNotCancelFuture() throws ExecutionException, InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Observable<String> observable = toObservable(future);
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

        observable.subscribe(v -> {}).unsubscribe();

        originalFutureTestHelper.finishRunningFuture();
        latch.await();

        //wait for the result
        verify(onNext).call(VALUE);
        verifyZeroInteractions(onError);
        verify(onComplete).call();
    }

    @Test
    public void testCancelOriginal() throws ExecutionException, InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Observable<String> observable = toObservable(future);
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
        future.cancel(true);

        latch.await();

        verify(onError).call(any(Throwable.class));
        verifyZeroInteractions(onNext);
        verifyZeroInteractions(onComplete);
    }

    @SuppressWarnings("unchecked")
    private <S> Action1<S> mockAction() {
        return mock(Action1.class);
    }

    @Test
    public void testUnsubscribe() throws ExecutionException, InterruptedException {
        T future = originalFutureTestHelper.createRunningFuture();

        Observable<String> observable = toObservable(future);
        Action1<String> onNext = mockAction();
        Action1<Throwable> onError = mockAction();
        Action0 onComplete = mock(Action0.class);

        Subscription subscription = observable.subscribe(
                onNext,
                onError,
                onComplete
        );

        subscription.unsubscribe();
        assertTrue(subscription.isUnsubscribed());

        originalFutureTestHelper.finishRunningFuture();
        Thread.sleep(10); //do not know how to wait for something to not happen

        verifyZeroInteractions(onNext);
        verifyZeroInteractions(onError);
        verifyZeroInteractions(onComplete);
    }


    @Test
    public void testConvertToCompletableException() throws ExecutionException, InterruptedException {
        doTestException(new RuntimeException("test"));
    }

    private void doTestException(final RuntimeException exception) throws InterruptedException {
        T future = originalFutureTestHelper.createExceptionalFuture(exception);

        Observable<String> observable = toObservable(future);
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
}
