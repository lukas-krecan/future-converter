package net.javacrumbs.futureconverter.java8common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Tests CompletableFuture. Just to be sure I am reading the spec correctly. Same tests are executed on
 * CompletionStage and CompletableFuture.
 */
public class UnfinishedCompletableFutureTest extends AbstractCompletionStageTest {
    private final CompletionStageFactory factory = new CompletionStageFactory();
    private final CompletableFuture<String> completableFuture = new CompletableFuture<>();

    protected CompletionStage<String> createCompletionStage() {
        return completableFuture;
    }

    @Override
    protected CompletionStage<String> createOtherCompletionStage() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        completableFuture.complete(VALUE2);
        return completableFuture;
    }

    protected CompletionStage<String> createExceptionalCompletionStage() {
        return createCompletionStage();
    }

    @Override
    protected void finishCalculation() {
        completableFuture.complete(VALUE);
    }

    @Override
    protected void finishCalculationExceptionally() {
        completableFuture.completeExceptionally(EXCEPTION);
    }
}