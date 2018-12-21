/*
 * Copyright © 2014-2019 the original author or authors.
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
package net.javacrumbs.futureconverter.common.test.apicommon;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutures;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import net.javacrumbs.futureconverter.common.test.AbstractConverterTest;
import net.javacrumbs.futureconverter.common.test.OriginalFutureTestHelper;
import net.javacrumbs.futureconverter.common.test.common.CommonOriginalFutureTestHelper;

import java.util.concurrent.Executors;

public class ApiCommonOriginalFutureTestHelper extends CommonOriginalFutureTestHelper implements OriginalFutureTestHelper<ApiFuture<String>> {
    private final ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

    @Override
    public ApiFuture<String> createFinishedFuture() {
        return ApiFutures.immediateFuture(AbstractConverterTest.VALUE);
    }

    @Override
    public ApiFuture<String> createRunningFuture() {
        return ApiFutures.transform(
            ApiFutures.immediateFuture(""),
            input -> {
                try {
                    waitForSignal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return AbstractConverterTest.VALUE;
            },
            executorService);
    }

    @Override
    public ApiFuture<String> createExceptionalFuture(Exception exception) {
        return ApiFutures.immediateFailedFuture(exception);
    }
}
