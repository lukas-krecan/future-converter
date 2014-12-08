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
package net.javacrumbs.futureconverter.common.test;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * Abstract test conversion from F to type T.
 *
 * @param <F> from
 * @param <T> to
 * @param <C> callback
 */
public abstract class AbstractConverterTest<F extends Future<String>, T extends Future<String>, C> {

    protected static final String VALUE = "test";

    private final CountDownLatch waitLatch = new CountDownLatch(1);

    protected abstract T convert(F originalFuture);

    protected abstract F createFinishedOriginal();

    protected abstract void addCallbackTo(T convertedFuture);

    protected abstract F createRunningFuture();

    protected abstract void verifyCallbackCalledWithCorrectValue() throws InterruptedException;

    protected abstract void waitForCalculationToFinish(T convertedFuture) throws InterruptedException;

    protected abstract void verifyCallbackCalledWithException(Exception exception) throws InterruptedException;

    protected abstract void verifyCallbackCalledWithException(Class<? extends Exception> exceptionClass) throws InterruptedException;

    protected abstract F createExceptionalFuture(Exception exception);

    protected void waitForSignal() throws InterruptedException {
        waitLatch.await();
    }


    @Test
    public void testConvertCompleted() throws ExecutionException, InterruptedException {
        F originalFuture = createFinishedOriginal();

        T converted = convert(originalFuture);
        assertEquals(VALUE, converted.get());
        assertEquals(true, converted.isDone());
        assertEquals(false, converted.isCancelled());
        addCallbackTo(converted);
        verifyCallbackCalledWithCorrectValue();
    }

    @Test
    public void testConvertRunning() throws ExecutionException, InterruptedException {
        F originalFuture = createRunningFuture();
        T convertedFuture = convert(originalFuture);
        addCallbackTo(convertedFuture);
        assertEquals(false, convertedFuture.isDone());
        assertEquals(false, convertedFuture.isCancelled());
        waitLatch.countDown();

        //wait for the result
        assertEquals(VALUE, convertedFuture.get());
        assertEquals(true, convertedFuture.isDone());
        assertEquals(false, convertedFuture.isCancelled());

        waitForCalculationToFinish(convertedFuture);
        verifyCallbackCalledWithCorrectValue();
    }

    @Test
    public void testCancelOriginal() throws ExecutionException, InterruptedException {
        F originalFuture = createRunningFuture();
        originalFuture.cancel(true);

        T convertedFuture = convert(originalFuture);

        try {
            convertedFuture.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, convertedFuture.isDone());
        assertEquals(true, convertedFuture.isCancelled());
        addCallbackTo(convertedFuture);
        verifyCallbackCalledWithException(CancellationException.class);
    }

    @Test
    public void testCancelNew() throws ExecutionException, InterruptedException {
        F originalFuture = createRunningFuture();
        T convertedFuture = convert(originalFuture);
        convertedFuture.cancel(true);

        try {
            convertedFuture.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, convertedFuture.isDone());
        assertEquals(true, convertedFuture.isCancelled());
        assertEquals(true, originalFuture.isDone());
        assertEquals(true, originalFuture.isCancelled());
        addCallbackTo(convertedFuture);
        verifyCallbackCalledWithException(CancellationException.class);
    }

    @Test
    public void testCancelBeforeConversion() throws ExecutionException, InterruptedException {
        F originalFuture = createRunningFuture();
        originalFuture.cancel(true);

        T convertedFuture = convert(originalFuture);
        assertFalse(convertedFuture.cancel(true));

        try {
            convertedFuture.get();
            fail("Exception expected");
        } catch (CancellationException e) {
            //ok
        }
        assertEquals(true, originalFuture.isDone());
        assertEquals(true, originalFuture.isCancelled());
        assertEquals(true, convertedFuture.isDone());
        assertEquals(true, convertedFuture.isCancelled());
    }

    @Test
    public void testCancelCompleted() throws ExecutionException, InterruptedException {
        F originalFuture = createFinishedOriginal();

        T convertedFuture = convert(originalFuture);
        addCallbackTo(convertedFuture);
        waitForCalculationToFinish(convertedFuture);

        assertEquals(VALUE, convertedFuture.get());
        assertEquals(true, convertedFuture.isDone());
        assertEquals(false, convertedFuture.isCancelled());

        verifyCallbackCalledWithCorrectValue();
        assertFalse(convertedFuture.cancel(true));
    }


    @Test
    public void testConvertWithException() throws ExecutionException, InterruptedException {
        Exception exception = new RuntimeException("test");
        doTestException(exception);
    }

    @Test
    public void testConvertWithIOException() throws ExecutionException, InterruptedException {
        Exception exception = new IOException("test");
        doTestException(exception);
    }

    protected void doTestException(final Exception exception) throws InterruptedException {
        F originalFuture = createExceptionalFuture(exception);
        T convertedFuture = convert(originalFuture);
        try {
            convertedFuture.get();
            fail("Exception expected");
        } catch (ExecutionException e) {
            assertEquals(exception, e.getCause());
        }
        assertEquals(true, convertedFuture.isDone());
        assertEquals(false, convertedFuture.isCancelled());

        addCallbackTo(convertedFuture);
        verifyCallbackCalledWithException(exception);
    }
}
