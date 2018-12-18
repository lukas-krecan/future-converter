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
package net.javacrumbs.futureconverter.common.test;

import java.util.concurrent.Future;

public abstract class AbstractConverterHelperBasedTest<F extends Future<String>, T extends Future<String>>
        extends AbstractConverterTest<F, T> {
    private final OriginalFutureTestHelper<F> originalFutureTestHelper;
    private final ConvertedFutureTestHelper<T> convertedFutureTestHelper;

    protected AbstractConverterHelperBasedTest(OriginalFutureTestHelper<F> originalFutureTestHelper, ConvertedFutureTestHelper<T> convertedFutureTestHelper) {
        this.originalFutureTestHelper = originalFutureTestHelper;
        this.convertedFutureTestHelper = convertedFutureTestHelper;
    }

    @Override
    protected abstract T convert(F originalFuture);

    @Override
    protected abstract F convertBack(T converted);

    @Override
    protected F createFinishedOriginal() {
        return originalFutureTestHelper.createFinishedFuture();
    }

    @Override
    protected F createExceptionalFuture(Exception exception) {
        return originalFutureTestHelper.createExceptionalFuture(exception);
    }

    @Override
    protected F createRunningFuture() {
        return originalFutureTestHelper.createRunningFuture();
    }

    @Override
    protected void finishOriginalFuture() {
        originalFutureTestHelper.finishRunningFuture();
    }

    @Override
    protected void addCallbackTo(T convertedFuture) {
        convertedFutureTestHelper.addCallbackTo(convertedFuture);
    }

    @Override
    protected void verifyCallbackCalledWithCorrectValue() throws InterruptedException {
        convertedFutureTestHelper.verifyCallbackCalledWithCorrectValue();
    }

    @Override
    protected void waitForCalculationToFinish(T convertedFuture) throws InterruptedException {
        convertedFutureTestHelper.waitForCalculationToFinish(convertedFuture);
    }

    @Override
    protected void verifyCallbackCalledWithException(Exception exception) throws InterruptedException {
        convertedFutureTestHelper.verifyCallbackCalledWithException(exception);
    }

    @Override
    protected void verifyCallbackCalledWithException(Class<? extends Exception> exceptionClass) throws InterruptedException {
        convertedFutureTestHelper.verifyCallbackCalledWithException(exceptionClass);
    }
}
