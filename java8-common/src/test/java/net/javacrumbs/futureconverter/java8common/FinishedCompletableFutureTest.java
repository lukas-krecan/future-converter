package net.javacrumbs.futureconverter.java8common;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Tests CompletableFuture. Just to be sure I am reading the spec correctly. Same tests are executed on
 * CompletionStage and CompletableFuture.
 */
public class FinishedCompletableFutureTest extends AbstractCompletionStageTest {

    protected CompletionStage<String> createCompletionStage() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        completableFuture.complete(VALUE);
        return completableFuture;
    }

    @Override
    protected CompletionStage<String> createOtherCompletionStage() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        completableFuture.complete(VALUE2);
        return completableFuture;
    }

    protected CompletionStage<String> createExceptionalCompletionStage() {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        completableFuture.completeExceptionally(EXCEPTION);
        return completableFuture;
    }

    @Override
    protected void finishCalculation() {

    }

    @Override
    protected void finishCalculationExceptionally() {

    }
}