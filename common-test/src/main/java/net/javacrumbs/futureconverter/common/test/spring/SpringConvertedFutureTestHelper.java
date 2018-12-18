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
package net.javacrumbs.futureconverter.common.test.spring;

import net.javacrumbs.futureconverter.common.test.AbstractConverterTest;
import net.javacrumbs.futureconverter.common.test.ConvertedFutureTestHelper;
import net.javacrumbs.futureconverter.common.test.common.CommonConvertedFutureTestHelper;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SpringConvertedFutureTestHelper extends CommonConvertedFutureTestHelper implements ConvertedFutureTestHelper<ListenableFuture<String>> {
    private final ListenableFutureCallback<String> callback = mock(ListenableFutureCallback.class);

    @Override
    public void waitForCalculationToFinish(ListenableFuture<String> convertedFuture) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        convertedFuture.addCallback(new ListenableFutureCallback<String>() {
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
    }

    @Override
    public void verifyCallbackCalledWithCorrectValue() {
        waitForCallback();
        verify(callback).onSuccess(AbstractConverterTest.VALUE);
    }


    @Override
    public void verifyCallbackCalledWithException(Exception exception) {
        waitForCallback();
        verify(callback).onFailure(exception);
    }

    @Override
    public void verifyCallbackCalledWithException(Class<? extends Exception> exceptionClass) {
        waitForCallback();
        verify(callback).onFailure(any(exceptionClass));
    }


    @Override
    public void addCallbackTo(ListenableFuture<String> convertedFuture) {
        convertedFuture.addCallback(new ListenableFutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                callback.onSuccess(result);
                callbackCalled();
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t);
                callbackCalled();
            }
        });
    }
}
