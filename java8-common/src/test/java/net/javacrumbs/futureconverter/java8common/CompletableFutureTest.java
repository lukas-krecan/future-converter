package net.javacrumbs.futureconverter.java8common;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Tests CompletableFuture. Just to be sure I am reading the spec correctly. Same tests are executed on
 * CompletionStage and CompletableFuture.
 */
public class CompletableFutureTest extends AbstractCompletionStageTest {
    private final CompletionStageFactory factory = new CompletionStageFactory();

    protected CompletionStage<String> createFinishedCompletionStage() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        completableFuture.complete(VALUE);
        return completableFuture;
    }

    protected CompletionStage<String> createExceptionalCompletionStage() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(EXCEPTION);
        return completableFuture;
    }


    private CompletionStage<String> createCompletionStage(ListenableFuture<String> listenableFuture) {
        return factory.createCompletableFuture((onSuccess, onError) -> {
            Futures.addCallback(listenableFuture, new FutureCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    onSuccess.accept(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    onError.accept(t);
                }
            });
        });
    }

}