package net.javacrumbs.futureconverter.common.test;


import java.util.concurrent.Future;

public interface ConvertedFutureTestHelper<T extends Future<String>> {
    void addCallbackTo(T convertedFuture);

    void verifyCallbackCalledWithCorrectValue() throws InterruptedException;

    void waitForCalculationToFinish(T convertedFuture) throws InterruptedException;

    void verifyCallbackCalledWithException(Exception exception) throws InterruptedException;

    void verifyCallbackCalledWithException(Class<? extends Exception> exceptionClass) throws InterruptedException;
}
