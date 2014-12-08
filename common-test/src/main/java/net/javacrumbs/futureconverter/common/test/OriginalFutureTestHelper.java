package net.javacrumbs.futureconverter.common.test;


import java.util.concurrent.Future;

public interface OriginalFutureTestHelper<F extends Future<String>> {

    F createFinishedOriginal();

    F createRunningFuture();

    F createExceptionalFuture(Exception exception);

    void finishOriginalFuture();
}
