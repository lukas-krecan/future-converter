package net.javacrumbs.futureconverter.java8common;

import java.util.concurrent.CompletionStage;

public class FinishedCompletionStageFactoryTest extends AbstractCompletionStageTest {
    private final CompletionStageFactory factory = new CompletionStageFactory();

    protected CompletionStage<String> createCompletionStage() {
        return factory.createCompletableFuture((onSuccess, onFailure) -> {
            onSuccess.accept(VALUE);
        });
    }

    @Override
    protected CompletionStage<String> createOtherCompletionStage() {
        return factory.createCompletableFuture((onSuccess, onFailure) -> {
            onSuccess.accept(VALUE2);
        });
    }

    protected CompletionStage<String> createExceptionalCompletionStage() {
        return factory.createCompletableFuture((onSuccess, onFailure) -> {
            onFailure.accept(EXCEPTION);
        });
    }

    @Override
    protected void finishCalculation() {

    }

    @Override
    protected void finishCalculationExceptionally() {

    }


}