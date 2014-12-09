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
package net.javacrumbs.futureconverter.common.test.guava;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import net.javacrumbs.futureconverter.common.test.AbstractConverterTest;
import net.javacrumbs.futureconverter.common.test.OriginalFutureTestHelper;
import net.javacrumbs.futureconverter.common.test.common.CommonOriginalFutureTestHelper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class GuavaOriginalFutureTestHelper extends CommonOriginalFutureTestHelper implements OriginalFutureTestHelper<ListenableFuture<String>> {
    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    @Override
    public ListenableFuture<String> createFinishedOriginal() {
        return Futures.immediateFuture(AbstractConverterTest.VALUE);
    }

    @Override
    public ListenableFuture<String> createRunningFuture() {
        return executorService.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    waitForSignal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return AbstractConverterTest.VALUE;
            }
        });
    }

    @Override
    public ListenableFuture<String> createExceptionalFuture(Exception exception) {
        return Futures.immediateFailedFuture(exception);
    }
}
