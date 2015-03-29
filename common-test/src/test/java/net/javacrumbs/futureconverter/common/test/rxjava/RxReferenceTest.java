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

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Test to check how is RX java supposed to work
 */
@Ignore
public class RxReferenceTest {

    private Action1<String> action = mock(Action1.class);

    @Test
    public void shouldCancelFutureIfOnlySubscriptionIsUnsubscribed() throws ExecutionException, InterruptedException, TimeoutException {
        Future<String> future = new CompletableFuture<>();

        Observable<String> observable = Observable.from(future, Schedulers.io());
        Subscription subscription = observable.subscribe(action);
        subscription.unsubscribe();

        try {
            future.get(10, SECONDS);
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertTrue(future.isCancelled());
        verifyZeroInteractions(action);
    }

    @Test
    public void shouldNotCancelFutureIfOneOfMultipleSubscriptionsIsUnsubscribed() {
        CompletableFuture<String> future = new CompletableFuture<>();

        Observable<String> observable = Observable.from(future);

    }
}
