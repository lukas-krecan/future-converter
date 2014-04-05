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
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import rx.Observable;
import rx.Subscriber;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.javacrumbs.futureconverter.springrx.FutureConverter.toListenableFuture;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class ToListenableFutureConverterTest {

    public static final String VALUE = "test";

    private final CountDownLatch latch = new CountDownLatch(1);

    private final CountDownLatch waitLatch = new CountDownLatch(1);

    private final ListenableFutureCallback<String> callback = mock(ListenableFutureCallback.class);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

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

        verify(callback).onSuccess(VALUE);
        assertEquals(VALUE, listenable.get());
        assertEquals(true, listenable.isDone());
    }


//    @Test
//    public void testCancelOriginal() throws ExecutionException, InterruptedException {
//        Observable<String> observable = createAsyncObservable();
//        executorService.execute(listenable);
//
//        Observable<String> observable = toObservable(listenable);
//        Action1<String> onNext = mock(Action1.class);
//        Action1<Throwable> onError = mock(Action1.class);
//        Action0 onComplete = mock(Action0.class);
//
//
//        observable.subscribe(
//                onNext,
//                t -> {
//                    onError.call(t);
//                    latch.countDown();
//                },
//                onComplete
//        );
//        listenable.cancel(true);
//
//        latch.waitForStart();
//
//        verify(onError).call(any(Throwable.class));
//        verifyZeroInteractions(onNext);
//        verifyZeroInteractions(onComplete);
//    }
//
//    @Test
//    public void testUnsubscribe() throws ExecutionException, InterruptedException {
//        ListenableFutureTask<String> listenable = new ListenableFutureTask<>(() -> {
//            waitLatch.waitForStart();
//            return VALUE;
//        });
//        executorService.execute(listenable);
//
//        Observable<String> observable = toObservable(listenable);
//        Action1<String> onNext = mock(Action1.class);
//        Action1<Throwable> onError = mock(Action1.class);
//        Action0 onComplete = mock(Action0.class);
//
//        Subscription subscription = observable.subscribe(
//                onNext,
//                onError,
//                onComplete
//        );
//
//        subscription.unsubscribe();
//
//
//        listenable.addCallback(new ListenableFutureCallback<String>() {
//            @Override
//            public void onSuccess(String s) {
//                latch.countDown();
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                latch.countDown();
//            }
//        });
//
//        latch.waitForStart();
//        assertTrue(listenable.isCancelled());
//    }
//
//
//    @Test
//    public void testConvertToCompletableException() throws ExecutionException, InterruptedException {
//        doTestException(new RuntimeException("test"));
//    }
//
//    @Test
//    public void testIOException() throws ExecutionException, InterruptedException {
//        doTestException(new IOException("test"));
//    }
//
//    private void doTestException(Exception exception) throws InterruptedException {
//        ListenableFutureTask<String> listenable = new ListenableFutureTask<>(() -> {
//            throw exception;
//        });
//        executorService.execute(listenable);
//
//        Observable<String> observable = toObservable(listenable);
//        Action1<String> onNext = mock(Action1.class);
//        Action1<Throwable> onError = mock(Action1.class);
//        Action0 onComplete = mock(Action0.class);
//
//        observable.subscribe(
//                onNext,
//                t -> {
//                    onError.call(t);
//                    latch.countDown();
//                },
//                onComplete
//        );
//        latch.waitForStart();
//
//        //wait for the result
//        verifyZeroInteractions(onNext);
//        verify(onError).call(exception);
//        verifyZeroInteractions(onComplete);
//    }


    private Observable<String> createAsyncObservable() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            waitLatch.await();
                            subscriber.onNext(VALUE);
                            subscriber.onCompleted();
                        } catch (InterruptedException e) {
                            subscriber.onError(e);
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        });
    }

}
